package com.nytimes.inversion

import com.nytimes.inversion.internal.InversionDelegates
import com.nytimes.inversioncodgen.cases.multipleNames.MyInterface
import com.nytimes.inversioncodgen.cases.multipleNames.MyInterface_Factory
import java.util.ServiceLoader
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("factory_com_nytimes_inversioncodgen_cases_multipleNames_MyInterface")
fun Inversion.mapOf(c: KClass<MyInterface>) =
    InversionDelegates.mapDelegateWithReceiver(ServiceLoader.load(MyInterface_Factory::class.java,
    MyInterface_Factory::class.java.classLoader).iterator().asSequence().toList())