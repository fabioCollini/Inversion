package com.nytimes.inversioncodgen.cases.generateImpl

import com.nytimes.inversion.InversionImpl

@InversionImpl
class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}