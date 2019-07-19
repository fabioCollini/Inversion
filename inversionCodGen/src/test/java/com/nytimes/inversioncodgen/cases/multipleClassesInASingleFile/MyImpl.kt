package com.nytimes.inversioncodgen.cases.multipleClassesInASingleFile

import com.nytimes.inversion.InversionImpl

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl("A")
fun provideImplA(): MyInterface = MyImpl()

@InversionImpl("B")
fun provideImplB(): MyInterface = MyImpl()
