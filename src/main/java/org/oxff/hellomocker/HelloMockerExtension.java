package org.oxff.hellomocker;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.Extension;
import burp.api.montoya.logging.Logging;
import org.oxff.hellomocker.http.MockHttpHandler;
import org.oxff.hellomocker.menu.SendToMockContextMenu;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.ui.MainTabPanel;

/**
 * HelloMocker BurpSuite扩展主入口
 *
 * @author oxff
 * @version 1.0
 */
public class HelloMockerExtension implements BurpExtension {

    private static MontoyaApi api;
    private static HelloMockerExtension instance;
    private MockRuleManager ruleManager;
    private ConfigStorage configStorage;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;
        instance = this;

        Extension extension = api.extension();
        Logging logging = api.logging();

        // 设置扩展名称
        extension.setName("HelloMocker");

        logging.logToOutput("HelloMocker Extension initializing...");

        try {
            // 初始化存储
            configStorage = new ConfigStorage(api);
            configStorage.loadConfig();

            // 初始化规则管理器
            ruleManager = new MockRuleManager(configStorage);
            ruleManager.loadRules();

            // 创建并注册UI
            MainTabPanel mainPanel = new MainTabPanel(api, ruleManager, configStorage);
            api.userInterface().registerSuiteTab("HelloMocker", mainPanel);

            // 注册HTTP处理器
            MockHttpHandler httpHandler = new MockHttpHandler(ruleManager, configStorage, logging);
            api.http().registerHttpHandler(httpHandler);
            logging.logToOutput("HTTP handler registered successfully");

            // 注册右键菜单
            SendToMockContextMenu contextMenu = new SendToMockContextMenu(api, ruleManager, configStorage, logging);
            api.userInterface().registerContextMenuItemsProvider(contextMenu);
            logging.logToOutput("Context menu registered successfully");

            logging.logToOutput("HelloMocker Extension initialized successfully!");

        } catch (Exception e) {
            logging.logToError("Failed to initialize HelloMocker Extension", e);
            throw new RuntimeException("Failed to initialize HelloMocker Extension", e);
        }
    }

    /**
     * 获取MontoyaApi实例
     */
    public static MontoyaApi getApi() {
        return api;
    }

    /**
     * 获取扩展实例
     */
    public static HelloMockerExtension getInstance() {
        return instance;
    }

    /**
     * 获取规则管理器
     */
    public MockRuleManager getRuleManager() {
        return ruleManager;
    }

    /**
     * 获取配置存储
     */
    public ConfigStorage getConfigStorage() {
        return configStorage;
    }
}
