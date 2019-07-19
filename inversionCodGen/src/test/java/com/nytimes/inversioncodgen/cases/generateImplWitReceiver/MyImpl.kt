package com.nytimes.inversioncodgen.cases.generateImplWitReceiver

import com.nytimes.inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun MyClass.provideImpl(): MyInterface = MyImpl()
