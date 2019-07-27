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

package inversion.internal

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object InversionDelegates {

    fun <T : Any> delegate(factoryImpl: () -> T): ReadOnlyProperty<Any?, () -> T> =
        object : ReadOnlyProperty<Any?, () -> T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): () -> T =
                factoryImpl
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