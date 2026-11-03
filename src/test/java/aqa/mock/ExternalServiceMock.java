package aqa.mock;

import aqa.environment.TestEnvironment;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Заглушка внешнего сервиса. Тест сам решает, что она ответит,
 * и потом проверяет, какие запросы к ней пришли от приложения.
 */
public class ExternalServiceMock {

    public static final String AUTH = "/auth";
    public static final String DO_ACTION = "/doAction";

    private WireMockServer server() {
        return TestEnvironment.wireMock();
    }

    @Step("Внешний сервис будет отвечать на /auth кодом {status}")
    public void authRespondsWith(int status) {
        stub(AUTH, status);
    }

    @Step("Внешний сервис будет отвечать на /doAction кодом {status}")
    public void doActionRespondsWith(int status) {
        stub(DO_ACTION, status);
    }

    @Step("Внешний сервис будет отвечать на /auth с задержкой {delayMillis} мс")
    public void authRespondsWithDelay(int delayMillis) {
        server().stubFor(post(urlPathEqualTo(AUTH))
                .willReturn(aResponse().withStatus(200).withFixedDelay(delayMillis)));
    }

    @Step("Внешний сервис будет обрывать соединение на запрос /auth")
    public void authDropsConnection() {
        server().stubFor(post(urlPathEqualTo(AUTH))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
    }

    private void stub(String path, int status) {
        server().stubFor(post(urlPathEqualTo(path)).willReturn(aResponse().withStatus(status)));
    }

    @Step("Проверить: внешний сервис получил ровно один запрос {path} с токеном {token}")
    public void verifyCalledOnce(String path, String token) {
        attachReceivedRequests();
        List<LoggedRequest> requests = server().findAll(anyRequestedFor(urlPathEqualTo(path)));
        assertThat(requests).as("Количество запросов на " + path).hasSize(1);

        LoggedRequest request = requests.get(0);
        assertThat(request.getMethod().getName()).as("HTTP-метод запроса").isEqualTo("POST");
        assertThat(request.getHeader("Content-Type")).as("Заголовок Content-Type")
                .startsWith("application/x-www-form-urlencoded");
        assertThat(request.getHeader("Accept")).as("Заголовок Accept").contains("application/json");
        assertThat(request.getBodyAsString()).as("Тело запроса").isEqualTo("token=" + token);
    }

    @Step("Проверить: внешний сервис не получал запросов на {path}")
    public void verifyNotCalled(String path) {
        attachReceivedRequests();
        assertThat(server().findAll(anyRequestedFor(urlPathEqualTo(path))))
                .as("Запросы на " + path).isEmpty();
    }

    @Step("Проверить: во внешний сервис не ушло ни одного запроса")
    public void verifyNoRequests() {
        attachReceivedRequests();
        assertThat(server().getAllServeEvents()).as("Запросы во внешний сервис").isEmpty();
    }

    /** Очищает журнал полученных запросов (настройки ответов сохраняются). */
    public void clearReceivedRequests() {
        server().resetRequests();
    }

    private void attachReceivedRequests() {
        String text = server().getAllServeEvents().stream()
                .map(ServeEvent::getRequest)
                .map(r -> r.getMethod() + " " + r.getUrl() + "\n" + r.getHeaders() + "\n" + r.getBodyAsString())
                .collect(Collectors.joining("\n----------\n"));
        Allure.addAttachment("Запросы, полученные внешним сервисом", "text/plain",
                text.isEmpty() ? "Запросов не было" : text);
    }
}
