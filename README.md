# aosp-libs

Shared prebuilt AOSP libraries for Gradle-based Android projects that use platform
APIs. This repository provides reusable build dependencies without keeping a
separate copy in every application repository.

## Contents

| File | Purpose |
| --- | --- |
| `framework.gradle.kts` | Shared Gradle configuration for consuming the framework JAR |
| `compile-only/framework.jar` | Android framework classes for compile-time use |

JAR and AAR files are tracked with Git LFS.

## Branches and API levels

Choose the branch matching the Android version and API level of your target AOSP
build:

| Branch | Android version | API level |
| --- | --- | --- |
| `a15_r36` | Android 15 | 35 |
| `b16_r4` | Android 16 | 36 |
| `c17_r1` | Android 17 | 37 |

`main` contains the shared Gradle script, guide, and Git LFS configuration. Use a
version branch for the framework JAR; the `r` suffix identifies the release
revision, not the API level.

For example, to select Android 16 in an existing app submodule:

```sh
git -C aosp-libs fetch origin
git -C aosp-libs checkout b16_r4
git -C aosp-libs lfs pull
```

Commit the updated submodule pointer in the consuming app repository. The Gradle
dependency path stays the same across version branches.

API mappings follow the [AOSP version reference](https://source.android.com/docs/setup/reference/build-numbers)
and [Android 17 documentation](https://developer.android.com/about/versions/17/summary).

## Use in a Gradle project

Install Git LFS, then add this repository as a submodule at your project root:

```sh
git lfs install
git submodule add git@gitme.com:DroidExt/aosp-libs.git aosp-libs
```

`gitme.com` is an SSH alias for GitHub. Configure it locally, or use
`git@github.com:DroidExt/aosp-libs.git` with your standard GitHub SSH setup.

Select a version branch as described above, then apply the shared script in the
consuming module's `build.gradle.kts` after applying the Android and Kotlin Android
plugins:

```kotlin
apply(from = rootProject.file("aosp-libs/framework.gradle.kts"))
```

The script adds `aosp-libs/compile-only/framework.jar` as a `compileOnly`
dependency, so the JAR is not packaged into the app. The target Android system
supplies these classes at runtime. It also places the JAR before SDK stubs on
Kotlin's compile classpath so hidden platform members resolve, while preserving
the original classpath's lazy task dependencies. No separate `compileOnly`
declaration or Kotlin task configuration is needed in the consuming module.

Applied Kotlin scripts do not have Kotlin plugin types on their compilation
classpath. The script resolves `KotlinCompile` through the applied Kotlin plugin
and accesses its libraries through Gradle's dynamic API.

The missing-file check is deferred until the consuming compile classpath is
resolved, so unrelated modules can build without the JAR. The `aosp-libs`
submodule must still be initialized so Gradle can load the script.

To use a different local framework JAR:

```sh
./gradlew :app:assembleDebug -PplatformFramework=/path/to/framework.jar
```

Relative override paths are resolved from the repository root. Use a framework
JAR matching the AOSP version you target. Adding the JAR does not grant platform
permissions.

For an existing checkout:

```sh
git submodule update --init aosp-libs
git -C aosp-libs lfs pull
```

Commit `.gitmodules` and the submodule pointer in the consuming repository to pin
the library version used by that project.
