package inversion.codgen.cases.generateDefAsClassProperty

import inversion.Inversion
import inversion.InversionDef
import inversion.of

class MyClass {
    @get:InversionDef
    val factory by Inversion.of(MyInterface::class)
}

interface MyInterface {
    fun doSomething()
}
