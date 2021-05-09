NoCopy Compiler Plugin   ![CI](https://github.com/AhmedMourad0/no-copy/workflows/CI/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.ahmedmourad.nocopy/nocopy-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.ahmedmourad.nocopy/nocopy-gradle-plugin)
========================
<img src="plugins/idea-plugin/src/main/resources/META-INF/pluginIcon.svg" alt="" width="200" />
A Kotlin compiler plugin that removes the `copy` method from data classes
 and enables using them as value-based classes.

## Usage

Include the gradle plugin in your project and apply `@NoCopy` to your data class.

### @NoCopy

`@NoCopy` prevents the kotlin compiler from generating the `copy` method:

```kotlin
@NoCopy
data class User(val name: String, val phoneNumber: String)
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // Unresolved reference: copy
```

## Why? I hear you ask.

The `copy` method of Kotlin data classes is a known language design problem, normally, you can't
remove it, you can't override it, and you can document it.

Why would you want to do that? Well, there are a couple of reasons:

- `copy` is a guaranteed source of binary incompatibility as you add new properties to the type when
 all you wanted was value semantics.
- If you want [value-based classes](https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html),
 `copy` will break your constructor invariants.
- Private constructors are basically meaningless as long as `copy` exists.

Consider something like this:

```kotlin
data class User private constructor(val name: String, val phoneNumber: String) {
    companion object {
        fun of(name: String, phoneNumber: String): Either<UserException, User> {
            return if (bad) {
                exception.left() //You can throw an exception here if you like instead.
            } else {
                User(name, phoneNumber).right()
            }
        }
    }
}
```
It would look like all instances of `User` must be valid and can't be `bad`, right?

Wrong:
```kotlin
User.of("Ahmed", "+201234567890").copy(phoneNumber = "Gotcha")
```
`copy` can bypass all the validations of your data class, it breaks your domain rules!
 
 For more detailed explanation, check out [this article](https://medium.com/swlh/value-based-classes-and-error-handling-in-kotlin-3f14727c0565?source=friends_link&sk=a16186408e1c8e317e3e11fd16e33710).

## Installation

### Using plugins DSL

- In your module-level `build.gradle`:

```gradle
plugins {
  id "dev.ahmedmourad.nocopy.nocopy-gradle-plugin" version "1.4.0"
}
```

### Using legacy plugin application

- In your project-level `build.gradle`:

```gradle
buildscript {
    repositories {
        mavenCentral()
        // Or
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "dev.ahmedmourad.nocopy:nocopy-gradle-plugin:1.4.0"
    }  
}
```

- In your module-level `build.gradle`:

```gradle
// For each module that needs to use the annotations
apply plugin: 'dev.ahmedmourad.nocopy.nocopy-gradle-plugin'
```

### IDE Support

- Install the IDEA plugin *`File -> Settings -> plugins -> Marketplace -> Kotlin NoCopy`*

- Disable the default inspection `File -> Settings -> Editor ->
 Inspections -> Kotlin -> Probably bugs -> Private data class constructor is...`. Currently, you have to do
 this manually due to a bug with the Kotlin plugin, [upvote](https://youtrack.jetbrains.com/issue/KT-37576).

## Caveats
 
- Kotlin compiler plugins are not a stable API. Compiled outputs from this plugin should be stable,
 but usage in newer versions of kotlinc are not guaranteed to be stable.

## Versions

| Kotlin Version | NoCopy Version |
| :------------: | :------------: |
| 1.5.0 | 1.4.0
| 1.4.32 | 1.3.0
| 1.4.20 | 1.2.0
| 1.4.0 | 1.1.0
| 1.3.72 | 1.0.0


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
 