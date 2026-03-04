# HelloMocker - BurpSuite HTTP Mock 插件

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![BurpSuite](https://img.shields.io/badge/BurpSuite-2023%2B-green.svg)](https://portswigger.net/burp)

HelloMocker 是一款功能强大的 BurpSuite HTTP Mock 插件，支持通过 Python 脚本动态生成响应，帮助安全测试人员快速模拟后端服务。

## ✨ 功能特性

- 🎯 **Mock 规则管理**：支持创建、编辑、删除、启用/禁用规则
- 🔍 **灵活匹配**：支持 5 种 URL 匹配类型（等于、包含、正则、开头、结尾）
- 📝 **多种响应模式**：
  - 静态响应：直接返回配置的响应内容
  - Python 脚本：执行 Python 文件动态生成响应
  - 代理转发：将请求转发到上游服务器
  - JAR 扩展：加载自定义 JAR 包处理请求（Stage 6）
- 📂 **Python 脚本支持**：从文件导入 Python 脚本，支持查看脚本内容
- 📦 **JAR 扩展支持**：加载自定义 JAR 包实现复杂业务逻辑（P2 功能）
- 🖱️ **右键快捷操作**：在 Proxy History 中右键快速创建 Mock 规则，支持弹窗编辑
- 🎨 **Burp 原生编辑器**：使用 BurpSuite 原生 HTTP 编辑器编辑响应内容
- 📝 **智能 URL 处理**：自动截断超长 URL，防止规则名称过长
- 💾 **配置持久化**：规则以 JSON 格式保存，支持导入导出，body 使用 Base64 编码

## 📸 界面预览

> 📷 截图占位符
> 
> ![规则列表面面](docs/screenshots/rule_list.png)
> 
> *Mock 规则列表面面，支持增删改查*

> 📷 截图占位符
> 
> ![规则编辑器](docs/screenshots/rule_editor.png)
> 
> *规则编辑器，配置 URL 匹配和响应*

> 📷 截图占位符
> 
> ![Python 脚本配置](docs/screenshots/python_config.png)
> 
> *Python 脚本配置，支持文件导入和预览*

> 📷 截图占位符
> 
> ![右键菜单](docs/screenshots/context_menu.png)
> 
> *Proxy History 右键菜单，快速创建 Mock 规则*

## 🚀 快速开始

### 环境要求

- BurpSuite Professional 2023.x 或更高版本
- Java 17 或更高版本
- Python 3.x（用于执行 Python 脚本）

### 安装步骤

1. **下载插件**
   ```bash
   git clone https://github.com/GitHubNull/helloMocker.git
   ```
   或直接下载 [Releases](https://github.com/GitHubNull/helloMocker/releases) 中的 JAR 文件

2. **加载插件**
   - 打开 BurpSuite
   - 进入 `Extensions` → `Installed`
   - 点击 `Add` 按钮
   - 选择下载的 `helloMocker-1.0.0.jar` 文件

3. **配置 Python 路径**
   - 切换到 `HelloMocker` → `Settings` Tab
   - 设置 Python 解释器路径（如：`/usr/bin/python3` 或 `C:\Python39\python.exe`）
   - 点击 `Test` 按钮验证路径

## 📖 使用指南

### 创建 Mock 规则

1. 打开 HelloMocker 插件界面
2. 点击 `Add Rule` 按钮
3. 配置规则信息：
   - **名称**：规则名称（如：API Mock）
   - **匹配条件**：
     - URL 模式：输入匹配模式（如：`/api/admin`）
     - 匹配类型：选择匹配方式（Contains/Equals/Regex 等）
     - HTTP 方法：可选，留空匹配所有方法
   - **响应配置**：
     - **静态响应**：设置状态码、响应头、响应体
     - **Python 脚本**：选择 Python 脚本文件
     - **代理转发**：配置目标主机和端口
4. 点击 `Save` 保存规则

### Python 脚本编写

创建 `.py` 文件，包含 `handle_request(request)` 函数：

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
    # 示例：根据 URL 路径返回不同响应
    if "/api/admin" in request["url"]:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": '{"code": 0, "message": "success", "data": {"admin": true}}'
        }
    
    return {
        "status": 404,
        "headers": {"Content-Type": "text/plain"},
        "body": "Not Found"
    }
```

保存文件后，在规则编辑器中选择该文件即可。

### 从 Proxy History 创建规则

1. 在 BurpSuite 的 Proxy History 中找到目标请求
2. 右键点击请求
3. 选择 `Send to Mock`
4. 插件会自动创建一个静态 Mock 规则
5. 可以在规则列表中编辑该规则

### 导入/导出规则

- **导出**：点击 `Export` 按钮，将规则保存为 JSON 文件
- **导入**：点击 `Import` 按钮，选择之前导出的 JSON 文件

## ⚙️ 配置说明

在 `Settings` Tab 中可以配置以下选项：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| Python 路径 | Python 解释器可执行文件路径 | `python3` |
| 脚本超时 | Python 脚本执行超时时间（毫秒） | `30000` |
| 最大规则数 | 允许的最多规则数量 | `1000` |
| 启用日志 | 是否启用详细日志输出 | `true` |

## 🛠️ 技术栈

- **语言**：Java 17
- **框架**：BurpSuite Montoya API
- **UI**：Swing（BurpSuite 原生外观）
- **依赖**：
  - Jackson（JSON 处理）
  - RSyntaxTextArea（代码预览）
  - Lombok（代码简化）

## 📁 项目结构

```
helloMocker/                                    # 主项目（Maven 多模块）
├── pom.xml                                     # 父 POM（多模块配置）
├── helloMocker-api/                            # API 模块（给用户依赖）
│   ├── pom.xml
│   └── src/main/java/org/oxff/hellomocker/api/
│       └── IMockHandler.java                   # JAR 扩展接口
├── helloMocker-plugin/                         # 插件模块（Burp 插件本体）
│   ├── pom.xml
│   └── src/main/java/org/oxff/hellomocker/
│       ├── HelloMockerExtension.java           # 插件入口
│       ├── handler/                            # 响应处理器
│       ├── http/                               # HTTP 处理器
│       ├── menu/                               # 右键菜单
│       ├── model/                              # 数据模型
│       ├── service/                            # 业务逻辑
│       ├── storage/                            # 数据存储
│       ├── ui/                                 # 界面组件
│       └── util/                               # 工具类
├── examples/                                   # 示例项目
│   └── jar-extension/                          # JAR 扩展示例
│       ├── pom.xml
│       ├── README.md                           # 详细开发指南
│       └── src/main/java/com/example/
│           └── ExampleHandler.java             # 示例处理器
├── doc/                                        # 文档
│   ├── USER_GUIDE.md                           # 使用教程
│   └── USER_GUIDE_EN.md                        # 英文教程
├── README.md                                   # 本文件
└── README_EN.md                                # 英文 README
```

## 📦 JAR 扩展开发（Stage 6）

HelloMocker 支持加载自定义 JAR 包实现复杂的请求处理逻辑。

### 快速开始

**最简单的方式：使用示例项目**

1. **复制示例项目**：
   ```bash
   cp -r examples/jar-extension my-handler
   cd my-handler
   ```

2. **修改处理器类**：编辑 `src/main/java/com/example/ExampleHandler.java`

3. **构建 JAR**：
   ```bash
   # 先安装 HelloMocker API 到本地仓库
   cd ..
   mvn clean install
   
   # 然后构建你的处理器
   cd my-handler
   mvn clean package
   ```

4. **在插件中使用**：
   - 创建/编辑规则，选择 **JAR_EXTENSION** 响应类型
   - 配置 JAR 文件路径和处理器类名（如：`com.example.ExampleHandler`）
   - 点击 **Load** 加载处理器

### 完整开发指南

查看 [examples/jar-extension/README.md](examples/jar-extension/README.md) 获取：
- 详细的 IMockHandler 接口说明
- 请求处理和响应构建示例
- Maven 配置详解
- 常见问题解答

## 🤝 参与贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 开源协议

本项目采用 [MIT](LICENSE) 协议开源。

## 🙏 致谢

- [BurpSuite](https://portswigger.net/burp) - 优秀的 Web 安全测试工具
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) - 代码编辑器组件

## 📧 联系我们

如有问题或建议，欢迎通过以下方式联系：

- GitHub Issues: [https://github.com/GitHubNull/helloMocker/issues](https://github.com/GitHubNull/helloMocker/issues)

---

**Star 🌟 本项目如果它对你有帮助！**
