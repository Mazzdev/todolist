package com.example.todolist.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.api.RetrofitInstance
import kotlinx.coroutines.launch

data class TodoItem(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)

class TodoViewModel : ViewModel() {

    private var nextId = 0

    var todoList by mutableStateOf(listOf<TodoItem>())
        private set

    var quoteText by mutableStateOf("")
        private set

    var quoteAuthor by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    fun loadTodos(context: Context) {
        val prefs = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("todos", emptySet()) ?: emptySet()

        todoList = saved.mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size == 3) {
                TodoItem(
                    id = parts[0].toInt(),
                    title = parts[1],
                    isDone = parts[2].toBoolean()
                )
            } else {
                null
            }
        }.sortedBy { it.id }

        nextId = (todoList.maxOfOrNull { it.id } ?: 0) + 1
    }

    private fun saveTodos(context: Context) {
        val prefs = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)

        val saved = todoList.map {
            "${it.id}|${it.title}|${it.isDone}"
        }.toSet()

        prefs.edit()
            .putStringSet("todos", saved)
            .apply()
    }

    fun addTodo(title: String, context: Context) {
        if (title.isNotBlank()) {
            val newItem = TodoItem(
                id = nextId++,
                title = title,
                isDone = false
            )

            todoList = todoList + newItem
            saveTodos(context)
        }
    }

    fun removeTodo(item: TodoItem, context: Context) {
        todoList = todoList - item
        saveTodos(context)
    }

    fun toggleDone(item: TodoItem, context: Context) {
        todoList = todoList.map {
            if (it.id == item.id) it.copy(isDone = !it.isDone)
            else it
        }

        saveTodos(context)
    }

    fun fetchQuote() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val response = RetrofitInstance.api.getRandomQuote()

                quoteText = response.content
                quoteAuthor = response.author
            } catch (e: Exception) {
                errorMessage = "Error while loading quote"
            } finally {
                isLoading = false
            }
        }
    }
}