mirror-compiler-plugin
========================

A Kotlin compiler plugin that facilitates using data classes as value-based classes
 by manipulating the `copy` method.

## Usage

Include the gradle plugin in your project and apply `@Shatter` or `@Mirror` to your data class.

### @Shatter

Shatter stops the kotlin compiler from generating the `copy` method:

```kotlin
@Shatter
data class User(val name: String, val phoneNumber: String)
```

You can also do something like this and it actually makes sense:

```kotlin
@Shatter
data class User private constructor(val name: String, val phoneNumber: String) {
    companion object {
        fun of(name: String, phoneNumber: String): Either<UserException, User> {
            return if (bad) {
                exception.left()
            } else {
                User(name, phoneNumber).right()
            }
        }
    }
}
```

Now you don't have to worry about your domain rules being broken by someone
 using the `copy` method with illegal values after the user object has been created:

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // Unresolved reference
```

Or you can use `@Mirror`.

### @Mirror

Mirror modifies `copy` to mirror the visibility of the annotated constructor:

```kotlin
data class User @Mirror private constructor(val name: String, val phoneNumber: String)
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // copy is private in User
```

Annotating a class with `@Mirror` means that `copy` will mirror
 the visibility of the least visible constructor where visibility is treated in this order:

`public > internal > protected > private` 

```kotlin
@Mirror
data class User(val name: String, val phoneNumber: String) {
    private constructor(phoneNumber: String) : this("Ahmed", phoneNumber)
}
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // copy is private in User
```

*However, mirroring internal constructors is not currently support.*
*For now, consider using `@Shatter` instead and provide your own `copy` method.*

## Installation (not published yet)

Apply the gradle plugin:

```gradle
buildscript {
  dependencies {
    classpath "com.ahmedmourad.mirror:mirror-compiler-plugin-gradle:0.0.1"
  }  
}

apply plugin: 'com.ahmedmourad.mirror.mirror-gradle-plugin'
```

The default configuration will add the `-annotations` artifact (which has
 the `@Mirror` and `@Shatter` annotations) 
You can configure custom behavior with properties on the `mirror` extension.

```
import com.ahmedmourad.mirror.core.Resolution

mirror {
  // Define custom annotations. The -annotations artifact won't be automatically added to
  // dependencies if you define your own.
  shatterAnnotation = "com.ahmedmourad.mirror.annotations.Shatter" // Default
  mirrorAnnotation = "com.ahmedmourad.mirror.annotations.Mirror" // Default

  resolution = Resolution.BY_ANNOTATION // SHATTER_ALL, MIRROR_ALL_BY_LEAST_VISIBLE, MIRROR_ALL_BY_PRIMARY
}
```

You can use resolution to choose how the plugin behaves:

| Resolution | Behaviour |
| ---------- | ----------|
| BY_ANNOTATIONS (default) | The plugin will only mirror or shatter `copy` of the data classes marked with specified annotations. |
| SHATTER_ALL | The plugin will shatter all `copy` methods of all data classes (no annotations needed). |
| MIRROR_ALL_BY_LEAST_VISIBLE | The plugin will mirror the least visible constructor for all copy methods of all data classes (no annotations needed). |
| MIRROR_ALL_BY_PRIMARY | The plugin will mirror the primary constructor for all copy methods of all data classes (no annotations needed). |


## Caveats

- Kotlin compiler plugins are not a stable API. Compiled outputs from this plugin should be stable,
but usage in newer versions of kotlinc are not guaranteed to be stable.

## Versions

| Kotlin Version | Mirror Version |
| :------------: | :------------: |
| 1.3.61 | 0.0.1 (current version)

## Road Map

- Add resolutions
- Add IDE plugin.
- Publish 0.0.1
- Support mirroring internal constructors.
- Support Maven.

License
-------

    Copyright (C) 2020 Ahmed Mourad

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [snapshots]: https://oss.sonatype.org/content/repositories/snapshots/