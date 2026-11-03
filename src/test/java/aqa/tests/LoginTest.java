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
import org.junit.jupiter.params.provider.ValueSource;

import static aqa.checks.ResponseChecks.assertError;
import static aqa.checks.ResponseChecks.assertMessageContains;
import static aqa.checks.ResponseChecks.assertOk;
import static aqa.mock.ExternalServiceMock.AUTH;
import static aqa.mock.ExternalServiceMock.DO_ACTION;

@Epic("Сервис действий пользователя")
@Feature("Вход (LOGIN)")
@DisplayName("Вход (LOGIN)")
class LoginTest extends BaseTest {

    @ParameterizedTest(name = "Успешный вход, если внешний сервис ответил кодом {0}")
    @ValueSource(ints = {200, 201, 204})
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешный вход, если внешний сервис подтвердил токен")
    void loginSucceedsWhenExternalServiceConfirms(int status) {
        String token = Tokens.valid();
        externalService.authRespondsWith(status);

        assertOk(app.login(token));

        externalService.verifyCalledOnce(AUTH, token);
        externalService.verifyNotCalled(DO_ACTION);
    }

    @ParameterizedTest(name = "Вход не выполнен, если внешний сервис ответил кодом {0}")
    @ValueSource(ints = {400, 401, 403, 404, 500, 502, 503})
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Вход не выполнен, если внешний сервис ответил ошибкой")
    void loginFailsWhenExternalServiceRejects(int status) {
        String token = Tokens.valid();
        externalService.authRespondsWith(status);

        assertError(app.login(token));

        externalService.verifyCalledOnce(AUTH, token);
        verifyTokenIsNotLoggedIn(token);
    }

    @ParameterizedTest(name = "Вход не выполнен, если внешний сервис ответил перенаправлением {0}")
    @ValueSource(ints = {301, 302})
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Вход не выполнен, если внешний сервис ответил перенаправлением")
    @Description("Ответ 3xx не подтверждает токен, значит вход не должен считаться успешным. "
            + "Приложение считает такой вход успешным (дефект BUG-2 в README).")
    void loginFailsWhenExternalServiceRedirects(int status) {
        String token = Tokens.valid();
        externalService.authRespondsWith(status);

        assertError(app.login(token));

        verifyTokenIsNotLoggedIn(token);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Вход не выполнен, если внешний сервис не ответил вовремя")
    @Description("Внешний сервис отвечает через 12 секунд. Приложение ждёт около 10 секунд и возвращает ошибку.")
    void loginFailsWhenExternalServiceTimesOut() {
        String token = Tokens.valid();
        externalService.authRespondsWithDelay(12_000);

        assertError(app.login(token));

        verifyTokenIsNotLoggedIn(token);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Вход не выполнен, если соединение с внешним сервисом оборвалось")
    void loginFailsWhenConnectionDropped() {
        String token = Tokens.valid();
        externalService.authDropsConnection();

        assertError(app.login(token));

        verifyTokenIsNotLoggedIn(token);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Повторный вход уже вошедшего пользователя отклоняется без обращения к внешнему сервису")
    void repeatedLoginIsRejected() {
        String token = Tokens.valid();
        userIsLoggedIn(token);

        Response response = app.login(token);

        assertError(response);
        assertMessageContains(response, "already exists");
        externalService.verifyNoRequests();
    }
}
