package inversion.codgen.cases.multipleClassesInASingleFile

import inversion.internal.InversionValidatorAdapter

interface MyInterface_Factory : () -> MyInterface

class MyInterface_FactoryValidator : InversionValidatorAdapter(MyInterface_Factory::class,
    MyInterface::class)
