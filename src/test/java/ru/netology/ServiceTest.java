package ru.netology;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.netology.model.*;
import ru.netology.repository.MoneyRepository;
import ru.netology.service.MoneyTransferService;

@SpringBootTest
public class ServiceTest {
    private static final BankCard CARD_A = new BankCard(
            "0000-0000-0000-0000",
            "11/23",
            "123"
    );

    private static final BankCard CARD_B = new BankCard(
            "0000-0000-0000-0001",
            "11/23",
            "123"
    );

    @Autowired
    private Environment env;

    @Test
    public void checkTransfer() {
        var repoMock = prepareRepositoryMock();

        var service = new MoneyTransferService(repoMock, env);

        var respTransfer = service.createTransfer(
                new MoneyTransferRequest(
                        CARD_A.getNumber(),
                        CARD_A.getValidUntil(),
                        CARD_A.getCvv(),
                        CARD_B.getNumber(),
                        new MoneyQuantity(
                                500,
                                "RUB"
                        )
                )
        );

        Assertions.assertEquals(HttpStatus.OK, respTransfer.getStatusCode());

        Assertions.assertInstanceOf(MoneyTransferResponse.class, respTransfer.getBody());

        Mockito.verify(
                        repoMock,
                        Mockito.times(1)
                )
                .getBalance(CARD_A);

        Mockito.verify(
                        repoMock,
                        Mockito.times(1)
                )
                .getTransfer(CARD_A);

        Mockito.verify(
                        repoMock,
                        Mockito.times(0)
                )
                .confirmTransfer(Mockito.any(String.class));

        ResponseEntity<OperationConfirmationResponse> respConfirm;

        try {
            service.confirmTransfer(
                    new OperationConfirmationRequest(
                            ((MoneyTransferResponse) respTransfer.getBody()).getOperationId(),
                            "INVALID_CODE"
                    )
            );

            Assertions.fail("transfer should not be confirmed by request with wrong confirmation code");
        } catch (WrongInputDataException e) {
            Assertions.assertEquals(1, e.getId());
        }

        respConfirm = service.confirmTransfer(
                new OperationConfirmationRequest(
                        ((MoneyTransferResponse) respTransfer.getBody()).getOperationId(),
                        env.getProperty("transfers_verification_code")
                )
        );

        Assertions.assertEquals(HttpStatus.OK, respConfirm.getStatusCode());

        Assertions.assertInstanceOf(OperationConfirmationResponse.class, respConfirm.getBody());

        try {
            service.createTransfer(
                    new MoneyTransferRequest(
                            CARD_A.getNumber(),
                            CARD_A.getValidUntil(),
                            CARD_A.getCvv(),
                            CARD_B.getNumber(),
                            new MoneyQuantity(
                                    501,
                                    "RUB"
                            )
                    )
            );

            Assertions.fail("transfer should not be created without enough balance");
        } catch (WrongInputDataException e) {
            Assertions.assertEquals(4, e.getId());
        }
    }

    private static MoneyRepository prepareRepositoryMock() {
        var mock = Mockito.mock(MoneyRepository.class);

        Mockito.when(mock.getCard(CARD_A.getNumber()))
                .thenReturn(CARD_A);

        Mockito.when(mock.getCard(CARD_B.getNumber()))
                .thenReturn(CARD_B);

        Mockito.when(mock.getTransfer(CARD_A))
                .thenReturn(null);

        Mockito.when(mock.getTransfer(CARD_B))
                .thenReturn(null);

        Mockito.when(mock.getBalance(CARD_A))
                .thenReturn(
                        new MoneyQuantity(
                                500,
                                "RUB"
                        )
                );

        Mockito.when(mock.getBalance(CARD_B))
                .thenReturn(
                        new MoneyQuantity(
                                500,
                                "RUB"
                        )
                );

        Mockito.when(
                        mock.saveTransfer(
                                Mockito.any(Transfer.class)
                        )
                )
                .thenReturn("0");

        return mock;
    }
}
