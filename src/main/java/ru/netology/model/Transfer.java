package ru.netology.model;

public record Transfer(BankCard source, BankCard destination, MoneyQuantity amount) {
}
