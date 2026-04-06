package com.example.rest.dto

import com.example.rest.model.TaskStatus
import jakarta.validation.constraints.NotNull

data class UpdateTaskStatusRequest(
    @field:NotNull(message = "status must not be null")
    val status: TaskStatus?
)
