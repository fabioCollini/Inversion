# Inversion [![CircleCI](https://circleci.com/gh/fabioCollini/Inversion.svg?style=svg)](https://circleci.com/gh/fabioCollini/Inversion) [![](https://jitpack.io/v/fabioCollini/Inversion.svg)](https://jitpack.io/#fabioCollini/Inversion) [![codecov](https://codecov.io/gh/fabioCollini/Inversion/branch/master/graph/badge.svg)](https://codecov.io/gh/fabioCollini/Inversion)


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

In case an extra logic is needed to create the real implementation, an `InversionProvider` annotated method can be used:

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

Multiple implementations can be managed using the `mapOf` method instead of `of`:

```kotlin
@get:InversionDef
val allValues by Inversion.mapOf(MyInterface::class)
```

In this way the `allValues` field is a `() -> Map<String, MyInterface>` that can be used to retrieve a map with all the implementations.

Multiple implementations can be defined specifying in the annotation a `String` that will be used as the key in the map:

```kotlin
@InversionProvider("A")
fun MyClass.provideImplA(): MyInterface = MyImplA()

@InversionProvider("B")
fun MyClass.provideImplB(): MyInterface = MyImplB()
```

A multi binding example is available in this [plaid fork](https://github.com/fabioCollini/plaid/), here the [commits](https://github.com/fabioCollini/plaid/compare/original-master...fabioCollini:master) 
that introduces Inversion and removes some reflection calls.

## Internal implementation

Under the hood Inversion is an annotation processor that generates the configuration to simplify the `ServiceLoader` usage. `ServiceLoader` is
a standard Java class that can be used to discover the implementations of an interface based on some config file. An explanation on how to
use a `ServiceLoader` on Android is available in the post 
[Patterns for accessing code from Dynamic Feature Modules](https://medium.com/androiddevelopers/patterns-for-accessing-code-from-dynamic-feature-modules-7e5dca6f9123),
here there are the [commits](https://github.com/fabioCollini/android-dynamic-code-loading/compare/initial-master...fabioCollini:master) to introduce
Inversion in the demo project.

`ServiceLoader` reads some resource files to retrieve the implementations to use, on Android the disk read can be avoided using R8.
R8 removes the `ServiceLoader` calls and replaces it with the direct instantiations. Looking at the code of the last example
in the apk optimized with R8 we can find the following code instead of the `ServiceLoader` invocation:
 
```java
Iterator it = Arrays.asList(new MultiInstanceInterface_Factory[] {
    new MultiInstanceInterface_FactoryImpl_B(), 
    new MultiInstanceInterface_FactoryImpl_A()
}).iterator();
```           

`MultiInstanceInterface_FactoryImpl_A` and `MultiInstanceInterface_FactoryImpl_B` are two classes generated by Inversion.         

## Validation

The Inversion annotation processor executes some basic validations to raise a compilation error in case of any misconfiguration.
A `validate` method in the `Inversion` object can be invoked to verify that there is an implementation defined for each definition.
This method uses reflection calls so it's better to invoke it in a test or in the application startup only in the debug build.

## JitPack configuration

Inversion is available on [JitPack](https://jitpack.io/#fabioCollini/Inversion/),
you can use it adding the JitPack repository in your `build.gradle` (in top level dir):
```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```
and the dependencies in the `build.gradle` of the modules:

```gradle
dependencies {
    kapt 'com.github.fabioCollini.inversion:inversionCodGen:0.3.1'
    implementation 'com.github.fabioCollini.inversion:inversionLib:0.3.1'
}
```

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