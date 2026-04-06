package com.example.rest.controller

import com.example.rest.dto.PageResponse
import com.example.rest.dto.TaskResponse
import com.example.rest.exception.GlobalExceptionHandler
import com.example.rest.exception.TaskNotFoundException
import com.example.rest.model.TaskStatus
import com.example.rest.service.TaskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest(controllers = [TaskController::class])
@Import(GlobalExceptionHandler::class)
class TaskControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var taskService: TaskService

    @Test
    fun `create returns 201`() {
        whenever(taskService.createTask(any())).thenReturn(Mono.just(testResponse()))

        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"Prepare report","description":"Monthly"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.status").isEqualTo("NEW")
    }

    @Test
    fun `create should fail validation`() {
        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"","description":"Monthly"}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create should fail when title is too short`() {
        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"ab","description":"Monthly"}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create should fail when title is too long`() {
        val longTitle = "x".repeat(101)
        webTestClient.post()
            .uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"$longTitle","description":"Monthly"}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `get by id returns 200`() {
        whenever(taskService.getTaskById(1)).thenReturn(Mono.just(testResponse()))

        webTestClient.get()
            .uri("/api/tasks/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
    }

    @Test
    fun `returns 404 for absent task`() {
        whenever(taskService.getTaskById(999)).thenReturn(Mono.error(TaskNotFoundException(999)))

        webTestClient.get()
            .uri("/api/tasks/999")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `list returns 200 with filtering pagination`() {
        val page = PageResponse(
            content = listOf(testResponse()),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1
        )
        whenever(taskService.getTasks(0, 10, "NEW")).thenReturn(Mono.just(page))

        webTestClient.get()
            .uri("/api/tasks?page=0&size=10&status=NEW")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalElements").isEqualTo(1)
            .jsonPath("$.content[0].status").isEqualTo("NEW")
    }

    @Test
    fun `list should fail for invalid pagination params`() {
        webTestClient.get()
            .uri("/api/tasks?page=-1&size=0")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `update status returns 200`() {
        whenever(taskService.updateStatus(eq(1), any())).thenReturn(Mono.just(testResponse(status = TaskStatus.DONE)))

        webTestClient.patch()
            .uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"status":"DONE"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("DONE")
    }

    @Test
    fun `delete returns 204`() {
        whenever(taskService.deleteTask(1)).thenReturn(Mono.empty())

        webTestClient.delete()
            .uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `delete returns 404 for absent task`() {
        whenever(taskService.deleteTask(999)).thenReturn(Mono.error(TaskNotFoundException(999)))

        webTestClient.delete()
            .uri("/api/tasks/999")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    private fun testResponse(
        id: Long = 1,
        status: TaskStatus = TaskStatus.NEW
    ): TaskResponse = TaskResponse(
        id = id,
        title = "Prepare report",
        description = "Monthly financial report",
        status = status,
        createdAt = LocalDateTime.of(2026, 3, 26, 12, 0),
        updatedAt = LocalDateTime.of(2026, 3, 26, 12, 0)
    )
}
