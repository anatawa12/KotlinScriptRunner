Kotlin Script Runner
====

Kotlin Script Runner is an Apache2 Licensed gradle plugin, for run kotlin script.

## Demo

see [example project](https://github.com/anatawa12/KotlinScriptRunner/blob/master/exapmple).

## Usage

apply plugin `com.anatawa12.kotlinScriptRunner` and create task with type `com.anatawa12.kotlinScriptRunner.KotlinScriptExec`

## Install

``` groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath "com.anatawa12.kotlinScriptRunner:KotlinScriptRunner:1.0"
    }
}

apply plugin: 'com.anatawa12.kotlinScriptRunner'
```

## Contribution

1. Fork it ([http://github.com/anatawa12/KotlinScriptRunner/fork](http://github.com/anatawa12/KotlinScriptRunner/fork))
1. Create your feature branch (git checkout -b my-new-feature)
1. Commit your changes (git commit -am 'Add some feature')
1. Push to the branch (git push origin my-new-feature)
1. Create new Pull Request

## Licence

[Apache2.0](https://github.com/anatawa12/KotlinScriptRunner/blob/master/LICENSE)

## Author

[anatawa12](https://github.com/anatawa12)
