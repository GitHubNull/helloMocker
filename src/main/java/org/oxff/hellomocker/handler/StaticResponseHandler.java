package org.oxff.hellomocker.handler;

import org.oxff.hellomocker.exception.MockProcessingException;
import org.oxff.hellomocker.model.MockContext;
import org.oxff.hellomocker.model.MockResponse;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.util.HttpUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 静态响应处理器
 * 返回配置的静态HTTP响应
 *
 * @author oxff
 * @version 1.0
 */
public class StaticResponseHandler implements ResponseHandler {

    @Override
    public boolean supports(ResponseConfig config) {
        return config != null && config.getType() == ResponseConfig.ResponseType.STATIC;
    }

    @Override
    public MockResponse handle(MockContext context, ResponseConfig config) throws MockProcessingException {
        if (!supports(config)) {
            throw new MockProcessingException(
                    MockProcessingException.ErrorType.CONFIGURATION_ERROR,
                    "StaticResponseHandler does not support this configuration"
            );
        }

        try {
            // 构建响应
            MockResponse.MockResponseBuilder responseBuilder = MockResponse.builder()
                    .statusCode(config.getStatusCode())
                    .headers(config.getHeaders() != null ? 
                            new HashMap<>(config.getHeaders()) : new HashMap<>())
                    .success(true)
                    .handlerType(getName());

            // 处理响应体
            String body = config.getBody();
            if (body != null && !body.isEmpty()) {
                responseBuilder.body(body.getBytes(StandardCharsets.UTF_8));
            } else {
                responseBuilder.body(new byte[0]);
            }

            MockResponse response = responseBuilder.build();

            // 设置默认Content-Type
            if (response.getHeader("Content-Type") == null) {
                String contentType = HttpUtils.getDefaultContentType(body != null ? body : "");
                response.addHeader("Content-Type", contentType);
            }

            return response;

        } catch (Exception e) {
            throw new MockProcessingException(
                    MockProcessingException.ErrorType.SCRIPT_EXECUTION_ERROR,
                    "Failed to generate static response: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public String getName() {
        return "StaticResponseHandler";
    }

    @Override
    public String getDescription() {
        return "Returns a static HTTP response based on configuration";
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
