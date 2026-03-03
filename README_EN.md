# HelloMocker - BurpSuite HTTP Mock Extension

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![BurpSuite](https://img.shields.io/badge/BurpSuite-2023%2B-green.svg)](https://portswigger.net/burp)

HelloMocker is a powerful BurpSuite HTTP Mock extension that supports dynamic response generation through Python scripts, helping security testers quickly simulate backend services.

## ✨ Features

- 🎯 **Mock Rule Management**: Create, edit, delete, and enable/disable rules
- 🔍 **Flexible Matching**: Support for 5 URL matching types (Equals, Contains, Regex, StartsWith, EndsWith)
- 📝 **Multiple Response Modes**:
  - Static Response: Return configured response content directly
  - Python Script: Execute Python files to generate responses dynamically
  - Proxy Forward: Forward requests to upstream servers
- 📂 **Python Script Support**: Import Python scripts from files with preview functionality
- 🖱️ **Right-Click Quick Action**: Quickly create Mock rules from Proxy History
- 💾 **Configuration Persistence**: Rules saved in JSON format with import/export support
- 🎨 **Modern UI**: Adopts FlatLaf interface style

## 📸 Interface Preview

> 📷 Screenshot placeholder
> 
> ![Rule List](docs/screenshots/rule_list.png)
> 
> *Mock rule list interface with CRUD support*

> 📷 Screenshot placeholder
> 
> ![Rule Editor](docs/screenshots/rule_editor.png)
> 
> *Rule editor for configuring URL matching and responses*

> 📷 Screenshot placeholder
> 
> ![Python Config](docs/screenshots/python_config.png)
> 
> *Python script configuration with file import and preview*

> 📷 Screenshot placeholder
> 
> ![Context Menu](docs/screenshots/context_menu.png)
> 
> *Proxy History context menu for quick Mock rule creation*

## 🚀 Quick Start

### Requirements

- BurpSuite Professional 2023.x or higher
- Java 17 or higher
- Python 3.x (for executing Python scripts)

### Installation

1. **Download the Plugin**
   ```bash
   git clone https://github.com/GitHubNull/helloMocker.git
   ```
   Or download the JAR file directly from [Releases](https://github.com/GitHubNull/helloMocker/releases)

2. **Load the Plugin**
   - Open BurpSuite
   - Go to `Extensions` → `Installed`
   - Click the `Add` button
   - Select the downloaded `helloMocker-1.0.0.jar` file

3. **Configure Python Path**
   - Switch to the `HelloMocker` → `Settings` tab
   - Set the Python interpreter path (e.g., `/usr/bin/python3` or `C:\Python39\python.exe`)
   - Click the `Test` button to verify the path

## 📖 Usage Guide

### Creating a Mock Rule

1. Open the HelloMocker plugin interface
2. Click the `Add Rule` button
3. Configure rule information:
   - **Name**: Rule name (e.g., API Mock)
   - **Match Conditions**:
     - URL Pattern: Enter the matching pattern (e.g., `/api/admin`)
     - Match Type: Select the matching method (Contains/Equals/Regex, etc.)
     - HTTP Method: Optional, leave blank to match all methods
   - **Response Configuration**:
     - **Static Response**: Set status code, headers, and body
     - **Python Script**: Select the Python script file
     - **Proxy Forward**: Configure target host and port
4. Click `Save` to save the rule

### Python Script Writing

Create a `.py` file containing the `handle_request(request)` function:

```python
def handle_request(request):
    """
    Process HTTP request and return response
    
    Args:
        request: Request dictionary containing the following fields:
            - url: Full URL
            - method: HTTP method (GET/POST, etc.)
            - headers: Request headers dictionary
            - body: Request body string
            - body_base64: Base64-encoded request body
            - path: URL path
            - query: Query string
            - host: Hostname
            - port: Port number
            - protocol: Protocol (http/https)
    
    Returns:
        dict: Response dictionary containing the following fields:
            - status: HTTP status code (integer)
            - headers: Response headers dictionary
            - body: Response body string
            - body_base64: Base64-encoded response body (optional)
            - delay: Delay time in milliseconds (optional)
    """
    # Example: Return different responses based on URL path
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

After saving the file, select it in the rule editor.

### Creating Rules from Proxy History

1. Find the target request in BurpSuite's Proxy History
2. Right-click on the request
3. Select `Send to Mock`
4. The plugin will automatically create a static Mock rule
5. You can edit the rule in the rule list

### Import/Export Rules

- **Export**: Click the `Export` button to save rules as a JSON file
- **Import**: Click the `Import` button and select a previously exported JSON file

## ⚙️ Configuration

The following options can be configured in the `Settings` tab:

| Configuration | Description | Default |
|--------------|-------------|---------|
| Python Path | Python interpreter executable path | `python3` |
| Script Timeout | Python script execution timeout (milliseconds) | `30000` |
| Max Rules | Maximum number of rules allowed | `1000` |
| Enable Logging | Whether to enable detailed logging | `true` |

## 🛠️ Technology Stack

- **Language**: Java 17
- **Framework**: BurpSuite Montoya API
- **UI**: Swing + FlatLaf
- **Dependencies**:
  - Jackson (JSON processing)
  - RSyntaxTextArea (Code preview)
  - Lombok (Code simplification)

## 📁 Project Structure

```
helloMocker/
├── src/main/java/org/oxff/hellomocker/
│   ├── HelloMockerExtension.java      # Plugin entry point
│   ├── api/                            # API interfaces
│   ├── engine/                         # Python script engine
│   ├── handler/                        # Response handlers
│   ├── http/                           # HTTP handlers
│   ├── menu/                           # Context menus
│   ├── model/                          # Data models
│   ├── service/                        # Business logic
│   ├── storage/                        # Data storage
│   ├── ui/                             # UI components
│   └── util/                           # Utility classes
├── doc/                                # Documentation
├── target/                             # Build output
└── pom.xml                             # Maven configuration
```

## 🤝 Contributing

Issues and Pull Requests are welcome!

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the [MIT](LICENSE) License.

## 🙏 Acknowledgments

- [BurpSuite](https://portswigger.net/burp) - Excellent web security testing tool
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) - Code editor component
- [FlatLaf](https://www.formdev.com/flatlaf/) - Modern Swing look and feel

## 📧 Contact

For questions or suggestions, please contact us through:

- GitHub Issues: [https://github.com/GitHubNull/helloMocker/issues](https://github.com/GitHubNull/helloMocker/issues)

---

**Star 🌟 this project if it helps you!**
