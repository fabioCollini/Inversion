package com.nytimes.inversioncodgen.cases.generateDef

import com.nytimes.inversion.internal.InversionValidator
import kotlin.reflect.KClass

interface MyInterface_Factory : () -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override val factoryClass: KClass<MyInterface_Factory> = MyInterface_Factory::class

  override val wrappedClass: KClass<MyInterface> = MyInterface::class
}
