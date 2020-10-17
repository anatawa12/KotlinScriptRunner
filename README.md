Kotlin Script Runner
====

The gradle plugin to run Kotlin Script as a task.

## Demo

<!-- see [example project](https://github.com/anatawa12/KotlinScriptRunner/blob/master/exapmple). -->
Now working...

## Usage

apply plugin `com.anatawa12.kotlinScriptRunner` and create task with type `com.anatawa12.kotlinScriptRunner.KotlinScriptExec`

**Options**

`KotlinScriptExec`

This class implements `JavaExecSpec` to configure classpath, system properties or else.
You can configure classpath, system properties and else like JavaExec task.

Other properties are shown below:

| property | type | description | default value |
| --- | --- | --- | --- |
| `kotlinVersion` | `String` | the version to execute kotlin script | **required** |
| `script` | `String` | the path to kotlin script | **required** |
| `noJdk` | `boolean` | adds `-no-jdk-reflect` to options of compiler | `false` |
| `noReflect` | `boolean` | adds `-no-reflect` to options of compiler | `false` |
| `noStdlib` | `boolean` | adds `-no-stdlib` to options of compiler | `false` |
| `nowarn` | `boolean` | adds `-nowarn` to options of compiler | `false` |

## Install

```groovy
plugins {
    id("com.anatawa12.kotlinScriptRunner") version "2.0.0"
}
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

[@anatawa12](https://github.com/anatawa12)
