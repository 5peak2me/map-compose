import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

pluginManager.apply("jacoco")

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Runs debug unit tests and generates an aggregate JaCoCo coverage report."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val coverageExclusions = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/ComposableSingletons*.*",
    )
    classDirectories.from(rootProject.subprojects.map { project ->
        project.files(
            project.fileTree(project.layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
                exclude(coverageExclusions)
            },
            project.fileTree(project.layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
                exclude(coverageExclusions)
            },
        )
    })
    sourceDirectories.from(rootProject.subprojects.map { project ->
        project.files("src/main/java", "src/main/kotlin")
    })
    executionData.from(rootProject.subprojects.map { project ->
        project.layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

rootProject.subprojects {
    pluginManager.apply("jacoco")

    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    plugins.withId("com.android.application") {
        rootProject.tasks.named("jacocoTestReport") {
            dependsOn(tasks.named("testDebugUnitTest"))
        }
    }
    plugins.withId("com.android.library") {
        rootProject.tasks.named("jacocoTestReport") {
            dependsOn(tasks.named("testDebugUnitTest"))
        }
    }
}
