package inversion.codgen.cases.generateDefWithParams

import inversion.Inversion
import inversion.InversionDef
import inversion.of

class MyClass

interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val MyClass.factory by Inversion.of(MyInterface::class)
