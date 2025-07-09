package com.github.speak2me.app.compose.map.route.plan.memento

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 通用负责人类 - 负责管理备忘录的存储和恢复
 * 这是备忘录模式中的 Caretaker 角色
 */
class Caretaker<State> {
    private val undoStack = ArrayDeque<Memento<State>>()
    private val redoStack = ArrayDeque<Memento<State>>()

    // 状态流，用于通知UI状态变化
    private val _state = MutableStateFlow(UndoRedoState<State>())
    val state: StateFlow<UndoRedoState<State>> = _state.asStateFlow()

    /**
     * 保存操作到撤销记录栈
     */
    fun save(
        state: State,
        description: String,
        metadata: Map<String, Any> = emptyMap()
    ) {
        val memento = Memento(
            state = state,
            description = description,
            metadata = metadata
        )

        undoStack.addLast(memento)
        redoStack.clear() // 新操作会清除恢复栈
        updateState()
    }

    /**
     * 检查是否可以撤销
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    /**
     * 检查是否可以恢复
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    /**
     * 撤销操作
     */
    fun undo(): Memento<State>? {
        if (!canUndo()) return null

        // 从撤销栈获取要撤销的状态（这是当前状态）
        val memento = undoStack.removeLast()

        // 将当前状态添加到恢复栈（用于恢复）
        redoStack.addLast(memento)

        updateState()
        // 返回应该恢复到的状态（撤销栈的最后一个状态，如果为空则返回null表示空状态）
        return undoStack.lastOrNull()
    }

    /**
     * 恢复操作
     */
    fun redo(): Memento<State>? {
        if (!canRedo()) return null

        // 从恢复栈获取要恢复的状态
        val memento = redoStack.removeLast()
        // 将恢复的状态添加到撤销栈
        undoStack.addLast(memento)

        updateState()
        return memento
    }

    /**
     * 获取撤销操作描述
     */
    fun getUndoDescription(): String? = undoStack.lastOrNull()?.description

    /**
     * 获取恢复操作描述
     */
    fun getRedoDescription(): String? = redoStack.lastOrNull()?.description

    /**
     * 清除所有历史记录
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
        updateState()
    }

    /**
     * 获取撤销历史记录
     */
    fun getUndoHistory(): List<Memento<State>> = undoStack.toList()

    /**
     * 获取恢复历史记录
     */
    fun getRedoHistory(): List<Memento<State>> = redoStack.toList()

    /**
     * 获取历史统计信息
     */
    fun getHistoryStats(): Map<State, Int> {
        return undoStack.groupingBy { it.state }.eachCount()
    }

    /**
     * 获取最近的操作列表
     */
    fun getRecentOperations(count: Int = 5): List<String> {
        return undoStack.takeLast(count).map { it.description }
    }

    /**
     * 获取操作历史摘要
     */
    fun getHistorySummary(): List<String> {
        return undoStack.map { "[${it.id}] ${it.description}" }
    }

    /**
     * 根据操作类型获取历史记录
     */
    fun getHistoryByState(state: State): List<Memento<State>> {
        return undoStack.filter { it == state }
    }

    /**
     * 获取撤销栈大小
     */
    fun getUndoSize(): Int = undoStack.size

    /**
     * 获取恢复栈大小
     */
    fun getRedoSize(): Int = redoStack.size

    /**
     * 更新状态
     */
    private fun updateState() {
        _state.value = UndoRedoState(
            canUndo = canUndo(),
            undoDescription = getUndoDescription(),
            canRedo = canRedo(),
            redoDescription = getRedoDescription(),
            currentState = undoStack.lastOrNull()?.state
        )
    }

    /**
     * 撤销/恢复状态数据类
     */
    data class UndoRedoState<State>(
        val canUndo: Boolean = false,
        val undoDescription: String? = null,
        val canRedo: Boolean = false,
        val redoDescription: String? = null,
        val currentState: State? = null
    )
}
