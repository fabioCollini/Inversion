package com.nytimes.inversioncodgen.cases.multipleNames

import kotlin.String

class MyInterface_FactoryImpl_A : MyInterface_Factory {
  override val name: String = "A"

  override fun invoke(param: MyClass): MyInterface = param.provideImplA()
}
