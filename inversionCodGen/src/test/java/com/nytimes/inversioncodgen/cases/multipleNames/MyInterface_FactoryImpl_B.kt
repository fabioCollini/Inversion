package com.nytimes.inversioncodgen.cases.multipleNames

import kotlin.String

class MyInterface_FactoryImpl_B : MyInterface_Factory {
  override val name: String = "B"

  override fun invoke(param: MyClass): MyInterface = param.provideImplB()
}
