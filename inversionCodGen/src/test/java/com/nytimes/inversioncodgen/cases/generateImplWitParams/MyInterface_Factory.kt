package com.nytimes.inversioncodgen.cases.generateImplWitParams

import com.nytimes.inversion.InversionValidator

interface MyInterface_Factory : (MyClass) -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
