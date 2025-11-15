package ru.netology.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MoneyTransferRequest(@NotEmpty String cardFromNumber, @NotEmpty String cardFromValidTill,
                                   @NotEmpty String cardFromCVV, @NotEmpty String cardToNumber,
                                   @NotNull MoneyQuantity amount) {
}
