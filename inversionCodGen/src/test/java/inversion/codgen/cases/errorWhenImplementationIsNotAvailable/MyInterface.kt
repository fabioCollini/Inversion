package inversion.codgen.cases.errorWhenImplementationIsNotAvailable

import inversion.Inversion
import inversion.InversionDef
import inversion.InversionValidate
import inversion.of

@InversionValidate
interface MyInterface {
    fun doSomething()

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}
