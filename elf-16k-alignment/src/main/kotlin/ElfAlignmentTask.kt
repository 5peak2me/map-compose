import ElfAlignmentTask.ElfAlignmentWorkAction.Parameters
import internal.green
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

@CacheableTask
internal abstract class ElfAlignmentTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val aarLibs: MapProperty<String, List<File>>

    @get:Input
    abstract val cmd: Property<String>

    @get:Input
    abstract val filter: Property<Boolean>

    @get:Input
    abstract val maxAlign: Property<Long>

    @get:Input
    @get:Optional
    abstract val abiFilters: SetProperty<String>

    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @get:Input
    abstract val csvOutput: Property<Boolean>

    @get:Input
    abstract val markdownOutput: Property<Boolean>

    @get:Input
    abstract val htmlOutput: Property<Boolean>

    @get:Input
    abstract val jsonOutput: Property<Boolean>

//    init {
//        outputs.upToDateWhen { false }
//    }

    @TaskAction
    fun execute() {
        workerExecutor.noIsolation().submit(ElfAlignmentWorkAction::class.java) {
            inputs.set(aarLibs.get())
            cmd.set(this@ElfAlignmentTask.cmd)
            filter.set(this@ElfAlignmentTask.filter)
            maxAlign.set(this@ElfAlignmentTask.maxAlign)
            abiFilters.set(this@ElfAlignmentTask.abiFilters)
            reportDir.set(this@ElfAlignmentTask.reportDir)

            csvOutput.set(this@ElfAlignmentTask.csvOutput)
            htmlOutput.set(this@ElfAlignmentTask.htmlOutput)
            jsonOutput.set(this@ElfAlignmentTask.jsonOutput)
            markdownOutput.set(this@ElfAlignmentTask.markdownOutput)
        }
    }

    abstract class ElfAlignmentWorkAction : WorkAction<Parameters> {

        abstract class Parameters : WorkParameters {
            abstract val inputs: MapProperty<String, List<File>>
            abstract val cmd: Property<String>
            abstract val filter: Property<Boolean>
            abstract val maxAlign: Property<Long>
            abstract val abiFilters: SetProperty<String>
            abstract val reportDir: DirectoryProperty

            abstract val csvOutput: Property<Boolean>
            abstract val htmlOutput: Property<Boolean>
            abstract val jsonOutput: Property<Boolean>
            abstract val markdownOutput: Property<Boolean>
        }

        override fun execute() {
            val aarLibs = parameters.inputs.get().mapNotNull { (name, libs) ->
                val jniLibs = libs.mapNotNull { lib ->
                    lib.processJniLib(parameters.maxAlign.get(), parameters.filter.get(), parameters.abiFilters.get())
                }.sortedBy { it.name }

                if (jniLibs.isEmpty()) null else AarLib(
                    name,
                    jniLibs = jniLibs,
                    cmd = parameters.cmd.get().format(name)
                )
            }

            if (aarLibs.isEmpty()) {
                println("🎉🎉🎉 Congratulations, No unaligned libs found. 🎉🎉🎉".green)
                return
            }
            Output.createEnabled(parameters).forEach {
                it.dump(aarLibs)
            }
        }

        private fun File.processJniLib(
            maxAlign: Long,
            filterEnabled: Boolean,
            abiFilters: Set<String>,
        ): JniLib? {
            val align = runCatching { ElfReader.maxPAlign(readBytes()) }.getOrElse { 0L }

            val jni = JniLib(name = name, abi = parentFile.name, align = align, path = path, max = maxAlign)

            return when {
                filterEnabled && align >= maxAlign -> null
                jni.abi !in abiFilters -> null
                else -> jni
            }
        }

    }

}
