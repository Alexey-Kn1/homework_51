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
        ResponseEntity<MoneyTransferResponse> res;
        BankCard cardSource;
        BankCard cardDestination;
        MoneyTransferResponse responseData;

        synchronized (repo) {
            cardSource = new BankCard(request.cardFromNumber(), request.cardFromValidTill(), request.cardFromCVV());

            var existingTransfer = repo.getTransfer(cardSource);

            if (existingTransfer != null) {
                throw new WrongInputDataException("Not approved transfer from the same card found", 1);
            }

            var sourceCardBalance = repo.getBalance(cardSource);

            if (!sourceCardBalance.getCurrency().equals(request.amount().getCurrency())) {
                throw new WrongInputDataException(
                        String.format(
                                "Source card has different currency: \"%s\"; requested: \"%s\"",
                                sourceCardBalance.getCurrency(),
                                request.amount().getCurrency()
                        ),
                        2
                );
            }

            cardDestination = repo.getCard(request.cardToNumber());

            var destinationCardBalance = repo.getBalance(cardDestination);

            if (!destinationCardBalance.getCurrency().equals(request.amount().getCurrency())) {
                throw new WrongInputDataException(
                        String.format(
                                "Destination card has different currency: \"%s\"; requested: \"%s\"",
                                destinationCardBalance.getCurrency(),
                                request.amount().getCurrency()
                        ),
                        3
                );
            }

            if (sourceCardBalance.getValue() < request.amount().getValue()) {
                throw new WrongInputDataException(
                        "Not enough balance",
                        4
                );
            }

            responseData = new MoneyTransferResponse(
                    repo.saveTransfer(
                            new Transfer(
                                    cardSource,
                                    cardDestination,
                                    request.amount()
                            )
                    )
            );

            res = new ResponseEntity<>(responseData, HttpStatus.OK);
        }

        log.info(
                String.format(
                        "created transfer from card with number \"%s\" to card with number \"%s\"; amount: %d %s; transfer ID: \"%s\"",
                        cardSource.number(),
                        cardDestination.number(),
                        request.amount().getValue(),
                        request.amount().getCurrency(),
                        responseData.operationId()
                )
        );

        return res;
    }

    public synchronized ResponseEntity<OperationConfirmationResponse> confirmTransfer(OperationConfirmationRequest request) {
        ResponseEntity<OperationConfirmationResponse> res;

        synchronized (repo) {
            if (!request.code().equals(env.getProperty("transfers_verification_code", ""))) {
                throw new WrongInputDataException(
                        "Wrong code",
                        1
                );
            }

            try {
                repo.confirmTransfer(request.operationId());
            } catch (OperationNotFoundById e) {
                throw new WrongInputDataException(
                        "Operation not found by ID",
                        2
                );
            }

            res = new ResponseEntity<>(
                    new OperationConfirmationResponse(request.operationId()),
                    HttpStatus.OK
            );
        }

        log.info(
                String.format(
                        "transfer with operation ID \"%s\" successfully confirmed",
                        request.operationId()
                )
        );

        return res;
    }
}
