# HelloMocker 技术架构设计文档

## 1. 架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                    BurpSuite Professional                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              HelloMocker Extension                      │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   │
│  │  │   UI Layer   │  │  Core Logic  │  │   Storage    │   │   │
│  │  │  (Swing)     │  │   Layer      │  │   (JSON)     │   │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────────────┘   │   │
│  │         │                 │                             │   │
│  │  ┌──────▼───────┐  ┌──────▼───────┐                    │   │
│  │  │ MockRulePanel│  │MockProcessor │                    │   │
│  │  │ RuleEditor   │  │RuleMatcher   │                    │   │
│  │  │ ConfigPanel  │  │ResponseGen   │                    │   │
│  │  └──────────────┘  └──────┬───────┘                    │   │
│  │                           │                             │   │
│  │              ┌────────────┼────────────┐                │   │
│  │              ▼            ▼            ▼                │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐    │   │
│  │  │PythonEngine  │ │ProxyForwarder│ │StaticHandler │    │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│         ┌─────────────────┼─────────────────┐                  │
│         ▼                 ▼                 ▼                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │Python Process│  │Remote Server │  │ Burp History │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

## 2. 核心组件

### 2.1 主入口
- **类**: `HelloMockerExtension`
- **职责**: 实现 `BurpExtension` 接口，初始化插件
- **功能**: 
  - 注册UI面板
  - 初始化配置存储
  - 注册HTTP处理器
  - 注册右键菜单

### 2.2 UI层
- **MainTabPanel**: 主Tab面板，包含多个子面板
  - **RuleListPanel**: 规则列表管理（增删改查、启用/禁用）
  - **RuleEditorPanel**: 规则编辑器（URL匹配、响应配置）
  - **ConfigPanel**: 全局配置（Python路径、默认设置）

### 2.3 核心逻辑层
- **MockRuleManager**: 规则管理器（CRUD、持久化）
- **RuleMatcher**: 规则匹配器（URL匹配算法）
- **MockProcessor**: Mock处理器（协调各响应生成器）
- **ResponseGenerator**: 响应生成器接口及实现

### 2.4 响应处理器
- **PythonScriptHandler**: Python脚本处理器（调用系统Python进程）
- **StaticResponseHandler**: 静态响应处理器（历史记录回放）
- **ProxyForwardHandler**: 代理转发处理器（请求重定向）

### 2.5 数据存储
- **ConfigStorage**: JSON文件持久化
- **MockRuleRepository**: 规则数据访问层

## 3. 数据模型

### 3.1 MockRule（Mock规则）
```java
public class MockRule {
    private String id;                    // UUID
    private String name;                  // 规则名称
    private boolean enabled;              // 是否启用
    private int priority;                 // 优先级（数字越小优先级越高）
    private MatchCondition matchCondition; // 匹配条件
    private ResponseConfig responseConfig; // 响应配置
    private LocalDateTime createdAt;      // 创建时间
    private LocalDateTime updatedAt;      // 更新时间
}
```

### 3.2 MatchCondition（匹配条件）
```java
public class MatchCondition {
    public enum MatchType {
        EQUALS,      // 完全相等
        CONTAINS,    // 包含
        REGEX,       // 正则匹配
        STARTS_WITH, // 以...开头
        ENDS_WITH    // 以...结尾
    }
    
    private MatchType type;
    private String urlPattern;    // URL匹配模式
    private String method;        // HTTP方法（可选，null表示全部）
}
```

### 3.3 ResponseConfig（响应配置）
```java
public class ResponseConfig {
    public enum ResponseType {
        PYTHON_SCRIPT,    // Python脚本
        STATIC,           // 静态响应
        PROXY_FORWARD     // 代理转发
    }
    
    private ResponseType type;
    
    // Python脚本模式
    private String pythonScript;      // 脚本代码
    private String pythonFilePath;    // 脚本文件路径（可选）
    
    // 静态响应模式
    private int statusCode;           // HTTP状态码
    private Map<String, String> headers;  // 响应头
    private String body;              // 响应体
    
    // 代理转发模式
    private String targetHost;        // 目标主机
    private int targetPort;           // 目标端口
    private boolean useSsl;           // 是否使用SSL
}
```

### 3.4 MockContext（Mock上下文）
```java
public class MockContext {
    private String url;
    private String method;
    private Map<String, String> headers;
    private byte[] body;
    private Map<String, Object> extra;  // 额外数据
}
```

## 4. Python脚本接口规范

