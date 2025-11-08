package ru.netology.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.netology.model.BankCard;
import ru.netology.model.MoneyQuantity;
import ru.netology.model.Transfer;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Repository
public class MoneyRepository {
    private static final Logger log = LoggerFactory.getLogger(MoneyRepository.class);
    private final Map<BankCard, MoneyQuantity> balances;
    private final Map<String, Transfer> openedTransfers;
    private BigInteger freeOperationId;
    private final String filePath;
    private boolean saveOnEachOperation;

    public MoneyRepository(@Value("${data_file_path:./data.json}") String filePath) {
        this.filePath = filePath;

        saveOnEachOperation = true;

        var gson = new Gson();

        StoredData parsed = null;

        try {
            parsed = gson.fromJson(
                    Files.readString(Path.of(filePath)),
                    StoredData.class
            );
        } catch (IOException ex) {
            balances = new HashMap<>();
            openedTransfers = new HashMap<>();
            freeOperationId = new BigInteger("0");

            log.info(
                    String.format(
                            "failed to read data from file \"%s\"; the file will be crated on the first save",
                            filePath
                    )
            );

            return;
        }

        log.info(
                String.format(
                        "data successfully read from file \"%s\"",
                        filePath
                )
        );

        balances = parsed.getBalances();
        openedTransfers = parsed.getOpenedTransfers();
        freeOperationId = parsed.getFreeOperationId();
    }

    public boolean savedOnEachOperation() {
        return saveOnEachOperation;
    }

    public void setSaveOnEachOperation(boolean saveOnEachOperation) {
        this.saveOnEachOperation = saveOnEachOperation;
    }

    private void saveIfNeeded() {
        if (saveOnEachOperation) {
            try {
                save();

                log.info(
                        String.format(
                                "data successfully saved to file \"%s\"",
                                filePath
                        )
                );
            } catch (IOException ex) {
                log.error(
                        String.format(
                                "failed to save data into \"%s\"",
                                filePath
                        )
                );
            }
        }
    }

    public void save() throws IOException {
        save(filePath, false);
    }

    public void save(String path, boolean format) throws IOException {
        var gson = new Gson();

        if (format) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
        }

        var presentation = new StoredData(balances, openedTransfers, freeOperationId);

        var serialized = gson.toJson(presentation);

        Files.writeString(Path.of(path), serialized, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public MoneyQuantity getBalance(BankCard card) {
        return balances.getOrDefault(card, new MoneyQuantity(0, "RUB"));
    }

    public void setBalance(BankCard card, MoneyQuantity balance) {
        balances.put(card, balance);

        saveIfNeeded();
    }

    public BankCard getCard(String number) {
        return balances.keySet()
                .stream()
                .filter(c -> c.getNumber().equals(number))
                .findAny()
                .orElse(null);
    }

    public String saveTransfer(Transfer transfer) {
        var id = freeOperationId.toString();

        freeOperationId = freeOperationId.add(new BigInteger("1"));

        openedTransfers.put(id, transfer);

        saveIfNeeded();

        return id;
    }

    public void confirmTransfer(String operationId) throws OperationNotFoundById {
        var transfer = openedTransfers.remove(operationId);

        if (transfer == null) {
            throw new OperationNotFoundById(operationId);
        }

        var sourceBalance = getBalance(transfer.getSource());
        var destinationBalance = getBalance(transfer.getDestination());

        var delta = transfer.getAmount().getValue();

        sourceBalance.setValue(sourceBalance.getValue() - delta);
        destinationBalance.setValue(destinationBalance.getValue() + delta);

        saveIfNeeded();
    }

    public void cancelTransfer(String operationId) throws OperationNotFoundById {
        var transfer = openedTransfers.remove(operationId);

        if (transfer == null) {
            throw new OperationNotFoundById(operationId);
        }

        saveIfNeeded();
    }

    public Transfer getTransfer(String operationId) {
        return openedTransfers.getOrDefault(operationId, null);
    }

    public Transfer getTransfer(BankCard source) {
        return openedTransfers.values()
                .stream()
                .filter(t -> t.getSource().equals(source))
                .findAny()
                .orElse(null);
    }
}
