package ru.netology.model;

import java.util.Objects;

public class Transfer {
    private BankCard source;
    private BankCard destination;
    private MoneyQuantity amount;

    public Transfer(BankCard source, BankCard destination, MoneyQuantity amount) {
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    public BankCard getSource() {
        return source;
    }

    public void setSource(BankCard source) {
        this.source = source;
    }

    public BankCard getDestination() {
        return destination;
    }

    public void setDestination(BankCard destination) {
        this.destination = destination;
    }

    public MoneyQuantity getAmount() {
        return amount;
    }

    public void setAmount(MoneyQuantity amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(source, transfer.source) && Objects.equals(destination, transfer.destination) && Objects.equals(amount, transfer.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, amount);
    }
}
