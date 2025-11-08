package ru.netology.repository;

import ru.netology.model.BankCard;
import ru.netology.model.MoneyQuantity;

public class StoredCardPresentation {
    private BankCard card;
    private MoneyQuantity balance;

    public StoredCardPresentation(BankCard card, MoneyQuantity balance) {
        this.card = card;
        this.balance = balance;
    }

    public BankCard getCard() {
        return card;
    }

    public MoneyQuantity getBalance() {
        return balance;
    }
}
