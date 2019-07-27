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

import inversion.InversionImpl
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

class ImplElementCalculator(
    private val processingEnv: ProcessingEnvironment,
    defs: List<DefElement>
) {
    fun calculateFromImpl(element: TypeElement): ImplClassElement? {
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
                if (checkDefExists(element, defClassName)) {
                    val defClass = processingEnv.elementUtils.getTypeElement(defClassName).asType().asTypeName() as ClassName
                    ImplClassElement(element, processingEnv.getPackageName(element), defClass)
                } else {
                    null
                }
            }
        }
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

    fun calculateFromProvider(element: ExecutableElement): ImplExecutableElement? {
        val returnType = element.returnType.asTypeName() as ClassName
        return if (checkDefExists(element, returnType.canonicalName))
            ImplExecutableElement(element, processingEnv.getPackageName(element), returnType)
        else
            null
    }

    private val allDefs = defs.map { it.defClass.canonicalName }

    private fun checkDefExists(element: Element, returnType: String): Boolean {
        return if (allDefs.contains(returnType) || processingEnv.elementUtils.getTypeElement("${returnType}_FactoryValidator") != null) {
            true
        } else {
            processingEnv.error("No definition found for $returnType", element)
            false
        }
    }
}