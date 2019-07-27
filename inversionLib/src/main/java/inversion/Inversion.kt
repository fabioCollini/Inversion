/*
 * Copyright 2019 Fabio Collini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package inversion

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

object Inversion

@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class InversionDef

@Target(AnnotationTarget.CLASS)
annotation class InversionImpl(val value: String = "", val def: KClass<*> = Nothing::class)

@Target(AnnotationTarget.FUNCTION)
annotation class InversionProvider(val value: String = "")

annotation class InversionValidate

fun <R, T : Any> Inversion.of(c: KClass<T>): ReadOnlyProperty<R, () -> T> =
    throw Exception("This method shouldn't never be invoked, there are some problems in the Inversion annotation processor")

fun <R, T : Any> Inversion.mapOf(c: KClass<T>): ReadOnlyProperty<R, () -> Map<String, T>> =
    throw Exception("This method shouldn't never be invoked, there are some problems in the Inversion annotation processor")
