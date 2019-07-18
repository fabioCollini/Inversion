package com.nytimes.inversioncodgen.cases.generateImpl

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of2
import com.nytimes.inversioncodgen.cases.generateDef.MyInterface

interface MyInterface {
    fun doSomething()

    companion object {
        @get:InversionDef
        val factory by Inversion.of2(MyInterface::class)
    }
}