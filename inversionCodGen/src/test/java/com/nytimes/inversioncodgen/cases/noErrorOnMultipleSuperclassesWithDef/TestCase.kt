package com.nytimes.inversioncodgen.cases.noErrorOnMultipleSuperclassesWithDef

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.InversionImpl
import com.nytimes.inversion.of

@InversionImpl(def = MyInterface::class)
class MyImplWithSuperClassAndAnotherInterface: BaseClass(), MyInterface

open class BaseClass

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}