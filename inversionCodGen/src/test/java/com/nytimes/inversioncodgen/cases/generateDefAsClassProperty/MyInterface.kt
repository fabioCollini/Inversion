package com.nytimes.inversioncodgen.cases.generateDefAsClassProperty

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of

class MyClass {
    @get:InversionDef
    val factory by Inversion.of(MyInterface::class)
}

interface MyInterface {
    fun doSomething()
}
