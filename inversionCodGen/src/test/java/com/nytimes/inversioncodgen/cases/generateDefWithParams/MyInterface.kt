package com.nytimes.inversioncodgen.cases.generateDefWithParams

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of

class MyClass

interface MyInterface {
    fun doSomething()

    companion object {
        @InversionDef
        val factory = Inversion.of(MyInterface::class).factory<MyClass>()
    }
}
