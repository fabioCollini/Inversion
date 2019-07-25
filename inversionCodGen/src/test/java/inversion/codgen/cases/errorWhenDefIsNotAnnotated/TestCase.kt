package inversion.codgen.cases.errorWhenDefIsNotAnnotated

import inversion.Inversion
import inversion.InversionDef
import inversion.InversionImpl
import inversion.of

@InversionImpl
class MyImplWithInterface: ExtraInterface

interface ExtraInterface

interface MyInterface {

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}