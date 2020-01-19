package inversion.codgen.cases.multipleNames

import inversion.internal.InversionValidatorAdapter
import inversion.internal.NamedGeneratedFactory

interface MyInterface_Factory : (MyClass) -> MyInterface, NamedGeneratedFactory

class MyInterface_FactoryValidator : InversionValidatorAdapter(MyInterface_Factory::class,
    MyInterface::class)
