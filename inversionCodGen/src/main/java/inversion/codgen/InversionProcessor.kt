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

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import inversion.*
import inversion.internal.InversionDelegates
import inversion.internal.InversionValidator
import inversion.internal.NamedGeneratedFactory
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class InversionProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() =
        mutableSetOf(
            InversionImpl::class.java.name,
            InversionProvider::class.java.name,
            InversionDef::class.java.name
        )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        val defs = roundEnvironment.getElementsAnnotatedWith(InversionDef::class.java)
            .filterIsInstance<ExecutableElement>()
            .map { DefElement(it, processingEnv.getPackageName(it)) }

        val implElementCalculator = ImplElementCalculator(processingEnv, defs)

        val impls = roundEnvironment.getElementsAnnotatedWith(InversionProvider::class.java)
            .filterIsInstance<ExecutableElement>()
            .mapNotNull { implElementCalculator.calculateFromProvider(it) } +
                roundEnvironment.getElementsAnnotatedWith(InversionImpl::class.java)
                    .filterIsInstance<TypeElement>()
                    .mapNotNull { implElementCalculator.calculateFromImpl(it) }

        impls.map { generateImpl(it) }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .forEach { (key, list) -> generateConfigFiles(processingEnv, key, list) }

        val validatorsToBeGenerated = defs.map { generateDefClass(it) }

        generateConfigFiles(
            processingEnv,
            InversionValidator::class.java.canonicalName,
            validatorsToBeGenerated
        )

        return true
    }

    private inline fun <reified T> loadServiceList(): List<T> =
        loadServiceList(T::class.java)

    private fun <T> loadServiceList(c: Class<T>): List<T> {
        return try {
            val provider = ServiceLoader.load(c, c.classLoader)
            val ret = mutableListOf<T>()
            val iterator = provider.iterator()
            while (iterator.hasNext()) {
                ret.add(iterator.next())
            }
            ret
        } catch (e: ServiceConfigurationError) {
            emptyList()
        }
    }

    private fun VariableElement.isReceiver() = simpleName.toString().contains('$')

    private fun generateImpl(element: ImplElement): Pair<String, String> {
        val factoryInterface = element.factoryInterface
        val suffix = if (element.instanceName.isEmpty()) "" else "_${element.instanceName}"
        val factoryClassName = "${factoryInterface.simpleName}Impl$suffix"
        FileSpec.builder(element.packageName, factoryClassName)
            .addType(
                TypeSpec.classBuilder(factoryClassName)
                    .addSuperinterface(factoryInterface)
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(element.defClass)
                            .apply {
                                element.parameters.forEach {
                                    addParameter(
                                        if (it.isReceiver()) "param" else it.simpleName.toString(),
                                        it.asType().asTypeName()
                                    )
                                }
                            }
                            .apply {
                                if (element.parameters.getOrNull(0)?.isReceiver() == true)
                                    addStatement("return param.${element.simpleName}()")
                                else
                                    addStatement(
                                        "return ${element.simpleName}(%L)",
                                        element.parameters.joinToString { it.simpleName.toString() }
                                    )
                            }
                            .build()
                    )
                    .run {
                        if (element.instanceName.isEmpty())
                            this
                        else
                            addProperty(
                                PropertySpec.builder("name", String::class, KModifier.OVERRIDE)
                                    .initializer("\"${element.instanceName}\"")
                                    .build()
                            )
                    }
                    .build()
            )
            .build()
            .writeTo(processingEnv.filer)

        return factoryInterface.canonicalName to "${element.packageName}.$factoryClassName"
    }

    private fun generateDefClass(element: DefElement): String {
        val returnType = element.defClass
        val receiver = element.receiver
        val realFactoryType = LambdaTypeName.get(
            returnType = returnType,
            parameters = *listOfNotNull(receiver?.asType()?.asTypeName()).toTypedArray()
        )
        val factoryInterface = element.factoryInterface
        val validatorClass = ClassName(
            returnType.packageName,
            returnType.simpleName + "_FactoryValidator"
        )
        FileSpec.builder(element.packageName, factoryInterface.simpleName)
            .addType(
                TypeSpec.interfaceBuilder(factoryInterface)
                    .addSuperinterface(realFactoryType)
                    .run {
                        if (element.isReturningMap)
                            addSuperinterface(NamedGeneratedFactory::class.java)
                        else
                            this
                    }
                    .build()
            )
            .addType(
                TypeSpec.classBuilder(
                    validatorClass
                )
                    .addSuperinterface(InversionValidator::class)
                    .addProperty(
                        PropertySpec.builder(
                            "factoryClass",
                            KClass::class.asClassName().parameterizedBy(factoryInterface),
                            KModifier.OVERRIDE
                        )
                            .initializer("%T::class", factoryInterface)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder(
                            "wrappedClass",
                            KClass::class.asClassName().parameterizedBy(returnType),
                            KModifier.OVERRIDE
                        )
                            .initializer("%T::class", returnType)
                            .build()
                    )
//                    .run {
//                        if (element.isReturningMap) {
//                            this
//                        } else {
//                            this
//                        }
//                    }
                    .build()
            )
            .build()
            .writeTo(processingEnv.filer)

        FileSpec.builder(
            "inversion",
            "Inversion_ext_${factoryInterface.canonicalName.replace('.', '_')}"
        )
            .addFunction(
                FunSpec.builder(if (element.isReturningMap) "mapOf" else "of")
                    .addAnnotation(
                        AnnotationSpec.builder(JvmName::class)
                            .addMember("\"factory_${returnType.toString().replace('.', '_')}\"")
                            .build()
                    )
                    .receiver(Inversion::class)
                    .addParameter("c", KClass::class.asClassName().parameterizedBy(returnType))
                    .let {
                        val prefix = if (element.isReturningMap) "mapDelegate" else "delegate"
                        val suffix =
                            if (element.isReturningMap) ".asSequence().toList()" else ".next()"
                        if (receiver == null)
                            it.addStatement(
                                "return %T.$prefix(%T.load(%T::class.java, %T::class.java.classLoader).iterator()$suffix)",
                                InversionDelegates::class,
                                ServiceLoader::class,
                                factoryInterface,
                                factoryInterface
                            )
                        else
                            it.addStatement(
                                "return %T.${prefix}WithReceiver(%T.load(%T::class.java, %T::class.java.classLoader).iterator()$suffix)",
                                InversionDelegates::class,
                                ServiceLoader::class,
                                factoryInterface,
                                factoryInterface
                            )
                    }
                    .build()
            )
            .build()
            .writeTo(processingEnv.filer)

        return validatorClass.canonicalName
    }
}