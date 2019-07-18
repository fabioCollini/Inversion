package com.nytimes.inversion

import com.nytimes.inversioncodgen.cases.generateDef.MyInterface
import com.nytimes.inversioncodgen.cases.generateDef.MyInterface_Factory
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("factory_com_nytimes_inversioncodgen_cases_generateDef_MyInterface")
fun Inversion.of2(c: KClass<MyInterface>) = delegate<MyInterface,
    MyInterface_Factory>(MyInterface_Factory::class)
