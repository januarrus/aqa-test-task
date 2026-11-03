package aqa.tests;

import aqa.client.AppClient;
import aqa.environment.ApplicationExtension;
import aqa.mock.ExternalServiceMock;
import io.qameta.allure.Step;
import org.junit.jupiter.api.extension.ExtendWith;

import static aqa.checks.ResponseChecks.assertError;
import static aqa.checks.ResponseChecks.assertOk;
import static aqa.mock.ExternalServiceMock.DO_ACTION;

@ExtendWith(ApplicationExtension.class)
public abstract class BaseTest {

    protected final AppClient app = new AppClient();
    protected final ExternalServiceMock externalService = new ExternalServiceMock();

    @Step("Предусловие: пользователь с токеном {token} успешно вошёл")
    protected void userIsLoggedIn(String token) {
        externalService.authRespondsWith(200);
        assertOk(app.login(token));
        externalService.clearReceivedRequests();
    }

    @Step("Проверить: токен {token} не авторизован — действие (ACTION) отклоняется")
    protected void verifyTokenIsNotLoggedIn(String token) {
        externalService.doActionRespondsWith(200);
        externalService.clearReceivedRequests();
        assertError(app.action(token));
        externalService.verifyNotCalled(DO_ACTION);
    }
}
