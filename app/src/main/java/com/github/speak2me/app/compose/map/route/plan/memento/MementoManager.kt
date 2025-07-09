package com.github.speak2me.app.compose.map.route.plan.memento

import com.github.speak2me.app.compose.map.route.plan.memento.Caretaker.*
import kotlinx.coroutines.flow.StateFlow

/**
 * 通用备忘录管理器 - 整合备忘录模式的三个角色
 * 提供统一的接口来管理撤销/重做功能，支持任意状态类型
 */
class MementoManager<State>(
    val originator: Originator<State> = Originator(),
    val caretaker: Caretaker<State> = Caretaker()
) {

    /**
     * 获取撤销/恢复状态流
     */
    val state: StateFlow<UndoRedoState<State>> = caretaker.state

    /**
     * 保存操作到历史记录
     */
    fun save(
        state: State,
        description: String = state.toString(),
        metadata: Map<String, Any> = emptyMap()
    ) {
        val memento = originator.createMemento(state, description, metadata)
        if (originator.validateMemento(memento)) {
            caretaker.save(
                description = memento.description,
                state = memento.state,
                metadata = memento.metadata
            )
        }
    }

    /**
     * 撤销操作
     */
    fun undo(): State? {
        val memento = caretaker.undo()
        return memento?.let {
            originator.restoreFromMemento(it)
        }
    }

    /**
     * 重做操作
     */
    fun redo(): State? {
        val memento = caretaker.redo()
        return memento?.let {
            originator.restoreFromMemento(it)
        }
    }

    /**
     * 检查是否可以撤销
     */
    fun canUndo(): Boolean = caretaker.canUndo()

    /**
     * 检查是否可以重做
     */
    fun canRedo(): Boolean = caretaker.canRedo()

    /**
     * 获取撤销操作描述
     */
    fun getUndoDescription(): String? = caretaker.getUndoDescription()

    /**
     * 获取重做操作描述
     */
    fun getRedoDescription(): String? = caretaker.getRedoDescription()

    /**
     * 清除所有历史记录
     */
    fun clear() = caretaker.clear()

    /**
     * 获取撤销历史记录
     */
    fun getUndoHistory(): List<Memento<State>> = caretaker.getUndoHistory()

    /**
     * 获取重做历史记录
     */
    fun getRedoHistory(): List<Memento<State>> = caretaker.getRedoHistory()

    /**
     * 获取历史统计信息
     */
    fun getHistoryStats(): Map<State, Int> = caretaker.getHistoryStats()

    /**
     * 获取最近的操作列表
     */
    fun getRecentOperations(count: Int = 5): List<String> = caretaker.getRecentOperations(count)

    /**
     * 获取操作历史摘要
     */
    fun getHistorySummary(): List<String> = caretaker.getHistorySummary()

    /**
     * 根据操作类型获取历史记录
     */
    fun getHistoryByState(state: State): List<Memento<State>> = caretaker.getHistoryByState(state)

    /**
     * 获取撤销栈大小
     */
    fun getUndoSize(): Int = caretaker.getUndoSize()

    /**
     * 获取重做栈大小
     */
    fun getRedoSize(): Int = caretaker.getRedoSize()

    /**
     * 验证备忘录
     */
    fun validateMemento(memento: Memento<State>): Boolean =
        originator.validateMemento(memento)
}
