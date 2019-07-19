package com.nytimes.inversioncodgen.cases.multipleClassesInASingleFile

import com.nytimes.inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider("A")
fun provideImplA(): MyInterface = MyImpl()

@InversionProvider("B")
fun provideImplB(): MyInterface = MyImpl()
