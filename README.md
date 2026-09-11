# aosp-libs

Shared prebuilt AOSP libraries for Gradle-based Android projects that use platform
APIs. This repository provides reusable build dependencies without keeping a
separate copy in every application repository.

## Contents

| File | Purpose |
| --- | --- |
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

`main` contains the shared guide and Git LFS configuration. Use a version branch
for the framework JAR; the `r` suffix identifies the release revision, not the API
level.

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
git submodule add git@gitme.com:OkieLe/aosp-libs.git aosp-libs
```

`gitme.com` is an SSH alias for GitHub. Configure it locally, or use
`git@github.com:OkieLe/aosp-libs.git` with your standard GitHub SSH setup.

In the consuming module's `build.gradle.kts`, add the import at the top of the
file and the remaining configuration after the `plugins` and `android` blocks:

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Relative overrides are resolved from the repository root.
val platformFramework = rootProject.file(
    providers.gradleProperty("platformFramework")
        .getOrElse("aosp-libs/compile-only/framework.jar")
)

// Validate only when the consuming compile classpath is resolved.
val platformFrameworkFiles = files(providers.provider {
    check(platformFramework.isFile) {
        "Missing platform framework JAR: $platformFramework. " +
            "Initialize aosp-libs, select a version branch, and run Git LFS pull, " +
            "or pass -PplatformFramework=/path/to/framework.jar."
    }
    platformFramework
})

// Platform classes must precede SDK stubs so Kotlin can see hidden members.
// Preserve lazy task dependencies from the original classpath.
afterEvaluate {
    tasks.withType<KotlinCompile>().configureEach {
        val originalLibraries = libraries.from.toList()
        libraries.setFrom(platformFrameworkFiles, originalLibraries)
    }
}

dependencies {
    compileOnly(platformFrameworkFiles)
}
```

Use `compileOnly` so the framework JAR is not packaged into the app. The target
Android system supplies these classes at runtime. For Kotlin compilation,
`compileOnly` alone does not ensure platform classes take precedence over SDK
stubs; the `KotlinCompile` configuration above prepends the JAR while retaining
the original classpath's lazy task dependencies. The provider defers the missing
file check until the classpath is resolved, so unrelated modules can build
without the JAR.

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
