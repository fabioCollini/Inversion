package com.nytimes.inversioncodgen

import com.google.auto.service.AutoService
import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.InversionImpl
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import kotlin.reflect.KClass

class ImplElement(
    element: ExecutableElement,
    val packageName: String
) {
    val methodName = element.simpleName.toString()
    val returnType = element.returnType.asTypeName() as ClassName
    val parameters: List<VariableElement> = element.parameters
    val simpleName: Name = element.simpleName
}

class DefElement(
    element: VariableElement,
    val packageName: String
) {
    val factoryType = element.asType().asTypeName() as ParameterizedTypeName
}

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(InversionProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class InversionProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() =
        mutableSetOf(InversionImpl::class.java.name, InversionDef::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {
        val impls = roundEnvironment?.getElementsAnnotatedWith(InversionImpl::class.java)
            .orEmpty()
            .filterIsInstance<ExecutableElement>()
            .map { ImplElement(it, getPackageName(it)) }

        impls.forEach { generateImpl(it) }
        
        val defs = roundEnvironment?.getElementsAnnotatedWith(InversionDef::class.java)
            .orEmpty()
            .filterIsInstance<VariableElement>()
            .map { DefElement(it, getPackageName(it)) }
        
        defs.forEach { generateDefClass(it) }
        
        return true
    }

    private fun getPackageName(it: Element) =
        processingEnv.elementUtils.getPackageOf(it).toString()

    private fun generateImpl(element: ImplElement) {
        val returnType = element.returnType
        val factoryInterface = ClassName(returnType.packageName, returnType.simpleName + "Factory")
        FileSpec.builder(element.packageName, "MyFactoryImpl")
            .addType(
                TypeSpec.classBuilder("${element.methodName}__factory")
                    .addSuperinterface(factoryInterface)
                    .addAnnotation(
                        AnnotationSpec.builder(AutoService::class)
                            .addMember(factoryInterface.simpleName + "::class")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(element.returnType)
                            .apply {
                                element.parameters.forEach {
                                    addParameter(
                                        it.simpleName.toString(),
                                        it.asType().asTypeName()
                                    )
                                }
                            }
                            .addStatement("return ${element.simpleName}(%L)",
                                element.parameters.joinToString { it.simpleName.toString() })
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))
    }

    private fun generateDefClass(element: DefElement) {
        val args = element.factoryType.typeArguments
        val returnType = args.last() as ClassName
        val realFactoryType = LambdaTypeName.get(
            returnType = returnType,
            parameters = *args.subList(
                0,
                args.size - 1
            ).toTypedArray()
        )
        val factoryInterface = ClassName(returnType.packageName, returnType.simpleName + "Factory")
        FileSpec.builder(element.packageName, "MyFactory")
            .addType(
                TypeSpec.interfaceBuilder(factoryInterface)
                    .addSuperinterface(realFactoryType)
                    .build()
            )
            .build()
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))

        FileSpec.builder("com.nytimes.inversion", "Inversion_ext_MyFactory")
            .addFunction(
                FunSpec.builder("factory" + if (args.size > 1) args.size - 1 else "")
                    .addAnnotation(
                        AnnotationSpec.builder(JvmName::class)
                            .addMember("\"factory_${returnType.toString().replace('.', '_')}\"")
                            .build()
                    )
                    .receiver(Inversion::class)
                    .addParameter("c", KClass::class.asClassName().parameterizedBy(returnType))
                    .returns(realFactoryType)
                    .addStatement("return loadSingleService<%T>()", factoryInterface)
                    .build()
            )
            .build()
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}