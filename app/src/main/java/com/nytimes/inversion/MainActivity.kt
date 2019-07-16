package com.nytimes.inversion

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nytimes.libinterface.MyInterface

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val impl = MyInterface.factory()
        findViewById<TextView>(R.id.text).text = impl.doSomething()
    }
}
