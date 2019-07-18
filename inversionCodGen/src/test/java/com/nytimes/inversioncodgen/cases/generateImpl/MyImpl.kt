package com.nytimes.inversioncodgen.cases.generateImpl

import com.nytimes.inversion.InversionImpl

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl
fun provideImpl(): MyInterface = MyImpl()
