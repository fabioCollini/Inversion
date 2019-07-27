package inversion.codgen.cases.generateImpl

class MyInterface_FactoryImpl : MyInterface_Factory {
  override fun invoke(): MyInterface = MyImpl()
}
