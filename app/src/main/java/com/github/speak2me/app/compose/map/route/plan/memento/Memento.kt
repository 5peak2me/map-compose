package com.github.speak2me.app.compose.map.route.plan.memento

import java.util.UUID

/**
 * 通用备忘录类 - 负责存储状态快照
 * 这是备忘录模式中的 Memento 角色
 */
data class Memento<State>(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val state: State,
    val description: String,
    val metadata: Map<String, Any> = emptyMap()
) {
    /**
     * 获取操作时间格式化字符串
     */
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    /**
     * 获取操作摘要信息
     */
    fun getSummary(): String {
        return "[${getFormattedTime()}] $description"
    }

    /**
     * 检查是否包含特定类型的元数据
     */
    fun hasMetadata(key: String): Boolean {
        return metadata.containsKey(key)
    }

    /**
     * 安全获取元数据
     */
    inline fun <reified T> getMetadata(key: String, defaultValue: T): T {
        return metadata[key] as? T ?: defaultValue
    }
} 