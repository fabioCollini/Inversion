package com.nytimes.inversioncodgen.cases.errorWhenDefIsNotAnnotated

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.InversionImpl
import com.nytimes.inversion.of

@InversionImpl
class MyImplWithInterface: ExtraInterface

interface ExtraInterface

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}