package com.nytimes.inversioncodgen.cases.generateImplWitParams

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of
import com.nytimes.inversioncodgen.cases.generateImpl.MyInterface

class MyClass

interface MyInterface {
    fun doSomething()

    companion object {
        @InversionDef
        val factory = Inversion.of(MyInterface::class).factory<MyClass>()
    }
}
