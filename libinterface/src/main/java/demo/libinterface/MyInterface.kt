package demo.libinterface

import inversion.Inversion
import inversion.InversionDef
import inversion.mapOf
import inversion.of

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


interface MultiInstanceInterface

@get:InversionDef
val multipleInstancesMap by Inversion.mapOf(MultiInstanceInterface::class)