package inversion.codgen.cases.multipleNames

import inversion.Inversion
import inversion.InversionDef
import inversion.mapOf

interface MyClass

interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val MyClass.factory by Inversion.mapOf(MyInterface::class)
