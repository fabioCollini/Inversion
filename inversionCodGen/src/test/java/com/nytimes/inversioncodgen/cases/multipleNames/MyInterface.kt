package com.nytimes.inversioncodgen.cases.multipleNames

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.mapOf

interface MyClass

interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val MyClass.factory by Inversion.mapOf(MyInterface::class)
