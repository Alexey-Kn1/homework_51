package ru.netology.model;

import java.util.Objects;

public class BankCard implements Comparable {
    private String number;
    private String validUntil;
    private String cvv;

    public BankCard(String number, String validUntil, String cvv) {
        this.number = number;
        this.validUntil = validUntil;
        this.cvv = cvv;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    @Override
    public int compareTo(Object o) {
        var other = (BankCard) o;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BankCard card = (BankCard) o;
        return Objects.equals(number, card.number) && Objects.equals(validUntil, card.validUntil) && Objects.equals(cvv, card.cvv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, validUntil, cvv);
    }
}
