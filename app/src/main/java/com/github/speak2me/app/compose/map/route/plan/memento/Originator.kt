package com.github.speak2me.app.compose.map.route.plan.memento

/**
 * 通用发起人类 - 负责创建备忘录和从备忘录恢复状态
 * 这是备忘录模式中的 Originator 角色，支持任意状态类型
 */
class Originator<State> {

    /**
     * 创建备忘录
     */
    fun createMemento(
        state: State,
        description: String,
        metadata: Map<String, Any> = emptyMap()
    ): Memento<State> {
        return Memento(
            state = state,
            description = description,
            metadata = metadata
        )
    }

    /**
     * 从备忘录恢复状态
     */
    fun restoreFromMemento(memento: Memento<State>): State {
        return memento.state
    }

    /**
     * 验证备忘录的有效性
     */
    fun validateMemento(memento: Memento<State>): Boolean {
        // 允许空状态，这样可以撤销到空状态
        return true
    }

    /**
     * 获取备忘录的摘要信息
     */
    fun getMementoSummary(memento: Memento<State>): String {
        return "${memento.description} (${memento.metadata})"
    }
}
