package inversion.codgen.cases.multipleNames

import inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider("A")
fun MyClass.provideImplA(): MyInterface = MyImpl()

@InversionProvider("B")
fun MyClass.provideImplB(): MyInterface = MyImpl()
