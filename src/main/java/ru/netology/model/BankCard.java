package ru.netology.model;

public record BankCard(String number, String validUntil, String cvv) implements Comparable<BankCard> {
    @Override
    public int compareTo(BankCard other) {
        int res = number.compareTo(other.number);

        if (res != 0) {
            return res;
        }

        res = validUntil.compareTo(other.validUntil);

        if (res != 0) {
            return res;
        }

        return cvv.compareTo(other.cvv);
    }
}
