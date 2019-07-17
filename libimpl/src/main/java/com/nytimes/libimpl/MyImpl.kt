package com.nytimes.libimpl

import com.nytimes.inversion.InversionImpl
import com.nytimes.libinterface.Container
import com.nytimes.libinterface.MyInterface

class MyImpl : MyInterface {
    private val s = "Hello world!!! " + System.currentTimeMillis()

    override fun doSomething(): String = s
}

@InversionImpl
fun provideImpl(param: Container): MyInterface = param.getOrCreate { MyImpl() }