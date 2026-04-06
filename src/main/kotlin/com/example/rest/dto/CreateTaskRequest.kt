package com.example.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTaskRequest(
    @field:NotBlank(message = "title must not be blank")
    @field:Size(min = 3, max = 100, message = "title length must be between 3 and 100")
    val title: String,
    val description: String?
)
