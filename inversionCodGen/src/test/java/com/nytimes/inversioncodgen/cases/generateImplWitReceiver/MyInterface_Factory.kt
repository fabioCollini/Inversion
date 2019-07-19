package com.nytimes.inversioncodgen.cases.generateImplWitReceiver

import com.nytimes.inversion.internal.InversionValidator

interface MyInterface_Factory : (MyClass) -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
