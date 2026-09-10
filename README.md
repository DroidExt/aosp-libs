# aosp-libs

Shared prebuilt AOSP libraries for Gradle-based Android projects that use platform
APIs. This repository provides reusable build dependencies without keeping a
separate copy in every application repository.

## Contents

| File | Purpose |
| --- | --- |
| `compile-only/framework.jar` | Android framework classes for compile-time use |

JAR and AAR files are tracked with Git LFS.

## Use in a Gradle project

Install Git LFS, then add this repository as a submodule at your project root:

```sh
git lfs install
git submodule add git@gitme.com:OkieLe/aosp-libs.git aosp-libs
```

`gitme.com` is an SSH alias for GitHub. Configure it locally, or use
`git@github.com:OkieLe/aosp-libs.git` with your standard GitHub SSH setup.

In the consuming module's `build.gradle.kts`:

```kotlin
dependencies {
    compileOnly(files(rootProject.file("aosp-libs/compile-only/framework.jar")))
}
```

Use `compileOnly` so the framework JAR is not packaged into the app. The target
Android system supplies these classes at runtime; use libraries matching the
AOSP version you target. Adding the JAR does not grant platform permissions.

For an existing checkout:

```sh
git submodule update --init aosp-libs
git -C aosp-libs lfs pull
```

Commit `.gitmodules` and the submodule pointer in the consuming repository to pin
the library version used by that project.
