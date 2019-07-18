package com.nytimes.inversioncodgen.cases.generateDefWithParams

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of

class MyClass

interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val MyClass.factory by Inversion.of(MyInterface::class)
