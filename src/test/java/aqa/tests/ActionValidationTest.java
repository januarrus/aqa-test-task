package aqa.tests;

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
import static aqa.checks.ResponseChecks.assertStatus;

@Epic("Сервис действий пользователя")
@Feature("Проверка параметра action")
@DisplayName("Проверка параметра action")
class ActionValidationTest extends BaseTest {

    static Stream<Arguments> invalidActions() {
        return Stream.of(
                Arguments.of("неизвестное действие", "UNKNOWN"),
                Arguments.of("строчными буквами", "login"),
                Arguments.of("смешанный регистр", "Login"),
                Arguments.of("пустое значение", ""));
    }

    @ParameterizedTest(name = "Неизвестное действие отклоняется: {0}")
    @MethodSource("invalidActions")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Неизвестное действие отклоняется")
    void invalidActionIsRejected(String caseName, String action) {
        externalService.authRespondsWith(200);
        externalService.doActionRespondsWith(200);

        Response response = app.send(Tokens.valid(), action);

        assertError(response);
        assertStatus(response, 400);
        externalService.verifyNoRequests();
    }

    static Stream<Arguments> actionsWithSpaces() {
        return Stream.of(
                Arguments.of("пробел в начале", " LOGIN"),
                Arguments.of("пробел в конце", "LOGIN "));
    }

    @ParameterizedTest(name = "Действие с лишними пробелами отклоняется: {0}")
    @MethodSource("actionsWithSpaces")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Действие с лишними пробелами отклоняется")
    @Description("По документации допустимы ровно три значения action. "
            + "Приложение молча обрезает пробелы и выполняет действие (дефект BUG-4 в README).")
    void actionWithSpacesIsRejected(String caseName, String action) {
        externalService.authRespondsWith(200);

        Response response = app.send(Tokens.valid(), action);

        assertError(response);
        externalService.verifyNoRequests();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Запрос без параметра action отклоняется")
    void missingActionIsRejected() {
        externalService.authRespondsWith(200);
        externalService.doActionRespondsWith(200);

        Response response = app.sendWithoutAction(Tokens.valid());

        assertError(response);
        assertStatus(response, 400);
        externalService.verifyNoRequests();
    }
}
