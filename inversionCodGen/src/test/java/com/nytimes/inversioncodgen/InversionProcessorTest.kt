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
                        val factory: () -> MyInterface = Inversion.factory(MyInterface::class)
                    }
                }
        """.trimIndent()
            )
            .compile()
            .succeededWithoutWarnings()
            .generatedFile("com/nytimes/libinterface/MyFactory.kt")
            .hasSourceEquivalentTo(
                """
                package com.nytimes.libinterface
                
                import com.nytimes.inversion.Inversion
                import kotlin.jvm.JvmName
                import kotlin.reflect.KClass
                
                interface MyInterfaceFactory : () -> MyInterface
                
                @JvmName("factory_com_nytimes_libinterface_MyInterface")
                fun Inversion.factory(c: KClass<MyInterface>): () -> MyInterface =
                    loadSingleService<MyInterfaceFactory>()

        """.trimIndent()
            )
    }

    @Test
    fun generateImpl() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addKotlin(
                "com/nytimes/libimpl/MyInterface.kt", """
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

            import com.google.auto.service.AutoService
            import com.nytimes.libinterface.MyInterface
            import com.nytimes.libinterface.MyInterfaceFactory
            
            @AutoService(MyInterfaceFactory::class)
            class provideImpl__factory : MyInterfaceFactory {
              override fun invoke(): MyInterface = provideImpl()
            }
            
        """.trimIndent()
            )
    }
}