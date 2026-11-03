package aqa.checks;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверки ответа тестируемого приложения. */
public final class ResponseChecks {

    private ResponseChecks() {
    }

    @Step("Проверить: приложение ответило успехом (result = OK)")
    public static void assertOk(Response response) {
        assertThat(response.statusCode()).as("HTTP-код ответа приложения").isEqualTo(200);
        assertJson(response);
        assertThat(response.jsonPath().getString("result")).as("Поле result в ответе").isEqualTo("OK");
    }

    @Step("Проверить: приложение ответило ошибкой (result = ERROR) и указало причину")
    public static void assertError(Response response) {
        assertThat(response.statusCode()).as("HTTP-код ответа приложения (ожидается код ошибки 4xx/5xx)")
                .isBetween(400, 599);
        assertJson(response);
        assertThat(response.jsonPath().getString("result")).as("Поле result в ответе").isEqualTo("ERROR");
        assertThat(response.jsonPath().getString("message")).as("Причина ошибки (поле message)").isNotBlank();
    }

    @Step("Проверить: HTTP-код ответа приложения — {expectedStatus}")
    public static void assertStatus(Response response, int expectedStatus) {
        assertThat(response.statusCode()).as("HTTP-код ответа приложения").isEqualTo(expectedStatus);
    }

    @Step("Проверить: в причине ошибки упоминается «{expectedText}»")
    public static void assertMessageContains(Response response, String expectedText) {
        assertThat(response.jsonPath().getString("message")).as("Причина ошибки (поле message)")
                .contains(expectedText);
    }

    private static void assertJson(Response response) {
        assertThat(response.contentType()).as("Формат ответа (Content-Type)").contains("application/json");
    }
}
