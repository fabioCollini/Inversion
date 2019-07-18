package com.nytimes.inversioncodgen.cases.generateImplWitParams

import com.nytimes.inversion.InversionImpl

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl
fun provideImpl(param: MyClass): MyInterface = MyImpl()
