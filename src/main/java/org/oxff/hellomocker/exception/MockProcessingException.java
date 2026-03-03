package org.oxff.hellomocker.exception;

/**
 * Mock处理异常
 * 当Mock规则处理过程中发生错误时抛出
 *
 * @author oxff
 * @version 1.0
 */
public class MockProcessingException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 错误类型
     */
    public enum ErrorType {
        CONFIGURATION_ERROR,    // 配置错误
        SCRIPT_EXECUTION_ERROR, // 脚本执行错误
        NETWORK_ERROR,          // 网络错误
        TIMEOUT_ERROR,          // 超时错误
        VALIDATION_ERROR,       // 验证错误
        UNKNOWN_ERROR           // 未知错误
    }

    private final ErrorType errorType;
    private final String ruleId;

    public MockProcessingException(String message) {
        super(message);
        this.errorType = ErrorType.UNKNOWN_ERROR;
        this.ruleId = null;
    }

    public MockProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.UNKNOWN_ERROR;
        this.ruleId = null;
    }

    public MockProcessingException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.ruleId = null;
    }

    public MockProcessingException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.ruleId = null;
    }

    public MockProcessingException(ErrorType errorType, String ruleId, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.ruleId = ruleId;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getRuleId() {
        return ruleId;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (ruleId != null) {
            sb.append("[Rule: ").append(ruleId).append("] ");
        }
        sb.append("[").append(errorType).append("] ");
        sb.append(super.getMessage());
        return sb.toString();
    }
}
