package inversion.codgen.cases.noErrorsWhenImplIsAvailable

import inversion.InversionProvider
import inversion.InversionValidate

@InversionValidate
class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun provideImpl(): MyInterface = MyImpl()
