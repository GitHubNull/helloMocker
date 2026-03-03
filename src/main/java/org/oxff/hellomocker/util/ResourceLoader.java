package org.oxff.hellomocker.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 资源文件读取工具类
 */
public class ResourceLoader {
    
    /**
     * 从资源文件读取文本内容
     * 
     * @param resourcePath 资源文件路径（相对于resources目录）
     * @return 文件内容字符串，如果读取失败返回null
     */
    public static String loadResourceAsString(String resourcePath) {
        try (InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            System.err.println("Failed to load resource: " + resourcePath + ", error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从资源文件读取Python脚本模板
     */
    public static String loadPythonScriptTemplate() {
        String content = loadResourceAsString("org/oxff/hellomocker/python_script_template.py");
        if (content == null) {
            // 返回默认模板（备用）
            return getDefaultPythonTemplate();
        }
        return content;
    }
    
    /**
     * 从资源文件读取Python帮助文档
     */
    public static String loadPythonHelpText() {
        String content = loadResourceAsString("org/oxff/hellomocker/python_help.txt");
        if (content == null) {
            // 返回默认帮助（备用）
            return getDefaultPythonHelp();
        }
        return content;
    }
    
    /**
     * 默认Python模板（备用）
     */
    private static String getDefaultPythonTemplate() {
        return "def handle_request(request):\n" +
               "    \"\"\"Handle HTTP request and return response.\"\"\"\n" +
               "    return {\n" +
               "        \"status\": 200,\n" +
               "        \"headers\": {\"Content-Type\": \"application/json\"},\n" +
               "        \"body\": '{\"code\": 0, \"message\": \"success\"}'\n" +
               "    }\n";
    }
    
    /**
     * 默认Python帮助（备用）
     */
    private static String getDefaultPythonHelp() {
        return "Python Script Help:\n" +
               "- The script must contain a handle_request(request) function\n" +
               "- Return a dict with: status, headers, body";
    }
}
