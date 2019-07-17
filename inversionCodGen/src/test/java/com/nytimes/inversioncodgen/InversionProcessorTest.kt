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

                import com.nytimes.inversion.*
                import kotlin.reflect.KClass
                
                interface MyInterface {
                    fun doSomething()
                
                    companion object {
                        @InversionDef
                        val factory = Inversion.factory(MyInterface::class)
                    }
                }
                """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .apply {
                generatedFile("com/nytimes/libinterface/MyFactory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.libinterface
                        
                        import com.nytimes.inversion.InversionValidator
                        
                        interface MyInterfaceFactory : () -> MyInterface
                        
                        class MyInterfaceFactoryValidator : InversionValidator {
                          override fun getFactoryClass() = MyInterfaceFactory::class
                        }
        
                        """.trimIndent()
                    )

                generatedFile("com/nytimes/inversion/Inversion_ext_MyFactory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.inversion
                        
                        import com.nytimes.libinterface.MyInterface
                        import com.nytimes.libinterface.MyInterfaceFactory
                        import kotlin.jvm.JvmName
                        import kotlin.reflect.KClass
                        
                        @JvmName("factory_com_nytimes_libinterface_MyInterface")
                        fun Inversion.factory(c: KClass<MyInterface>): () -> MyInterface =
                            loadSingleService<MyInterfaceFactory>()

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
            .generatedFile("com/nytimes/libimpl/MyFactoryImpl.kt")
            .hasSourceEquivalentTo(
                """
            package com.nytimes.libimpl

            import com.nytimes.libinterface.MyInterface
            import com.nytimes.libinterface.MyInterfaceFactory
            
            class provideImpl__factory : MyInterfaceFactory {
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
                        val factory = Inversion.factory(MyInterface::class)
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
                        val factory = Inversion.factory(MyInterface::class)
                    }
                }
                """.trimIndent()
            )
            .compile()
            .failed()
            .withErrorContaining("Implementation not found for com.nytimes.libinterface.MyInterfaceFactory")
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
                        val factory: (MyClass) -> MyInterface = Inversion.factory1(MyInterface::class)
                    }
                }
        """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .apply {
                generatedFile("com/nytimes/libinterface/MyFactory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.libinterface
                        
                        import com.nytimes.inversion.InversionValidator
                        
                        interface MyInterfaceFactory : (MyClass) -> MyInterface
        
                        class MyInterfaceFactoryValidator : InversionValidator {
                          override fun getFactoryClass() = MyInterfaceFactory::class
                        }

                        """.trimIndent()
                    )

                generatedFile("com/nytimes/inversion/Inversion_ext_MyFactory.kt")
                    .hasSourceEquivalentTo(
                        """
                        package com.nytimes.inversion
                        
                        import com.nytimes.libinterface.MyClass
                        import com.nytimes.libinterface.MyInterface
                        import com.nytimes.libinterface.MyInterfaceFactory
                        import kotlin.jvm.JvmName
                        import kotlin.reflect.KClass
                        
                        @JvmName("factory_com_nytimes_libinterface_MyInterface")
                        fun Inversion.factory1(c: KClass<MyInterface>): (MyClass) -> MyInterface =
                            loadSingleService<MyInterfaceFactory>()
        
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
            .generatedFile("com/nytimes/libimpl/MyFactoryImpl.kt")
            .hasSourceEquivalentTo(
                """
                package com.nytimes.libimpl
    
                import com.nytimes.libinterface.MyClass
                import com.nytimes.libinterface.MyInterface
                import com.nytimes.libinterface.MyInterfaceFactory
                
                class provideImpl__factory : MyInterfaceFactory {
                  override fun invoke(param: MyClass): MyInterface = provideImpl(param)
                }
                
                """.trimIndent()
            )
    }
}