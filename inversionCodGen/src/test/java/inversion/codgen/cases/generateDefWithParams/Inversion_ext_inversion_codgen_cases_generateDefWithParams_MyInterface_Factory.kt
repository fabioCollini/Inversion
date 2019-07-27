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

import inversion.codgen.cases.generateDefWithParams.MyInterface
import inversion.codgen.cases.generateDefWithParams.MyInterface_Factory
import inversion.internal.InversionDelegates
import java.util.ServiceLoader
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("factory_inversion_codgen_cases_generateDefWithParams_MyInterface")
fun Inversion.of(c: KClass<MyInterface>) =
    InversionDelegates.delegateWithReceiver(ServiceLoader.load(MyInterface_Factory::class.java,
    MyInterface_Factory::class.java.classLoader).iterator().next())
