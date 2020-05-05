NoCopy Compiler Plugin
========================
<img src="idea-plugin/src/main/resources/META-INF/pluginIcon.svg" alt="" width="200" />
A Kotlin compiler plugin that enables using data classes as value-based classes
 by manipulating the `copy` method.

## Usage

Include the gradle plugin in your project and apply `@NoCopy` or `@LeastVisibleCopy` to your data class.

### @NoCopy

`@NoCopy` prevents the kotlin compiler from generating the `copy` method:

```kotlin
@NoCopy
data class User(val name: String, val phoneNumber: String)
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // Unresolved reference
```

Now, you can do something like this and it actually makes sense:

```kotlin
@NoCopy
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

You no longer have to worry about your domain rules being broken by someone
 using the `copy` method with illegal values after an object has been instantiated.

Or you could use `@LeastVisibleCopy`.

### @LeastVisibleCopy

`@LeastVisibleCopy` modifies `copy` to mirror the visibility of the least visible constructor of the annotated class:

```kotlin
@LeastVisibleCopy
data class User private constructor(val name: String, val phoneNumber: String)
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // copy is private in User
```

The visibility is treated in this order:

`public > internal > protected > private` 

```kotlin
@LeastVisibleCopy
data class User(val name: String, val phoneNumber: String) {
    private constructor(phoneNumber: String) : this("Ahmed", phoneNumber)
}
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // copy is private in User
```

## Installation

- In your project-level `build.gradle`:

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "dev.ahmedmourad.nocopy:nocopy-gradle-plugin:0.1.0"
    }  
}
```

- In your module-level `build.gradle`:

```gradle
// For each module that needs to use the annotations
apply plugin: 'dev.ahmedmourad.nocopy.nocopy-gradle-plugin'
```

- Install the IDEA plugin *`File -> Settings -> plugins -> Marketplace -> Kotlin NoCopy`*

- Disable the default inspection `File -> Settings -> Editor ->
 Inspections -> Kotlin -> Probably bugs -> Private data class constructor is...`. Currently, you have to do
  this manually due to a bug with the Kotlin plugin, [upvote](https://youtrack.jetbrains.com/issue/KT-37576).

## Caveats

- Mirroring internal constructors is not currently supported. For now, consider using `@NoCopy` instead
 and providing your own cloning method, there are inspections included that will give you an error when you
  do this. *(Fix planned in a future release)*
  
- For now, you cannot have methods named `copy` with the same signature (return type included) in
 your `@NoCopy` annotated data class or you will get ide and compiler errors. *(Fix planned in a future release)*
 
- Kotlin compiler plugins are not a stable API. Compiled outputs from this plugin should be stable,
 but usage in newer versions of kotlinc are not guaranteed to be stable.

## Versions

| Kotlin Version | NoCopy Version |
| :------------: | :------------: |
| 1.3.72 | 0.1.0

## Roadmap

- Support mirroring internal constructors.
- Support having `copy` named methods in `@NoCopy` annotated data classes.
- Migrate to Arrow-Meta.
- Auto install IDEA plugin.
- Go Multiplatform.
- Add annotation to convert regular classes to value-based classes
- Move to Kotlin DSL for Gradle build scripts

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
 