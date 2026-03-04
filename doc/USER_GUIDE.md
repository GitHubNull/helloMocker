# HelloMocker 使用教程

本文档详细介绍 HelloMocker BurpSuite HTTP Mock 插件的使用方法，帮助您快速上手并充分利用插件功能。

---

## 目录

1. [环境准备](#环境准备)
2. [安装插件](#安装插件)
3. [界面概览](#界面概览)
4. [创建 Mock 规则](#创建-mock-规则)
5. [响应类型详解](#响应类型详解)
6. [Python 脚本开发](#python-脚本开发)
7. [从 Proxy History 创建规则](#从-proxy-history-创建规则)
8. [规则管理](#规则管理)
9. [配置设置](#配置设置)
10. [导入导出规则](#导入导出规则)
11. [常见问题](#常见问题)

---

## 环境准备

### 系统要求

| 组件 | 最低版本 | 说明 |
|------|----------|------|
| BurpSuite Professional | 2023.x | 基于 Montoya API 开发 |
| Java | 17+ | 运行环境 |
| Python | 3.x | 用于执行 Python 脚本（可选） |

### 检查 Python 环境

在终端中执行以下命令检查 Python 是否已安装：

```bash
# Windows
python --version
# 或
python3 --version

# Linux/macOS
python3 --version
```

如果未安装，请从 [Python 官网](https://www.python.org/downloads/) 下载安装。

---

## 安装插件

### 方式一：从 Release 下载（推荐）

1. 访问项目的 [Releases](https://github.com/GitHubNull/helloMocker/releases) 页面
2. 下载最新版本的 `helloMocker-1.0.0.jar` 文件
3. 打开 BurpSuite，进入 **Extensions** → **Installed**
4. 点击 **Add** 按钮
5. 在弹出的对话框中：
   - Extension type: 选择 **Java**
   - Extension file: 点击 **Select file**，选择下载的 JAR 文件
6. 点击 **Next**，等待插件加载完成
7. 看到 "Loaded successfully" 提示即表示安装成功

### 方式二：从源码构建

```bash
# 克隆仓库
git clone https://github.com/GitHubNull/helloMocker.git

# 进入项目目录
cd helloMocker

# 构建 JAR 包
mvn clean package -DskipTests

# 构建完成后，JAR 文件位于 target/helloMocker-1.0.0.jar
```

### 验证安装

安装成功后，在 BurpSuite 中应该能看到：
- 顶部菜单栏出现 **HelloMocker** 标签页
- Extensions 列表中显示 HelloMocker 插件信息

---

## 界面概览

HelloMocker 插件主界面分为三个标签页：

### 1. Rules（规则列表）

这是主要的工作区域，包含：

- **工具栏**：添加规则、导入/导出、启用/禁用规则等操作按钮
- **规则表格**：显示所有 Mock 规则的列表
  - 名称：规则的名称
  - URL 模式：匹配的 URL 模式
  - 匹配类型：Equals/Contains/Regex/StartsWith/EndsWith
  - 响应类型：Static/Python/Proxy
  - 状态：启用/禁用
  - 优先级：数字越小优先级越高
- **操作按钮**：编辑、删除、复制规则

### 2. Settings（设置）

配置插件的全局参数：

- **Python 路径**：Python 解释器的可执行文件路径
- **脚本超时**：Python 脚本执行的最大时间（毫秒）
- **最大规则数**：允许创建的最大规则数量
- **启用日志**：是否输出详细日志

### 3. Help（帮助）

显示快速帮助信息和版本信息。

---

## 创建 Mock 规则

### 基本步骤

1. 切换到 **HelloMocker** → **Rules** 标签页
2. 点击工具栏的 **Add Rule** 按钮
3. 在弹出的规则编辑器中配置规则信息
4. 点击 **Save** 保存规则

### 规则编辑器详解

规则编辑器分为三个区域：

#### 基本信息

| 字段 | 说明 | 示例 |
|------|------|------|
| 规则名称 | 给规则起个名字 | "API Mock - Admin" |
| 启用状态 | 是否立即启用该规则 | 勾选表示启用 |
| 优先级 | 数字越小优先级越高，匹配时按优先级排序 | 1（最高优先级） |

#### 匹配条件

| 字段 | 说明 | 示例 |
|------|------|------|
| URL 模式 | 要匹配的 URL 字符串 | `/api/admin` |
| 匹配类型 | URL 匹配方式 | Contains |
| HTTP 方法 | 可选，指定匹配的方法 | GET（留空匹配所有） |

**匹配类型说明**：

- **Equals**：URL 完全相等
- **Contains**：URL 包含指定字符串
- **Regex**：使用正则表达式匹配
- **StartsWith**：URL 以指定字符串开头
- **EndsWith**：URL 以指定字符串结尾

#### 响应配置

选择四种响应类型之一：

1. **静态响应**：直接返回配置的状态码、响应头和响应体
2. **Python 脚本**：执行 Python 文件动态生成响应
3. **代理转发**：将请求转发到上游服务器
4. **JAR 扩展**：加载自定义 JAR 包处理请求（Stage 6）

---

## 响应类型详解

### 1. 静态响应（Static Response）

适用于返回固定的响应内容。

**配置项**：

| 字段 | 说明 | 示例 |
|------|------|------|
| 状态码 | HTTP 状态码 | 200 |
| 响应头 | 每行一个，格式为 `Name: Value` | `Content-Type: application/json` |
| 响应体 | 返回的响应内容 | `{"code": 0, "msg": "success"}` |

**示例场景**：
- 模拟 API 接口返回固定数据
- 返回错误页面进行异常测试
- 快速验证前端对特定响应的处理

### 2. Python 脚本（Python Script）

适用于需要动态生成响应的场景。

**配置项**：

| 字段 | 说明 |
|------|------|
| 脚本文件 | 选择本地的 `.py` 文件 |
| 脚本预览 | 显示选中的脚本内容（只读） |

**工作原理**：
1. 当请求匹配规则时，插件调用 Python 解释器执行脚本
2. 将请求信息以 JSON 格式传递给脚本的 `handle_request` 函数
3. 脚本返回响应字典，插件将其转换为 HTTP 响应

**适用场景**：
- 根据请求参数返回不同响应
- 模拟复杂的业务逻辑
- 动态生成测试数据
- 实现请求签名验证/绕过

### 3. 代理转发（Proxy Forward）

适用于将请求转发到其他服务器处理。

**配置项**：

| 字段 | 说明 | 示例 |
|------|------|------|
| 目标主机 | 转发到的服务器地址 | `127.0.0.1` |
| 目标端口 | 服务器端口 | `8081` |
| 使用 SSL | 是否使用 HTTPS 连接 | 不勾选 |

**工作原理**：
1. 当请求匹配规则时，插件将请求转发到配置的目标服务器
2. 目标服务器处理请求并返回响应
3. 插件将响应原样返回给客户端

**适用场景**：
- 将请求转发到本地开发服务器
- 使用外部服务（如 Flask/Django）处理请求
- 实现复杂的 Mock 逻辑而不修改插件

### 4. JAR 扩展（JAR Extension）

适用于需要 Java 代码处理请求的场景（P2 功能）。

**配置项**：

| 字段 | 说明 | 示例 |
|------|------|------|
| JAR 文件 | 包含处理器的 JAR 包路径 | `/path/to/myhandler.jar` |
| 处理器类 | 实现 IMockHandler 接口的类全名 | `com.example.MyHandler` |

**工作原理**：
1. 插件使用 URLClassLoader 动态加载 JAR 文件
2. 通过反射创建处理器实例（需实现 IMockHandler 接口）
3. 调用处理器的 `handleRequest()` 方法处理请求
4. 将处理器返回的响应返回给客户端

**适用场景**：
- 需要 Java 代码实现的复杂业务逻辑
- 需要复用现有的 Java 库或框架
- 需要更好的性能和类型安全
- 团队协作开发 Mock 处理器

**开发步骤**：
1. 创建 Maven/Gradle 项目
2. 添加依赖（Burp Montoya API 和插件 API）
3. 实现 `IMockHandler` 接口
4. 打包为 JAR 文件
5. 在插件中配置 JAR 路径和类名

**示例处理器代码**：

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
        
        // 自定义处理逻辑
        String body = String.format(
            "{\"url\": \"%s\", \"method\": \"%s\", \"handled_by\": \"JAR\"}",
            url, method
        );
        
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n\r\n" +
                body;
        
        return HttpResponse.httpResponse(response);
    }
    
    @Override
    public String getName() {
        return "My Custom Handler";
    }
    
    @Override
    public void init() {
        // 初始化操作，如加载配置
    }
    
    @Override
    public void destroy() {
        // 清理操作
    }
}
```

**接口说明**：

- `handleRequest(HttpRequest request)`: 核心方法，处理 HTTP 请求并返回响应
- `getName()`: 返回处理器名称（用于显示）
- `getDescription()`: 返回处理器描述
- `init()`: 初始化回调，在加载 JAR 后调用
- `destroy()`: 销毁回调，在卸载 JAR 前调用

---

## Python 脚本开发

### 脚本模板

创建一个 `.py` 文件，包含以下结构：

```python
def handle_request(request):
    """
    处理 HTTP 请求并返回响应
    
    Args:
        request: 请求字典，包含以下字段：
            - url: 完整 URL
            - method: HTTP 方法（GET/POST 等）
            - headers: 请求头字典
            - body: 请求体字符串
            - body_base64: Base64 编码的请求体
            - path: URL 路径
            - query: 查询字符串
            - host: 主机名
            - port: 端口
            - protocol: 协议（http/https）
    
    Returns:
        dict: 响应字典，包含以下字段：
            - status: HTTP 状态码（整数）
            - headers: 响应头字典
            - body: 响应体字符串
            - body_base64: Base64 编码的响应体（可选）
            - delay: 延迟时间（毫秒，可选）
    """
    # 在这里编写你的逻辑
    pass
```

### 示例脚本

#### 示例 1：根据 URL 路径返回不同响应

```python
def handle_request(request):
    path = request.get("path", "")
    
    if "/api/admin" in path:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": '{"code": 0, "message": "success", "data": {"role": "admin"}}'
        }
    elif "/api/user" in path:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": '{"code": 0, "message": "success", "data": {"role": "user"}}'
        }
    
    return {
        "status": 404,
        "headers": {"Content-Type": "text/plain"},
        "body": "Not Found"
    }
```

#### 示例 2：读取请求参数并返回

```python
import json
import urllib.parse

def handle_request(request):
    query_string = request.get("query", "")
    params = urllib.parse.parse_qs(query_string)
    
    # 获取特定参数
    user_id = params.get("id", ["unknown"])[0]
    
    response_data = {
        "code": 0,
        "message": "success",
        "data": {
            "user_id": user_id,
            "received_params": dict(params)
        }
    }
    
    return {
        "status": 200,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(response_data)
    }
```

#### 示例 3：模拟延迟响应

```python
def handle_request(request):
    import time
    
    # 模拟 2 秒延迟
    time.sleep(2)
    
    return {
        "status": 200,
        "headers": {"Content-Type": "application/json"},
        "body": '{"code": 0, "message": "delayed response"}',
        "delay": 2000  # 也可以在这里指定延迟（毫秒）
    }
```

#### 示例 4：处理 POST 请求体

```python
import json

def handle_request(request):
    method = request.get("method", "")
    body = request.get("body", "")
    
    if method == "POST":
        try:
            # 尝试解析 JSON 请求体
            data = json.loads(body)
            username = data.get("username", "unknown")
            
            return {
                "status": 200,
                "headers": {"Content-Type": "application/json"},
                "body": json.dumps({
                    "code": 0,
                    "message": f"Hello, {username}!"
                })
            }
        except json.JSONDecodeError:
            return {
                "status": 400,
                "headers": {"Content-Type": "application/json"},
                "body": '{"code": -1, "message": "Invalid JSON"}'
            }
    
    return {
        "status": 405,
        "headers": {"Content-Type": "text/plain"},
        "body": "Method Not Allowed"
    }
```

### 调试技巧

1. **查看错误信息**：脚本执行出错时，响应体会包含错误堆栈信息
2. **使用日志**：可以在脚本中使用 `print()` 输出调试信息（会显示在 BurpSuite 的 Output 窗口）
3. **本地测试**：先在本地命令行测试脚本逻辑，确认无误后再导入插件

---

## 从 Proxy History 创建规则

这是最快捷的创建 Mock 规则的方式。

### 操作步骤

1. 在 BurpSuite 中找到 **Proxy** → **HTTP history** 标签页
2. 找到你想要 Mock 的请求
3. 右键点击该请求
4. 在弹出的菜单中选择 **Send to Mock**
5. 根据设置，插件会：
   - **启用弹窗编辑**（默认）：打开规则编辑器，预填充请求信息，可直接编辑后保存
   - **禁用弹窗编辑**：自动创建静态 Mock 规则，不显示编辑器

### 弹窗编辑模式（推荐）

当 **Show Send to Mock Dialog** 选项启用时：

1. 右键点击请求，选择 **Send to Mock**
2. 弹出规则编辑器，已预填充：
   - 规则名称：基于 URL 自动生成（自动截断超长 URL）
   - 匹配条件：设置为该请求的 URL（Contains 匹配）
   - 响应配置：使用该请求的实际响应内容
   - HTTP 编辑器：使用 Burp 原生编辑器显示响应，支持 Raw/Headers/Hex 视图
3. 在编辑器中可以：
   - 修改规则名称、匹配条件
   - 在 HTTP 编辑器中编辑响应内容（支持中文，自动识别编码）
   - 切换响应类型（Static/Python/Proxy/JAR）
4. 点击 **Save** 保存规则

**优势**：
- 创建前可预览和编辑响应内容
- 避免创建不符合需求的规则
- 支持直接修改响应体中的中文内容（UTF-8 编码）

### 静默创建模式

当 **Show Send to Mock Dialog** 选项禁用时：

1. 右键点击请求，选择 **Send to Mock**
2. 插件自动创建规则，不显示编辑器
3. 显示成功提示
4. 可在 Rules 标签页查看和编辑

### 配置弹窗选项

在 **Settings** 标签页中：

- **Show Send to Mock Dialog**：勾选启用弹窗编辑，取消勾选静默创建
- 默认：启用（推荐）

**说明**：弹窗编辑模式下，规则名称会自动截断：
- 移除 URL 查询参数（`?key=value` 部分）
- 如果路径超过 30 字符，保留最后 30 字符（前面加 `...`）
- 示例：`/api/v1/users/12345/profile?token=xxx` → `...sers/12345/profile`

### 使用场景

- 快速保存某个 API 的响应用于后续测试
- 基于真实响应创建 Mock，然后修改特定字段进行测试
- 批量创建多个 API 的 Mock 规则

---

## 规则管理

### 工具栏按钮

规则列表工具栏提供以下操作按钮：

| 按钮 | 功能 | 说明 |
|------|------|------|
| **Add** | 添加规则 | 打开规则编辑器创建新规则 |
| **Import** | 导入规则 | 从 JSON 文件导入规则 |
| **Export** | 导出规则 | 将规则导出为 JSON 文件 |
| **Edit** | 编辑规则 | 编辑选中的规则（双击也可打开） |
| **Delete** | 删除规则 | 删除选中的规则 |
| **Up** | 上移 | 将选中规则在列表中上移一位（调整显示顺序） |
| **Down** | 下移 | 将选中规则在列表中下移一位（调整显示顺序） |

**注意**：Edit、Delete、Up、Down 按钮需要先在表格中选中规则才会启用。

### 启用/禁用规则

- 在规则表格中，勾选/取消勾选 **启用** 列的复选框
- 或者选中规则后，点击工具栏的 **Enable** / **Disable** 按钮

### 编辑规则

1. 在规则表格中选中要编辑的规则
2. 点击工具栏的 **Edit** 按钮（或双击规则行）
3. 在弹出的编辑器中修改配置
4. 点击 **Save** 保存

### 删除规则

1. 在规则表格中选中要删除的规则
2. 点击工具栏的 **Delete** 按钮
3. 在确认对话框中点击 **Yes** 确认删除

### 复制规则

1. 在规则表格中选中要复制的规则
2. 点击工具栏的 **Duplicate** 按钮
3. 会创建一个完全相同的规则（名称会加上 "Copy" 后缀）

### 调整显示顺序

使用 **Up** 和 **Down** 按钮可以调整规则在列表中的显示顺序：

1. 选中要移动的规则
2. 点击 **Up** 按钮将规则上移一位
3. 点击 **Down** 按钮将规则下移一位

**注意**：此操作仅调整显示顺序，不影响规则匹配优先级。

### 调整优先级

规则的优先级决定了匹配顺序：
- 数字越小，优先级越高
- 匹配时会按优先级从高到低依次检查
- 第一个匹配的规则会被使用

调整优先级的方法：
1. 编辑规则，修改 **优先级** 字段
2. 优先级可以是任意整数，建议使用 1, 2, 3... 的顺序

---

## 配置设置

### 配置 Python 路径

1. 切换到 **HelloMocker** → **Settings** 标签页
2. 找到 **Python 路径** 配置项
3. 输入 Python 解释器的完整路径：
   - Windows: `C:\Python39\python.exe` 或 `C:\Users\用户名\AppData\Local\Programs\Python\Python39\python.exe`
   - Linux/macOS: `/usr/bin/python3` 或 `/usr/local/bin/python3`
4. 点击 **Test** 按钮验证路径是否正确
5. 看到 "Python is working!" 提示表示配置成功

### 其他配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| 脚本超时 | Python 脚本执行的最大时间（毫秒），超时会被终止 | 30000 |
| 最大规则数 | 允许创建的最大规则数量，防止内存溢出 | 1000 |
| 启用日志 | 是否输出详细的调试日志 | true |
| 显示 Send to Mock 弹窗 | 右键 Send to Mock 后是否显示编辑弹窗 | true |

### 配置保存

所有配置会自动保存，下次启动 BurpSuite 时会自动加载。

---

## 导入导出规则

### 导出规则

1. 在 Rules 标签页，点击工具栏的 **Export** 按钮
2. 选择保存位置，输入文件名
3. 规则会以 JSON 格式保存到指定文件

**导出文件格式示例**：

```json
[
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
      "type": "STATIC",
      "statusCode": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "body": "{\"code\": 0, \"msg\": \"success\"}"
    }
  }
]
```

### 导入规则

1. 在 Rules 标签页，点击工具栏的 **Import** 按钮
2. 选择之前导出的 JSON 文件
3. 插件会读取文件并导入所有规则
4. 如果存在相同 ID 的规则，会提示是否覆盖

### 使用场景

- **团队协作**：将 Mock 规则分享给团队成员
- **环境迁移**：在不同机器间迁移配置
- **版本控制**：将规则文件提交到 Git 仓库
- **备份恢复**：定期备份规则配置

---

## 常见问题

### Q1: 插件安装失败，提示 "Extension failed to load"

**可能原因及解决方法**：

1. **Java 版本不兼容**
   - 检查 Java 版本：`java -version`
   - 确保使用 Java 17 或更高版本

2. **JAR 文件损坏**
   - 重新下载或构建 JAR 文件
   - 验证文件完整性

3. **BurpSuite 版本过低**
   - 确保 BurpSuite Professional 2023.x 或更高版本

### Q2: Python 脚本执行失败

**可能原因及解决方法**：

1. **Python 路径配置错误**
   - 在 Settings 中检查 Python 路径
   - 点击 Test 按钮验证

2. **脚本语法错误**
   - 检查脚本是否有语法错误
   - 在本地先用 `python script.py` 测试

3. **缺少依赖库**
   - 确保脚本依赖的库已安装
   - 使用 `pip install 库名` 安装

4. **脚本超时**
   - 增加脚本超时时间设置
   - 优化脚本执行效率

### Q3: Mock 规则没有生效

**可能原因及解决方法**：

1. **规则未启用**
   - 检查规则的启用状态

2. **匹配条件不正确**
   - 检查 URL 模式和匹配类型
   - 使用 Contains 匹配时确保包含正确的字符串

3. **优先级问题**
   - 检查是否有其他高优先级规则先匹配
   - 调整当前规则的优先级

4. **BurpSuite 拦截设置**
   - 确保请求经过 BurpSuite 代理
   - 检查拦截设置是否正确

### Q4: 如何调试 Python 脚本？

**调试方法**：

1. **使用 print 输出**：
   ```python
   def handle_request(request):
       print(f"Received request: {request['url']}")
       print(f"Method: {request['method']}")
       # ... 其他逻辑
   ```
   输出会显示在 BurpSuite 的 **Output** 窗口。

2. **查看错误响应**：脚本出错时，响应体会包含完整的错误堆栈。

3. **本地测试**：
   ```python
   # 在脚本末尾添加测试代码
   if __name__ == "__main__":
       test_request = {
           "url": "http://example.com/api/test",
           "method": "GET",
           "headers": {},
           "body": ""
       }
       print(handle_request(test_request))
   ```

### Q5: 支持哪些 Python 库？

插件使用系统 Python 解释器执行脚本，因此：
- 所有标准库都可以使用
- 已安装的第三方库可以使用
- 可以使用 `pip` 安装需要的库

常用库示例：
- `json`：JSON 处理
- `re`：正则表达式
- `urllib.parse`：URL 解析
- `base64`：Base64 编码/解码
- `hashlib`：哈希计算
- `random`：随机数生成
- `datetime`：日期时间处理

### Q6: 规则匹配的顺序是怎样的？

规则按照以下顺序匹配：

1. 按 **优先级** 排序（数字越小越优先）
2. 相同优先级按 **创建时间** 排序（先创建的优先）
3. 依次检查每条规则的匹配条件
4. 第一个匹配的规则会被使用，后续规则不再检查

**建议**：
- 将更具体的规则设置更高的优先级（更小的数字）
- 将通用的规则设置较低的优先级

### Q7: 如何清空所有规则？

目前插件没有一键清空功能，可以通过以下方式：

1. **手动删除**：选中规则后点击 Delete 按钮逐个删除
2. **导入空文件**：创建一个包含空数组 `[]` 的 JSON 文件并导入

### Q8: JAR 扩展加载失败怎么办？

**可能原因及解决方法**：

1. **JAR 文件路径错误**
   - 检查 JAR 文件路径是否正确
   - 使用 Browse 按钮选择文件

2. **类名不正确**
   - 确保输入的是完整的类全名（包含包名）
   - 示例：`com.example.MyHandler` 而不是 `MyHandler`

3. **未实现 IMockHandler 接口**
   - 检查类是否实现了 `IMockHandler` 接口
   - 查看 JAR 是否包含插件 API 依赖

4. **缺少无参构造函数**
   - 确保处理器类有 public 无参构造函数

5. **依赖冲突**
   - 检查 JAR 是否包含与 BurpSuite 冲突的依赖
   - 使用 Maven Shade 插件打包时可排除冲突依赖

### Q9: 中文显示乱码怎么办？

**解决方法**：

1. **确保 Content-Type 包含 charset=UTF-8**
   - 在响应头中添加：`Content-Type: application/json; charset=UTF-8`

2. **导出/导入时使用 UTF-8 编码**
   - 插件会自动处理编码，确保编辑器和文件都使用 UTF-8

3. **Python 脚本中使用 UTF-8**
   - 文件开头添加：`# -*- coding: utf-8 -*-`
   - 中文字符串使用 Unicode：`u"中文"`

4. **Burp 编辑器中中文乱码**
   - 插件已自动添加 charset=UTF-8
   - 确保原始响应包含正确的 charset 声明

---

## 最佳实践

### 1. 规则命名规范

使用有意义的规则名称，便于管理：
- `API - User Login`：用户登录接口
- `API - Admin - Get Users`：管理员获取用户列表
- `Error - 500 Test`：500 错误测试

### 2. 合理使用优先级

- 1-10：核心业务接口
- 11-50：普通 API 接口
- 51-100：测试和调试规则
- 100+：通用兜底规则

### 3. Python 脚本组织

- 将脚本放在专门的目录中
- 使用版本控制管理脚本
- 添加注释说明脚本用途

### 4. 定期备份

- 定期导出规则配置
- 将规则文件纳入版本控制
- 记录重要的配置变更

### 5. 测试验证

- 创建规则后及时测试验证
- 使用 BurpSuite Repeater 发送测试请求
- 检查响应是否符合预期

---

## 获取帮助

如果在使用过程中遇到问题：

1. 查看本教程的常见问题部分
2. 查阅项目的 [GitHub Issues](https://github.com/GitHubNull/helloMocker/issues)
3. 提交新的 Issue，描述问题和复现步骤

---

**文档版本**: 1.0  
**最后更新**: 2026-03-03
