package aqa.environment;

import aqa.config.TestConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Тестовое окружение: заглушка внешнего сервиса (WireMock) + тестируемое приложение.
 * Поднимается один раз на весь прогон и останавливается в конце.
 */
public final class TestEnvironment implements ExtensionContext.Store.CloseableResource {

    private static volatile TestEnvironment current;

    private final WireMockServer wireMock;
    private final Process app;

    private TestEnvironment(WireMockServer wireMock, Process app) {
        this.wireMock = wireMock;
        this.app = app;
    }

    static TestEnvironment start() {
        // Сначала заглушка, потом приложение: приложение будет ходить в неё.
        WireMockServer wireMock = new WireMockServer(options().port(TestConfig.MOCK_PORT));
        wireMock.start();

        Process app = TestConfig.START_APP ? launchApp() : null;
        waitUntilAppIsUp(app);

        current = new TestEnvironment(wireMock, app);
        return current;
    }

    public static WireMockServer wireMock() {
        if (current == null) {
            throw new IllegalStateException("Тестовое окружение не запущено");
        }
        return current.wireMock;
    }

    private static Process launchApp() {
        File jar = new File(TestConfig.JAR_PATH);
        if (!jar.isFile()) {
            throw new IllegalStateException("Не найден jar приложения: " + jar.getAbsolutePath()
                    + ". Положите его в папку app/ или укажите путь через -Dapp.jar=...");
        }
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        File log = new File("target/app.log");
        log.getParentFile().mkdirs();
        try {
            Process process = new ProcessBuilder(
                    java,
                    "-Dsecret=" + TestConfig.API_KEY,
                    "-Dmock=http://localhost:" + TestConfig.MOCK_PORT + "/",
                    "-jar", jar.getAbsolutePath())
                    .redirectErrorStream(true)
                    .redirectOutput(log)
                    .start();
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));
            return process;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось запустить приложение", e);
        }
    }

    private static void waitUntilAppIsUp(Process app) {
        URI uri = URI.create(TestConfig.APP_URL);
        long deadline = System.currentTimeMillis() + TestConfig.APP_START_TIMEOUT_SEC * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (app != null && !app.isAlive()) {
                throw new IllegalStateException(
                        "Приложение завершилось при запуске, подробности в target/app.log");
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), 500);
                return;
            } catch (IOException notYet) {
                sleep(500);
            }
        }
        throw new IllegalStateException("Приложение не ответило за " + TestConfig.APP_START_TIMEOUT_SEC
                + " с, подробности в target/app.log");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        if (app != null) {
            app.destroy();
            try {
                if (!app.waitFor(10, TimeUnit.SECONDS)) {
                    app.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        wireMock.stop();
    }
}
