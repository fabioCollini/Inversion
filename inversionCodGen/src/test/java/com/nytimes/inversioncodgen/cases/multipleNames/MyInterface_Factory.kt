package com.nytimes.inversioncodgen.cases.multipleNames

import com.nytimes.inversion.InversionValidator
import com.nytimes.inversion.NamedGeneratedFactory

interface MyInterface_Factory : (MyClass) -> MyInterface, NamedGeneratedFactory

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
