package com.example.rest.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(ex: TaskNotFoundException): ResponseEntity<ErrorResponse> =
        buildError(HttpStatus.NOT_FOUND, ex.message ?: "Task not found")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage ?: "invalid value"}" }
            .ifBlank { "Validation failed" }
        return buildError(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> =
        buildError(HttpStatus.BAD_REQUEST, ex.message ?: "Validation failed")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        buildError(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInput(ex: ServerWebInputException): ResponseEntity<ErrorResponse> =
        buildError(HttpStatus.BAD_REQUEST, ex.reason ?: "Malformed request")

    private fun buildError(status: HttpStatus, message: String): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = status.value(),
                error = status.reasonPhrase,
                message = message
            )
        )
}
