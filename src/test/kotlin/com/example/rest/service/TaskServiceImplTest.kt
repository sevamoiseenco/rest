package com.example.rest.service

import com.example.rest.dto.CreateTaskRequest
import com.example.rest.dto.UpdateTaskStatusRequest
import com.example.rest.exception.TaskNotFoundException
import com.example.rest.model.Task
import com.example.rest.model.TaskStatus
import com.example.rest.repository.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TaskServiceImplTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    @InjectMocks
    private lateinit var taskService: TaskServiceImpl

    @Test
    fun `successful task creation`() {
        val task = testTask()
        whenever(taskRepository.save("Prepare report", "Monthly")).thenReturn(task)

        StepVerifier.create(taskService.createTask(CreateTaskRequest("Prepare report", "Monthly")))
            .assertNext {
                assertEquals(task.id, it.id)
                assertEquals(TaskStatus.NEW, it.status)
            }
            .verifyComplete()

        verify(taskRepository).save("Prepare report", "Monthly")
    }

    @Test
    fun `get task by id success`() {
        val task = testTask(id = 100)
        whenever(taskRepository.findById(100)).thenReturn(task)

        StepVerifier.create(taskService.getTaskById(100))
            .assertNext { assertEquals(100L, it.id) }
            .verifyComplete()
    }

    @Test
    fun `error when task not found`() {
        whenever(taskRepository.findById(777)).thenReturn(null)

        StepVerifier.create(taskService.getTaskById(777))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `update status success`() {
        val updated = testTask(status = TaskStatus.DONE)
        whenever(taskRepository.updateStatus(1, TaskStatus.DONE)).thenReturn(updated)

        StepVerifier.create(taskService.updateStatus(1, UpdateTaskStatusRequest(TaskStatus.DONE)))
            .assertNext { assertEquals(TaskStatus.DONE, it.status) }
            .verifyComplete()

        verify(taskRepository).updateStatus(1, TaskStatus.DONE)
    }

    @Test
    fun `delete task success`() {
        whenever(taskRepository.deleteById(9)).thenReturn(true)

        StepVerifier.create(taskService.deleteTask(9))
            .verifyComplete()

        verify(taskRepository).deleteById(9)
    }

    @Test
    fun `delete should fail when task absent`() {
        whenever(taskRepository.deleteById(9)).thenReturn(false)

        StepVerifier.create(taskService.deleteTask(9))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `get tasks with pagination and filtering`() {
        val task = testTask(status = TaskStatus.NEW)
        whenever(taskRepository.findAll(0, 10, TaskStatus.NEW)).thenReturn(listOf(task))
        whenever(taskRepository.count(TaskStatus.NEW)).thenReturn(1)

        StepVerifier.create(taskService.getTasks(0, 10, "NEW"))
            .assertNext {
                assertEquals(0, it.page)
                assertEquals(10, it.size)
                assertEquals(1L, it.totalElements)
                assertEquals(1, it.totalPages)
                assertEquals(1, it.content.size)
            }
            .verifyComplete()
    }

    @Test
    fun `invalid status should return bad request error`() {
        StepVerifier.create(taskService.getTasks(0, 10, "NOT_VALID"))
            .expectError(IllegalArgumentException::class.java)
            .verify()

        verify(taskRepository, never()).findAll(any(), any(), any())
    }

    private fun testTask(
        id: Long = 1,
        title: String = "Prepare report",
        description: String? = "Monthly",
        status: TaskStatus = TaskStatus.NEW,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 3, 26, 12, 0, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2026, 3, 26, 12, 0, 0)
    ): Task = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
