package com.nytimes.inversion

import com.nytimes.inversioncodgen.cases.generateDefWithParams.MyInterface
import com.nytimes.inversioncodgen.cases.generateDefWithParams.MyInterface_Factory
import kotlin.reflect.KClass

@JvmName("factory_com_nytimes_inversioncodgen_cases_generateDefWithParams_MyInterface")
fun Inversion.of(c: KClass<MyInterface>) = InversionFactory<MyInterface>(MyInterface_Factory::class)