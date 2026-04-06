package com.example.rest.service

import com.example.rest.dto.CreateTaskRequest
import com.example.rest.dto.PageResponse
import com.example.rest.dto.TaskDtoMapper
import com.example.rest.dto.TaskResponse
import com.example.rest.dto.UpdateTaskStatusRequest
import com.example.rest.exception.TaskNotFoundException
import com.example.rest.model.TaskStatus
import com.example.rest.repository.TaskRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.math.ceil

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository
) : TaskService {

    override fun createTask(request: CreateTaskRequest): Mono<TaskResponse> =
        blockingMono {
            val saved = taskRepository.save(
                title = request.title.trim(),
                description = request.description?.trim()?.ifBlank { null }
            )
            TaskDtoMapper.toResponse(saved)
        }

    override fun getTaskById(id: Long): Mono<TaskResponse> =
        blockingMono {
            val task = taskRepository.findById(id) ?: throw TaskNotFoundException(id)
            TaskDtoMapper.toResponse(task)
        }

    override fun getTasks(page: Int, size: Int, status: String?): Mono<PageResponse<TaskResponse>> =
        blockingMono {
            require(page >= 0) { "page must be greater than or equal to 0" }
            require(size > 0) { "size must be greater than 0" }

            val parsedStatus = parseStatus(status)
            val tasks = taskRepository.findAll(page, size, parsedStatus).map(TaskDtoMapper::toResponse)
            val totalElements = taskRepository.count(parsedStatus)
            val totalPages = if (totalElements == 0L) 0 else ceil(totalElements.toDouble() / size).toInt()
            PageResponse(
                content = tasks,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages
            )
        }

    override fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse> =
        blockingMono {
            val status = request.status ?: throw IllegalArgumentException("status must not be null")
            val task = taskRepository.updateStatus(id, status) ?: throw TaskNotFoundException(id)
            TaskDtoMapper.toResponse(task)
        }

    override fun deleteTask(id: Long): Mono<Void> =
        blockingMono {
            val deleted = taskRepository.deleteById(id)
            if (!deleted) {
                throw TaskNotFoundException(id)
            }
            true
        }
            .then()

    private fun parseStatus(status: String?): TaskStatus? =
        status
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.uppercase()
            ?.let {
                runCatching { TaskStatus.valueOf(it) }
                    .getOrElse { throw IllegalArgumentException("Unknown status '$it'") }
            }

    private fun <T> blockingMono(supplier: () -> T): Mono<T> =
        Mono.fromCallable(supplier).subscribeOn(Schedulers.boundedElastic())
}
