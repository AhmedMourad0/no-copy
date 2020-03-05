mirror-compiler-plugin
========================

A Kotlin compiler plugin that facilitates using data classes as value-based classes
 by manipulating the `copy` method.

## Usage

Include the gradle plugin in your project and apply `@Shatter` or `@Mirror` to your data class.

### @Shatter

`@Shatter` stops the kotlin compiler from generating the `copy` method:

```kotlin
@Shatter
data class User(val name: String, val phoneNumber: String)
```

```kotlin
User("Ahmed", "+201234567890").copy(phoneNumber = "Happy birthday!") // Unresolved reference
```

Now, you can do something like this and it actually makes sense:

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

You no longer have to worry about your domain rules being broken by someone
 using the `copy` method with illegal values after the user object has been created.

Or you can use `@Mirror`.

### @Mirror

`@Mirror` modifies `copy` to mirror the visibility of the annotated constructor:

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

*For now, consider using `@Shatter` instead and provide your own cloning method.*

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

## Caveats

- Kotlin compiler plugins are not a stable API. Compiled outputs from this plugin should be stable,
but usage in newer versions of kotlinc are not guaranteed to be stable.

## Versions

| Kotlin Version | Mirror Version |
| :------------: | :------------: |
| 1.3.61 | 0.0.1 (current version)

## Road Map

- Remove the warning for private constructor on data classes
- Publish 0.0.1
- Support mirroring internal constructors.
- Allow adding functions named `copy` to data classes
- Migrate to Arrow-Meta

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