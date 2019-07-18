package com.nytimes.inversioncodgen.cases.errorWhenImplementationIsNotAvailable

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.InversionValidate
import com.nytimes.inversion.of

@InversionValidate
interface MyInterface {
    fun doSomething()

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}
