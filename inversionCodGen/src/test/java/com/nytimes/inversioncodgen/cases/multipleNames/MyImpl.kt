package com.nytimes.inversioncodgen.cases.multipleNames

import com.nytimes.inversion.InversionImpl

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl("A")
fun MyClass.provideImplA(): MyInterface = MyImpl()

@InversionImpl("B")
fun MyClass.provideImplB(): MyInterface = MyImpl()
