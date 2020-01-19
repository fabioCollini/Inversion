package inversion.codgen.cases.multipleClassesInASingleFile

import inversion.internal.InversionValidatorAdapter

interface MyInterface2_Factory : () -> MyInterface2

class MyInterface2_FactoryValidator : InversionValidatorAdapter(MyInterface2_Factory::class,
    MyInterface2::class)
