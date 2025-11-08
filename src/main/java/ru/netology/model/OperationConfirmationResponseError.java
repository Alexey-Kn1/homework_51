package ru.netology.model;

public class OperationConfirmationResponseError
        extends ErrorResponse
        implements OperationConfirmationResponse {

    public OperationConfirmationResponseError(String message, int id) {
        super(message, id);
    }
}
