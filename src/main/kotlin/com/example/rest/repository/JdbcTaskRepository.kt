package com.example.rest.repository

import com.example.rest.model.Task
import com.example.rest.model.TaskStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class JdbcTaskRepository(
    private val jdbcClient: JdbcClient
) : TaskRepository {
    companion object {
        private const val SELECT_BASE = """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks
        """
        private const val SELECT_BY_ID = """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks
            WHERE id = :id
        """
        private const val INSERT_TASK = """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
            RETURNING id
        """
        private const val UPDATE_STATUS = """
            UPDATE tasks
            SET status = :status, updated_at = :updatedAt
            WHERE id = :id
        """
    }

    override fun save(title: String, description: String?): Task {
        val now = LocalDateTime.now()
        val id = jdbcClient.sql(INSERT_TASK.trimIndent())
            .param("title", title)
            .param("description", description)
            .param("status", TaskStatus.NEW.name)
            .param("createdAt", now)
            .param("updatedAt", now)
            .query(Long::class.java)
            .single()

        return findById(id) ?: error("Task with id=$id was not found right after insert")
    }

    override fun findById(id: Long): Task? =
        jdbcClient.sql(SELECT_BY_ID.trimIndent())
            .param("id", id)
            .query(::mapTask)
            .optional()
            .orElse(null)

    override fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task> {
        val offset = page * size
        val (sql, params) = if (status == null) {
            Pair(
                """
                $SELECT_BASE
                ORDER BY created_at DESC
                LIMIT :size OFFSET :offset
                """.trimIndent(),
                mapOf("size" to size, "offset" to offset)
            )
        } else {
            Pair(
                """
                $SELECT_BASE
                WHERE status = :status
                ORDER BY created_at DESC
                LIMIT :size OFFSET :offset
                """.trimIndent(),
                mapOf("status" to status.name, "size" to size, "offset" to offset)
            )
        }

        var stmt: JdbcClient.StatementSpec = jdbcClient.sql(sql)
        params.forEach { (key, value) -> stmt = stmt.param(key, value) }
        return stmt.query(::mapTask).list()
    }

    override fun count(status: TaskStatus?): Long {
        val (sql, params) = if (status == null) {
            Pair("SELECT COUNT(*) FROM tasks", emptyMap<String, Any>())
        } else {
            Pair("SELECT COUNT(*) FROM tasks WHERE status = :status", mapOf("status" to status.name))
        }

        var stmt: JdbcClient.StatementSpec = jdbcClient.sql(sql)
        params.forEach { (key, value) -> stmt = stmt.param(key, value) }
        return stmt.query(Long::class.java).single()
    }

    override fun updateStatus(id: Long, status: TaskStatus): Task? {
        val updatedRows = jdbcClient.sql(UPDATE_STATUS.trimIndent())
            .param("status", status.name)
            .param("updatedAt", LocalDateTime.now())
            .param("id", id)
            .update()

        return if (updatedRows == 0) null else findById(id)
    }

    override fun deleteById(id: Long): Boolean {
        val affected = jdbcClient.sql("DELETE FROM tasks WHERE id = :id")
            .param("id", id)
            .update()
        return affected > 0
    }

    @Suppress("UNUSED_PARAMETER")
    private fun mapTask(rs: ResultSet, rowNum: Int): Task {
        return Task(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            status = TaskStatus.valueOf(rs.getString("status")),
            createdAt = rs.getObject("created_at", LocalDateTime::class.java),
            updatedAt = rs.getObject("updated_at", LocalDateTime::class.java)
        )
    }
}
