package inversion.codgen.cases.multipleClassesInASingleFile

import inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider("A")
fun provideImplA(): MyInterface = MyImpl()

@InversionProvider("B")
fun provideImplB(): MyInterface = MyImpl()
