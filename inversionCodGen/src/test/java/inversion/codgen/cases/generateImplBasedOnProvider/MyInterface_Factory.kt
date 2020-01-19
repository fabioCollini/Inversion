package inversion.codgen.cases.generateImplBasedOnProvider

import inversion.internal.InversionValidatorAdapter

interface MyInterface_Factory : () -> MyInterface

class MyInterface_FactoryValidator : InversionValidatorAdapter(MyInterface_Factory::class,
    MyInterface::class)
