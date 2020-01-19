package inversion.codgen.cases.generateImplOnCompanionObject

class MyInterface_FactoryImpl : MyInterface_Factory {
  override fun invoke(): MyInterface = MyImpl.Companion.provideImpl()
}
