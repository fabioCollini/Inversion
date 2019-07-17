package com.nytimes.inversion

import java.util.*
import kotlin.reflect.KClass

object Inversion {

    inline fun <reified T> loadSingleService(): T {
        val provider = ServiceLoader.load(T::class.java, T::class.java.classLoader)
        return provider.iterator().next()
    }

    inline fun <reified T> loadServiceList(): List<T> {
        return loadServiceList(T::class.java)
    }

    fun <T> loadServiceList(c: Class<T>): List<T> {
        return try {
            val provider = ServiceLoader.load(c, c.classLoader)
            val ret = mutableListOf<T>()
            val iterator = provider.iterator()
            while (iterator.hasNext()) {
                ret.add(iterator.next())
            }
            ret
        } catch (e: ServiceConfigurationError) {
            emptyList()
        }
    }
}

@Target(AnnotationTarget.FIELD)
annotation class InversionDef

@Target(AnnotationTarget.FUNCTION)
annotation class InversionImpl

annotation class InversionValidate

fun <T : Any> Inversion.factory(c: KClass<T>): () -> T = TODO()

@JvmName("factory1")
fun <T : Any, P> Inversion.factory1(c: KClass<T>): (P) -> T = TODO()

interface InversionValidator {
    fun getFactoryClass(): KClass<*>
}