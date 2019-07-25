package inversion.codgen.cases.noErrorOnMultipleSuperclassesWithDef

import inversion.Inversion
import inversion.InversionDef
import inversion.InversionImpl
import inversion.of

@InversionImpl(def = MyInterface::class)
class MyImplWithSuperClassAndAnotherInterface: BaseClass(), MyInterface

open class BaseClass

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}