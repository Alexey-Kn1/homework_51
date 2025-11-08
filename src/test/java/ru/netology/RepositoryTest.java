package ru.netology;

import org.junit.jupiter.api.Test;
import ru.netology.model.BankCard;
import ru.netology.model.MoneyQuantity;
import ru.netology.model.Transfer;
import ru.netology.repository.MoneyRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RepositoryTest {
    private static final String STORED_DATA_FILE_PATH = "./src/test/resources/serialized_repository_data.json";

    @Test
    public void checkRepository() throws IOException {
        var r = new MoneyRepository(STORED_DATA_FILE_PATH);

        r.setSaveOnEachOperation(false);

        r.setBalance(
                new BankCard("0000-0000-0000-0000", "00/00", "000"),
                new MoneyQuantity(0, "RUB")
        );

        var transferId = r.saveTransfer(
                new Transfer(
                        new BankCard("0000-0000-0000-0000", "00/00", "000"),
                        new BankCard("0000-0000-0000-0000", "00/00", "000"),
                        new MoneyQuantity(0, "RUB")
                )
        );

        r.save(STORED_DATA_FILE_PATH, true);

        r = new MoneyRepository(STORED_DATA_FILE_PATH);

        r.setSaveOnEachOperation(false);

        r.cancelTransfer(transferId);

        Files.delete((Path.of(STORED_DATA_FILE_PATH)));
    }
}
