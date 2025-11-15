package ru.netology.model;

import jakarta.validation.constraints.NotEmpty;

public record OperationConfirmationRequest(@NotEmpty String operationId, @NotEmpty String code) {
}
