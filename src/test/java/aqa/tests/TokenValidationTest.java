package aqa.tests;

import aqa.client.AppClient;
import aqa.data.Tokens;
import io.qameta.allure.Description;
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
import static aqa.checks.ResponseChecks.assertOk;
import static aqa.checks.ResponseChecks.assertStatus;
import static aqa.mock.ExternalServiceMock.AUTH;

@Epic("Сервис действий пользователя")
@Feature("Проверка формата токена")
@DisplayName("Проверка формата токена")
class TokenValidationTest extends BaseTest {

    static Stream<Arguments> invalidTokens() {
        return Stream.of(
                Arguments.of("31 символ — на один короче", Tokens.random(31)),
                Arguments.of("33 символа — на один длиннее", Tokens.random(33)),
                Arguments.of("есть строчная латинская буква", Tokens.random(31) + "a"),
                Arguments.of("все буквы строчные", Tokens.random(Tokens.DIGITS + "abcdef", 32)),
                Arguments.of("есть спецсимвол", Tokens.random(31) + "!"),
                Arguments.of("есть дефис", Tokens.random(31) + "-"),
                Arguments.of("есть пробел", Tokens.random(16) + " " + Tokens.random(15)),
                Arguments.of("есть кириллица", Tokens.random(31) + "Ж"),
                Arguments.of("пустой токен", ""));
    }

    @ParameterizedTest(name = "Токен неверного формата отклоняется: {0}")
    @MethodSource("invalidTokens")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен неверного формата отклоняется")
    void invalidTokenIsRejected(String caseName, String token) {
        // Внешний сервис готов ответить успехом: если приложение пропустит
        // неверный токен, тест это поймает.
        externalService.authRespondsWith(200);

        Response response = app.login(token);

        assertError(response);
        assertStatus(response, 400);
        externalService.verifyNoRequests();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Запрос без токена отклоняется")
    void missingTokenIsRejected() {
        externalService.authRespondsWith(200);

        Response response = app.sendWithoutToken(AppClient.LOGIN);

        assertError(response);
        assertStatus(response, 400);
        externalService.verifyNoRequests();
    }

    static Stream<Arguments> validTokens() {
        return Stream.of(
                Arguments.of("только цифры", Tokens.random(Tokens.DIGITS, 32)),
                Arguments.of("только буквы A–F", Tokens.random(Tokens.LETTERS_A_TO_F, 32)),
                Arguments.of("только буквы G–Z", Tokens.random(Tokens.LETTERS_G_TO_Z, 32)),
                Arguments.of("весь алфавит A–Z", "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + Tokens.random(Tokens.DIGITS, 6)),
                Arguments.of("цифры и любые заглавные буквы", Tokens.random(Tokens.DIGITS + Tokens.LETTERS, 32)));
    }

    @ParameterizedTest(name = "Токен, корректный по документации, принимается: {0}")
    @MethodSource("validTokens")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен, корректный по документации, принимается")
    @Description("По документации токен — 32 символа из A-Z и 0-9. "
            + "Варианты с буквами G–Z падают: приложение принимает только буквы A–F (дефект BUG-1 в README).")
    void documentedValidTokenIsAccepted(String caseName, String token) {
        externalService.authRespondsWith(200);

        assertOk(app.login(token));

        externalService.verifyCalledOnce(AUTH, token);
    }
}
