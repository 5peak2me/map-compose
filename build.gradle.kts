import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    alias(libs.plugins.jetbrains.dokka)
}

dependencies {
    subprojects {
        if (name == "app") return@subprojects
        dokka(project(path))
    }
}

dokka {
    dokkaPublications.html {
        moduleName.set("ktx")
        moduleVersion.set(providers.gradleProperty("artifact_version"))
    }

    pluginsConfiguration {
        withType<DokkaHtmlPluginParameters>().configureEach {
            customStyleSheets.from(layout.projectDirectory.file("docs/dokka/styles/ktx-dokka.css"))
        }
    }
}

subprojects {
    if (name != "app") {
        group = "com.github.5peak2me.ktx"
        version = "1.0.0"

        plugins.apply("maven-publish")
//        plugins.apply("org.jetbrains.dokka")

        tasks.withType<KotlinCompile> {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_11
                freeCompilerArgs.add("-Xexplicit-api=strict")
                freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
            }
        }
    }
}
