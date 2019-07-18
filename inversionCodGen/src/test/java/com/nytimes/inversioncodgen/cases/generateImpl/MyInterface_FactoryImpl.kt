package com.nytimes.inversioncodgen.cases.generateImpl

class MyInterface_FactoryImpl : MyInterface_Factory {
  override fun invoke(): MyInterface = provideImpl()
}
