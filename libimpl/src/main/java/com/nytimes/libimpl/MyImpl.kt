package com.nytimes.libimpl

import com.nytimes.inversion.InversionImpl
import com.nytimes.libinterface.MyInterface

class MyImpl : MyInterface {
    override fun doSomething() {
        println("Hello world!")
    }
}

@InversionImpl
fun provideImpl(): MyInterface = MyImpl()


//@AutoService(MyInterfaceFactory::class)
//class MyFactoryImpl2 : MyInterfaceFactory {
//    override fun invoke(): MyInterface = provideImpl()
//}