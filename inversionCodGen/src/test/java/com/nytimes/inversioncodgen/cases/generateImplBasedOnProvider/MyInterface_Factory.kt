package com.nytimes.inversioncodgen.cases.generateImplBasedOnProvider

import com.nytimes.inversion.internal.InversionValidator

interface MyInterface_Factory : () -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
