package ru.netology.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.model.*;
import ru.netology.service.MoneyTransferService;

@RestController
@RequestMapping("/")
public class MoneyTransferController {
    private final MoneyTransferService service;

    public MoneyTransferController(MoneyTransferService service) {
        this.service = service;
    }

    @PostMapping("transfer")
    public ResponseEntity<MoneyTransferResponse> executeTransfer(@Valid @RequestBody MoneyTransferRequest request) {
        return service.createTransfer(request);
    }

    @PostMapping("confirmOperation")
    public ResponseEntity<OperationConfirmationResponse> confirmOperation(@Valid @RequestBody OperationConfirmationRequest request) {
        return service.confirmTransfer(request);
    }
}
