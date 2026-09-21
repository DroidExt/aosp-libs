import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.kotlin.dsl.withGroovyBuilder

// AGP 9 provides Kotlin without applying org.jetbrains.kotlin.android. Applied
// scripts cannot import Kotlin plugin types directly, so use the standalone
// Kotlin plugin's classloader when present, or the Android plugin's otherwise.
val kotlinPluginHost = listOf(
    "org.jetbrains.kotlin.android",
    "com.android.application",
    "com.android.library",
    "com.android.dynamic-feature",
    "com.android.test",
).firstNotNullOfOrNull { plugins.findPlugin(it) }
    ?: error("Apply the Android plugin before aosp-libs/framework.gradle.kts")

val kotlinCompileType = kotlinPluginHost
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
