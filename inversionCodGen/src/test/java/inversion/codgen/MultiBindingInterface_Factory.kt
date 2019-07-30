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

package inversion.codgen

import inversion.internal.InversionValidator
import inversion.internal.NamedGeneratedFactory
import kotlin.reflect.KClass

interface MultiBindingInterface_Factory : () -> MultiBindingInterface, NamedGeneratedFactory

class MultiBindingInterface_FactoryValidator : InversionValidator {
  override val factoryClass: KClass<MultiBindingInterface_Factory> = MultiBindingInterface_Factory::class

  override val wrappedClass: KClass<MultiBindingInterface> = MultiBindingInterface::class
}
