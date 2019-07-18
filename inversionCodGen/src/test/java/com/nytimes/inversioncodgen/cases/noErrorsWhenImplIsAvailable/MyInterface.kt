package com.nytimes.inversioncodgen.cases.noErrorsWhenImplIsAvailable

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of2

interface MyInterface {
    fun doSomething()

    companion object {
        @get:InversionDef
        val factory by Inversion.of2(MyInterface::class)
    }
}
