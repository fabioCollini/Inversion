package inversion.codgen.cases.generateImplWitReceiver

import inversion.internal.InversionValidator
import kotlin.reflect.KClass

interface MyInterface_Factory : (MyClass) -> MyInterface

class MyInterface_FactoryValidator : InversionValidator {
  override val factoryClass: KClass<MyInterface_Factory> = MyInterface_Factory::class

  override val wrappedClass: KClass<MyInterface> = MyInterface::class
}
