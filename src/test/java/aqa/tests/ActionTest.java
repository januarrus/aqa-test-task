package aqa.tests;

import aqa.data.Tokens;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static aqa.checks.ResponseChecks.assertError;
import static aqa.checks.ResponseChecks.assertOk;
import static aqa.mock.ExternalServiceMock.DO_ACTION;

@Epic("Сервис действий пользователя")
@Feature("Действие (ACTION)")
@DisplayName("Действие (ACTION)")
class ActionTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Действие выполняется после успешного входа")
    void actionSucceedsAfterLogin() {
        String token = Tokens.valid();
        userIsLoggedIn(token);
        externalService.doActionRespondsWith(200);

        assertOk(app.action(token));

        externalService.verifyCalledOnce(DO_ACTION, token);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Действие недоступно без предварительного входа")
    void actionIsRejectedWithoutLogin() {
        String token = Tokens.valid();
        externalService.doActionRespondsWith(200);

        assertError(app.action(token));

        externalService.verifyNoRequests();
    }

    @ParameterizedTest(name = "Действие не выполнено, если внешний сервис ответил кодом {0}")
    @ValueSource(ints = {400, 404, 500, 503})
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Действие не выполнено, если внешний сервис ответил ошибкой")
    void actionFailsWhenExternalServiceRejects(int status) {
        String token = Tokens.valid();
        userIsLoggedIn(token);
        externalService.doActionRespondsWith(status);

        assertError(app.action(token));

        externalService.verifyCalledOnce(DO_ACTION, token);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("После ошибки внешнего сервиса пользователь остаётся в системе и может повторить действие")
    void sessionSurvivesExternalServiceError() {
        String token = Tokens.valid();
        userIsLoggedIn(token);
        externalService.doActionRespondsWith(500);
        assertError(app.action(token));

        externalService.doActionRespondsWith(200);
        externalService.clearReceivedRequests();

        assertOk(app.action(token));
        externalService.verifyCalledOnce(DO_ACTION, token);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Вход одного пользователя не даёт права на действие другому")
    void loginOfOneUserDoesNotAuthorizeAnother() {
        String loggedInToken = Tokens.valid();
        String otherToken = Tokens.valid();
        userIsLoggedIn(loggedInToken);

        verifyTokenIsNotLoggedIn(otherToken);
    }
}
