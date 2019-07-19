package com.nytimes.inversioncodgen.cases.generateImplBasedOnProvider

class MyInterface_FactoryImpl : MyInterface_Factory {
  override fun invoke(): MyInterface = provideImpl()
}
