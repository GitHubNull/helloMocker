# BurpSuite HTTP Mock 插件产品需求文档 (PRD)

## 1. 文档说明

### 1.1 文档目的
本文档旨在明确 BurpSuite HTTP Mock 插件的产品定义、功能需求及逻辑细节，为研发团队提供开发依据，确保项目按时、高质量交付。

### 1.2 版本修订记录
| 版本号 | 修订日期 | 修订人 | 修订内容 | 备注 |
| :--- | :--- | :--- | :--- | :--- |
| V1.0 | 2026-03-03 | 产品经理 | 初始版本创建 | 基于用户需求创建 |

## 2. 项目概述

### 2.1 项目背景
在安全测试与开发调试过程中，测试人员经常需要模拟特定的后端响应（Mock）以验证前端逻辑、绕过签名校验或测试异常分支。目前 BurpSuite 原生的请求拦截与修改功能操作繁琐，且难以通过脚本动态生成响应；现有的插件大多仅支持静态响应，缺乏对 Python/Java 动态逻辑及重定向转发的灵活支持。

### 2.2 项目价值
本项目旨在开发一款功能强大的 BurpSuite HTTP Mock 插件，通过支持 Python 脚本、Java JAR 包导入、请求转发及历史记录快速 Mock 等功能，大幅降低安全测试人员的环境依赖成本，提升渗透测试与漏洞挖掘的效率。

### 2.3 适用对象
*   渗透测试工程师
*   安全研究员
*   后端开发人员

## 3. 功能需求详细描述

### 3.1 功能总表
| 序号 | 功能模块 | 功能名称 | 优先级 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Mock管理 | Mock规则配置界面 | P0 | 提供UI界面用于管理所有的Mock规则，包括启用/禁用、匹配规则、响应类型等。 |
| 2 | 脚本引擎 | Python代码在线编写与执行 | P0 | 在插件界面直接编写Python代码处理请求并返回响应。 |
| 3 | 脚本引擎 | Python脚本文件导入 | P1 | 支持导入本地 .py 文件作为响应逻辑。 |
| 4 | 转发代理 | 请求重定向转发 | P1 | 将匹配的请求转发至指定上游服务器，支持由外部服务（如Python Flask/Django）处理。 |
| 5 | 扩展集成 | Java JAR包导入执行 | P2 | 支持加载用户编写的 JAR 包，执行内部的 Java 响应逻辑。 |
| 6 | 历史集成 | 历史记录快速Mock | P0 | 右键发送 HTTP 历史记录到 Mock 列表，支持原样返回或编辑后返回。 |

### 3.2 核心业务流程
1.  用户在 BurpSuite 中加载插件。
2.  用户创建 Mock 规则：配置 URL 匹配条件（正则/等于）。
3.  用户选择响应模式：
    *   **Python 脚本模式**：编写或导入脚本。
    *   **Java 扩展模式**：导入 JAR 包并指定处理类。
    *   **转发模式**：配置目标 IP/端口。
    *   **历史回放模式**：从历史记录加载。
4.  当 BurpSuite 拦截到请求或代理流经请求时，插件匹配规则。
5.  若匹配成功，执行对应逻辑，拦截原始请求并返回 Mock 响应。

### 3.3 功能模块详细设计

#### 3.3.1 Python 脚本响应模块
*   **界面设计**：在 Mock 规则编辑 Tab 下提供代码编辑器（支持语法高亮）。
*   **功能逻辑**：
    *   **在线编写**：用户可在编辑器中直接输入 Python 代码。插件需内置或调用本地 Python 环境（建议支持 Python 3.x）。
    *   **脚本导入**：提供“Import Script”按钮，加载本地 `.py` 文件，文件内容同步显示在编辑器中。
    *   **API 交互**：插件需向 Python 上下文注入请求对象（包含 Headers、Body、Method、URL），Python 脚本需返回标准的 HTTP Response 结构（Status Code、Headers、Body）。
    *   **异常处理**：若脚本执行出错，Mock 响应体应包含错误堆栈信息，方便调试。

#### 3.3.2 请求重定向转发模块
*   **配置项**：目标主机、目标端口、是否启用 SSL。
*   **功能逻辑**：
    *   插件将匹配的请求原样（或修改后）转发至配置的第三方服务地址。
    *   支持用户在第三方服务（如本地运行的 Python 脚本服务）中编写复杂逻辑。
    *   插件作为中间人，将第三方服务的响应原样返回给客户端。

#### 3.3.3 Java JAR 包导入模块
*   **功能逻辑**：
    *   支持在界面加载外部 JAR 文件。
    *   定义标准接口（Interface），例如 `IHttpMockHandler`，用户开发的 JAR 需实现该接口。
    *   插件通过反射机制实例化 JAR 中的处理类，传入 `IHttpRequestResponse` 对象，获取返回的响应数据。

#### 3.3.4 历史记录集成模块
*   **入口**：BurpSuite Proxy History 或 Logger++ 等日志列表的右键菜单。
*   **菜单项**：
    *   **"Send to Mock"**：将选中的请求添加到 Mock 列表。
*   **编辑能力**：
    *   **原样 Mock**：自动保存该请求对应的原始响应，规则激活时直接返回保存的响应。
    *   **编辑响应**：在 Mock 列表中双击条目，打开编辑器，允许用户手动修改状态码、响应头、响应体后再保存。

## 4. 非功能需求

### 4.1 性能要求
*   插件运行不应明显拖慢 BurpSuite 代理转发的速度。
*   Python 脚本执行建议采用进程池或长连接机制，避免频繁启动解释器带来的开销。
*   Mock 规则匹配算法需高效，建议使用 Hash Map 或 Trie 树处理大量规则。

### 4.2 兼容性
*   **BurpSuite 版本**：支持 BurpSuite v2023.x 及以上版本（基于 Montoya API 开发，以获得更好的扩展性）。
*   **Java 版本**：编译环境 JDK 17+（适配新版 BurpSuite）。
*   **Python 环境**：用户需本地配置 Python 环境，插件通过配置项指定 Python 解释器路径。

### 4.3 可维护性
*   插件需支持配置的导出与导入（JSON/XML格式），方便在不同环境间迁移 Mock 规则。

## 5. 附录

### 5.1 Python 脚本交互接口定义示例
```python
# 插件注入的上下文对象 request
def handle_request(request):
    # request.url, request.method, request.body, request.headers
    
    # 修改逻辑
    if "admin" in request.url:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": "{\"code\": 0, \"msg\": \"mock success\"}"
        }
    else:
        return {
            "status": 404,
            "headers": {},
            "body": "Not Found"
        }
```

### 5.2 Java 接口定义示例
```java
public interface IMockHandler {
    /**
     * 处理请求并返回响应
     * @param request 包含请求信息的对象
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