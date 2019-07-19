package com.nytimes.inversioncodgen.cases.generateImpl

import com.nytimes.inversion.InversionValidator

interface MyInterface_Factory : () -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
