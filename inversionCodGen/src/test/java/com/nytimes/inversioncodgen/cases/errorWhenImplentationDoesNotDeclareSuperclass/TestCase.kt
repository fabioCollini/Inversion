package com.nytimes.inversioncodgen.cases.errorWhenImplentationDoesNotDeclareSuperclass

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.InversionImpl
import com.nytimes.inversion.of
import com.nytimes.inversioncodgen.cases.noErrorOnMultipleSuperclassesWithDef.MyInterface

@InversionImpl
class MyEmptyImpl

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}