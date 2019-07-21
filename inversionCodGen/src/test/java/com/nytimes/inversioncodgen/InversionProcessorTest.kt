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
        val fileName = "com/nytimes/inversioncodgen/cases/$dir/$name"
        ret = addKotlin(
            fileName,
            File("src/test/java/$fileName").readText()
        )
    }
    return ret
}

fun SuccessfulCompilationClause.generatedFiles(dir: String, vararg names: String) {
    names.forEach { name ->
        val fileName = "com/nytimes/inversioncodgen/cases/$dir/$name"
        val generatedFileName = if (name.startsWith("Inversion_"))
            "com/nytimes/inversion/$name"
        else
            fileName
        generatedFile(generatedFileName).hasSourceEquivalentTo(File("src/test/java/$fileName").readText())
    }
}

fun isResultFile(name: String) =
    name.startsWith("Inversion_") || name.endsWith("_Factory.kt") || name.contains("_FactoryImpl")

fun verifyDir(dir: String) {
    val files = File("src/test/java/com/nytimes/inversioncodgen/cases/$dir").listFiles().map { it.name }

    val compiler = kotlinc().withProcessors(InversionProcessor())
        .addSources(dir, *files.filter { it != "error.txt" && !isResultFile(it) }.toTypedArray())

    val error  = files.filter { it == "error.txt" }.getOrNull(0)

    if (error == null) {
        val results = files.filter { it != "error.txt" && isResultFile(it) }
        compiler.compile()
            .succeeded()
            .generatedFiles(dir, *results.toTypedArray())
    } else {
        compiler.compile()
            .failed()
            .withErrorContaining(File("src/test/java/com/nytimes/inversioncodgen/cases/$dir/error.txt").readText())
    }
}

class InversionProcessorTest {
    @Test
    fun generateDef() {
        verifyDir("generateDef")
    }

    @Test
    fun multipleClassesInASingleFile() {
        verifyDir("multipleClassesInASingleFile")
    }

    @Test
    fun generateImpl() {
        verifyDir("generateImpl")
    }

    @Test
    fun generateImplBasedOnProvider() {
        verifyDir("generateImplBasedOnProvider")
    }

    @Test
    fun noErrorsWhenImplIsAvailable() {
        verifyDir("noErrorsWhenImplIsAvailable")
    }

    @Test
    fun errorWhenImplementationIsNotAvailable() {
        verifyDir("errorWhenImplementationIsNotAvailable")
    }

    @Test
    fun generateDefWithParams() {
        verifyDir("generateDefWithParams")
    }

    @Test
    fun generateDefAsClassProperty() {
        verifyDir("generateDefAsClassProperty")
    }

    @Test
    fun generateImplWitParams() {
        verifyDir("generateImplWitParams")
    }

    @Test
    fun generateImplWitReceiver() {
        verifyDir("generateImplWitReceiver")
    }

    @Test
    fun multipleNames() {
        verifyDir("multipleNames")
    }
}