/*
 * Copyright 2019 Fabio Collini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package inversion.codgen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import inversion.InversionImpl
import inversion.InversionProvider
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

class ImplElementCalculator(
    private val processingEnv: ProcessingEnvironment,
    private val defs: List<DefElement>
) {
    fun calculateFromImpl(element: TypeElement): ImplElement? {
        val defAnnotationClass = extractClassFromImplAnnotation(element)
        val interfaces = defAnnotationClass?.let { listOf(it) }
            ?: (element.interfaces + element.superclass).filter { it.toString() != "java.lang.Object" }.map {
                it.asTypeName().toString()
            }

        return when {
            interfaces.isEmpty() -> {
                processingEnv.error("No superclass or interface found for $element", element)
                null
            }
            interfaces.size > 1 -> {
                processingEnv.error(
                    "Multiple superclasses/interfaces found for $element, please use the def parameter in InversionImpl annotation",
                    element
                )
                null
            }
            else -> {
                val defClassName = interfaces[0]
                val annotationValue =
                    element.getAnnotationsByType(InversionImpl::class.java).firstOrNull()?.value
                checkAndCreateImpl(defClassName, element, annotationValue) {
                    createImpl(defClassName, element)
                }
            }
        }
    }

    private inline fun checkAndCreateImpl(
        defClassName: String,
        element: Element,
        annotationValue: String?,
        f: () -> ImplElement?
    ): ImplElement? {
        val def = defs.firstOrNull { it.defClass.canonicalName == defClassName }
        return if (def != null) {
            if (checkMultiMap(annotationValue, element, def.isReturningMap))
                f()
            else
                null
        } else {
            val validator =
                processingEnv.elementUtils.getTypeElement("${defClassName}_FactoryValidator")
            if (validator != null) {
//                if (checkMultiMap(annotationValue, element, validator)
//                    f()
//                else
                    null
            } else {
                processingEnv.error("No definition found for $defClassName", element)
                null
            }
        }
    }

    private fun checkMultiMap(
        annotationValue: String?,
        element: Element,
        multiMap: Boolean
    ): Boolean {
        return if (multiMap && annotationValue.isNullOrEmpty()) {
            processingEnv.error(
                "No key defined for implementation, use the annotation value to define it",
                element
            )
            false
        } else {
            true
        }
    }

    private fun createImpl(
        defClassName: String,
        element: TypeElement
    ): ImplClassElement {
        val defClass =
            processingEnv.elementUtils.getTypeElement(defClassName).asType().asTypeName() as ClassName
        return ImplClassElement(element, processingEnv.getPackageName(element), defClass)
    }

    private fun extractClassFromImplAnnotation(element: TypeElement): String? {
        val value = try {
            val implAnnotation = element.getAnnotation(InversionImpl::class.java)
            implAnnotation.def.java.canonicalName
        } catch (e: MirroredTypeException) {
            e.typeMirror.toString()
        }
        return value.takeIf { it != "java.lang.Void" }
    }

    fun calculateFromProvider(element: ExecutableElement): ImplElement? {
        val returnType = element.returnType.asTypeName() as ClassName
        val annotationValue =
            element.getAnnotationsByType(InversionProvider::class.java).firstOrNull()?.value
        return checkAndCreateImpl(returnType.canonicalName, element, annotationValue) {
            ImplExecutableElement(element, processingEnv.getPackageName(element), returnType)
        }
    }
}