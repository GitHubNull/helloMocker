# HelloMocker API 使用示例

这是一个完整的 **HelloMocker JAR 扩展** 示例项目，展示了如何实现 `IMockHandler` 接口来创建自定义的 Mock 处理器。

## 📋 功能特性

本示例展示了以下功能：

- ✅ **多路由支持** - 根据 URL 路径返回不同响应
- ✅ **请求信息解析** - URL、Method、Headers、Body
- ✅ **查询参数解析** - 支持 `?page=1&limit=10` 格式
- ✅ **多种响应格式** - JSON、错误响应、延迟响应
- ✅ **请求统计** - 统计每个端点的访问次数
- ✅ **生命周期管理** - init() 和 destroy() 回调
- ✅ **异常处理** - 完整的错误处理和日志输出

## 🚀 快速开始

### 1. 环境要求

- **Java**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **BurpSuite**: Professional 2023.x 或更高版本
- **HelloMocker**: 1.0.0 或更高版本（已安装）

### 2. 构建步骤

```bash
# 步骤 1：先安装 HelloMocker 主项目到本地 Maven 仓库
# （在父目录执行）
cd ..
mvn clean install

# 步骤 2：进入示例项目目录
cd helloMocker-api-example

# 步骤 3：构建示例项目
mvn clean package

# 步骤 4：验证生成的 JAR 文件
ls -lh target/*.jar
# 应该看到：helloMocker-api-example-1.0.0.jar
```

### 3. 在 HelloMocker 插件中使用

1. **打开 HelloMocker 插件**
   - 在 BurpSuite 中切换到 HelloMocker 标签页

2. **创建 Mock 规则**
   - 点击 **Add Rule** 按钮
   - 配置规则名称，例如：`Example Handler Test`
   - 设置匹配条件，例如：
     - URL Pattern: `/api/`
     - Match Type: `Contains`

3. **配置 JAR 扩展**
   - 在 **Response Config** 选项卡，选择 **Response Type**: `JAR_EXTENSION`
   - 配置以下参数：
     - **JAR File**: 点击 Browse，选择 `target/helloMocker-api-example-1.0.0.jar`
     - **Handler Class**: 输入 `com.example.ExampleHandler`
   - 点击 **Load** 按钮加载处理器
   - 如果成功，会显示状态：`Loaded - ExampleHandler-v1.0`

4. **保存并测试**
   - 点击 **Save** 保存规则
   - 使用 BurpSuite Repeater 或浏览器发送请求测试

## 📡 API 端点列表

本示例提供了以下 REST API 端点：

### 1. GET /api/hello
**简单的问候接口**

```bash
curl http://example.com/api/hello
```

**响应示例：**
```json
{
  "message": "Hello from HelloMocker JAR Extension!",
  "handler": "ExampleHandler-v1.0",
  "requestCount": 1,
  "timestamp": "2026-03-04T10:30:00"
}
```

### 2. GET /api/users
**用户列表接口（支持分页）**

```bash
# 默认分页
curl http://example.com/api/users

# 指定分页参数
curl "http://example.com/api/users?page=2&limit=5"
```

**响应示例：**
```json
{
  "users": [
    {"id": 1, "name": "User 1", "email": "user1@example.com"},
    {"id": 2, "name": "User 2", "email": "user2@example.com"}
  ],
  "page": 1,
  "limit": 10,
  "total": 100,
  "requestCount": 5
}
```

### 3. GET /api/users/{id}
**用户详情接口**

```bash
curl http://example.com/api/users/123
```

**响应示例：**
```json
{
  "id": "123",
  "name": "User 123",
  "email": "user123@example.com",
  "role": "member",
  "createdAt": "2024-01-01T00:00:00",
  "requestCount": 3
}
```

**测试 404 错误：**
```bash
curl http://example.com/api/users/999
# 返回 404 Not Found
```

### 4. POST /api/echo
**回显请求信息**

```bash
curl -X POST http://example.com/api/echo \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'
```

**响应示例：**
```json
{
  "echo": true,
  "method": "POST",
  "url": "http://example.com/api/echo",
  "headers": {...},
  "body": "{\"test\": \"data\"}",
  "requestCount": 2
}
```

### 5. GET /api/delay
**延迟响应（模拟慢接口）**

```bash
curl http://example.com/api/delay
# 等待 2 秒后返回响应
```

**响应示例：**
```json
{
  "message": "Delayed response after 2 seconds",
  "requestCount": 1
}
```

### 6. GET /api/error
**模拟错误响应（500）**

```bash
curl http://example.com/api/error
# 返回 HTTP 500 错误
```

