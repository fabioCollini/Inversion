package com.nytimes.inversioncodgen.cases.generateDef

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of

interface MyInterface {
    fun doSomething()

    companion object {
        @InversionDef
        val factory = Inversion.of(MyInterface::class).factory()
    }
}