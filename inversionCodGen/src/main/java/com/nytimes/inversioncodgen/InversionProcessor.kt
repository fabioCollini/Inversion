package com.nytimes.inversioncodgen

import com.google.auto.service.AutoService
import com.nytimes.inversion.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KClass


class ImplElement(
    element: ExecutableElement,
    val packageName: String
) {
    val methodName = element.simpleName.toString()
    val returnType = element.returnType.asTypeName() as ClassName
    val parameters: List<VariableElement> = element.parameters
    val simpleName: Name = element.simpleName
    val factoryInterface = ClassName(returnType.packageName, returnType.simpleName + "Factory")
}

class DefElement(
    element: VariableElement,
    val packageName: String
) {
    val factoryType = element.asType().asTypeName() as ParameterizedTypeName
    val returnType = factoryType.typeArguments.last() as ClassName
    val factoryInterface = ClassName(returnType.packageName, returnType.simpleName + "Factory")
}

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(InversionProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class InversionProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() =
        mutableSetOf(
            InversionImpl::class.java.name,
            InversionDef::class.java.name,
            InversionValidate::class.java.name
        )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        val impls = roundEnvironment.getElementsAnnotatedWith(InversionImpl::class.java)
            .filterIsInstance<ExecutableElement>()
            .map { ImplElement(it, getPackageName(it)) }

        impls.forEach { generateImpl(it) }

        val defs = roundEnvironment.getElementsAnnotatedWith(InversionDef::class.java)
            .filterIsInstance<VariableElement>()
            .map { DefElement(it, getPackageName(it)) }

        defs.forEach { generateDefClass(it) }

        roundEnvironment.getElementsAnnotatedWith(InversionValidate::class.java)
            .firstOrNull()
            ?.let { element ->
                log("validate")
                validateAllDependencies(element, defs, impls)
            }

        return true
    }

    private fun validateAllDependencies(
        element: Element,
        defs: List<DefElement>,
        impls: List<ImplElement>
    ) {
        defs.map { it.factoryInterface }
            .map { it.canonicalName }
            .forEach { factoryClass ->
                val implementations = readImplementationsFromRes(getResourceFile(factoryClass)) +
                        impls.filter { it.factoryInterface.canonicalName == factoryClass }
                if (implementations.isEmpty()) {
                    error("Implementation not found for $factoryClass", element, null)
                }
            }

        Inversion.loadServiceList<InversionValidator>()
            .map { it.getFactoryClass() }
            .forEach { factoryClass ->
                val implementations = Inversion.loadServiceList(factoryClass.java) +
                        readImplementationsFromRes(getResourceFile(factoryClass.java.canonicalName)) +
                        impls.filter { it.factoryInterface.canonicalName == factoryClass.java.canonicalName }
                if (implementations.isEmpty()) {
                    error("Implementation not found for $factoryClass", element, null)
                }
            }
    }

    private fun getPackageName(it: Element) =
        processingEnv.elementUtils.getPackageOf(it).toString()

    private fun generateImpl(element: ImplElement) {
        val factoryInterface = element.factoryInterface
        val factoryClassName = "${element.methodName}__factory"
        FileSpec.builder(element.packageName, "MyFactoryImpl")
            .addType(
                TypeSpec.classBuilder(factoryClassName)
                    .addSuperinterface(factoryInterface)
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

        generateConfigFiles(
            factoryInterface.canonicalName,
            "${element.packageName}.$factoryClassName"
        )
    }

    private fun generateDefClass(element: DefElement) {
        val args = element.factoryType.typeArguments
        val returnType = element.returnType
        val realFactoryType = LambdaTypeName.get(
            returnType = returnType,
            parameters = *args.subList(
                0,
                args.size - 1
            ).toTypedArray()
        )
        val factoryInterface = element.factoryInterface
        val validatorClass = ClassName(
            returnType.packageName,
            returnType.simpleName + "FactoryValidator"
        )
        FileSpec.builder(element.packageName, "MyFactory")
            .addType(
                TypeSpec.interfaceBuilder(factoryInterface)
                    .addSuperinterface(realFactoryType)
                    .build()
            )
            .addType(
                TypeSpec.classBuilder(
                    validatorClass
                )
                    .addSuperinterface(InversionValidator::class)
                    .addFunction(
                        FunSpec.builder("getFactoryClass")
                            .addModifiers(KModifier.OVERRIDE)
                            .addStatement("return %T::class", factoryInterface)
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))

        generateConfigFiles(
            InversionValidator::class.java.canonicalName,
            validatorClass.canonicalName
        )

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

    /**
     * Kotlin conversion of the AutoService method
     * https://github.com/google/auto/blob/master/service/processor/src/main/java/com/google/auto/service/processor/AutoServiceProcessor.java
     */
    private fun generateConfigFiles(providerInterface: String, newService: String) {
        val resourceFile = getResourceFile(providerInterface)
        log("Working on resource file: $resourceFile")
        try {
            val allServices = readImplementationsFromRes(resourceFile)

            if (allServices.contains(newService)) {
                log("No new service entries being added.")
                return
            }

            allServices.add(newService)
            log("New service file contents: $allServices")
            val fileObject = processingEnv.filer.createResource(
                StandardLocation.CLASS_OUTPUT, "",
                resourceFile
            )
            val out = fileObject.openOutputStream()
            ServicesFiles.writeServiceFile(allServices, out)
            out.close()
            log("Wrote to: " + fileObject.toUri())
        } catch (e: IOException) {
            fatalError("Unable to create $resourceFile, $e")
            return
        }
    }

    private fun readImplementationsFromRes(resourceFile: String): MutableSet<String> {
        val allServices = mutableSetOf<String>()
        try {
            // would like to be able to print the full path
            // before we attempt to get the resource in case the behavior
            // of filer.getResource does change to match the spec, but there's
            // no good way to resolve CLASS_OUTPUT without first getting a resource.
            val existingFile = processingEnv.filer.getResource(
                StandardLocation.CLASS_OUTPUT, "",
                resourceFile
            )
            log("Looking for existing resource file at " + existingFile.toUri())
            val oldServices = ServicesFiles.readServiceFile(existingFile.openInputStream())
            log("Existing service entries: $oldServices")
            allServices.addAll(oldServices)
        } catch (e: IOException) {
            // According to the javadoc, Filer.getResource throws an exception
            // if the file doesn't already exist.  In practice this doesn't
            // appear to be the case.  Filer.getResource will happily return a
            // FileObject that refers to a non-existent file but will throw
            // IOException if you try to open an input stream for it.
            log("Resource file did not already exist.")
        }
        return allServices
    }

    private fun getResourceFile(providerInterface: String) =
        "META-INF/services/$providerInterface"

    private fun log(msg: String) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, msg)
        }
    }

    private fun error(msg: String, element: Element, annotation: AnnotationMirror?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg, element, annotation)
    }

    private fun fatalError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: $msg")
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}