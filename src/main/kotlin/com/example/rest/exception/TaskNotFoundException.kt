package com.example.rest.exception

class TaskNotFoundException(id: Long) : RuntimeException("Task with id=$id not found")
