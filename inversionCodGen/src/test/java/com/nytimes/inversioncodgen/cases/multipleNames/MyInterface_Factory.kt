package com.nytimes.inversioncodgen.cases.multipleNames

import com.nytimes.inversion.internal.InversionValidator
import com.nytimes.inversion.internal.NamedGeneratedFactory
import kotlin.reflect.KClass

interface MyInterface_Factory : (MyClass) -> MyInterface, NamedGeneratedFactory

class MyInterface_FactoryValidator : InversionValidator {
  override val factoryClass: KClass<MyInterface_Factory> = MyInterface_Factory::class

  override val wrappedClass: KClass<MyInterface> = MyInterface::class
}
