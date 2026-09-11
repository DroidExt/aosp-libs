import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.kotlin.dsl.withGroovyBuilder

check(pluginManager.hasPlugin("org.jetbrains.kotlin.android")) {
    "Apply org.jetbrains.kotlin.android before aosp-libs/framework.gradle.kts"
}

// Applied scripts cannot import Kotlin plugin types directly.
val kotlinCompileType = plugins.getPlugin("org.jetbrains.kotlin.android")
    .javaClass.classLoader.loadClass("org.jetbrains.kotlin.gradle.tasks.KotlinCompile")
    .asSubclass(Task::class.java)

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
    tasks.withType(kotlinCompileType).configureEach {
        withGroovyBuilder {
            val libraries = getProperty("libraries") as ConfigurableFileCollection
            val originalLibraries = libraries.from.toList()
            libraries.setFrom(platformFrameworkFiles, originalLibraries)
        }
    }
}

dependencies {
    add("compileOnly", platformFrameworkFiles)
}
