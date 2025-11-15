package ru.netology;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;
import ru.netology.model.*;

import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContainerTest {
    private static final String DEFAULT_VERIFICATION_CODE = "123456";
    private static final String DEFAULT_DATA_FILE_PATH = "/data.json";
    private static GenericContainer<?> appContainer;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    public static void prepareContainer() throws Exception {
        var image = new ImageFromDockerfile()
                .withDockerfile(Path.of("./Dockerfile"));

        appContainer = new GenericContainer<>(image)
                .withExposedPorts(5500)
                .withCopyToContainer(
                        MountableFile.forClasspathResource("data_prepared_for_transfer_test.json"),
                        DEFAULT_DATA_FILE_PATH
                );

        appContainer.start();
    }

    @Test
    public void checkOperations() {
        final String addr = "http://localhost:" + appContainer.getMappedPort(5500);
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

        var transferRespSuccess = restTemplate.postForEntity(addr + "/transfer", transfer, MoneyTransferResponse.class);

        Assertions.assertEquals(HttpStatus.OK, transferRespSuccess.getStatusCode());

        var transferCreationResponse = transferRespSuccess.getBody();

        // Existing operation on this card found.
        var transferRespErr = restTemplate.postForEntity(addr + "/transfer", transfer, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, transferRespErr.getStatusCode());

        var confirmation = new OperationConfirmationRequest(transferCreationResponse.getOperationId(), DEFAULT_VERIFICATION_CODE + "ЫЫЫЫЫ"); // Wrong code.

        var confirmRespErr = restTemplate.postForEntity(addr + "/confirmOperation", confirmation, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, confirmRespErr.getStatusCode());

        confirmation = new OperationConfirmationRequest(transferCreationResponse.getOperationId(), DEFAULT_VERIFICATION_CODE);

        var confirmRespSuccess = restTemplate.postForEntity(addr + "/confirmOperation", confirmation, OperationConfirmationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, confirmRespSuccess.getStatusCode());

        Assertions.assertEquals(transferRespSuccess.getStatusCode(), confirmRespSuccess.getStatusCode());

        // Operation has already been confirmed.
        confirmRespErr = restTemplate.postForEntity(addr + "/confirmOperation", confirmation, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, confirmRespErr.getStatusCode());

        // Check that balance decreased on the card with number "0000-0000-0000-0000".
        transferRespErr = restTemplate.postForEntity(addr + "/transfer", transfer, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, transferRespErr.getStatusCode());

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
        transferRespSuccess = restTemplate.postForEntity(addr + "/transfer", transfer, MoneyTransferResponse.class);

        Assertions.assertEquals(HttpStatus.OK, transferRespSuccess.getStatusCode());

        confirmation = new OperationConfirmationRequest(transferRespSuccess.getBody().getOperationId(), DEFAULT_VERIFICATION_CODE);

        confirmRespSuccess = restTemplate.postForEntity(addr + "/confirmOperation", confirmation, OperationConfirmationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, confirmRespSuccess.getStatusCode());

        Assertions.assertEquals(transferRespSuccess.getBody().getOperationId(), confirmRespSuccess.getBody().getOperationId());
    }
}
