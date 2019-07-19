package com.nytimes.inversioncodgen.cases.generateImplWitParams

import com.nytimes.inversion.InversionProvider

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun provideImpl(param: MyClass): MyInterface = MyImpl()
