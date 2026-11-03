package aqa.environment;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Поднимает окружение один раз на весь прогон (корневой Store JUnit сам закроет его в конце)
 * и перед каждым тестом очищает заглушку: настройки ответов и журнал полученных запросов.
 */
public class ApplicationExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot()
                .getStore(ExtensionContext.Namespace.GLOBAL)
                .getOrComputeIfAbsent(TestEnvironment.class, key -> TestEnvironment.start(), TestEnvironment.class);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        TestEnvironment.wireMock().resetAll();
    }
}
