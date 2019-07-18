package com.nytimes.inversioncodgen

import kompile.testing.kotlinc
import org.junit.Test

class InversionProcessorTest {
    @Test
    fun generateDef() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libimpl/MyInterface.kt", """
                package com.nytimes.libinterface

                import com.nytimes.inversion.Inversion
                import com.nytimes.inversion.InversionDef
                import com.nytimes.inversion.of
                
                interface MyInterface {
                    fun doSomething()
                
                    companion object {
                        @InversionDef
                        val factory = Inversion.of(MyInterface::class).factory()
                    }
                }
                """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .apply {
                generatedFile("com/nytimes/libinterface/MyInterface_Factory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.libinterface
                        
                        import com.nytimes.inversion.InversionValidator
                        
                        interface MyInterface_Factory : () -> MyInterface
                        
                        class MyInterface_FactoryValidator : InversionValidator {
                          override fun getFactoryClass() = MyInterface_Factory::class
                        }
        
                        """.trimIndent()
                    )

                generatedFile("com/nytimes/inversion/Inversion_ext_com_nytimes_libinterface_MyInterface_Factory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.inversion
                        
                        import com.nytimes.libinterface.MyInterface
                        import com.nytimes.libinterface.MyInterface_Factory
                        import kotlin.jvm.JvmName
                        import kotlin.reflect.KClass
                        
                        @JvmName("factory_com_nytimes_libinterface_MyInterface")
                        fun Inversion.of(c: KClass<MyInterface>) = InversionFactory<MyInterface>(MyInterface_Factory::class)

                        """.trimIndent()
                    )
            }
    }

    @Test
    fun generateImpl() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libinterface/MyInterface.kt", """
                package com.nytimes.libinterface

                interface MyInterface {
                    fun doSomething()
                }
                """.trimIndent()
            )
            .addKotlin(
                "com/nytimes/libimpl/MyImpl.kt", """
                package com.nytimes.libimpl

                import com.nytimes.inversion.InversionImpl
                import com.nytimes.libinterface.MyInterface
 
                class MyImpl : MyInterface {
                    override fun doSomething() {
                        println("Hello world!")
                    }
                }
                
                @InversionImpl
                fun provideImpl(): MyInterface = MyImpl()
        """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .generatedFile("com/nytimes/libimpl/MyInterface_FactoryImpl.kt")
            .hasSourceEquivalentTo(
                """
            package com.nytimes.libimpl

            import com.nytimes.libinterface.MyInterface
            import com.nytimes.libinterface.MyInterface_Factory
            
            class MyInterface_FactoryImpl : MyInterface_Factory {
              override fun invoke(): MyInterface = provideImpl()
            }
            
        """.trimIndent()
            )
    }

    @Test
    fun noErrorsWhenImplIsAvailable() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libinterface/MyInterface.kt", """
                package com.nytimes.libinterface

                import com.nytimes.inversion.*

                interface MyInterface {
                    fun doSomething()

                    companion object {
                        @InversionDef
                        val factory = Inversion.of(MyInterface::class).factory()
                    }
                }
                """.trimIndent()
            )
            .addKotlin(
                "com/nytimes/libimpl/MyImpl.kt", """
                package com.nytimes.libimpl

                import com.nytimes.inversion.*
                import com.nytimes.libinterface.MyInterface
 
                @InversionValidate
                class MyImpl : MyInterface {
                    override fun doSomething() {
                        println("Hello world!")
                    }
                }
                
                @InversionImpl
                fun provideImpl(): MyInterface = MyImpl()
                """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
    }

    @Test
    fun errorWhenImplementationIsNotAvailable() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libimpl/MyInterface.kt", """
                package com.nytimes.libinterface

                import com.nytimes.inversion.*
                import kotlin.reflect.KClass
                
                @InversionValidate
                interface MyInterface {
                    fun doSomething()
                
                    companion object {
                        @InversionDef
                        val factory = Inversion.of(MyInterface::class).factory()
                    }
                }
                """.trimIndent()
            )
            .compile()
            .failed()
            .withErrorContaining("Implementation not found for com.nytimes.libinterface.MyInterface_Factory")
    }

    @Test
    fun generateDefWithParams() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libimpl/MyInterface.kt", """
                package com.nytimes.libinterface

                import com.nytimes.inversion.*
                import kotlin.reflect.KClass
                
                class MyClass
                
                interface MyInterface {
                    fun doSomething()
                
                    companion object {
                        @InversionDef
                        val factory = Inversion.of(MyInterface::class).factory<MyClass>
                    }
                }
        """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .apply {
                generatedFile("com/nytimes/libinterface/MyInterface_Factory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.libinterface
                        
                        import com.nytimes.inversion.InversionValidator
                        
                        interface MyInterface_Factory : (MyClass) -> MyInterface
        
                        class MyInterface_FactoryValidator : InversionValidator {
                          override fun getFactoryClass() = MyInterface_Factory::class
                        }

                        """.trimIndent()
                    )

                generatedFile("com/nytimes/inversion/Inversion_ext_com_nytimes_libinterface_MyInterface_Factory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.inversion
                        
                        import com.nytimes.libinterface.MyInterface
                        import com.nytimes.libinterface.MyInterface_Factory
                        import kotlin.jvm.JvmName
                        import kotlin.reflect.KClass
                        
                        @JvmName("factory_com_nytimes_libinterface_MyInterface")
                        fun Inversion.of(c: KClass<MyInterface>) = InversionFactory<MyInterface>(MyInterface_Factory::class)
        
                        """.trimIndent()
                    )
            }
    }

    @Test
    fun generateImplWitParams() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libinterface/MyInterface.kt", """
                package com.nytimes.libinterface

                class MyClass
                
                interface MyInterface {
                    fun doSomething()
                }
                """.trimIndent()
            )
            .addKotlin(
                "com/nytimes/libimpl/MyImpl.kt", """
                package com.nytimes.libimpl

                import com.nytimes.inversion.InversionImpl
                import com.nytimes.libinterface.MyClass
                import com.nytimes.libinterface.MyInterface
 
                class MyImpl : MyInterface {
                    override fun doSomething() {
                        println("Hello world!")
                    }
                }
                
                @InversionImpl
                fun provideImpl(param: MyClass): MyInterface = MyImpl()
        """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .generatedFile("com/nytimes/libimpl/MyInterface_FactoryImpl.kt")
            .hasSourceEquivalentTo(
                """
                package com.nytimes.libimpl
    
                import com.nytimes.libinterface.MyClass
                import com.nytimes.libinterface.MyInterface
                import com.nytimes.libinterface.MyInterface_Factory
                
                class MyInterface_FactoryImpl : MyInterface_Factory {
                  override fun invoke(param: MyClass): MyInterface = provideImpl(param)
                }
                
                """.trimIndent()
            )
    }
}