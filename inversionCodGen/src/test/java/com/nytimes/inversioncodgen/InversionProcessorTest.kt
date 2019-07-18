package com.nytimes.inversioncodgen

import kompile.testing.SuccessfulCompilationClause
import kompile.testing.kotlinc
import org.junit.Test
import java.io.File

fun kompile.testing.Compiler.addSources(
    dir: String,
    vararg names: String
): kompile.testing.Compiler {
    var ret = this
    names.forEach { name ->
        val fileName = "com/nytimes/inversioncodgen/cases/$dir/$name.kt"
        ret = addKotlin(
            fileName,
            File("src/test/java/$fileName").readText()
        )
    }
    return ret
}

fun SuccessfulCompilationClause.generatedFiles(dir: String, vararg names: String) {
    names.forEach { name ->
        val fileName = "com/nytimes/inversioncodgen/cases/$dir/$name.kt"
        val generatedFileName = if (name.startsWith("Inversion_"))
            "com/nytimes/inversion/$name.kt"
        else
            fileName
        generatedFile(generatedFileName).hasSourceEquivalentTo(File("src/test/java/$fileName").readText())
    }
}

class InversionProcessorTest {
    @Test
    fun generateDef() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addSources("generateDef", "MyInterface")
            .compile()
            .succeededWithoutWarnings()
            .generatedFiles(
                "generateDef",
                "MyInterface_Factory",
                "Inversion_ext_com_nytimes_inversioncodgen_cases_generateDef_MyInterface_Factory"
            )
    }

    @Test
    fun generateImpl() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addSources("generateImpl", "MyInterface", "MyImpl")
            .compile()
            .succeededWithoutWarnings()
            .generatedFiles("generateImpl", "MyInterface_FactoryImpl")
    }

    @Test
    fun noErrorsWhenImplIsAvailable() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addSources("noErrorsWhenImplIsAvailable", "MyInterface", "MyImpl")
            .compile()
            .succeededWithoutWarnings()
    }

    @Test
    fun errorWhenImplementationIsNotAvailable() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addSources("errorWhenImplementationIsNotAvailable", "MyInterface")
            .compile()
            .failed()
            .withErrorContaining("Implementation not found for com.nytimes.inversioncodgen.cases.errorWhenImplementationIsNotAvailable.MyInterface_Factory")
    }

    @Test
    fun generateDefWithParams() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addSources("generateDefWithParams", "MyInterface")
            .compile()
            .succeededWithoutWarnings()
            .generatedFiles(
                "generateDefWithParams",
                "MyInterface_Factory",
                "Inversion_ext_com_nytimes_inversioncodgen_cases_generateDefWithParams_MyInterface_Factory"
            )
    }

    @Test
    fun generateImplWitParams() {
        kotlinc()
            .withProcessors(InversionProcessor())
            .addSources("generateImplWitParams", "MyInterface", "MyImpl")
            .compile()
            .succeededWithoutWarnings()
            .generatedFiles(
                "generateImplWitParams",
                "MyInterface_FactoryImpl"
            )
    }
}