package com.nytimes.inversion

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nytimes.libinterface.Container
import com.nytimes.libinterface.factory
import com.nytimes.libinterface.multipleInstancesMap

object MyContainer : Container {
    private var last: Any? = null
    override fun <V> getOrCreate(f: () -> V): V =
        if (last == null)
            f().also { last = it }
        else
            last as V
}

@InversionValidate
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val impl = MyContainer.factory()
        val map = multipleInstancesMap()
        findViewById<TextView>(R.id.text).text = impl.doSomething() + map
    }
}
