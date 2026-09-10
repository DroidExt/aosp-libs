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
