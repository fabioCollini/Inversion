package com.nytimes.libinterface

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef

interface MyInterface {
    fun doSomething()

    companion object {
        @InversionDef
        val factory = Inversion.factory(MyInterface::class)
    }
}


//        val factory: InversionFactory2<MyInterface, String> = Inversion.factory2(MyInterface::class)

////val createMyInterface = Inversion.factory(MyInterface::class)
//
////val createMyInterfaceVal = Inversion.factory2<MyInterface, String>(MyInterface::class)
////
////fun createMyInterface(): MyInterface = Inversion.get(MyInterface::class)
////
////fun createMyInterfaces(): List<MyInterface> = Inversion.getList(MyInterface::class)
////
//interface MyInterfaceFactory2 : InversionFactory2<MyInterface, String>
//interface MyInterfaceFactory : InversionFactory<MyInterface>
//
//@JvmName("factory_MyInterface")
//fun Inversion.factory(c: KClass<MyInterface>): InversionFactory<MyInterface> = loadSingleService<MyInterfaceFactory>()
//
//@JvmName("factory2_MyInterface")
//fun Inversion.factory2(c: KClass<MyInterface>): InversionFactory2<MyInterface, String> =
//    loadSingleService<MyInterfaceFactory2>()
////fun <T: MyInterface, P: String> Inversion.factory2(c: KClass<T>): InversionFactory2<in T, in P> = loadSingleService<MyInterfaceFactory>()