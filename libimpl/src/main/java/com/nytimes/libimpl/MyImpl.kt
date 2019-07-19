package com.nytimes.libimpl

import com.nytimes.inversion.InversionImpl
import com.nytimes.libinterface.Container
import com.nytimes.libinterface.MultiInstanceInterface
import com.nytimes.libinterface.MyInterface

class MyImpl : MyInterface {
    private val s = "Hello world!!! " + System.currentTimeMillis()

    override fun doSomething(): String = s
}

@InversionImpl
fun Container.provideImpl(): MyInterface = getOrCreate { MyImpl() }

@InversionImpl("A")
fun provideImplA(): MultiInstanceInterface =
    object : MultiInstanceInterface {
        override fun toString() = "InstanceA"
    }

@InversionImpl("B")
fun provideImplB(): MultiInstanceInterface =
    object : MultiInstanceInterface {
        override fun toString() = "InstanceB"
    }