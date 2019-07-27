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

package demo.libinterface

import inversion.Inversion
import inversion.InversionDef
import inversion.mapOf
import inversion.of

interface Container {
    fun <V> getOrCreate(f: () -> V): V
}

interface MyInterface {
    fun doSomething(): String

//    companion object {
//        @get:InversionDef
//        val factory by Inversion.of(MyInterface::class)
//    }
}

@get:InversionDef
val Container.factory by Inversion.of(MyInterface::class)


interface MultiInstanceInterface

@get:InversionDef
val multipleInstancesMap by Inversion.mapOf(MultiInstanceInterface::class)