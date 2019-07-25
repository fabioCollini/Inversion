package inversion

import inversion.codgen.cases.generateDefAsClassProperty.MyInterface
import inversion.codgen.cases.generateDefAsClassProperty.MyInterface_Factory
import inversion.internal.InversionDelegates
import java.util.ServiceLoader
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("factory_inversion_codgen_cases_generateDefAsClassProperty_MyInterface")
fun Inversion.of(c: KClass<MyInterface>) =
    InversionDelegates.delegateWithReceiver(ServiceLoader.load(MyInterface_Factory::class.java,
    MyInterface_Factory::class.java.classLoader).iterator().next())
