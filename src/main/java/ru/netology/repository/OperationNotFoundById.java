package ru.netology.repository;

public class OperationNotFoundById extends RuntimeException {
    public OperationNotFoundById(String id) {
        super(String.format("Operation not found by id \"%s\"", id));
    }
}
