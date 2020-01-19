package inversion

import inversion.codgen.cases.generateDefWithParams.MyInterface
import inversion.codgen.cases.generateDefWithParams.MyInterface_Factory
import inversion.internal.InversionDelegates
import java.util.ServiceLoader
import kotlin.Suppress
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("factory_inversion_codgen_cases_generateDefWithParams_MyInterface")
fun Inversion.of(@Suppress("UNUSED_PARAMETER") c: KClass<MyInterface>) =
    InversionDelegates.delegateWithReceiver(ServiceLoader.load(MyInterface_Factory::class.java,
    MyInterface_Factory::class.java.classLoader).iterator().next())
