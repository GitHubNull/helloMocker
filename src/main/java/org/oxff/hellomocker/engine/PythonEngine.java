package org.oxff.hellomocker.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oxff.hellomocker.model.MockContext;
import org.oxff.hellomocker.model.MockResponse;
import org.oxff.hellomocker.storage.ConfigStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Python脚本引擎
 * 负责调用系统Python进程执行脚本并获取响应
 *
 * @author oxff
 * @version 1.0
 */
public class PythonEngine {

    private final ConfigStorage configStorage;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    // Python脚本包装模板
    private static final String PYTHON_WRAPPER_TEMPLATE = """
import sys
import json
import base64

# 读取输入JSON
input_data = sys.stdin.read()
request_data = json.loads(input_data)

# 用户脚本
%s

# 执行处理
if __name__ == "__main__":
    try:
        result = handle_request(request_data)
        # 确保结果是字典
        if not isinstance(result, dict):
            result = {
                "status": 500,
                "headers": {"Content-Type": "text/plain"},
                "body": "Error: handle_request must return a dictionary"
            }
        
        # 序列化输出
        output = json.dumps(result)
        print(output)
    except Exception as e:
        error_result = {
            "status": 500,
            "headers": {"Content-Type": "text/plain"},
            "body": f"Python Error: {str(e)}\\n"
        }
        print(json.dumps(error_result))
""";

    public PythonEngine(ConfigStorage configStorage) {
        this.configStorage = configStorage;
        this.objectMapper = new ObjectMapper();
        // 创建线程池用于执行Python脚本
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * 执行Python脚本
     *
     * @param context      请求上下文
     * @param pythonScript Python脚本代码
     * @return Mock响应
     */
    public MockResponse execute(MockContext context, String pythonScript) {
        if (pythonScript == null || pythonScript.trim().isEmpty()) {
            return MockResponse.error("Python script is empty");
        }

        // 验证Python路径
        String pythonPath = configStorage.getPythonPath();
        if (!configStorage.isPythonPathValid()) {
            return MockResponse.error("Python path is not valid: " + pythonPath);
        }

        try {
            // 准备请求数据
            Map<String, Object> requestData = convertContextToMap(context);
            String requestJson = objectMapper.writeValueAsString(requestData);

            // 包装脚本
            String wrappedScript = String.format(PYTHON_WRAPPER_TEMPLATE, pythonScript);

            // 执行脚本
            return executePythonScript(pythonPath, wrappedScript, requestJson);

        } catch (Exception e) {
            return MockResponse.error("Failed to execute Python script: " + e.getMessage());
        }
    }

    /**
     * 从文件执行Python脚本
     *
     * @param context      请求上下文
     * @param filePath     Python脚本文件路径
     * @return Mock响应
     */
    public MockResponse executeFromFile(MockContext context, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return MockResponse.error("Python file path is empty");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return MockResponse.error("Python file not found: " + filePath);
        }

        if (!file.canRead()) {
            return MockResponse.error("Cannot read Python file: " + filePath);
        }

        try {
            // 读取文件内容
            String script = readFile(file);
            return execute(context, script);
        } catch (IOException e) {
            return MockResponse.error("Failed to read Python file: " + e.getMessage());
        }
    }

    /**
     * 执行Python脚本（核心方法）
     */
    private MockResponse executePythonScript(String pythonPath, String script, String inputJson) {
        // 使用线程池执行，支持超时
        Future<MockResponse> future = executorService.submit(() -> {
            // 创建临时脚本文件
            File tempScript = File.createTempFile("hellomocker_script_", ".py");
            tempScript.deleteOnExit();

            try {
                // 写入脚本
                try (FileWriter writer = new FileWriter(tempScript)) {
                    writer.write(script);
                }

                // 构建进程
                ProcessBuilder pb = new ProcessBuilder(
                        pythonPath,
                        tempScript.getAbsolutePath()
                );

                pb.redirectErrorStream(true); // 合并错误流到输出流

                // 启动进程
                Process process = pb.start();

                // 写入输入JSON
                try (OutputStream os = process.getOutputStream();
                     OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    osw.write(inputJson);
                    osw.flush();
                }

                // 读取输出
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                }

                // 等待进程完成
                boolean finished = process.waitFor(configStorage.getDefaultTimeout(), TimeUnit.MILLISECONDS);

                if (!finished) {
                    process.destroyForcibly();
                    return MockResponse.error("Python script execution timeout (" + configStorage.getDefaultTimeout() + "ms)");
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    return MockResponse.error("Python script exited with code " + exitCode + ": " + output);
                }

                // 解析输出JSON
                return parsePythonOutput(output.toString());

            } finally {
                // 清理临时文件
                tempScript.delete();
            }
        });

