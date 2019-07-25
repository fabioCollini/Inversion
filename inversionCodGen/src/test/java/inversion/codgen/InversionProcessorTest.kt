package inversion.codgen

import junit.framework.Assert.assertTrue
import kompile.testing.SuccessfulCompilationClause
import kompile.testing.kotlinc
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.net.URLClassLoader

private const val CASES_DIR = "inversion/codgen/cases/"

fun kompile.testing.Compiler.addSources(
    dir: String,
    vararg names: String
): kompile.testing.Compiler {
    var ret = this
    names.forEach { name ->
        val fileName = "$CASES_DIR$dir/$name"
        ret = addKotlin(
            fileName,
            File("src/test/java/$fileName").readText()
        )
    }
    return ret
}

fun SuccessfulCompilationClause.generatedFiles(dir: String, vararg names: String) {
    names.forEach { name ->
        val fileName = "$CASES_DIR$dir/$name"
        val generatedFileName = if (name.startsWith("Inversion_"))
            "inversion/$name"
        else
            fileName
        generatedFile(generatedFileName).hasSourceEquivalentTo(File("src/test/java/$fileName").readText())
    }
}

fun isResultFile(name: String) =
    name.startsWith("Inversion_") || name.endsWith("_Factory.kt") || name.contains("_FactoryImpl")

fun verifyDir(dir: String) {
    val files =
        File("src/test/java/$CASES_DIR$dir").listFiles().map { it.name }

    val compiler = kotlinc().withProcessors(InversionProcessor())
        .addSources(dir, *files.filter { it != "error.txt" && !isResultFile(it) }.toTypedArray())

    val error = files.filter { it == "error.txt" }.getOrNull(0)

    if (error == null) {
        val results = files.filter { it != "error.txt" && isResultFile(it) }
        compiler.compile()
            .succeeded()
            .generatedFiles(dir, *results.toTypedArray())
    } else {
        compiler
            .withClasspath(URLClassLoader.newInstance(arrayOf(File("/usr/lib/jvm/java-8-oracle/lib/tools.jar").toURI().toURL())))
            .compile()
            .failed()
            .withErrorContaining(File("src/test/java/$CASES_DIR$dir/error.txt").readText())
    }
}

@RunWith(Parameterized::class)
class InversionProcessorTest(private val dir: String) {
    @Test
    fun generateClasses() {
        verifyDir(dir)
    }

    @Test
    fun checkDirExists() {
        val file = File("src/test/java/$CASES_DIR$dir")
        println(file.absolutePath)
        assertTrue(file.exists())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun params(): List<String> {
            println(System.getProperty("java.version"))
            println("path " + File("src/test/java/$CASES_DIR").absoluteFile)
            return File("src/test/java/$CASES_DIR").listFiles().map { it.name }
        }
    }
}