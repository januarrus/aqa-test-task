package aqa.tests;

import aqa.client.AppClient;
import aqa.data.Tokens;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static aqa.checks.ResponseChecks.assertError;
import static aqa.checks.ResponseChecks.assertStatus;

@Epic("Сервис действий пользователя")
@Feature("Доступ по API-ключу")
@DisplayName("Доступ по API-ключу")
class ApiKeyTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Запрос без API-ключа отклоняется")
    void requestWithoutApiKeyIsRejected() {
        externalService.authRespondsWith(200);

        Response response = app.sendWithoutApiKey(Tokens.valid(), AppClient.LOGIN);

        assertError(response);
        assertStatus(response, 401);
        externalService.verifyNoRequests();
    }

    static Stream<Arguments> wrongApiKeys() {
        return Stream.of(
                Arguments.of("совсем другой ключ", "wrongKey123"),
                Arguments.of("ключ в другом регистре", "QAZwsxEDC"),
                Arguments.of("ключ с лишним символом", "qazWSXedc1"),
                Arguments.of("ключ без последнего символа", "qazWSXed"),
                Arguments.of("пустой ключ", ""));
    }

    @ParameterizedTest(name = "Запрос с неверным API-ключом отклоняется: {0}")
    @MethodSource("wrongApiKeys")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Запрос с неверным API-ключом отклоняется")
    void requestWithWrongApiKeyIsRejected(String caseName, String apiKey) {
        externalService.authRespondsWith(200);

        Response response = app.sendWithApiKey(apiKey, Tokens.valid(), AppClient.LOGIN);

        assertError(response);
        assertStatus(response, 401);
        externalService.verifyNoRequests();
    }
}