        try {
            return future.get(configStorage.getDefaultTimeout() + 1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return MockResponse.error("Python script execution timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return MockResponse.error("Python script execution interrupted");
        } catch (ExecutionException e) {
            return MockResponse.error("Python script execution failed: " + e.getCause().getMessage());
        }
    }

    /**
     * 将MockContext转换为Map（用于JSON序列化）
     */
    private Map<String, Object> convertContextToMap(MockContext context) {
        Map<String, Object> map = new HashMap<>();

        if (context != null) {
            map.put("url", context.getUrl());
            map.put("method", context.getMethod());
            map.put("headers", context.getHeaders());
            map.put("path", context.getPath());
            map.put("query", context.getQuery());
            map.put("host", context.getHost());
            map.put("port", context.getPort());
            map.put("protocol", context.getProtocol());

            // Body处理：同时提供字符串和base64编码
            if (context.getBody() != null) {
                String bodyString = new String(context.getBody(), StandardCharsets.UTF_8);
                map.put("body", bodyString);
                map.put("body_base64", java.util.Base64.getEncoder().encodeToString(context.getBody()));
            } else {
                map.put("body", "");
                map.put("body_base64", "");
            }
        }

        return map;
    }

    /**
     * 解析Python输出JSON
     */
    private MockResponse parsePythonOutput(String output) {
        try {
            // 找到JSON开始的位置（可能前面有Python的print输出）
            int jsonStart = output.indexOf("{");
            int jsonArrayStart = output.indexOf("[");

            if (jsonStart == -1 && jsonArrayStart == -1) {
                return MockResponse.error("Python script did not return valid JSON: " + output);
            }

            // 选择最早出现的{或[
            int startIndex = jsonStart;
            if (jsonArrayStart != -1 && (jsonStart == -1 || jsonArrayStart < jsonStart)) {
                startIndex = jsonArrayStart;
            }

            String jsonStr = output.substring(startIndex);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);

            // 构建MockResponse
            MockResponse.MockResponseBuilder builder = MockResponse.builder();

            // 获取状态码
            if (result.containsKey("status")) {
                Object status = result.get("status");
                if (status instanceof Number) {
                    builder.statusCode(((Number) status).intValue());
                } else {
                    try {
                        builder.statusCode(Integer.parseInt(status.toString()));
                    } catch (NumberFormatException e) {
                        builder.statusCode(200);
                    }
                }
            } else {
                builder.statusCode(200);
            }

            // 获取headers
            if (result.containsKey("headers")) {
                @SuppressWarnings("unchecked")
                Map<String, String> headers = (Map<String, String>) result.get("headers");
                builder.headers(headers != null ? headers : new HashMap<>());
            } else {
                builder.headers(new HashMap<>());
            }

            // 获取body（支持body或body_base64）
            byte[] bodyBytes = null;
            if (result.containsKey("body_base64")) {
                String base64Body = (String) result.get("body_base64");
                if (base64Body != null && !base64Body.isEmpty()) {
                    try {
                        bodyBytes = java.util.Base64.getDecoder().decode(base64Body);
                    } catch (IllegalArgumentException e) {
                        // base64解码失败，使用普通body
                    }
                }
            }

            if (bodyBytes == null && result.containsKey("body")) {
                Object body = result.get("body");
                if (body != null) {
                    bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
                }
            }

            builder.body(bodyBytes != null ? bodyBytes : new byte[0]);

            // 获取延迟
            if (result.containsKey("delay")) {
                Object delay = result.get("delay");
                if (delay instanceof Number) {
                    builder.delay(((Number) delay).intValue());
                }
            }

            builder.success(true);
            builder.handlerType("PYTHON_SCRIPT");

            return builder.build();

        } catch (Exception e) {
            return MockResponse.error("Failed to parse Python output: " + e.getMessage() + "\nOutput: " + output);
        }
    }

    /**
     * 读取文件内容
     */
    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 关闭引擎，释放资源
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
