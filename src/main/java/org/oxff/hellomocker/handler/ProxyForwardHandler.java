package org.oxff.hellomocker.handler;

import org.oxff.hellomocker.exception.MockProcessingException;
import org.oxff.hellomocker.model.MockContext;
import org.oxff.hellomocker.model.MockResponse;
import org.oxff.hellomocker.model.ResponseConfig;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理转发响应处理器
 * 将请求转发到上游服务器并返回响应
 *
 * @author oxff
 * @version 1.0
 */
public class ProxyForwardHandler implements ResponseHandler {

    private static final int DEFAULT_TIMEOUT = 30000; // 30秒超时

    @Override
    public boolean supports(ResponseConfig config) {
        return config != null && config.getType() == ResponseConfig.ResponseType.PROXY_FORWARD;
    }

    @Override
    public MockResponse handle(MockContext context, ResponseConfig config) throws MockProcessingException {
        if (!supports(config)) {
            throw new MockProcessingException(
                    MockProcessingException.ErrorType.CONFIGURATION_ERROR,
                    "ProxyForwardHandler does not support this configuration"
            );
        }

        String targetHost = config.getTargetHost();
        int targetPort = config.getTargetPort();
        boolean useSsl = config.isUseSsl();

        if (targetHost == null || targetHost.trim().isEmpty()) {
            return MockResponse.error("Target host is not configured");
        }

        try {
            return forwardRequest(context, targetHost, targetPort, useSsl);
        } catch (Exception e) {
            return MockResponse.error("Proxy forward failed: " + e.getMessage());
        }
    }

    /**
     * 转发请求到目标服务器
     */
    private MockResponse forwardRequest(MockContext context, String targetHost, int targetPort, boolean useSsl) throws Exception {
        // 构建目标URL
        String protocol = useSsl ? "https" : "http";
        String targetUrl = buildTargetUrl(context, protocol, targetHost, targetPort);

        URL url = new URL(targetUrl);
        HttpURLConnection connection = null;

        try {
            // 创建连接
            if (useSsl) {
                connection = (HttpsURLConnection) url.openConnection();
                // 信任所有证书（开发测试用）
                ((HttpsURLConnection) connection).setSSLSocketFactory(createTrustAllSslSocketFactory());
                ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            // 设置请求方法
            connection.setRequestMethod(context.getMethod());
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // 复制请求头
            if (context.getHeaders() != null) {
                for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
                    // 跳过Host头，使用目标服务器的Host
                    if (entry.getKey().equalsIgnoreCase("Host")) {
                        connection.setRequestProperty("Host", targetHost + ":" + targetPort);
                    } else {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
            }

            // 发送请求体
            if (context.getBody() != null && context.getBody().length > 0) {
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(context.getBody());
                    os.flush();
                }
            }

            // 获取响应
            int statusCode = connection.getResponseCode();
            Map<String, String> responseHeaders = new HashMap<>();

            // 读取响应头
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            if (headerFields != null) {
                for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                    if (entry.getKey() != null && !entry.getValue().isEmpty()) {
                        responseHeaders.put(entry.getKey(), String.join(", ", entry.getValue()));
                    }
                }
            }

            // 读取响应体
            byte[] responseBody;
            try (InputStream is = getInputStream(connection, statusCode)) {
                responseBody = readAllBytes(is);
            }

            // 构建MockResponse
            return MockResponse.builder()
                    .statusCode(statusCode)
                    .headers(responseHeaders)
                    .body(responseBody)
                    .success(true)
                    .handlerType("PROXY_FORWARD")
                    .build();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 构建目标URL
     */
    private String buildTargetUrl(MockContext context, String protocol, String targetHost, int targetPort) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        sb.append(targetHost);

        // 添加端口（如果不是默认端口）
        boolean isDefaultPort = (protocol.equals("http") && targetPort == 80) ||
                (protocol.equals("https") && targetPort == 443);
        if (!isDefaultPort) {
            sb.append(":").append(targetPort);
        }

        // 添加路径
        String path = context.getPath();
        if (path != null && !path.isEmpty()) {
            if (!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
        }

        // 添加查询字符串
        String query = context.getQuery();
        if (query != null && !query.isEmpty()) {
            sb.append("?").append(query);
        }

        return sb.toString();
    }

    /**
     * 获取输入流（处理成功和失败响应）
     */
    private InputStream getInputStream(HttpURLConnection connection, int statusCode) throws IOException {
        if (statusCode >= 200 && statusCode < 300) {
            return connection.getInputStream();
        } else {
            InputStream errorStream = connection.getErrorStream();
            return errorStream != null ? errorStream : new ByteArrayInputStream(new byte[0]);
        }
    }

    /**
     * 读取所有字节
     */
    private byte[] readAllBytes(InputStream is) throws IOException {
        if (is == null) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    /**
     * 创建信任所有证书的SSL Socket Factory（仅用于测试）
     */
    private SSLSocketFactory createTrustAllSslSocketFactory() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }

    @Override
    public String getName() {
        return "ProxyForwardHandler";
    }

    @Override
    public String getDescription() {
        return "Forwards requests to upstream server and returns the response";
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
