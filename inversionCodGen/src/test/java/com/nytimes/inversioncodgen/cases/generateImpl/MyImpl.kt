package com.nytimes.inversioncodgen.cases.generateImpl

import com.nytimes.inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun provideImpl(): MyInterface = MyImpl()
