package inversion.codgen.cases.generateDefWithParams

import inversion.internal.InversionValidatorAdapter

interface MyInterface_Factory : (MyClass) -> MyInterface

class MyInterface_FactoryValidator : InversionValidatorAdapter(MyInterface_Factory::class,
    MyInterface::class)
