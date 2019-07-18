package com.nytimes.inversion

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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

    fun <T : Any, F : Any> delegate(factoryClass: KClass<F>): ReadOnlyProperty<Any, () -> T> {
        return object : ReadOnlyProperty<Any, () -> T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): () -> T {
                return {
                    InversionFactory<T>(factoryClass).factory()()
                }
            }
        }
    }

    fun <R, T : Any, F : Any> delegateWithReceiver(factoryClass: KClass<F>): ReadOnlyProperty<R, () -> T> {
        return object : ReadOnlyProperty<R, () -> T> {
            override fun getValue(thisRef: R, property: KProperty<*>): () -> T {
                return {
                    InversionFactory<T>(factoryClass).factory<R>()(thisRef)
                }
            }
        }
    }
}

@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class InversionDef

@Target(AnnotationTarget.FUNCTION)
annotation class InversionImpl

annotation class InversionValidate

class InversionFactory<T : Any>(private val c: KClass<*>) {
    fun factory(): () -> T = Inversion.loadSingleService((c as KClass<() -> T>).java)
    fun <P> factory(): (P) -> T = Inversion.loadSingleService((c as KClass<(P) -> T>).java)
}

interface InversionValidator {
    fun getFactoryClass(): KClass<*>
}

fun <R, T : Any> Inversion.of(c: KClass<T>): ReadOnlyProperty<R, () -> T> = TODO()
