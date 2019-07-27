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

import java.io.*
import java.nio.charset.StandardCharsets
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.StandardLocation

/**
 * A helper class for reading and writing Services files.
 * Kotlin conversion of the AutoService class
 * https://github.com/google/auto/blob/master/service/processor/src/main/java/com/google/auto/service/processor/ServicesFiles.java
 */
internal object ServicesFiles {
    /**
     * Reads the set of service classes from a service file.
     *
     * @param input not `null`. Closed after use.
     * @return a not `null Set` of service class names.
     * @throws IOException
     */
    fun readServiceFile(input: InputStream): Set<String> {
        return BufferedReader(
            InputStreamReader(
                input,
                StandardCharsets.UTF_8
            )
        ).useLines { sequence ->
            sequence.mapNotNull { fullLine ->
                removeComment(fullLine)
                    .takeIf { it.isNotEmpty() }
            }
        }.toSet()
    }

    private fun removeComment(it: String): String {
        val commentStart = it.indexOf('#')
        val line = if (commentStart >= 0) {
            it.substring(0, commentStart)
        } else {
            it
        }
        return line.trim { it <= ' ' }
    }

    /**
     * Writes the set of service class names to a service file.
     *
     * @param output not `null`. Not closed after use.
     * @param services a not `null Collection` of service class names.
     * @throws IOException
     */
    fun writeServiceFile(services: Collection<String>, output: OutputStream) {
        val writer = BufferedWriter(OutputStreamWriter(output, StandardCharsets.UTF_8))
        for (service in services) {
            writer.write(service)
            writer.newLine()
        }
        writer.flush()
    }
}

/**
 * Kotlin conversion of the AutoService method
 * https://github.com/google/auto/blob/master/service/processor/src/main/java/com/google/auto/service/processor/AutoServiceProcessor.java
 */
fun generateConfigFiles(processingEnv: ProcessingEnvironment, providerInterface: String, newServices: List<String>) {
    val resourceFile = getResourceFile(providerInterface)
    processingEnv.log("Working on resource file: $resourceFile")
    try {
        val allServices = readImplementationsFromRes(processingEnv, resourceFile)

        if (allServices.containsAll(newServices)) {
            processingEnv.log("No new service entries being added.")
            return
        }

        allServices.addAll(newServices)
        processingEnv.log("New service file contents: $allServices")
        val fileObject = processingEnv.filer.createResource(
            StandardLocation.CLASS_OUTPUT, "",
            resourceFile
        )
        val out = fileObject.openOutputStream()
        ServicesFiles.writeServiceFile(allServices, out)
        out.close()
        processingEnv.log("Wrote to: " + fileObject.toUri())
    } catch (e: IOException) {
        processingEnv.fatalError("Unable to create $resourceFile, $e")
        return
    }
}

fun readImplementationsFromRes(processingEnv: ProcessingEnvironment, resourceFile: String): MutableSet<String> {
    val allServices = mutableSetOf<String>()
    try {
        // would like to be able to print the full path
        // before we attempt to get the resource in case the behavior
        // of filer.getResource does change to match the spec, but there's
        // no good way to resolve CLASS_OUTPUT without first getting a resource.
        val existingFile = processingEnv.filer.getResource(
            StandardLocation.CLASS_OUTPUT, "",
            resourceFile
        )
        processingEnv.log("Looking for existing resource file at " + existingFile.toUri())
        val oldServices = ServicesFiles.readServiceFile(existingFile.openInputStream())
        processingEnv.log("Existing service entries: $oldServices")
        allServices.addAll(oldServices)
    } catch (e: IOException) {
        // According to the javadoc, Filer.getResource throws an exception
        // if the file doesn't already exist.  In practice this doesn't
        // appear to be the case.  Filer.getResource will happily return a
        // FileObject that refers to a non-existent file but will throw
        // IOException if you try to open an input stream for it.
        processingEnv.log("Resource file did not already exist.")
    }
    return allServices
}

fun getResourceFile(providerInterface: String) =
    "META-INF/services/$providerInterface"

