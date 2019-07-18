package com.nytimes.inversioncodgen.cases.generateImplWitReceiver

import com.nytimes.inversion.InversionImpl

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl
fun MyClass.provideImpl(): MyInterface = MyImpl()
