package com.nytimes.inversioncodgen.cases.multipleNames

import com.nytimes.inversion.internal.InversionValidator
import com.nytimes.inversion.internal.NamedGeneratedFactory

interface MyInterface_Factory : (MyClass) -> MyInterface, NamedGeneratedFactory

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
