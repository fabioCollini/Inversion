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

class Verifier(private val dir: String, vararg names: String) {
    private var compiler = kotlinc().withProcessors(InversionProcessor())
        .addSources(dir, *names)

    fun generatedFiles(vararg names: String) {
        compiler.compile()
            .succeededWithoutWarnings()
            .generatedFiles(dir, *names)
    }

    fun withErrorContaining(error: String) {
        compiler.compile()
            .failed()
            .withErrorContaining(error)
    }
}

fun verify(dir: String, vararg names: String) = Verifier(dir, *names)

class InversionProcessorTest {
    @Test
    fun generateDef() {
        verify("generateDef", "MyInterface")
            .generatedFiles(
                "MyInterface_Factory",
                "Inversion_ext_com_nytimes_inversioncodgen_cases_generateDef_MyInterface_Factory"
            )
    }

    @Test
    fun multipleClassesInASingleFile() {
        verify("multipleClassesInASingleFile", "MyInterface", "MyImpl")
            .generatedFiles(
                "MyInterface_Factory"
            )
    }

    @Test
    fun generateImpl() {
        verify("generateImplBasedOnProvider", "MyInterface", "MyImpl")
            .generatedFiles("MyInterface_FactoryImpl")
    }

    @Test
    fun noErrorsWhenImplIsAvailable() {
        verify("noErrorsWhenImplIsAvailable", "MyInterface", "MyImpl")
            .generatedFiles()
    }

    @Test
    fun errorWhenImplementationIsNotAvailable() {
        verify("errorWhenImplementationIsNotAvailable", "MyInterface")
            .withErrorContaining("Implementation not found for com.nytimes.inversioncodgen.cases.errorWhenImplementationIsNotAvailable.MyInterface_Factory")
    }

    @Test
    fun generateDefWithParams() {
        verify("generateDefWithParams", "MyInterface")
            .generatedFiles(
                "MyInterface_Factory",
                "Inversion_ext_com_nytimes_inversioncodgen_cases_generateDefWithParams_MyInterface_Factory"
            )
    }

    @Test
    fun generateImplWitParams() {
        verify("generateImplWitParams", "MyInterface", "MyImpl")
            .generatedFiles(
                "MyInterface_FactoryImpl"
            )
    }

    @Test
    fun generateImplWitReceiver() {
        verify("generateImplWitReceiver", "MyInterface", "MyImpl")
            .generatedFiles(
                "MyInterface_FactoryImpl"
            )
    }

    @Test
    fun multipleNames() {
        verify("multipleNames", "MyInterface", "MyImpl")
            .generatedFiles(
                "MyInterface_FactoryImpl_A",
                "MyInterface_FactoryImpl_B",
                "MyInterface_Factory",
                "Inversion_ext_com_nytimes_inversioncodgen_cases_multipleNames_MyInterface_Factory"
            )
    }
}