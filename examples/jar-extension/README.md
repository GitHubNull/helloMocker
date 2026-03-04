# HelloMocker JAR 扩展示例

这是一个完整的 HelloMocker JAR 扩展示例项目，展示了如何实现 `IMockHandler` 接口来创建自定义的 Mock 处理器。

## 📋 目录

- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [构建部署](#构建部署)
- [在插件中使用](#在插件中使用)
- [常见问题](#常见问题)

## 🚀 快速开始

### 1. 环境要求

- **Java**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **BurpSuite**: Professional 2023.x 或更高版本
- **HelloMocker**: 1.0.0 或更高版本

### 2. 构建步骤

```bash
# 1. 首先构建 HelloMocker 父项目（安装 API 到本地仓库）
cd ../..
mvn clean install

# 2. 进入示例项目目录
cd examples/jar-extension

# 3. 构建示例项目
mvn clean package

# 4. 生成的 JAR 文件位于 target/helloMocker-example-handler-1.0.0.jar
```

### 3. 在插件中使用

1. 在 HelloMocker 插件中创建/编辑规则
2. 选择 **Response Type** 为 `JAR_EXTENSION`
3. 配置：
   - **JAR File**: 选择 `target/helloMocker-example-handler-1.0.0.jar`
   - **Handler Class**: 输入 `com.example.ExampleHandler`
4. 点击 **Load** 加载处理器
5. 保存规则并测试

## 📁 项目结构

```
jar-extension/
├── pom.xml                           # Maven 配置
├── README.md                         # 本文件
└── src/main/java/com/example/
    └── ExampleHandler.java           # 示例处理器实现
```

## 📝 开发指南

### 实现 IMockHandler 接口

```java
package com.example;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.oxff.hellomocker.api.IMockHandler;

public class MyHandler implements IMockHandler {
    
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        // 处理请求逻辑
        String body = "{\"message\": \"Hello\"}";
        
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n\r\n" +
                body;
        
        return HttpResponse.httpResponse(response);
    }
    
    @Override
    public String getName() {
        return "My Handler";
    }
    
    @Override
    public void init() {
        // 初始化操作
    }
    
    @Override
    public void destroy() {
        // 清理操作
    }
}
```

### 访问请求信息

```java
@Override
public HttpResponse handleRequest(HttpRequest request) {
    // URL 信息
    String url = request.url();
    String method = request.method();
    
    // 请求头
    List<HttpHeader> headers = request.headers();
    for (HttpHeader header : headers) {
        String name = header.name();
        String value = header.value();
    }
    
    // 请求体
    ByteArray body = request.body();
    byte[] bodyBytes = body.getBytes();
    String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
    
    // ... 处理逻辑
}
```

### 构建响应

```java
private HttpResponse buildResponse(int statusCode, String contentType, String body) {
    StringBuilder sb = new StringBuilder();
    
    // 状态行
    sb.append("HTTP/1.1 ").append(statusCode).append(" OK\r\n");
    
    // 响应头
    sb.append("Content-Type: ").append(contentType).append("\r\n");
    sb.append("Content-Length: ").append(body.getBytes().length).append("\r\n");
    sb.append("\r\n");
    
    // 响应体
    sb.append(body);
    
    return HttpResponse.httpResponse(sb.toString());
}
```

## 🔨 构建部署

### Maven 配置要点

**依赖配置** (`pom.xml`):

```xml
<dependencies>
    <!-- HelloMocker API (provided scope) -->
    <dependency>
        <groupId>oxff.org</groupId>
        <artifactId>helloMocker-api</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>  <!-- 重要：provided 作用域 -->
    </dependency>
    
    <!-- Burp Montoya API (provided scope) -->
    <dependency>
        <groupId>net.portswigger.burp.extensions</groupId>
        <artifactId>montoya-api</artifactId>
        <version>2023.10.2</version>
        <scope>provided</scope>  <!-- 重要：provided 作用域 -->
    </dependency>
</dependencies>
```

**为什么使用 `provided` 作用域？**

- `helloMocker-api` 和 `montoya-api` 在运行时已由插件和 BurpSuite 提供
- 使用 `provided` 避免将这些依赖打包进你的 JAR
- 减少 JAR 体积，避免类冲突

### 打包配置

使用 Maven Shade Plugin 创建 fat JAR（如果你需要包含其他依赖）：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <configuration>
        <minimizeJar>true</minimizeJar>
        <artifactSet>
            <excludes>
                <!-- 排除 provided 依赖 -->
                <exclude>oxff.org:helloMocker-api</exclude>
                <exclude>net.portswigger.burp.extensions:montoya-api</exclude>
            </excludes>
        </artifactSet>
    </configuration>
</plugin>
```

### 构建命令

```bash
# 开发构建
mvn clean package

# 跳过测试（更快）
mvn clean package -DskipTests

# 安装到本地仓库
mvn clean install
```

## 🔌 在插件中使用

### 步骤 1：创建规则

1. 打开 HelloMocker 插件的 **Rules** 标签页
2. 点击 **Add Rule** 按钮
3. 配置基本信息：
   - **Name**: `My JAR Handler`
   - **Match Condition**: 设置 URL 匹配规则

### 步骤 2：配置 JAR 扩展

1. 在 **Response Config** 标签页，选择 **Response Type** 为 `JAR_EXTENSION`
2. 配置 **JAR Extension** 选项卡：
   - **JAR File**: 点击 Browse，选择你的 JAR 文件
   - **Handler Class**: 输入完整的类名（如 `com.example.ExampleHandler`）
3. 点击 **Load** 按钮加载处理器
4. 如果加载成功，状态会显示 "Loaded - [Handler Name]"

### 步骤 3：测试

1. 保存规则
2. 使用 BurpSuite Repeater 或浏览器发送匹配的请求
3. 查看响应是否符合预期

## ❓ 常见问题

### Q1: 加载 JAR 时提示 "Failed to load JAR"

**可能原因：**
- JAR 文件路径错误
- 类名输入不正确（需要完整类名，包含包名）
- 类没有实现 `IMockHandler` 接口
- 类没有 public 无参构造函数

**解决方法：**
1. 检查 JAR 文件路径是否正确
2. 确认类名格式为 `com.example.MyHandler`（不是 `MyHandler`）
3. 检查代码是否实现了 `IMockHandler` 接口
4. 添加 public 无参构造函数：
   ```java
   public MyHandler() {
       // 默认构造函数
   }
   ```

### Q2: 类找不到（ClassNotFoundException）

**原因：** 类没有被打包进 JAR

**解决方法：**
1. 检查 `pom.xml` 中的 `maven-shade-plugin` 配置
2. 确保你的类在 `src/main/java` 目录下
3. 重新构建：`mvn clean package`

### Q3: 请求处理时报错

**调试方法：**
1. 在 `handleRequest` 方法中添加 try-catch：
   ```java
   @Override
   public HttpResponse handleRequest(HttpRequest request) {
       try {
           // 你的逻辑
       } catch (Exception e) {
           e.printStackTrace();
           return createErrorResponse(e.getMessage());
       }
   }
   ```
2. 查看 BurpSuite 的 **Output** 窗口获取堆栈信息

### Q4: 如何更新处理器？

1. 修改代码
2. 重新构建：`mvn clean package`
3. 在插件中点击 **Unload** 卸载旧版本
4. 重新选择 JAR 文件
5. 点击 **Load** 加载新版本

### Q5: 可以同时加载多个 JAR 吗？

可以，每个规则可以配置不同的 JAR 文件和处理器类。

## 📚 参考资料

- [HelloMocker 主项目](../../README.md)
- [使用教程](../../doc/USER_GUIDE.md)
- [IMockHandler 接口](../../helloMocker-api/src/main/java/org/oxff/hellomocker/api/IMockHandler.java)
- [Burp Montoya API 文档](https://portswigger.github.io/burp-extensions-montoya-api/)

## 🤝 贡献

欢迎提交改进建议或更多示例！

---

**版本**: 1.0.0  
**更新日期**: 2026-03-03