package internal

internal val String.yellow
    get() = "\u001B[33m$this\u001B[0m"

internal val String.green
    get() = "\u001B[32m$this\u001B[0m"

internal fun String.maven(): String {
    return when {
        isGoogle() -> "https://maven.google.com/web/index.html#$this"
        isJitpack() -> "https://jitpack.io/#${replace(":", "/").substringBeforeLast("/")}"
        else -> "https://central.sonatype.com/artifact/${replace(":", "/")}"
    }
}

private fun String.isGoogle(): Boolean {
    return startsWith("androidx.") || startsWith("com.google.") || startsWith("com.android.")
}

private fun String.isJitpack(): Boolean {
    return startsWith("com.github") || startsWith("io.github")
}
