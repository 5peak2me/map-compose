import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.impl.capitalizeFirstChar
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import internal.aarLibsWithJNI
import internal.abiFilters
import internal.cmd
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal class ElfAlignmentPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            val extension = extensions.create("elfAlignment", ElfAlignmentExtension::class.java, project.objects)
            plugins.withType(AppPlugin::class.java) {
                val androidComponents = extensions.getByType<ApplicationAndroidComponentsExtension>()
                androidComponents.onVariants { variant ->
                    val analyzeTask = tasks.register(
                        "analyze${variant.name.capitalizeFirstChar()}Alignment",
                        ElfAlignmentTask::class.java,
                    )
                    analyzeTask.configure {
                        group = PLUGIN_NAME

                        aarLibs.set(variant.aarLibsWithJNI())
                        cmd.set(variant.cmd(project))
                        abiFilters.set(variant.abiFilters(project))
                        filter.set(extension.filter)
                        maxAlign.set(extension.maxAlign)
                        reportDir.set(layout.buildDirectory.dir("reports/$PLUGIN_NAME/${variant.name}"))

                        with(extension.output) {
                            csvOutput.set(csv)
                            htmlOutput.set(html)
                            jsonOutput.set(json)
                            markdownOutput.set(md)
                        }

//                        outputs.upToDateWhen { false }
                    }
                    if (extension.resoleOnBuild.get()) {
                        afterEvaluate {
                            tasks.named("merge${variant.name.capitalizeFirstChar()}NativeLibs").dependsOn(analyzeTask)
                        }
                    }
                }
            }
        }
    }

    internal companion object {
        internal const val PLUGIN_NAME = "elf-16k-alignment"
    }

}
