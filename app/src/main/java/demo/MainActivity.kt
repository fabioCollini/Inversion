package demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import demo.libinterface.Container
import demo.libinterface.factory
import demo.libinterface.multipleInstancesMap
import inversion.InversionValidate
import com.inversion.R

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
