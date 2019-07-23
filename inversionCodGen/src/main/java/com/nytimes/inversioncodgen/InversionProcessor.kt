package com.nytimes.inversioncodgen

import com.google.auto.service.AutoService
import com.nytimes.inversion.*
import com.nytimes.inversion.internal.InversionDelegates
import com.nytimes.inversion.internal.InversionValidator
import com.nytimes.inversion.internal.NamedGeneratedFactory
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.proto
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import java.io.File
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import kotlin.reflect.KClass


private val Element.isCompanionObject: Boolean
    get() {
        return (kotlinMetadata as KotlinClassMetadata).data.proto.classKind == ProtoBuf.Class.Kind.COMPANION_OBJECT
    }

private fun factoryInterface(type: ClassName) =
    ClassName(type.packageName, type.simpleName + "_Factory")

interface ImplElement {
    val packageName: String
    val returnType: ClassName
    val parameters: List<VariableElement>
    val simpleName: Name
    val factoryInterface: ClassName get() = factoryInterface(returnType)
    val instanceName: String
}

class ImplExecutableElement(
    element: ExecutableElement,
    override val packageName: String
) : ImplElement {
    override val returnType = element.returnType.asTypeName() as ClassName
    override val parameters: List<VariableElement> = element.parameters
    override val simpleName: Name = element.simpleName
    override val instanceName = element.getAnnotation(InversionProvider::class.java).value
}

class ImplClassElement(
    element: TypeElement,
    override val packageName: String
) : ImplElement {
    override val returnType = element.interfaces[0].asTypeName() as ClassName
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
    val returnType: ClassName
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
    val factoryInterface get() = factoryInterface(returnType)
}

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(InversionProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class InversionProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() =
        mutableSetOf(
            InversionImpl::class.java.name,
            InversionProvider::class.java.name,
            InversionDef::class.java.name,
            InversionValidate::class.java.name
        )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        val impls = roundEnvironment.getElementsAnnotatedWith(InversionProvider::class.java)
            .filterIsInstance<ExecutableElement>()
            .map { ImplExecutableElement(it, getPackageName(it)) } +
                roundEnvironment.getElementsAnnotatedWith(InversionImpl::class.java)
                    .filterIsInstance<TypeElement>()
                    .map { ImplClassElement(it, getPackageName(it)) }

        impls.map { generateImpl(it) }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .forEach { (key, list) -> generateConfigFiles(processingEnv, key, list) }

        val defs = roundEnvironment.getElementsAnnotatedWith(InversionDef::class.java)
            .filterIsInstance<ExecutableElement>()
            .map { DefElement(it, getPackageName(it)) }

        val validators = defs.map { generateDefClass(it) }

        generateConfigFiles(processingEnv, InversionValidator::class.java.canonicalName, validators)

        roundEnvironment.getElementsAnnotatedWith(InversionValidate::class.java)
            .firstOrNull()
            ?.let { element ->
                processingEnv.log("validate")
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
                val implementations = readImplementationsFromRes(processingEnv, getResourceFile(factoryClass)) +
                        impls.filter { it.factoryInterface.canonicalName == factoryClass }
                if (implementations.isEmpty()) {
                    processingEnv.error("Implementation not found for $factoryClass", element, null)
                }
            }

        loadServiceList<InversionValidator>()
            .forEach {
                val factoryClass = it.factoryClass
                val implementations = loadServiceList(factoryClass.java) +
                        readImplementationsFromRes(processingEnv, getResourceFile(factoryClass.java.canonicalName)) +
                        impls.filter { it.factoryInterface.canonicalName == factoryClass.java.canonicalName }
                if (implementations.isEmpty()) {
                    val className = it.wrappedClass.asClassName().canonicalName
                    processingEnv.error(
                        "Implementation not found for $className",
                        processingEnv.elementUtils.getTypeElement(className),
                        null
                    )
                }
            }
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

    private fun getPackageName(it: Element) =
        processingEnv.elementUtils.getPackageOf(it).toString()

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
                            .returns(element.returnType)
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
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))

        return factoryInterface.canonicalName to "${element.packageName}.$factoryClassName"
    }

    private fun generateDefClass(element: DefElement): String {
        val returnType = element.returnType
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
                        PropertySpec.builder("factoryClass", KClass::class.asClassName().parameterizedBy(factoryInterface), KModifier.OVERRIDE)
                            .initializer("%T::class", factoryInterface)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("wrappedClass", KClass::class.asClassName().parameterizedBy(returnType), KModifier.OVERRIDE)
                            .initializer("%T::class", returnType)
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))

        FileSpec.builder(
            "com.nytimes.inversion",
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
            .writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]))

        return validatorClass.canonicalName
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}