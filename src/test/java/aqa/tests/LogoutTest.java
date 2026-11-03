package aqa.tests;

import aqa.data.Tokens;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static aqa.checks.ResponseChecks.assertError;
import static aqa.checks.ResponseChecks.assertOk;
import static aqa.mock.ExternalServiceMock.DO_ACTION;

@Epic("Сервис действий пользователя")
@Feature("Выход (LOGOUT)")
@DisplayName("Выход (LOGOUT)")
class LogoutTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("После выхода действие становится недоступным")
    void actionIsRejectedAfterLogout() {
        String token = Tokens.valid();
        userIsLoggedIn(token);

        assertOk(app.logout(token));

        externalService.verifyNoRequests();
        verifyTokenIsNotLoggedIn(token);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Выход без предварительного входа возвращает ошибку")
    void logoutWithoutLoginIsRejected() {
        assertError(app.logout(Tokens.valid()));

        externalService.verifyNoRequests();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("После выхода можно снова войти и выполнить действие")
    void userCanLoginAgainAfterLogout() {
        String token = Tokens.valid();
        userIsLoggedIn(token);
        assertOk(app.logout(token));

        userIsLoggedIn(token);
        externalService.doActionRespondsWith(200);

        assertOk(app.action(token));
        externalService.verifyCalledOnce(DO_ACTION, token);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Выход одного пользователя не затрагивает другого")
    void logoutOfOneUserDoesNotAffectAnother() {
        String firstToken = Tokens.valid();
        String secondToken = Tokens.valid();
        userIsLoggedIn(firstToken);
        userIsLoggedIn(secondToken);

        assertOk(app.logout(firstToken));

        externalService.doActionRespondsWith(200);
        assertOk(app.action(secondToken));
        externalService.verifyCalledOnce(DO_ACTION, secondToken);
    }
}
