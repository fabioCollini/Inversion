package com.nytimes.inversioncodgen.cases.noErrorsWhenImplIsAvailable

import com.nytimes.inversion.InversionImpl
import com.nytimes.inversion.InversionValidate

@InversionValidate
class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl
fun provideImpl(): MyInterface = MyImpl()
