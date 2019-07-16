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
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass


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
        roundEnvironment?.getElementsAnnotatedWith(InversionImpl::class.java)
            .orEmpty()
            .filterIsInstance<ExecutableElement>()
            .forEach { generateImpl(it) }
        roundEnvironment?.getElementsAnnotatedWith(InversionDef::class.java)
            .orEmpty()
            .filterIsInstance<VariableElement>()
            .forEach { generateDefClass(it) }
        return true
    }

    private fun getPackageName(it: Element) =
        processingEnv.elementUtils.getPackageOf(it).toString()

    private fun generateImpl(element: ExecutableElement) {
        val methodName = element.simpleName.toString()
        val pack = getPackageName(element)
        val returnType = element.returnType.asTypeName() as ClassName
        val factoryInterface = ClassName(returnType.packageName, returnType.simpleName + "Factory")
        val file = FileSpec.builder(pack, "MyFactoryImpl")
            .addType(
                TypeSpec.classBuilder("${methodName}__factory")
                    .addSuperinterface(factoryInterface)
                    .addAnnotation(
                        AnnotationSpec.builder(AutoService::class)
                            .addMember(factoryInterface.simpleName + "::class")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(element.returnType.asTypeName())
                            .addStatement("return ${element.simpleName}()")
                            .build()
                    )
                    .build()
            )
            .build()

        file.writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))
    }

    private fun generateDefClass(element: VariableElement) {
        val pack = getPackageName(element)
        val returnType = element.asType().asTypeName()
        val arg = (returnType as ParameterizedTypeName).typeArguments[0] as ClassName
        val factoryInterface = ClassName(arg.packageName, arg.simpleName + "Factory")
        val file = FileSpec.builder(pack, "MyFactory")
            .addType(
                TypeSpec.interfaceBuilder(factoryInterface)
                    .addSuperinterface(returnType)
                    .build()
            )
            .addFunction(
                FunSpec.builder("factory")
                    .addAnnotation(
                        AnnotationSpec.builder(JvmName::class)
                            .addMember("\"factory_${arg.toString().replace('.', '_')}\"")
                            .build()
                    )
                    .receiver(Inversion::class)
                    .addParameter("c", KClass::class.asClassName().parameterizedBy(arg))
                    .returns(returnType)
                    .addStatement("return loadSingleService<%T>()", factoryInterface)
                    .build()
            )
            .build()

        file.writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}