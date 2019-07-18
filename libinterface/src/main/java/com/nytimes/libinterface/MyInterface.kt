package com.nytimes.libinterface

import com.nytimes.inversion.Inversion
import com.nytimes.inversion.InversionDef
import com.nytimes.inversion.of

interface Container {
    fun <V> getOrCreate(f: () -> V): V
}

interface MyInterface {
    fun doSomething(): String

//    companion object {
//        @get:InversionDef
//        val factory by Inversion.of(MyInterface::class)
//    }
}

@get:InversionDef
val Container.factory by Inversion.of(MyInterface::class)
