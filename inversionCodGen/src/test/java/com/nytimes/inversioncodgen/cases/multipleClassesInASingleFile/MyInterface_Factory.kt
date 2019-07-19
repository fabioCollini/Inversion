package com.nytimes.inversioncodgen.cases.multipleClassesInASingleFile

import com.nytimes.inversion.internal.InversionValidator

interface MyInterface_Factory : () -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override fun getFactoryClass() = MyInterface_Factory::class
}
