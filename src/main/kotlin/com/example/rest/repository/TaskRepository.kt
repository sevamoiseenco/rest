package com.example.rest.repository

import com.example.rest.model.Task
import com.example.rest.model.TaskStatus

interface TaskRepository {
    fun save(title: String, description: String?): Task
    fun findById(id: Long): Task?
    fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task>
    fun count(status: TaskStatus?): Long
    fun updateStatus(id: Long, status: TaskStatus): Task?
    fun deleteById(id: Long): Boolean
}
