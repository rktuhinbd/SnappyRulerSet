package com.rkt.snappyrulerset.domain.entity

/**
 * Manages undo/redo functionality for the drawing application.
 * Maintains a history of drawing states and provides methods to navigate through them.
 * 
 * @param max Maximum number of states to keep in history (default: 50)
 */
class UndoRedoManager(private val max: Int = 50) {
    private val past = ArrayDeque<DrawingState>()
    private val future = ArrayDeque<DrawingState>()


    /**
     * Saves a new state to the history and clears the redo stack
     */
    fun push(state: DrawingState) {
        past.addLast(state)
        if (past.size > max) past.removeFirst()
        future.clear()
    }

    /**
     * Checks if undo is possible (at least 2 states in history)
     */
    fun canUndo() = past.size > 1
    
    /**
     * Checks if redo is possible (states available in redo stack)
     */
    fun canRedo() = future.isNotEmpty()

    /**
     * Undoes the last action and returns the previous state
     */
    fun undo(current: DrawingState): DrawingState {
        if (!canUndo()) return current
        val last = past.removeLast()
        future.addLast(last)
        return past.last()
    }

    /**
     * Redoes the last undone action and returns the next state
     */
    fun redo(current: DrawingState): DrawingState {
        val next = future.removeLastOrNull() ?: return current
        past.addLast(next)
        return next
    }
}