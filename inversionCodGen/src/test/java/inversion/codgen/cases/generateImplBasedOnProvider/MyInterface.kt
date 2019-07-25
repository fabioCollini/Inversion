package inversion.codgen.cases.generateImplBasedOnProvider

import inversion.Inversion
import inversion.InversionDef
import inversion.of

interface MyInterface {
    fun doSomething()

    companion object {
        @get:InversionDef
        val factory by Inversion.of(MyInterface::class)
    }
}