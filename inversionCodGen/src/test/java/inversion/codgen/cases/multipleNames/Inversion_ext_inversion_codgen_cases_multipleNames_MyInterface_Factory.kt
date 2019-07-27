package inversion

import inversion.codgen.cases.multipleNames.MyInterface
import inversion.codgen.cases.multipleNames.MyInterface_Factory
import inversion.internal.InversionDelegates
import java.util.ServiceLoader
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("factory_inversion_codgen_cases_multipleNames_MyInterface")
fun Inversion.mapOf(c: KClass<MyInterface>) =
    InversionDelegates.mapDelegateWithReceiver(ServiceLoader.load(MyInterface_Factory::class.java,
    MyInterface_Factory::class.java.classLoader).iterator().asSequence().toList())
