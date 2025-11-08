package ru.netology.model;

import java.util.Objects;

public class OperationConfirmationResponseSuccess implements OperationConfirmationResponse {
    private String operationId;

    public OperationConfirmationResponseSuccess(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OperationConfirmationResponseSuccess that = (OperationConfirmationResponseSuccess) o;
        return Objects.equals(operationId, that.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(operationId);
    }
}
