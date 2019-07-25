package inversion.codgen.cases.generateImplWitReceiver

import inversion.Inversion
import inversion.InversionDef
import inversion.of

interface MyClass

interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val MyClass.factory by Inversion.of(MyInterface::class)
