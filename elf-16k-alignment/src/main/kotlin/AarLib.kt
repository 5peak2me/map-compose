import internal.maven

internal data class AarLib(
    val name: String,
    val url: String = name.maven(),
    val jniLibs: List<JniLib> = emptyList(),
    val cmd: String,
)

internal data class JniLib(
    val name: String,
    val abi: String,
    val align: Long,
    val path: String,
    @Transient val max: Long,
) {
    fun compatibility(console: Boolean = false) = if (console) {
        if (align >= max) "✔" else "⚠"
    } else {
        if (align >= max) "✅" else "❌"
    }
}
