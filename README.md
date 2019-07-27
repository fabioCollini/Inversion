# Inversion [![CircleCI](https://circleci.com/gh/fabioCollini/Inversion.svg?style=svg)](https://circleci.com/gh/fabioCollini/Inversion) [![](https://jitpack.io/v/fabioCollini/Inversion.svg)](https://jitpack.io/#fabioCollini/Inversion)


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

The `create` field is a `() -> MyInterface` lambda, it can be used to create a new instance. The first module
doesn't contain any real implementation of `MyInterface`.

A second module defines the real implementation annotated with `InversionImpl`: 

```kotlin
@InversionImpl
class MyImpl : MyInterface {
    override fun doSomething() = "Hello world!"
}
```

And that's all! Now the real implementation can be retrieved invoking `create`:

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

## Custom implementation creation

In case an extra logic is needed to create the real implementation an `InversionProvider` annotated method can be used:

```kotlin
@InversionProvider
fun provideImpl(): MyInterface = MyImpl() 
```

If a parameter is needed the `InversionDef` annotated property can be defined as an extension property:

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

A multi binding example is available in this [plaid fork](https://github.com/fabioCollini/plaid/), here the [commits](https://github.com/fabioCollini/plaid/compare/original-master...fabioCollini:master) 
that introduces Inversion and removes some reflection calls.

## License

    Copyright 2019 Fabio Collini

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.