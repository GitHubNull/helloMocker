package org.oxff.hellomocker.util;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.HttpHeader;
import org.oxff.hellomocker.model.ResponseConfig;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP响应工具类
 * 用于在ResponseConfig和Burp HttpResponse之间转换
 *
 * @author oxff
 * @version 1.0
 */
public class HttpResponseUtils {

    /**
     * 从ResponseConfig创建HttpResponse
     * 修复中文乱码：确保Content-Type包含charset=UTF-8，并使用UTF-8字节数组创建
     *
     * @param api    MontoyaApi实例
     * @param config 响应配置
     * @return HttpResponse对象
     */
    public static HttpResponse createHttpResponse(MontoyaApi api, ResponseConfig config) {
        if (config == null || api == null) {
            return null;
        }

        StringBuilder responseBuilder = new StringBuilder();

        // 状态行
        responseBuilder.append("HTTP/1.1 ").append(config.getStatusCode()).append(" OK\r\n");

        // 标记是否已添加Content-Type
        boolean hasContentType = false;
        String contentTypeValue = null;

        // Headers - 先收集所有非Content-Type的header
        if (config.getHeaders() != null) {
            for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                String headerName = entry.getKey();
                String headerValue = entry.getValue();
                
                if (headerName.equalsIgnoreCase("Content-Type")) {
                    hasContentType = true;
                    contentTypeValue = headerValue;
                } else {
                    responseBuilder.append(headerName).append(": ").append(headerValue).append("\r\n");
                }
            }
        }

        // 处理Content-Type，确保包含charset=UTF-8
        if (!hasContentType) {
            // 没有Content-Type，添加默认的
            responseBuilder.append("Content-Type: application/json; charset=UTF-8\r\n");
        } else {
            // 有Content-Type，检查是否包含charset
            if (contentTypeValue != null) {
                String lowerContentType = contentTypeValue.toLowerCase();
                if (!lowerContentType.contains("charset")) {
                    // 没有charset，添加UTF-8
                    contentTypeValue += "; charset=UTF-8";
                }
                responseBuilder.append("Content-Type: ").append(contentTypeValue).append("\r\n");
            }
        }

        // 空行分隔
        responseBuilder.append("\r\n");

        // Body
        if (config.getBody() != null) {
            responseBuilder.append(config.getBody());
        }

        // 使用UTF-8字节数组创建HttpResponse，确保中文编码正确
        String responseString = responseBuilder.toString();
        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        return HttpResponse.httpResponse(ByteArray.byteArray(responseBytes));
    }

    /**
     * 从HttpResponse提取数据创建ResponseConfig
     *
     * @param response HttpResponse对象
     * @return 响应配置
     */
    public static ResponseConfig extractResponseConfig(HttpResponse response) {
        if (response == null) {
            return null;
        }

        ResponseConfig config = new ResponseConfig();
        config.setType(ResponseConfig.ResponseType.STATIC);
        config.setStatusCode(response.statusCode());

        // 提取Headers
        Map<String, String> headers = response.headers().stream()
                .collect(Collectors.toMap(
                        HttpHeader::name,
                        HttpHeader::value,
                        (v1, v2) -> v1  // 处理重复header的情况
                ));
        config.setHeaders(headers);

        // 提取Body
        config.setBody(response.bodyToString());

        return config;
    }

    /**
     * 合并ResponseConfig（更新时保留非静态响应类型的配置）
     *
     * @param existingConfig 现有配置
     * @param newStaticConfig 新的静态响应配置（从编辑器获取）
     * @return 合并后的配置
     */
    public static ResponseConfig mergeResponseConfig(ResponseConfig existingConfig, ResponseConfig newStaticConfig) {
        if (existingConfig == null) {
            return newStaticConfig;
        }

        // 如果原配置不是静态响应，保留原类型
        if (existingConfig.getType() != ResponseConfig.ResponseType.STATIC) {
            return existingConfig;
        }

        return newStaticConfig;
    }
}