package com.nytimes.inversion.internal

import kotlin.reflect.KClass

interface InversionValidator {
    fun getFactoryClass(): KClass<*>
}