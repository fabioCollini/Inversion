/*
 * Copyright 2008 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nytimes.inversioncodgen

import java.io.*
import java.nio.charset.StandardCharsets

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