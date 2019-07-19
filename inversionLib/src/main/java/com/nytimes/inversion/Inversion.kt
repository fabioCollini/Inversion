package com.nytimes.inversion

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object Inversion {

    inline fun <reified T> loadSingleService(): T =
        loadSingleService(T::class.java)

    inline fun <T> loadSingleService(c: Class<T>): T {
        val provider = ServiceLoader.load(c, c.classLoader)
        return provider.iterator().next()
    }

    inline fun <reified T> loadServiceList(): List<T> =
        loadServiceList(T::class.java)

    inline fun <T> loadServiceList(c: Class<T>): List<T> {
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

    fun <T : Any> delegate(factoryImpl: () -> T): ReadOnlyProperty<Any, () -> T> {
        return object : ReadOnlyProperty<Any, () -> T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): () -> T =
                factoryImpl
        }
    }

    fun <T : Any, F> mapDelegate(factoryImpl: List<F>): ReadOnlyProperty<Any?, () -> Map<String, T>>
            where F : () -> T, F : NamedGeneratedFactory {
        return object : ReadOnlyProperty<Any?, () -> Map<String, T>> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): () -> Map<String, T> =
                {
                    factoryImpl.associate { it.name to it() }
                }
        }
    }

    fun <R, T : Any> delegateWithReceiver(factoryImpl: (R) -> T): ReadOnlyProperty<R, () -> T> {
        return object : ReadOnlyProperty<R, () -> T> {
            override fun getValue(thisRef: R, property: KProperty<*>): () -> T =
                { factoryImpl(thisRef) }
        }
    }

    fun <R, T : Any, F> mapDelegateWithReceiver(factoryImpl: List<F>): ReadOnlyProperty<R, () -> Map<String, T>>
            where F : (R) -> T, F : NamedGeneratedFactory {
        return object : ReadOnlyProperty<R, () -> Map<String, T>> {
            override fun getValue(thisRef: R, property: KProperty<*>): () -> Map<String, T> =
                {
                    factoryImpl.associate { it.name to it(thisRef) }
                }
        }
    }
}

@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class InversionDef

@Target(AnnotationTarget.FUNCTION)
annotation class InversionProvider(val value: String = "")

annotation class InversionValidate

interface InversionValidator {
    fun getFactoryClass(): KClass<*>
}

fun <R, T : Any> Inversion.of(c: KClass<T>): ReadOnlyProperty<R, () -> T> = TODO()

fun <R, T : Any> Inversion.mapOf(c: KClass<T>): ReadOnlyProperty<R, () -> Map<String, T>> = TODO()

interface NamedGeneratedFactory {
    val name: String
}