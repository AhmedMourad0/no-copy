NoCopy Compiler Plugin  ![CI](https://github.com/AhmedMourad0/no-copy/workflows/CI/badge.svg)
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
 `copy` will break your constructor invariants
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
It would look like all instances of `User` must be valid and can't `bad`, right?

Wrong:
```kotlin
User.of("Ahmed", "+201234567890").copy(phoneNumber = "Gotcha")
```
`copy` can bypass all the validations of your data class, it breaks your domain rules!
 
There are a couple of interesting discussions on the subject,
 [here](https://www.reddit.com/r/Kotlin/comments/hjoyxx/nocopy_compiler_plugin_for_kotlin/)
 and [here](https://www.reddit.com/r/androiddev/comments/hj3yq8/nocopy_compiler_plugin_for_kotlin/).

## Installation

- In your project-level `build.gradle`:

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "dev.ahmedmourad.nocopy:nocopy-gradle-plugin:1.0.0"
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

- Currently, you cannot have a method named `copy` with the same
  signature (return type included) in your `@NoCopy` annotated data
  class or you will get IDE and compiler errors. (Attempting this,
  however, can be considered a bad practice as `copy` has a very defined
  behaviour in `Kotlin`, replacing it with your own custom
  implementation can be misleading)
  
- Kotlin compiler plugins are not a stable API. Compiled outputs from this plugin should be stable,
 but usage in newer versions of kotlinc are not guaranteed to be stable.

## Versions

| Kotlin Version | NoCopy Version |
| :------------: | :------------: |
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
 