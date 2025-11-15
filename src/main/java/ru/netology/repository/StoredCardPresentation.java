package ru.netology.repository;

import ru.netology.model.BankCard;
import ru.netology.model.MoneyQuantity;

public record StoredCardPresentation(BankCard card, MoneyQuantity balance) {
}