### 4.1 请求对象结构（Java传递给Python）
```json
{
  "url": "http://example.com/api/test",
  "method": "POST",
  "headers": {
    "Content-Type": "application/json",
    "User-Agent": "Mozilla/5.0"
  },
  "body": "base64_encoded_body_content",
  "body_bytes": [1, 2, 3, 4],
  "path": "/api/test",
  "query": "param1=value1&param2=value2",
  "host": "example.com",
  "port": 80,
  "protocol": "http"
}
```

### 4.2 响应对象结构（Python返回给Java）
```json
{
  "status": 200,
  "headers": {
    "Content-Type": "application/json",
    "X-Custom-Header": "value"
  },
  "body": "response body content",
  "body_base64": "base64_encoded_content",
  "delay": 0
}
```

### 4.3 Python脚本模板
```python
def handle_request(request):
    """
    处理HTTP请求并返回响应
    
    Args:
        request: 请求对象，包含url, method, headers, body等属性
    
    Returns:
        dict: 响应对象，包含status, headers, body等
    """
    # 示例逻辑
    if "/api/admin" in request["url"]:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": '{"code": 0, "message": "success", "data": {}}'
        }
    
    return {
        "status": 404,
        "headers": {"Content-Type": "text/plain"},
        "body": "Not Found"
    }
```

## 5. 项目结构

```
helloMocker/
├── pom.xml
├── doc/
│   ├── PRD.md
│   └── ARCHITECTURE.md              # 本文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/oxff/hellomocker/
│   │   │       ├── HelloMockerExtension.java      # 主入口
│   │   │       ├── api/                           # API接口
│   │   │       │   ├── IMockHandler.java
│   │   │       │   └── MockRequest.java
│   │   │       ├── model/                         # 数据模型
│   │   │       │   ├── MockRule.java
│   │   │       │   ├── MatchCondition.java
│   │   │       │   ├── ResponseConfig.java
│   │   │       │   └── MockContext.java
│   │   │       ├── service/                       # 业务逻辑
│   │   │       │   ├── MockRuleManager.java
│   │   │       │   ├── RuleMatcher.java
│   │   │       │   └── MockProcessor.java
│   │   │       ├── handler/                       # 响应处理器
│   │   │       │   ├── ResponseHandler.java
│   │   │       │   ├── PythonScriptHandler.java
│   │   │       │   ├── StaticResponseHandler.java
│   │   │       │   └── ProxyForwardHandler.java
│   │   │       ├── engine/                        # 脚本引擎
│   │   │       │   └── PythonEngine.java
│   │   │       ├── storage/                       # 数据存储
│   │   │       │   ├── ConfigStorage.java
│   │   │       │   └── MockRuleRepository.java
│   │   │       ├── ui/                            # UI界面
│   │   │       │   ├── MainTabPanel.java
│   │   │       │   ├── panel/
│   │   │       │   │   ├── RuleListPanel.java
│   │   │       │   │   ├── RuleEditorPanel.java
│   │   │       │   │   ├── ConfigPanel.java
│   │   │       │   │   └── PythonEditorPanel.java
│   │   │       │   ├── component/
│   │   │       │   │   ├── MockRuleTable.java
│   │   │       │   │   ├── MatchConditionPanel.java
│   │   │       │   │   └── ResponseConfigPanel.java
│   │   │       │   └── dialog/
│   │   │       │       └── ImportScriptDialog.java
│   │   │       ├── menu/                          # 右键菜单
│   │   │       │   └── SendToMockContextMenu.java
│   │   │       ├── http/                          # HTTP处理
│   │   │       │   └── MockHttpHandler.java
│   │   │       ├── util/                          # 工具类
│   │   │       │   ├── JsonUtils.java
│   │   │       │   ├── HttpUtils.java
│   │   │       │   └── UIUtils.java
│   │   │       └── exception/                     # 异常
│   │   │           └── MockProcessingException.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── services/
│   │       │       └── burp.api.montoya.BurpExtension
│   │       └── icons/
│   └── test/
│       └── java/
└── target/
```

## 6. 关键技术决策

### 6.1 Python引擎实现
- **方案**: 调用系统Python进程
- **原因**: 支持Python 3.x，无需嵌入Jython
- **实现**: 使用 `ProcessBuilder` 启动Python进程，通过JSON进行数据交换
- **优化**: 使用进程池或长连接机制，避免频繁启动开销

### 6.2 UI框架
- **方案**: Swing (BurpSuite原生外观)
- **原因**: 使用BurpSuite自带的外观和主题，确保插件与BurpSuite界面风格一致
- **实现**: 所有UI组件继承标准JPanel，使用BurpSuite提供的主题和样式

