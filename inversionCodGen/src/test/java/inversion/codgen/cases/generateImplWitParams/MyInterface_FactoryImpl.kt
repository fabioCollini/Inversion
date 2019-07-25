package inversion.codgen.cases.generateImplWitParams

class MyInterface_FactoryImpl : MyInterface_Factory {
  override fun invoke(param: MyClass): MyInterface = provideImpl(param)
}
