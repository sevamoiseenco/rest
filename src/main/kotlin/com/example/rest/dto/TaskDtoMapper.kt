package com.example.rest.dto

import com.example.rest.model.Task

object TaskDtoMapper {
    fun toResponse(task: Task): TaskResponse =
        TaskResponse(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
}
