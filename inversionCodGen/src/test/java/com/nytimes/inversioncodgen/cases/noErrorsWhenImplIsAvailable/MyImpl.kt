package com.nytimes.inversioncodgen.cases.noErrorsWhenImplIsAvailable

import com.nytimes.inversion.InversionProvider
import com.nytimes.inversion.InversionValidate

@InversionValidate
class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionProvider
fun provideImpl(): MyInterface = MyImpl()
