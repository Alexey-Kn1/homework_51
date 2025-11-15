package ru.netology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.netology.model.*;
import ru.netology.repository.MoneyRepository;
import ru.netology.repository.OperationNotFoundById;

@Service
public class MoneyTransferService {
    private static final Logger log = LoggerFactory.getLogger(MoneyTransferService.class);
    private final MoneyRepository repo;
    private final Environment env;

    public MoneyTransferService(MoneyRepository repo, Environment env) {
        this.repo = repo;
        this.env = env;
    }

    public ResponseEntity<MoneyTransferResponse> createTransfer(MoneyTransferRequest request) {
        var cardSource = new BankCard(request.getCardFromNumber(), request.getCardFromValidTill(), request.getCardFromCVV());

        var existingTransfer = repo.getTransfer(cardSource);

        if (existingTransfer != null) {
            throw new WrongInputDataException("Not approved transfer from the same card found", 1);
        }

        var sourceCardBalance = repo.getBalance(cardSource);

        if (!sourceCardBalance.getCurrency().equals(request.getAmount().getCurrency())) {
            throw new WrongInputDataException(
                    String.format(
                            "Source card has different currency: \"%s\"; requested: \"%s\"",
                            sourceCardBalance.getCurrency(),
                            request.getAmount().getCurrency()
                    ),
                    2
            );
        }

        var cardDestination = repo.getCard(request.getCardToNumber());

        var destinationCardBalance = repo.getBalance(cardDestination);

        if (!destinationCardBalance.getCurrency().equals(request.getAmount().getCurrency())) {
            throw new WrongInputDataException(
                    String.format(
                            "Destination card has different currency: \"%s\"; requested: \"%s\"",
                            destinationCardBalance.getCurrency(),
                            request.getAmount().getCurrency()
                    ),
                    3
            );
        }

        if (sourceCardBalance.getValue() < request.getAmount().getValue()) {
            throw new WrongInputDataException(
                    "Not enough balance",
                    4
            );
        }

        var responseData = new MoneyTransferResponse(
                repo.saveTransfer(
                        new Transfer(
                                cardSource,
                                cardDestination,
                                request.getAmount()
                        )
                )
        );

        ResponseEntity<MoneyTransferResponse> res = new ResponseEntity<>(responseData, HttpStatus.OK);

        log.info(
                String.format(
                        "created transfer from card with number \"%s\" to card with number \"%s\"; amount: %d %s; transfer ID: \"%s\"",
                        cardSource.getNumber(),
                        cardDestination.getNumber(),
                        request.getAmount().getValue(),
                        request.getAmount().getCurrency(),
                        responseData.getOperationId()
                )
        );

        return res;
    }

    public synchronized ResponseEntity<OperationConfirmationResponse> confirmTransfer(OperationConfirmationRequest request) {
        if (!request.getCode().equals(env.getProperty("transfers_verification_code", ""))) {
            throw new WrongInputDataException(
                    "Wrong code",
                    1
            );
        }

        try {
            repo.confirmTransfer(request.getOperationId());
        } catch (OperationNotFoundById e) {
            throw new WrongInputDataException(
                    "Operation not found by ID",
                    2
            );
        }

        ResponseEntity<OperationConfirmationResponse> res = new ResponseEntity<>(
                new OperationConfirmationResponse(request.getOperationId()),
                HttpStatus.OK
        );

        log.info(
                String.format(
                        "transfer with operation ID \"%s\" successfully confirmed",
                        request.getOperationId()
                )
        );

        return res;
    }
}
