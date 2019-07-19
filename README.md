# Inversion

Inversion simplifies the `ServiceLoader` usage to retrieve all the implementations of a certain interface.
Using Inversion it's easy to use the dependency inversion in a multi module project.

## Basic example

A first module defines an interface and a `create` field (annotated with `InversionDef`) to create the real implementation of the interface:

```kotlin
interface MyInterface {
    fun doSomething(): String

    companion object {
        @get:InversionDef
        val create by Inversion.of(MyInterface::class)
    }
}
```

The `create` field is a `() -> MyInterface` lambda so can be used to create a new instance. The first module
doesn't contain any real implementation of `MyInterface`.

A second module defines the real implementation and an `InversionProvider` annotated method to create it:

```kotlin
class MyImpl : MyInterface {
    override fun doSomething() = "Hello world!"
}

@InversionProvider
fun provideImpl(): MyInterface = MyImpl()
```

And that's all! Now from a third module (for example an Android application) the real
implementation can be retrieved invoking `create`:

```kotlin
@InversionValidate
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val impl = MyInterface.create()
        findViewById<TextView>(R.id.text).text = impl.doSomething()
    }
}
```

In case a parameter is needed to construct the real implementation the `InversionDef` annotated property can
be defined as an extension property:

```kotlin
interface MyInterface {
    fun doSomething()
}

@get:InversionDef
val Application.factory by Inversion.of(MyInterface::class)
```

In this example the `Application` instance can be used to create the implementation in the `InversionProvider`
annotated method:

```kotlin
class MyImpl(val app: Application) : MyInterface {
    override fun doSomething() {
        //...
    }
}

@InversionProvider
fun Application.provideImpl(): MyInterface = MyImpl(this)
```

## Multi binding

When multiple implementations must be defined the `mapOf` method can be used instead of `of`:

```kotlin
@get:InversionDef
val allValues by Inversion.mapOf(MyInterface::class)
```

In this way the `allValues` field is a `() -> Map<String, MyInterface>` that can be used to retrieve a map with all the implementations.

Multiple implementations can be defined specifying in the annotation a `String` that will be used as key in the map: 

```kotlin
@InversionProvider("A")
fun MyClass.provideImplA(): MyInterface = MyImplA()

@InversionProvider("B")
fun MyClass.provideImplB(): MyInterface = MyImplB()
```

A multi binding example is available in this [plaid fork](https://github.com/fabioCollini/plaid/), here the [commit](https://github.com/fabioCollini/plaid/commit/6ba6b722547e078bf324e6bd5e51fe0840220541) 
that introduces Inversion and removes some reflection calls.