### 6.3 代码编辑器
- **方案**: RSyntaxTextArea
- **原因**: 支持语法高亮、代码折叠、行号显示
- **依赖**: `com.fifesoft:rsyntaxtextarea:3.x`

### 6.4 数据序列化
- **方案**: Jackson (JSON)
- **原因**: 性能好，支持复杂对象映射
- **依赖**: `com.fasterxml.jackson.core:jackson-databind:2.x`

### 6.5 URL匹配算法
- **方案**: 基于优先级的有序列表 + 正则表达式缓存
- **时间复杂度**: O(n) 遍历规则列表
- **优化**: 使用ConcurrentHashMap缓存编译后的Pattern对象

## 7. 接口定义

### 7.1 响应处理器接口
```java
public interface ResponseHandler {
    /**
     * 检查是否支持该响应配置
     */
    boolean supports(ResponseConfig config);
    
    /**
     * 生成响应
     */
    MockResponse handle(MockContext context, ResponseConfig config) 
        throws MockProcessingException;
}
```

### 7.2 扩展接口（供用户扩展）
```java
public interface IMockHandler {
    /**
     * 处理请求并返回响应
     * @param request 请求对象
     * @return 响应字节数组
     */
    byte[] handle(MockRequest request);
}

public class MockRequest {
    private String url;
    private String method;
    private Map<String, String> headers;
    private byte[] body;
    // getters and setters...
}
```

## 8. 配置存储格式

### 8.1 配置文件路径
```
{BURP_CONFIG_DIR}/hellomocker/
├── config.json          # 全局配置
└── rules/
    ├── rule_001.json    # 规则文件
    ├── rule_002.json
    └── ...
```

### 8.2 全局配置示例
```json
{
  "version": "1.0",
  "pythonPath": "/usr/bin/python3",
  "defaultTimeout": 30000,
  "maxRules": 1000,
  "enableLogging": true
}
```

### 8.3 规则存储示例
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Admin API Mock",
  "enabled": true,
  "priority": 1,
  "matchCondition": {
    "type": "CONTAINS",
    "urlPattern": "/api/admin",
    "method": "GET"
  },
  "responseConfig": {
    "type": "PYTHON_SCRIPT",
    "pythonScript": "def handle_request(request):\n    return {...}",
    "pythonFilePath": null
  },
  "createdAt": "2026-03-03T10:00:00",
  "updatedAt": "2026-03-03T10:00:00"
}
```

## 9. 异常处理策略

### 9.1 异常分类
- **MockProcessingException**: 业务处理异常
- **ScriptExecutionException**: 脚本执行异常
- **ConfigurationException**: 配置错误异常
- **StorageException**: 存储操作异常

### 9.2 错误响应
当Mock处理失败时，返回包含错误信息的响应：
```json
{
  "status": 500,
  "headers": {"Content-Type": "text/plain"},
  "body": "Mock Processing Error: {error_message}\nStack trace:\n{stack_trace}"
}
```

## 10. 性能考虑

### 10.1 Python脚本执行
- 使用进程池复用Python进程
- 设置超时机制（默认30秒）
- 限制脚本执行内存（通过Python参数）

### 10.2 规则匹配
- 编译后的正则表达式缓存
- 优先级排序，常用规则前置
- 考虑使用Trie树优化（大量规则时）

### 10.3 内存管理
- 规则对象使用软引用缓存
- 大响应体使用流式处理
- 定期清理过期缓存

## 11. 安全考虑

### 11.1 脚本安全
- Python脚本在独立进程中执行
- 限制脚本执行时间
- 限制脚本内存使用
- 禁止危险操作（通过Python沙箱或白名单）

### 11.2 输入验证
- 验证URL模式合法性
- 验证Python脚本语法（可选）
- 防止JSON注入

### 11.3 权限控制
- 插件只在BurpSuite环境中运行
- 文件操作限制在配置目录内

## 12. 测试策略

### 12.1 单元测试
- 使用JUnit 5
- 测试覆盖：Model、Service、Util
- Mock外部依赖

### 12.2 集成测试
- 测试Python脚本执行
- 测试BurpSuite API集成
- 测试UI交互

### 12.3 性能测试
- 规则匹配性能
- Python脚本执行性能
- 并发处理能力

---

**文档版本**: 1.0
**创建日期**: 2026-03-03
**作者**: AI Assistant
**状态**: 已批准
