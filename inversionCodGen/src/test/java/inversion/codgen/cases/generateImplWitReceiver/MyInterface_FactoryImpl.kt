package inversion.codgen.cases.generateImplWitReceiver

class MyInterface_FactoryImpl : MyInterface_Factory {
  override fun invoke(param: MyClass): MyInterface = param.provideImpl()
}
