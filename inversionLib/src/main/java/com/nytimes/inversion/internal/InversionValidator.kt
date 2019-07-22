package com.nytimes.inversion.internal

import kotlin.reflect.KClass

interface InversionValidator {
    val wrappedClass: KClass<*>

    val factoryClass: KClass<*>
}