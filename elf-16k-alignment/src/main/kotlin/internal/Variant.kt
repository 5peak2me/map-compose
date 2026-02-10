package internal

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.kotlin.dsl.getByType
import java.io.File
import kotlin.collections.get
import kotlin.collections.orEmpty

/**
 * only arm64-v8a/x86_64 libs need to be aligned
 */
private val regex = Regex(".*(arm64-v8a|x86_64).*\\.so$")

private inline val ResolvedArtifactResult.identifier: String
    get() = id.componentIdentifier.displayName

private fun ResolvedArtifactResult.soLibs(): List<File> = file.walkTopDown()
    .filter { it.isFile && regex.containsMatchIn(it.absolutePath) }.toList()

internal fun ApplicationVariant.aarLibsWithJNI(): Map<String, List<File>> {
    val localLibs = this.sources.jniLibs?.all?.get()?.flatMap {
        it.flatMap { it.asFile.listFiles()?.mapNotNull { it }?.flatMap {
            it.listFiles()?.mapNotNull { it }.orEmpty()
        }.orEmpty() }
    }.orEmpty()
    println(localLibs)

    return runtimeConfiguration.incoming.artifactView {
        attributes.attribute(
            AndroidArtifacts.ARTIFACT_TYPE,
            AndroidArtifacts.ArtifactType.JNI.type
        )
    }.artifacts.artifacts
        .associate {
            it.identifier to it.soLibs()
        } + mapOf("app" to localLibs)
}

//context(project: Project)
internal fun Variant.abiFilters(project: Project): Set<String>? {
    val android = project.extensions.getByType<ApplicationExtension>()
    val productFlavors = android.productFlavors
    return if (productFlavors.isEmpty()) {
        android.defaultConfig.ndk.abiFilters
    } else {
        val flavorName = productFlavors.names.firstOrNull { name.contains(it, true) }
        productFlavors.associate {
            it.name to it.ndk.abiFilters
        }[flavorName].orEmpty()
    }
}

private val runner by lazy {
    if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew"
}

//context(project: Project)
internal fun Variant.cmd(project: Project): String {
    return "./${runner} :${project.name}:dependencyInsight --configuration ${name}RuntimeClasspath --dependency %s -s"
}
