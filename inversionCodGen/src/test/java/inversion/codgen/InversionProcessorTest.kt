/*
 * Copyright 2019 Fabio Collini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package inversion.codgen

import kompile.testing.SuccessfulCompilationClause
import kompile.testing.kotlinc
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

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

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun params(): List<String> {
            return File("src/test/java/$CASES_DIR").listFiles().map { it.name }
        }
    }
}