package inversion.codgen.cases.multipleClassesInASingleFile

import inversion.internal.InversionValidator
import kotlin.reflect.KClass

interface MyInterface2_Factory : () -> MyInterface2

class MyInterface2_FactoryValidator : InversionValidator {
  override val factoryClass: KClass<MyInterface2_Factory> = MyInterface2_Factory::class

  override val wrappedClass: KClass<MyInterface2> = MyInterface2::class
}
