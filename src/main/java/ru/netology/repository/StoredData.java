package ru.netology.repository;

import ru.netology.model.BankCard;
import ru.netology.model.MoneyQuantity;
import ru.netology.model.Transfer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoredData {
    private List<StoredCardPresentation> cards;
    private Map<String, Transfer> openedTransfers;
    private BigInteger freeOperationId;

    public StoredData(Map<BankCard, MoneyQuantity> balances, Map<String, Transfer> openedTransfers, BigInteger freeOperationId) {
        cards = new ArrayList<>(balances.size());

        for (var balanceData : balances.entrySet()) {
            cards.add(
                    new StoredCardPresentation(balanceData.getKey(), balanceData.getValue())
            );
        }

        this.openedTransfers = openedTransfers;
        this.freeOperationId = freeOperationId;
    }

    public Map<BankCard, MoneyQuantity> getBalances() {
        var res = new HashMap<BankCard, MoneyQuantity>(cards.size());

        for (var storedPresentation : cards) {
            res.put(storedPresentation.getCard(), storedPresentation.getBalance());
        }

        return res;
    }

    public Map<String, Transfer> getOpenedTransfers() {
        return openedTransfers;
    }

    public BigInteger getFreeOperationId() {
        return freeOperationId;
    }
}
