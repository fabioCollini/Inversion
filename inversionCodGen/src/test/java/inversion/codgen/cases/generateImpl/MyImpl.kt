package inversion.codgen.cases.generateImpl

import inversion.InversionImpl

@InversionImpl
class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}