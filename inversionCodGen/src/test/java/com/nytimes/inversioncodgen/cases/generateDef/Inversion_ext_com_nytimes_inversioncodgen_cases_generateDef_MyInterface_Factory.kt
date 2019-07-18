package com.nytimes.inversion

import com.nytimes.inversioncodgen.cases.generateDef.MyInterface
import com.nytimes.inversioncodgen.cases.generateDef.MyInterface_Factory
import kotlin.reflect.KClass

@JvmName("factory_com_nytimes_inversioncodgen_cases_generateDef_MyInterface")
fun Inversion.of(c: KClass<MyInterface>) = InversionFactory<MyInterface>(MyInterface_Factory::class)