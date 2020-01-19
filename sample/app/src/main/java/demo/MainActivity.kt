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

package demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.inversion.BuildConfig
import demo.libinterface.Container
import demo.libinterface.factory
import demo.libinterface.multipleInstancesMap
import com.inversion.R
import inversion.Inversion

object MyContainer : Container {
    private var last: Any? = null
    override fun <V> getOrCreate(f: () -> V): V =
        if (last == null)
            f().also { last = it }
        else
            last as V
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Inversion.validate()
        }
        setContentView(R.layout.activity_main)
        val impl = MyContainer.factory()
        val map = multipleInstancesMap()
        findViewById<TextView>(R.id.text).text = impl.doSomething() + map
    }
}
