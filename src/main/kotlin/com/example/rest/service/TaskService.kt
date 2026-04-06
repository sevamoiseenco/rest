package com.example.rest.service

import com.example.rest.dto.CreateTaskRequest
import com.example.rest.dto.PageResponse
import com.example.rest.dto.TaskResponse
import com.example.rest.dto.UpdateTaskStatusRequest
import reactor.core.publisher.Mono

interface TaskService {
    fun createTask(request: CreateTaskRequest): Mono<TaskResponse>
    fun getTaskById(id: Long): Mono<TaskResponse>
    fun getTasks(page: Int, size: Int, status: String?): Mono<PageResponse<TaskResponse>>
    fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse>
    fun deleteTask(id: Long): Mono<Void>
}
