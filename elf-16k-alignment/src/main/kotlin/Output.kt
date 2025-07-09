import ElfAlignmentPlugin.Companion.PLUGIN_NAME
import ElfAlignmentTask.ElfAlignmentWorkAction.Parameters
import com.android.build.gradle.internal.cxx.json.jsonStringOf
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.TextBorder
import com.jakewharton.picnic.renderText
import com.jakewharton.picnic.table
import internal.yellow
import org.gradle.api.file.DirectoryProperty
import java.io.FileWriter

internal object Output {

    private val headers = listOf("artifact", "name", "abi", "p_align", "16k compatible"/*, "cmd"*/)

    abstract class Dumper {
        protected val aarNames = mutableSetOf<String>()
        protected val jniNames = mutableSetOf<String>()

        abstract fun dump(aarLibs: List<AarLib>)

        protected fun buildTableRows(libs: List<AarLib>) =
            buildString {
                libs.forEachIndexed { index, aar ->
                    aar.jniLibs.forEach { jni ->
                        appendLine("\t<tr>")
                        if (!aarNames.contains(aar.name)) {
                            appendLine("\t\t<td rowspan=\"${aar.jniLibs.size}\"><a href=\"${aar.url}\">${aar.name}</a></td>")
                        }
                        if (jniNames.add(jni.name)) {
                            val rowSpan = if (aar.jniLibs.size > 1) 2 else 1
                            appendLine("\t\t<td rowspan=\"$rowSpan\"><a href=\"${jni.path}\">${jni.name}</a></td>")
                        }
                        appendLine("\t\t<td>${jni.abi}</td>")
                        appendLine("\t\t<td>${jni.align}</td>")
                        appendLine("\t\t<td>${jni.compatibility()}</td>")
                        if (aarNames.add(aar.name)) {
                            appendLine("\t\t<td rowspan=\"${aar.jniLibs.size}\"><a href=\"${aar.cmd}\" target=\"_blank\">▶️</a></td>")
                        }
                        if (index != libs.lastIndex) {
                            appendLine("\t</tr>")
                        } else {
                            append("\t</tr>")
                        }
                    }
                }
            }

        protected fun String.writeToFile(dir: DirectoryProperty, extension: String, template: Boolean = false) {
            val target = dir.file("$PLUGIN_NAME.$extension").get().asFile
            val content = if (template) {
                val content =
                    this@Dumper.javaClass.classLoader.getResourceAsStream(target.name)?.bufferedReader()?.readText()
                        .orEmpty()
                content.replace("REPLACE_ME", this)
            } else this
            FileWriter(target).use { it.write(content) }
            println("$extension report is available at: $target")
        }
    }

    fun createEnabled(p: Parameters): List<Dumper> {
        val dir = p.reportDir
        return buildList {
            add(Console())
            if (p.csvOutput.getOrElse(false)) add(Csv(dir))
            if (p.htmlOutput.getOrElse(false)) add(Html(dir))
            if (p.jsonOutput.getOrElse(false)) add(Json(dir))
            if (p.markdownOutput.getOrElse(false)) add(Markdown(dir))
        }
    }

    class Console : Dumper() {
        override fun dump(aarLibs: List<AarLib>) {
            table {
                cellStyle {
                    border = true
                    alignment = TextAlignment.MiddleLeft
                    paddingLeft = 1
                    paddingRight = 1
                }
                header { row { headers.forEach { cell(it) } } }
                body {
                    aarLibs.forEach { aar ->
                        aar.jniLibs.sortedBy { it.name }.forEach { jni ->
                            row {
                                if (!aarNames.contains(aar.name)) {
                                    cell(aar.name) { rowSpan = aar.jniLibs.size }
                                }
                                if (jniNames.add(jni.name)) {
                                    cell(jni.name) {
                                        val sizeOfJni = aar.jniLibs.groupingBy { it.name }.eachCount()[jni.name]
                                        rowSpan = sizeOfJni ?: 1
                                    }
                                }
                                cell(jni.abi)
                                cell(jni.align)
                                cell(jni.compatibility(console = true))
//                                if (aarNames.add(aar.name)) {
//                                    cell(aar.cmd) { rowSpan = aar.jniLibs.size }
//                                }
                                aarNames.add(aar.name)
                            }
                        }
                    }
                }
                footer {
                    row {
                        cell("Powered by Gradle & elf-16k-alignment") {
                            columnSpan = headers.size
                        }
                        cellStyle { alignment = TextAlignment.MiddleRight }
                    }
                }
            }.renderText(border = TextBorder.ROUNDED).yellow.run(::println)
        }
    }

    class Markdown(private val dir: DirectoryProperty) : Dumper() {
        override fun dump(aarLibs: List<AarLib>) = buildTableRows(aarLibs).writeToFile(dir, "md", template = true)
    }

    class Csv(private val dir: DirectoryProperty) : Dumper() {
        override fun dump(aarLibs: List<AarLib>) {
            buildString {
                appendLine(headers.joinToString(","))
                aarLibs.forEach { aar ->
                    aar.jniLibs.forEach { jni ->
                        listOf(
                            aar.name,
                            jni.name,
                            jni.abi,
                            jni.align,
                            jni.compatibility(),
//                            aar.cmd
                        ).joinToString(",").apply(::appendLine)
                    }
                }
            }.writeToFile(dir, "csv")
        }
    }

    class Json(private val dir: DirectoryProperty) : Dumper() {
        override fun dump(aarLibs: List<AarLib>) = jsonStringOf(aarLibs).writeToFile(dir, "json")
    }

    class Html(private val dir: DirectoryProperty) : Dumper() {
        override fun dump(aarLibs: List<AarLib>) = buildTableRows(aarLibs).writeToFile(dir, "html", template = true)
    }

}
