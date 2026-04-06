package com.example.rest.controller

import com.example.rest.dto.CreateTaskRequest
import com.example.rest.dto.PageResponse
import com.example.rest.dto.TaskResponse
import com.example.rest.dto.UpdateTaskStatusRequest
import com.example.rest.service.TaskService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Validated
@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): Mono<TaskResponse> =
        taskService.createTask(request)

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable @Positive id: Long): Mono<TaskResponse> =
        taskService.getTaskById(id)

    @GetMapping
    fun getTasks(
        @RequestParam @Min(0) page: Int,
        @RequestParam @Positive size: Int,
        @RequestParam(required = false) status: String?
    ): Mono<PageResponse<TaskResponse>> =
        taskService.getTasks(page, size, status)

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable @Positive id: Long,
        @Valid @RequestBody request: UpdateTaskStatusRequest
    ): Mono<TaskResponse> = taskService.updateStatus(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(@PathVariable @Positive id: Long): Mono<Void> =
        taskService.deleteTask(id)
}
