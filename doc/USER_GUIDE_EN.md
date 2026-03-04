# HelloMocker User Guide

This document provides a comprehensive guide on how to use the HelloMocker BurpSuite HTTP Mock extension, helping you get started quickly and make the most of its features.

---

## Table of Contents

1. [Environment Setup](#environment-setup)
2. [Installation](#installation)
3. [Interface Overview](#interface-overview)
4. [Creating Mock Rules](#creating-mock-rules)
5. [Response Types](#response-types)
6. [Python Script Development](#python-script-development)
7. [Creating Rules from Proxy History](#creating-rules-from-proxy-history)
8. [Rule Management](#rule-management)
9. [Configuration Settings](#configuration-settings)
10. [Importing and Exporting Rules](#importing-and-exporting-rules)
11. [FAQ](#faq)

---

## Environment Setup

### System Requirements

| Component | Minimum Version | Notes |
|-----------|-----------------|-------|
| BurpSuite Professional | 2023.x | Built on Montoya API |
| Java | 17+ | Runtime environment |
| Python | 3.x | For executing Python scripts (optional) |

### Checking Python Environment

Run the following commands in your terminal to check if Python is installed:

```bash
# Windows
python --version
# or
python3 --version

# Linux/macOS
python3 --version
```

If not installed, download and install from the [Python official website](https://www.python.org/downloads/).

---

## Installation

### Method 1: Download from Release (Recommended)

1. Visit the project's [Releases](https://github.com/GitHubNull/helloMocker/releases) page
2. Download the latest `helloMocker-1.0.0.jar` file
3. Open BurpSuite and navigate to **Extensions** → **Installed**
4. Click the **Add** button
5. In the dialog that appears:
   - Extension type: Select **Java**
   - Extension file: Click **Select file** and choose the downloaded JAR file
6. Click **Next** and wait for the extension to load
7. A "Loaded successfully" message indicates successful installation

### Method 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/GitHubNull/helloMocker.git

# Enter the project directory
cd helloMocker

# Build the project (multi-module Maven project)
mvn clean install -DskipTests

# After building, the plugin JAR is located at helloMocker-plugin/target/helloMocker-plugin-1.0.0.jar
# The API JAR is located at helloMocker-api/target/helloMocker-api-1.0.0.jar
```

### Verifying Installation

After successful installation, you should see:
- A **HelloMocker** tab in the top menu bar of BurpSuite
- HelloMocker extension information in the Extensions list

---

## Interface Overview

The HelloMocker extension main interface is divided into three tabs:

### 1. Rules

This is the primary working area, containing:

- **Toolbar**: Buttons for adding rules, importing/exporting, enabling/disabling rules
- **Rules Table**: Displays a list of all Mock rules
  - Name: The rule name
  - URL Pattern: The matching URL pattern
  - Match Type: Equals/Contains/Regex/StartsWith/EndsWith
  - Response Type: Static/Python/Proxy
  - Status: Enabled/Disabled
  - Priority: Lower numbers have higher priority
- **Action Buttons**: Edit, delete, and duplicate rules

### 2. Settings

Configure global parameters for the extension:

- **Python Path**: Path to the Python interpreter executable
- **Script Timeout**: Maximum execution time for Python scripts (milliseconds)
- **Max Rules**: Maximum number of rules allowed
- **Enable Logging**: Whether to output detailed logs

### 3. Help

Displays quick help information and version details.

---

## Creating Mock Rules

### Basic Steps

1. Switch to the **HelloMocker** → **Rules** tab
2. Click the **Add Rule** button in the toolbar
3. Configure the rule information in the rule editor
4. Click **Save** to save the rule

### Rule Editor Details

The rule editor is divided into three sections:

#### Basic Information

| Field | Description | Example |
|-------|-------------|---------|
| Rule Name | Give your rule a name | "API Mock - Admin" |
| Enabled | Whether to enable this rule immediately | Checked means enabled |
| Priority | Lower numbers have higher priority for matching | 1 (highest priority) |

#### Match Conditions

| Field | Description | Example |
|-------|-------------|---------|
| URL Pattern | The URL string to match | `/api/admin` |
| Match Type | How to match the URL | Contains |
| HTTP Method | Optional, specify the method to match | GET (leave blank to match all) |

**Match Type Descriptions**:

- **Equals**: URL must match exactly
- **Contains**: URL must contain the specified string
- **Regex**: Match using regular expression
- **StartsWith**: URL must start with the specified string
- **EndsWith**: URL must end with the specified string

#### Response Configuration

Choose one of four response types:

1. **Static Response**: Return configured status code, headers, and body directly
2. **Python Script**: Execute Python file to generate response dynamically
3. **Proxy Forward**: Forward the request to an upstream server
4. **JAR Extension**: Load custom JAR package to handle requests (Stage 6)

---

## Response Types

### 1. Static Response

Useful for returning fixed response content.

**Configuration Items**:

| Field | Description | Example |
|-------|-------------|---------|
| Status Code | HTTP status code | 200 |
| Headers | One per line, format: `Name: Value` | `Content-Type: application/json` |
| Body | The response content to return | `{"code": 0, "msg": "success"}` |

**Example Scenarios**:
- Simulate API endpoints returning fixed data
- Return error pages for exception testing
- Quickly verify frontend handling of specific responses

### 2. Python Script

Useful for scenarios requiring dynamic response generation.

**Configuration Items**:

| Field | Description |
|-------|-------------|
| Script File | Select a local `.py` file |
| Script Preview | Displays the selected script content (read-only) |

**How It Works**:
1. When a request matches the rule, the extension calls the Python interpreter to execute the script
2. Request information is passed to the script's `handle_request` function in JSON format
3. The script returns a response dictionary, which the extension converts to an HTTP response

**Use Cases**:
- Return different responses based on request parameters
- Simulate complex business logic
- Generate test data dynamically
- Implement request signature validation/bypass

### 3. Proxy Forward

Useful for forwarding requests to other servers for processing.

**Configuration Items**:

| Field | Description | Example |
|-------|-------------|---------|
| Target Host | The server address to forward to | `127.0.0.1` |
| Target Port | The server port | `8081` |
| Use SSL | Whether to use HTTPS connection | Unchecked |

**How It Works**:
1. When a request matches the rule, the extension forwards it to the configured target server
2. The target server processes the request and returns a response
3. The extension returns the response to the client as-is

**Use Cases**:
- Forward requests to a local development server
- Use external services (like Flask/Django) to handle requests
- Implement complex Mock logic without modifying the extension

### 4. JAR Extension

Suitable for scenarios requiring Java code to process requests.

**Configuration Items**:

| Field | Description | Example |
|-------|-------------|---------|
| JAR File | Path to the JAR package containing the handler | `/path/to/myhandler.jar` |
| Handler Class | Fully qualified class name implementing IMockHandler | `com.example.MyHandler` |

**Operation Steps**:

1. **Fill in Configuration**:
   - Click **Browse...** to select the JAR file, or manually enter the full path
   - Enter the fully qualified class name in the **Handler Class** field (e.g., `com.example.MyHandler`)

2. **Load JAR (Optional but Recommended)**:
   - Click the **Load** button to preload the JAR file
   - If loaded successfully, the status will show **Status: Loaded - [Handler Name]**
   - If loading fails, an error message will pop up - check your configuration according to the提示

3. **Save Rule**:
   - Click **Save** to save the rule
   - Even without clicking Load, the JAR will be automatically loaded when a request matches

**How It Works**:
1. The extension uses URLClassLoader to dynamically load the JAR file
2. Creates handler instance via reflection (must implement IMockHandler interface)
3. Calls the handler's `handleRequest()` method to process the request
4. Returns the response from the handler to the client

**Use Cases**:
- Complex business logic requiring Java code
- Reusing existing Java libraries or frameworks
- Better performance and type safety
- Team collaboration on Mock handlers

**Development Steps**:
1. Create a Maven/Gradle project
2. Add dependencies (Burp Montoya API and plugin API)
3. Implement the `IMockHandler` interface
4. Package as a JAR file
5. Configure JAR path and class name in the plugin

**Important Notes**:
- **Class name must be complete**: Use the fully qualified class name (including package), e.g., `com.example.MyHandler`, not just `MyHandler`
- **Pre-validation**: It's recommended to click the Load button before saving to verify the JAR can be loaded properly
- **Path issues**: Ensure the JAR file path is correct; using the Browse button can help avoid path errors

**Example Handler Code**:

```java
package com.example;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.oxff.hellomocker.api.IMockHandler;

public class MyHandler implements IMockHandler {
    
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        // Get request information
        String url = request.url();
        String method = request.method();
        
        // Custom processing logic
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
        // Initialization, e.g., load configuration
    }
    
    @Override
    public void destroy() {
        // Cleanup operations
    }
}
```

**Interface Documentation**:

- `handleRequest(HttpRequest request)`: Core method to process HTTP request and return response
- `getName()`: Returns handler name (for display)
- `getDescription()`: Returns handler description
- `init()`: Initialization callback, called after loading JAR
- `destroy()`: Destroy callback, called before unloading JAR

---

## Python Script Development

### Script Template

Create a `.py` file with the following structure:

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
    # Write your logic here
    pass
```

### Example Scripts

#### Example 1: Return Different Responses Based on URL Path

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

#### Example 2: Read Request Parameters and Return

```python
import json
import urllib.parse

def handle_request(request):
    query_string = request.get("query", "")
    params = urllib.parse.parse_qs(query_string)
    
    # Get specific parameter
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

#### Example 3: Simulate Delayed Response

```python
def handle_request(request):
    import time
    
    # Simulate 2-second delay
    time.sleep(2)
    
    return {
        "status": 200,
        "headers": {"Content-Type": "application/json"},
        "body": '{"code": 0, "message": "delayed response"}',
        "delay": 2000  # Can also specify delay here (milliseconds)
    }
```

#### Example 4: Handle POST Request Body

```python
import json

def handle_request(request):
    method = request.get("method", "")
    body = request.get("body", "")
    
    if method == "POST":
        try:
            # Try to parse JSON request body
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

### Debugging Tips

1. **View Error Messages**: When script execution fails, the response body contains error stack trace information
2. **Use Logging**: You can use `print()` in scripts to output debug information (displayed in BurpSuite's Output window)
3. **Local Testing**: Test script logic locally via command line before importing into the extension

---

## Creating Rules from Proxy History

This is the quickest way to create Mock rules.

### Steps

1. In BurpSuite, navigate to **Proxy** → **HTTP history**
2. Find the request you want to mock
3. Right-click on the request
4. Select **Send to Mock** from the context menu
5. Based on settings, the extension will:
   - **Enable Dialog Editing** (default): Open the rule editor with pre-filled request info, allowing direct editing before saving
   - **Disable Dialog Editing**: Automatically create a static Mock rule without showing the editor

### Dialog Editing Mode (Recommended)

When **Show Send to Mock Dialog** option is enabled:

1. Right-click on a request and select **Send to Mock**
2. A rule editor pops up, pre-filled with:
   - Rule Name: Auto-generated based on URL (automatically truncates超长 URLs)
   - Match Condition: Set to the request's URL (Contains match)
   - Response Configuration: Uses the actual response content from the request
   - HTTP Editor: Uses Burp native editor to display response, supporting Raw/Headers/Hex views
3. In the editor, you can:
   - Modify rule name and match conditions
   - Edit response content in the HTTP editor (supports Chinese, auto-detects encoding)
   - Switch response types (Static/Python/Proxy/JAR)
4. Click **Save** to save the rule

**Advantages**:
- Preview and edit response content before creating
- Avoid creating rules that don't meet requirements
- Support direct editing of Chinese content in response body (UTF-8 encoding)

### Silent Creation Mode

When **Show Send to Mock Dialog** option is disabled:

1. Right-click on a request and select **Send to Mock**
2. The extension automatically creates a rule without showing the editor
3. A success message is displayed
4. You can view and edit it in the Rules tab

### Configure Dialog Option

In the **Settings** tab:

- **Show Send to Mock Dialog**: Check to enable dialog editing, uncheck for silent creation
- Default: Enabled (recommended)

**Note**: In dialog editing mode, rule names are automatically truncated:
- Removes URL query parameters (`?key=value` part)
- If path exceeds 30 characters, keeps the last 30 characters (adding `...` at the front)
- Example: `/api/v1/users/12345/profile?token=xxx` → `...sers/12345/profile`

### Use Cases

- Quickly save an API response for subsequent testing
- Create a Mock based on a real response, then modify specific fields for testing
- Batch create Mock rules for multiple APIs

---

## Rule Management

### Toolbar Buttons

The rule list toolbar provides the following action buttons:

| Button | Function | Description |
|--------|----------|-------------|
| **Add** | Add Rule | Open rule editor to create a new rule |
| **Import** | Import Rules | Import rules from JSON file |
| **Export** | Export Rules | Export rules to JSON file |
| **Edit** | Edit Rule | Edit selected rule (double-click also works) |
| **Delete** | Delete Rule | Delete selected rule |
| **Up** | Move Up | Move selected rule up one position in the list |
| **Down** | Move Down | Move selected rule down one position in the list |

**Note**: Edit, Delete, Up, and Down buttons require a rule to be selected in the table first.

### Enabling/Disabling Rules

- In the rules table, check/uncheck the **Enabled** column checkbox
- Or select a rule and click the **Enable** / **Disable** button in the toolbar

### Editing Rules

1. Select the rule to edit in the rules table
2. Click the **Edit** button in the toolbar (or double-click the rule row)
3. Modify the configuration in the editor
4. Click **Save** to save changes

### Deleting Rules

1. Select the rule to delete in the rules table
2. Click the **Delete** button in the toolbar
3. Click **Yes** in the confirmation dialog to confirm deletion

### Duplicating Rules

1. Select the rule to duplicate in the rules table
2. Click the **Duplicate** button in the toolbar
3. An identical rule will be created (name will have "Copy" suffix)

### Adjusting Priority

Rule priority determines matching order:
- Lower numbers have higher priority
- Matching checks rules in priority order from high to low
- The first matching rule is used

Methods to adjust priority:
1. Edit the rule and modify the **Priority** field
2. Priority can be any integer;建议使用 1, 2, 3... sequence is recommended

---

## Configuration Settings

### Configuring Python Path

1. Switch to the **HelloMocker** → **Settings** tab
2. Find the **Python Path** configuration item
3. Enter the full path to the Python interpreter:
   - Windows: `C:\Python39\python.exe` or `C:\Users\Username\AppData\Local\Programs\Python\Python39\python.exe`
   - Linux/macOS: `/usr/bin/python3` or `/usr/local/bin/python3`
4. Click the **Test** button to verify the path is correct
5. A "Python is working!" message indicates successful configuration

### Other Configuration Items

| Configuration | Description | Default |
|---------------|-------------|---------|
| Script Timeout | Maximum execution time for Python scripts (milliseconds), will be terminated if exceeded | 30000 |
| Max Rules | Maximum number of rules allowed, prevents memory overflow | 1000 |
| Enable Logging | Whether to output detailed debug logs | true |
| Show Send to Mock Dialog | Whether to show edit dialog after right-click Send to Mock | true |

### Configuration Persistence

All configurations are automatically saved and will be loaded automatically the next time BurpSuite starts.

---

## Importing and Exporting Rules

### Exporting Rules

1. In the Rules tab, click the **Export** button in the toolbar
2. Choose a save location and enter a filename
3. Rules will be saved in JSON format to the specified file

**Example Export File Format**:

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

### Importing Rules

1. In the Rules tab, click the **Import** button in the toolbar
2. Select a previously exported JSON file
3. The extension will read the file and import all rules
4. If a rule with the same ID exists, you will be prompted whether to overwrite

### Use Cases

- **Team Collaboration**: Share Mock rules with team members
- **Environment Migration**: Migrate configurations between different machines
- **Version Control**: Commit rule files to Git repository
- **Backup and Recovery**: Regularly backup rule configurations

---

## FAQ

### Q1: Extension installation failed with "Extension failed to load"

**Possible Causes and Solutions**:

1. **Incompatible Java Version**
   - Check Java version: `java -version`
   - Ensure Java 17 or higher is used

2. **Corrupted JAR File**
   - Re-download or rebuild the JAR file
   - Verify file integrity

3. **BurpSuite Version Too Low**
   - Ensure BurpSuite Professional 2023.x or higher

### Q2: Python script execution failed

**Possible Causes and Solutions**:

1. **Incorrect Python Path Configuration**
   - Check Python path in Settings
   - Click Test button to verify

2. **Script Syntax Errors**
   - Check script for syntax errors
   - Test locally first with `python script.py`

3. **Missing Dependency Libraries**
   - Ensure required libraries are installed
   - Install with `pip install library-name`

4. **Script Timeout**
   - Increase script timeout setting
   - Optimize script execution efficiency

### Q3: Mock rules not taking effect

**Possible Causes and Solutions**:

1. **Rule Not Enabled**
   - Check the rule's enabled status

2. **Incorrect Match Conditions**
   - Check URL pattern and match type
   - When using Contains match, ensure correct string is included

3. **Priority Issues**
   - Check if other higher priority rules match first
   - Adjust the current rule's priority

4. **BurpSuite Interception Settings**
   - Ensure requests go through BurpSuite proxy
   - Check interception settings are correct

### Q4: How to debug Python scripts?

**Debugging Methods**:

1. **Use print Output**:
   ```python
   def handle_request(request):
       print(f"Received request: {request['url']}")
       print(f"Method: {request['method']}")
       # ... other logic
   ```
   Output is displayed in BurpSuite's **Output** window.

2. **View Error Response**: When script errors occur, the response body contains complete error stack trace.

3. **Local Testing**:
   ```python
   # Add test code at the end of script
   if __name__ == "__main__":
       test_request = {
           "url": "http://example.com/api/test",
           "method": "GET",
           "headers": {},
           "body": ""
       }
       print(handle_request(test_request))
   ```

### Q5: Which Python libraries are supported?

The extension uses the system Python interpreter to execute scripts, therefore:
- All standard libraries can be used
- Installed third-party libraries can be used
- You can use `pip` to install required libraries

Common library examples:
- `json`: JSON processing
- `re`: Regular expressions
- `urllib.parse`: URL parsing
- `base64`: Base64 encoding/decoding
- `hashlib`: Hash calculation
- `random`: Random number generation
- `datetime`: Date and time processing

### Q6: What is the rule matching order?

Rules are matched in the following order:

1. Sorted by **Priority** (lower numbers have higher priority)
2. Same priority sorted by **Creation Time** (earlier created have higher priority)
3. Match conditions are checked for each rule in sequence
4. The first matching rule is used, subsequent rules are not checked

**Recommendations**:
- Set higher priority (lower number) for more specific rules
- Set lower priority for general rules

### Q7: How to clear all rules?

Currently, the extension does not have a one-click clear feature. You can:

1. **Manual Deletion**: Select rules and click Delete button one by one
2. **Import Empty File**: Create a JSON file containing an empty array `[]` and import it

### Q8: JAR extension failed to load?

**Common Errors and Solutions**:

#### Error 1: "JAR file path is not configured"
- **Cause**: JAR file path is not filled in
- **Solution**: Click the Browse button to select the JAR file, or manually enter the full path

#### Error 2: "Handler class name is not configured"
- **Cause**: Handler class name is not filled in
- **Solution**: Enter the fully qualified class name in the Handler Class field (e.g., `com.example.MyHandler`)

#### Error 3: "JAR file not found"
- **Cause**: JAR file path is incorrect or file does not exist
- **Solution**:
  - Check if the path is correct
  - Use the Browse button to reselect the file
  - Ensure the file has not been moved or deleted

#### Error 4: "Failed to load JAR extension"
- **Cause**: JAR file format is incorrect or class loading failed
- **Solution**:
  1. **Incorrect class name**: Ensure using the fully qualified class name (including package), e.g., `com.example.MyHandler`, not just `MyHandler`
  2. **IMockHandler interface not implemented**: Check if the class implements the `IMockHandler` interface
  3. **Missing no-arg constructor**: Ensure the handler class has a public no-arg constructor
  4. **Dependency conflicts**: Check if the JAR contains dependencies conflicting with BurpSuite

#### Debugging Tips
1. **Use Load button for pre-validation**: Click the Load button before saving the rule to detect issues early
2. **Check Burp Output window**: Detailed error stack traces are output to BurpSuite's Output window
3. **Verify JAR contents**: Use `jar tf your.jar` command to view JAR contents and confirm class files exist

### Q9: Chinese characters display as garbled text?

**Solutions**:

1. **Ensure Content-Type includes charset=UTF-8**
   - Add to response headers: `Content-Type: application/json; charset=UTF-8`

2. **Use UTF-8 encoding for import/export**
   - The extension automatically handles encoding, ensure editors and files use UTF-8

3. **Use UTF-8 in Python scripts**
   - Add at beginning of file: `# -*- coding: utf-8 -*-`
   - Use Unicode for Chinese strings: `u"Chinese"`

4. **Garbled text in Burp editor**
   - The extension automatically adds charset=UTF-8
   - Ensure the original response contains correct charset declaration

---

## Best Practices

### 1. Rule Naming Conventions

Use meaningful rule names for easier management:
- `API - User Login`: User login interface
- `API - Admin - Get Users`: Admin get user list
- `Error - 500 Test`: 500 error test

### 2. Reasonable Use of Priority

- 1-10: Core business interfaces
- 11-50: Normal API interfaces
- 51-100: Testing and debugging rules
- 100+: General fallback rules

### 3. Python Script Organization

- Keep scripts in a dedicated directory
- Use version control to manage scripts
- Add comments explaining script purpose

### 4. Regular Backups

- Regularly export rule configurations
- Include rule files in version control
- Record important configuration changes

### 5. Testing and Verification

- Test and verify rules promptly after creation
- Use BurpSuite Repeater to send test requests
- Check if responses match expectations

---

## Getting Help

If you encounter issues during use:

1. Check the FAQ section of this guide
2. Review the project's [GitHub Issues](https://github.com/GitHubNull/helloMocker/issues)
3. Submit a new Issue describing the problem and reproduction steps

---

**Document Version**: 1.1  
**Last Updated**: 2026-03-04
