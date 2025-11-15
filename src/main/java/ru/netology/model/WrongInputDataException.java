package ru.netology.model;

public class WrongInputDataException extends RuntimeException {
    private final String description;
    private final int id;

    public WrongInputDataException(String description, int id) {
        super(String.format("Error %d: %s", id, description));
        this.description = description;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }
}
