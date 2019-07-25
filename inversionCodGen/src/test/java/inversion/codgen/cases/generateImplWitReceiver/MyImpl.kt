package inversion.codgen.cases.generateImplWitReceiver

import inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun MyClass.provideImpl(): MyInterface = MyImpl()
