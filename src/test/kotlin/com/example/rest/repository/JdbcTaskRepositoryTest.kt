package com.example.rest.repository

import com.example.rest.model.Task
import com.example.rest.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JdbcTaskRepositoryTest {

    @Mock
    private lateinit var jdbcClient: JdbcClient

    @Mock
    private lateinit var statementSpec: JdbcClient.StatementSpec

    @Mock
    private lateinit var longQuerySpec: JdbcClient.MappedQuerySpec<Long>

    @Mock
    private lateinit var taskQuerySpec: JdbcClient.MappedQuerySpec<Task>

    private lateinit var repository: JdbcTaskRepository

    @BeforeEach
    fun setUp() {
        repository = JdbcTaskRepository(jdbcClient)
        whenever(jdbcClient.sql(any<String>())).thenReturn(statementSpec)
        whenever(statementSpec.param(any<String>(), any())).thenReturn(statementSpec)
    }

    @Test
    fun `findById should map row to domain object`() {
        val mapperCaptor = argumentCaptor<RowMapper<Task>>()
        whenever(statementSpec.query(mapperCaptor.capture())).thenReturn(taskQuerySpec)
        whenever(taskQuerySpec.optional()).thenReturn(Optional.of(testTask(id = 42)))

        val result = repository.findById(42)

        assertEquals(42L, result?.id)
        assertEquals("Prepare report", result?.title)
        verify(statementSpec).param("id", 42L)
        verify(jdbcClient).sql(any<String>())

        val mapper = mapperCaptor.firstValue
        val rs = org.mockito.kotlin.mock<ResultSet>()
        whenever(rs.getLong("id")).thenReturn(5L)
        whenever(rs.getString("title")).thenReturn("Mapped task")
        whenever(rs.getString("description")).thenReturn("Mapped description")
        whenever(rs.getString("status")).thenReturn("DONE")
        val created = LocalDateTime.of(2026, 4, 1, 10, 0)
        val updated = LocalDateTime.of(2026, 4, 1, 11, 0)
        whenever(rs.getObject("created_at", LocalDateTime::class.java)).thenReturn(created)
        whenever(rs.getObject("updated_at", LocalDateTime::class.java)).thenReturn(updated)

        val mapped = requireNotNull(mapper.mapRow(rs, 0))
        assertEquals(5L, mapped.id)
        assertEquals(TaskStatus.DONE, mapped.status)
        assertEquals(created, mapped.createdAt)
    }

    @Test
    fun `findAll should include status filter in SQL when status provided`() {
        whenever(statementSpec.query(any<RowMapper<Task>>())).thenReturn(taskQuerySpec)
        whenever(taskQuerySpec.list()).thenReturn(listOf(testTask(status = TaskStatus.NEW)))
        val sqlCaptor = argumentCaptor<String>()
        whenever(jdbcClient.sql(sqlCaptor.capture())).thenReturn(statementSpec)

        val result = repository.findAll(page = 1, size = 10, status = TaskStatus.NEW)

        assertEquals(1, result.size)
        assertTrue(sqlCaptor.firstValue.contains("WHERE status = :status"))
        verify(statementSpec).param("status", "NEW")
        verify(statementSpec).param("size", 10)
        verify(statementSpec).param("offset", 10)
    }

    @Test
    fun `findAll should not include status filter when status is null`() {
        whenever(statementSpec.query(any<RowMapper<Task>>())).thenReturn(taskQuerySpec)
        whenever(taskQuerySpec.list()).thenReturn(emptyList())
        val sqlCaptor = argumentCaptor<String>()
        whenever(jdbcClient.sql(sqlCaptor.capture())).thenReturn(statementSpec)

        val result = repository.findAll(page = 0, size = 5, status = null)

        assertTrue(result.isEmpty())
        assertFalse(sqlCaptor.firstValue.contains("WHERE status = :status"))
        verify(statementSpec, never()).param(eq("status"), any())
        verify(statementSpec).param("size", 5)
        verify(statementSpec).param("offset", 0)
    }

    @Test
    fun `count should support optional status`() {
        whenever(statementSpec.query(Long::class.java)).thenReturn(longQuerySpec)
        whenever(longQuerySpec.single()).thenReturn(3L)
        val sqlCaptor = argumentCaptor<String>()
        whenever(jdbcClient.sql(sqlCaptor.capture())).thenReturn(statementSpec)

        val withFilter = repository.count(TaskStatus.DONE)

        assertEquals(3L, withFilter)
        assertTrue(sqlCaptor.firstValue.contains("WHERE status = :status"))
        verify(statementSpec).param("status", "DONE")
    }

    @Test
    fun `deleteById returns true when row deleted and false otherwise`() {
        whenever(statementSpec.update()).thenReturn(1).thenReturn(0)

        val deleted = repository.deleteById(10)
        val notDeleted = repository.deleteById(11)

        assertTrue(deleted)
        assertFalse(notDeleted)
        verify(statementSpec).param("id", 10L)
        verify(statementSpec).param("id", 11L)
    }

    @Test
    fun `updateStatus returns null when no rows affected`() {
        whenever(statementSpec.update()).thenReturn(0)

        val result = repository.updateStatus(20, TaskStatus.DONE)

        assertNull(result)
        verify(statementSpec).param("id", 20L)
        verify(statementSpec).param("status", "DONE")
    }

    private fun testTask(
        id: Long = 1,
        status: TaskStatus = TaskStatus.NEW
    ): Task = Task(
        id = id,
        title = "Prepare report",
        description = "Monthly",
        status = status,
        createdAt = LocalDateTime.of(2026, 4, 1, 10, 0),
        updatedAt = LocalDateTime.of(2026, 4, 1, 10, 0)
    )
}
