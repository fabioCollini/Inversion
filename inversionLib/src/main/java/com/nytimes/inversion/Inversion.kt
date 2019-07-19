package com.nytimes.inversion

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

object Inversion

@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class InversionDef

@Target(AnnotationTarget.CLASS)
annotation class InversionImpl(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
annotation class InversionProvider(val value: String = "")

annotation class InversionValidate

fun <R, T : Any> Inversion.of(c: KClass<T>): ReadOnlyProperty<R, () -> T> =
    throw Exception("This method shouldn't never be invoked, there are some problems in the Inversion annotation processor")

fun <R, T : Any> Inversion.mapOf(c: KClass<T>): ReadOnlyProperty<R, () -> Map<String, T>> =
    throw Exception("This method shouldn't never be invoked, there are some problems in the Inversion annotation processor")
