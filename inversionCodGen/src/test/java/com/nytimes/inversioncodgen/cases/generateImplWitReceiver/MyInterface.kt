package com.nytimes.inversioncodgen.cases.generateImplWitReceiver

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of

interface MyClass

interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val MyClass.factory by Inversion.of(MyInterface::class)
