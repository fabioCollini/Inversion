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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isCompanionObject
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import inversion.InversionImpl
import inversion.InversionProvider
import javax.lang.model.element.*

private val Element.isCompanionObject: Boolean
    @UseExperimental(KotlinPoetMetadataPreview::class)
    get() {
        return (this as TypeElement).toImmutableKmClass().isCompanionObject
    }

private fun factoryInterface(type: ClassName) =
    ClassName(type.packageName, type.simpleName + "_Factory")

interface ImplElement {
    val packageName: String
    val defClass: ClassName
    val parameters: List<VariableElement>
    val simpleName: Name
    val factoryInterface: ClassName get() = factoryInterface(defClass)
    val instanceName: String
}

class ImplExecutableElement(
    element: ExecutableElement,
    override val packageName: String,
    override val defClass: ClassName
) : ImplElement {
    override val parameters: List<VariableElement> = element.parameters
    override val simpleName: Name = element.simpleName
    override val instanceName = element.getAnnotation(InversionProvider::class.java).value
}

class ImplClassElement(
    element: TypeElement,
    override val packageName: String,
    override val defClass: ClassName
) : ImplElement {
    override val parameters: List<VariableElement> = emptyList()
    override val simpleName: Name = element.simpleName
    override val instanceName = element.getAnnotation(InversionImpl::class.java).value
}

class DefElement(
    private val element: ExecutableElement,
    val packageName: String
) {
    val receiver: Element?
        get() = element.parameters.getOrNull(0)
            ?: when {
                element.modifiers.contains(Modifier.STATIC) -> null
                element.enclosingElement.isCompanionObject -> null
                else -> element.enclosingElement
            }
    val factoryType get() = element.returnType.asTypeName() as ParameterizedTypeName
    val defClass: ClassName
        get() {
            val ret = factoryType.typeArguments.last()
            return if (ret is ParameterizedTypeName) {
                ret.typeArguments.last() as ClassName
            } else {
                ret as ClassName
            }
        }
    val isReturningMap: Boolean
        get() {
            val ret = factoryType.typeArguments.last()
            return ret is ParameterizedTypeName
        }
    val factoryInterface get() = factoryInterface(defClass)
}
