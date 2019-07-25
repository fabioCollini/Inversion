package inversion.codgen.cases.errorWhenImplentationDoesNotDeclareSuperclass

import inversion.Inversion
import inversion.InversionDef
import inversion.InversionImpl
import inversion.of
import inversion.codgen.cases.noErrorOnMultipleSuperclassesWithDef.MyInterface

@InversionImpl
class MyEmptyImpl

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}