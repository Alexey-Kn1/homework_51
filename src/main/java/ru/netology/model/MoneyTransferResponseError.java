package ru.netology.model;

public class MoneyTransferResponseError
        extends ErrorResponse
        implements MoneyTransferResponse {

    public MoneyTransferResponseError(String message, int id) {
        super(message, id);
    }
}
