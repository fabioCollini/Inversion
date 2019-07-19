package com.nytimes.inversioncodgen.cases.generateImplBasedOnProvider

import com.nytimes.inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun provideImpl(): MyInterface = MyImpl()
