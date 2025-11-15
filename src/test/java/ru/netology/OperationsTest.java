package ru.netology;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.netology.model.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
                "data_file_path=./src/test/resources/data.json"
        }
)
public class OperationsTest {
    public static final String PREPARED_DATA_FILE_PATH = "./src/test/resources/data_prepared_for_transfer_test.json";

    private static Environment env;
    private static MockMvc mvc;

    public OperationsTest(@Autowired Environment env, @Autowired MockMvc mvc) {
        OperationsTest.env = env;
        OperationsTest.mvc = mvc;
    }

    @AfterAll
    public static void cleanup() throws Exception {
        String tmpDataPath = env.getProperty("data_file_path");

        try {
            Files.delete(Path.of(tmpDataPath));
        } catch (Exception ignored) {

        }

        Files.copy(Path.of(PREPARED_DATA_FILE_PATH), Path.of(tmpDataPath));
    }

    @Test
    public void checkOperations() throws Exception {
        String code = env.getProperty("transfers_verification_code");

        Gson gson = new Gson();

        var transfer = new MoneyTransferRequest(
                "0000-0000-0000-0000",
                "03/23",
                "000",
                "0000-0000-0000-0001",
                new MoneyQuantity(
                        500,
                        "RUB"
                )
        );

        var responseBody = mvc.perform(
                        post("/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(transfer))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Existing operation on this card found.
        mvc.perform(
                        post("/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(transfer))
                )
                .andExpect(status().isBadRequest());

        var transferCreationResponse = gson.fromJson(responseBody, MoneyTransferResponse.class);

        var confirmation = new OperationConfirmationRequest(transferCreationResponse.getOperationId(), code + "ЫЫЫЫЫ"); // Wrong code.

        mvc.perform(
                        post("/confirmOperation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(confirmation))
                )
                .andExpect(status().isBadRequest());

        confirmation = new OperationConfirmationRequest(transferCreationResponse.getOperationId(), code);

        responseBody = mvc.perform(
                        post("/confirmOperation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(confirmation))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var confirmationResponse = gson.fromJson(responseBody, OperationConfirmationResponse.class);

        Assertions.assertEquals(confirmationResponse.getOperationId(), transferCreationResponse.getOperationId());

        // Operation has already been confirmed.
        mvc.perform(
                        post("/confirmOperation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(confirmation))
                )
                .andExpect(status().isBadRequest());

        // // Check that balance decreased on the card with number "0000-0000-0000-0000".
        mvc.perform(
                        post("/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(transfer))
                )
                .andExpect(status().isBadRequest());

        transfer = new MoneyTransferRequest(
                "0000-0000-0000-0001",
                "03/23",
                "000",
                "0000-0000-0000-0000",
                new MoneyQuantity(
                        1000,
                        "RUB"
                )
        );

        // Check that balance increased on the card with number "0000-0000-0000-0001".
        responseBody = mvc.perform(
                        post("/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(transfer))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        transferCreationResponse = gson.fromJson(responseBody, MoneyTransferResponse.class);

        confirmation = new OperationConfirmationRequest(transferCreationResponse.getOperationId(), code);

        responseBody = mvc.perform(
                        post("/confirmOperation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(confirmation))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        confirmationResponse = gson.fromJson(responseBody, OperationConfirmationResponse.class);

        Assertions.assertEquals(confirmationResponse.getOperationId(), transferCreationResponse.getOperationId());
    }
}
