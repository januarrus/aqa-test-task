package aqa.client;

import aqa.config.TestConfig;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Отправляет запросы в тестируемое приложение (POST /endpoint).
 * Каждый запрос и ответ автоматически прикладывается к отчёту Allure.
 */
public class AppClient {

    public static final String LOGIN = "LOGIN";
    public static final String ACTION = "ACTION";
    public static final String LOGOUT = "LOGOUT";

    private static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
            .setBaseUri(TestConfig.APP_URL)
            .setBasePath("/endpoint")
            .setConfig(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig()
                    .defaultContentCharset("UTF-8")
                    .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
            .setContentType(ContentType.URLENC)
            .setAccept(ContentType.JSON)
            .addFilter(new AllureRestAssured()
                    .setRequestAttachmentName("Запрос к приложению")
                    .setResponseAttachmentName("Ответ приложения"))
            .build();

    @Step("Вход (LOGIN) с токеном {token}")
    public Response login(String token) {
        return post(TestConfig.API_KEY, params(token, LOGIN));
    }

    @Step("Действие (ACTION) с токеном {token}")
    public Response action(String token) {
        return post(TestConfig.API_KEY, params(token, ACTION));
    }

    @Step("Выход (LOGOUT) с токеном {token}")
    public Response logout(String token) {
        return post(TestConfig.API_KEY, params(token, LOGOUT));
    }

    @Step("Запрос с action = «{action}» и токеном «{token}»")
    public Response send(String token, String action) {
        return post(TestConfig.API_KEY, params(token, action));
    }

    @Step("Запрос с API-ключом «{apiKey}»: action = {action}")
    public Response sendWithApiKey(String apiKey, String token, String action) {
        return post(apiKey, params(token, action));
    }

    @Step("Запрос без заголовка X-Api-Key: action = {action}")
    public Response sendWithoutApiKey(String token, String action) {
        return post(null, params(token, action));
    }

    @Step("Запрос без параметра token: action = {action}")
    public Response sendWithoutToken(String action) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", action);
        return post(TestConfig.API_KEY, params);
    }

    @Step("Запрос без параметра action: токен {token}")
    public Response sendWithoutAction(String token) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("token", token);
        return post(TestConfig.API_KEY, params);
    }

    private static Map<String, String> params(String token, String action) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("token", token);
        params.put("action", action);
        return params;
    }

    private Response post(String apiKey, Map<String, String> formParams) {
        RequestSpecification request = given().spec(BASE_SPEC).formParams(formParams);
        if (apiKey != null) {
            request.header("X-Api-Key", apiKey);
        }
        return request.when().post().then().extract().response();
    }
}
