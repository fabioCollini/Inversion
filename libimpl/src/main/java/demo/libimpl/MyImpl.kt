package demo.libimpl

import inversion.InversionImpl
import inversion.InversionProvider
import demo.libinterface.Container
import demo.libinterface.MultiInstanceInterface
import demo.libinterface.MyInterface

class MyImpl : MyInterface {
    private val s = "Hello world!!! " + System.currentTimeMillis()

    override fun doSomething(): String = s
}

@InversionProvider
fun Container.provideImpl(): MyInterface = getOrCreate { MyImpl() }

@InversionImpl("A")
class MultiInstanceInterfaceImpl : MultiInstanceInterface {
    override fun toString() = "InstanceA"
}

@InversionProvider("B")
fun provideImplB(): MultiInstanceInterface =
    object : MultiInstanceInterface {
        override fun toString() = "InstanceB"
    }