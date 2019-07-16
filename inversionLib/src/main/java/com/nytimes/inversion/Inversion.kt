package com.nytimes.inversion

import java.util.*
import kotlin.reflect.KClass

object Inversion {

    inline fun <reified T> loadSingleService(): T {
        val provider = ServiceLoader.load(T::class.java, T::class.java.classLoader)
        return provider.iterator().next()
    }

    inline fun <reified T> loadServiceList(): List<T> {
        val provider = ServiceLoader.load(T::class.java, T::class.java.classLoader)
        val ret = mutableListOf<T>()
        val iterator = provider.iterator()
        while (iterator.hasNext()) {
            ret.add(iterator.next())
        }
        return ret
    }
}

@Target(AnnotationTarget.FIELD)
annotation class InversionDef

@Target(AnnotationTarget.FUNCTION)
annotation class InversionImpl

//fun <T : Any> Inversion.get(c: KClass<T>): T = TODO()
//
//fun Inversion.get(c: KClass<MyInterface>): MyInterface =
//    loadSingleService<MyInterfaceFactory>().create()
//
//fun <T : Any> Inversion.getList(c: KClass<T>): List<T> = TODO()
//
//@JvmName("getList_MyInterface")
//fun Inversion.getList(c: KClass<MyInterface>): List<MyInterface> =
//    loadServiceList<MyInterfaceFactory>().map { it.create() }

fun <T : Any> Inversion.factory(c: KClass<T>): () -> T = TODO()


interface InversionFactory2<T, P> {
    operator fun invoke(p: P): T
}

@JvmName("factory2")
fun <T : Any, P> Inversion.factory2(c: KClass<T>): InversionFactory2<T, P> = TODO()