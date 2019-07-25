package inversion.codgen.cases.generateImplWitParams

import inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun provideImpl(param: MyClass): MyInterface = MyImpl()
