package aqa.config;

/**
 * Все настройки в одном месте. Любую можно переопределить при запуске:
 * mvn clean test -Dapp.jar=путь/к/приложению.jar
 */
public final class TestConfig {

    /** Адрес тестируемого приложения. */
    public static final String APP_URL = System.getProperty("app.url", "http://localhost:8080");

    /** Статический API-ключ (он же параметр -Dsecret при запуске приложения). */
    public static final String API_KEY = System.getProperty("app.apiKey", "qazWSXedc");

    /** Порт, на котором поднимается заглушка внешнего сервиса (WireMock). */
    public static final int MOCK_PORT = Integer.parseInt(System.getProperty("mock.port", "8888"));

    /** Путь к jar тестируемого приложения. */
    public static final String JAR_PATH = System.getProperty("app.jar", "app/internal-0.0.1-SNAPSHOT.jar");

    /** true — тесты сами запускают приложение; false — приложение уже запущено вручную. */
    public static final boolean START_APP = Boolean.parseBoolean(System.getProperty("app.start", "true"));

    /** Сколько секунд ждать запуска приложения. */
    public static final int APP_START_TIMEOUT_SEC = Integer.parseInt(System.getProperty("app.startTimeout", "60"));

    private TestConfig() {
    }
}
