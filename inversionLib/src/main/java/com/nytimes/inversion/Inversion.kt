package com.nytimes.inversion

import java.util.*
import kotlin.reflect.KClass

object Inversion {

    inline fun <reified T> loadSingleService(): T =
        loadSingleService(T::class.java)

    fun <T> loadSingleService(c: Class<T>): T {
        val provider = ServiceLoader.load(c, c.classLoader)
        return provider.iterator().next()
    }

    inline fun <reified T> loadServiceList(): List<T> =
        loadServiceList(T::class.java)

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

@JvmName("factory1_invoke")
fun <T : Any> Inversion.of(c: KClass<T>): InversionFactory<T> = TODO()

class InversionFactory<T : Any>(private val c: KClass<*>) {
    fun factory(): () -> T = Inversion.loadSingleService((c as KClass<() -> T>).java)
    fun <P> factory(): (P) -> T = Inversion.loadSingleService((c as KClass<(P) -> T>).java)
}

interface InversionValidator {
    fun getFactoryClass(): KClass<*>
}