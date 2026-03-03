package org.oxff.hellomocker.handler;

import org.oxff.hellomocker.engine.PythonEngine;
import org.oxff.hellomocker.exception.MockProcessingException;
import org.oxff.hellomocker.model.MockContext;
import org.oxff.hellomocker.model.MockResponse;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.storage.ConfigStorage;

/**
 * Python脚本响应处理器
 * 执行Python脚本并返回响应
 *
 * @author oxff
 * @version 1.0
 */
public class PythonScriptHandler implements ResponseHandler {

    private final PythonEngine pythonEngine;

    public PythonScriptHandler(ConfigStorage configStorage) {
        this.pythonEngine = new PythonEngine(configStorage);
    }

    @Override
    public boolean supports(ResponseConfig config) {
        return config != null && config.getType() == ResponseConfig.ResponseType.PYTHON_SCRIPT;
    }

    @Override
    public MockResponse handle(MockContext context, ResponseConfig config) throws MockProcessingException {
        if (!supports(config)) {
            throw new MockProcessingException(
                    MockProcessingException.ErrorType.CONFIGURATION_ERROR,
                    "PythonScriptHandler does not support this configuration"
            );
        }

        // 优先使用脚本文件
        if (config.getPythonFilePath() != null && !config.getPythonFilePath().trim().isEmpty()) {
            return pythonEngine.executeFromFile(context, config.getPythonFilePath());
        }

        // 使用内联脚本
        if (config.getPythonScript() != null && !config.getPythonScript().trim().isEmpty()) {
            return pythonEngine.execute(context, config.getPythonScript());
        }

        return MockResponse.error("No Python script provided");
    }

    @Override
    public String getName() {
        return "PythonScriptHandler";
    }

    @Override
    public String getDescription() {
        return "Executes Python script to generate dynamic response";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    /**
     * 关闭处理器，释放资源
     */
    public void shutdown() {
        if (pythonEngine != null) {
            pythonEngine.shutdown();
        }
    }
}
