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

package demo.libimpl

import inversion.InversionImpl
import inversion.InversionProvider
import demo.libinterface.Container
import demo.libinterface.MultiInstanceInterface
import demo.libinterface.MyInterface

class MyImpl : MyInterface {
    private val s = "Hello world!!! " + System.currentTimeMillis()

    override fun doSomething(): String = s
}

@InversionProvider
fun Container.provideImpl(): MyInterface = getOrCreate { MyImpl() }

@InversionImpl("A")
class MultiInstanceInterfaceImpl : MultiInstanceInterface {
    override fun toString() = "InstanceA"
}

@InversionProvider("B")
fun provideImplB(): MultiInstanceInterface =
    object : MultiInstanceInterface {
        override fun toString() = "InstanceB"
    }