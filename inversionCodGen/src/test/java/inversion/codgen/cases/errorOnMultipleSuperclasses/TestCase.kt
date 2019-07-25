package inversion.codgen.cases.errorOnMultipleSuperclasses

import inversion.Inversion
import inversion.InversionDef
import inversion.InversionImpl
import inversion.of

@InversionImpl
class MyImplWithSuperClassAndAnotherInterface: BaseClass(), MyInterface

open class BaseClass

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}