**响应示例：**
```json
{
  "error": "Internal Server Error",
  "message": "This is a simulated error",
  "code": 500,
  "requestCount": 1
}
```

### 7. GET /api/stats
**请求统计信息**

```bash
curl http://example.com/api/stats
```

**响应示例：**
```json
{
  "handler": "ExampleHandler-v1.0",
  "totalRequests": 15,
  "uniqueEndpoints": 4,
  "startTime": "2026-03-04T10:00:00",
  "endpoints": {
    "/api/hello": 5,
    "/api/users": 3,
    "/api/echo": 4,
    "/api/delay": 3
  }
}
```

## 📦 开发自己的处理器

### 步骤 1：复制示例项目

```bash
# 复制整个示例项目作为基础
cp -r helloMocker-api-example my-custom-handler
cd my-custom-handler
```

### 步骤 2：修改项目配置

编辑 `pom.xml`：
```xml
<artifactId>my-custom-handler</artifactId>
<name>My Custom Handler</name>
<description>My custom HelloMocker JAR extension</description>
```

### 步骤 3：实现自己的处理器

编辑 `src/main/java/com/example/ExampleHandler.java`：

```java
package com.example;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.oxff.hellomocker.api.IMockHandler;

public class MyHandler implements IMockHandler {
    
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        // 获取请求信息
        String url = request.url();
        String method = request.method();
        
        // 实现自己的逻辑
        String responseBody = "{\"message\": \"Hello from MyHandler!\"}";
        
        // 构建 HTTP 响应
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n\r\n" +
                responseBody;
        
        return HttpResponse.httpResponse(httpResponse);
    }
    
    @Override
    public String getName() {
        return "MyHandler";
    }
    
    @Override
    public void init() {
        System.out.println("MyHandler initialized!");
    }
    
    @Override
    public void destroy() {
        System.out.println("MyHandler destroyed!");
    }
}
```

### 步骤 4：构建并测试

```bash
mvn clean package
# 生成的 JAR：target/my-custom-handler-1.0.0.jar
```

## 🔧 常见问题

### Q1: 构建时提示找不到 helloMocker 依赖

**错误信息：**
```
Could not find artifact oxff.org:helloMocker:jar:1.0.0
```

**解决方法：**
先安装主项目到本地 Maven 仓库：
```bash
cd ..  # 回到父目录（helloMocker/）
mvn clean install
```

### Q2: 加载 JAR 时提示 "Failed to load JAR"

**可能原因：**
1. JAR 文件路径不正确
2. Handler Class 名称输入错误（需要完整类名）
3. 类没有实现 `IMockHandler` 接口
4. 类没有 public 无参构造函数

**检查清单：**
- ✅ JAR 文件存在且可访问
- ✅ Handler Class: `com.example.ExampleHandler`（不是 `ExampleHandler`）
- ✅ 类实现了 `IMockHandler` 接口
- ✅ 类有 `public ExampleHandler()` 构造函数

### Q3: 如何在 BurpSuite 中查看处理器的日志输出？

**方法：**
1. 在 BurpSuite 中，点击顶部菜单 **Window** → **Application** → **Extensions**
2. 或者查看 **Output** 标签页（如果有）
3. 日志格式：`[ExampleHandler-v1.0] xxxxx`

### Q4: 如何调试处理器代码？

**方法 1：使用 try-catch**
```java
@Override
public HttpResponse handleRequest(HttpRequest request) {
    try {
        // 你的代码
    } catch (Exception e) {
        e.printStackTrace();
        return buildErrorResponse(e.getMessage());
    }
}
```

**方法 2：使用 System.out.println**
```java
System.out.println("[MyHandler] Request URL: " + request.url());
```

**方法 3：IDE 远程调试**（高级）
在 Maven 构建时启用调试参数，然后使用 IDE 连接调试。

### Q5: 如何更新已加载的处理器？

**步骤：**
1. 修改代码
2. 重新构建：`mvn clean package`
3. 在 HelloMocker 插件中：
   - 点击 **Unload** 卸载旧版本
   - 重新选择 JAR 文件
   - 点击 **Load** 加载新版本

## 📚 参考资源

- [HelloMocker 主项目 README](../README.md)
- [使用教程](../doc/USER_GUIDE.md)
- [IMockHandler 接口源码](../src/main/java/org/oxff/hellomocker/api/IMockHandler.java)
- [Burp Montoya API 文档](https://portswigger.github.io/burp-extensions-montoya-api/)

## 📄 许可证

与主项目一致：[MIT License](../LICENSE)

---

**版本**: 1.0.0  
**更新日期**: 2026-03-04