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

# Build the JAR package
mvn clean package -DskipTests

# After building, the JAR file is located at target/helloMocker-1.0.0.jar
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

Choose one of three response types:

1. **Static Response**: Return configured status code, headers, and body directly
2. **Python Script**: Execute Python file to generate response dynamically
3. **Proxy Forward**: Forward the request to an upstream server

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
5. The extension automatically creates a static Mock rule:
   - Rule Name: Auto-generated based on URL
   - Match Condition: Set to the request's URL (Contains match)
   - Response Configuration: Uses the actual response content from the request
6. Switch to the HelloMocker Rules tab to see the newly created rule
7. Double-click the rule to edit it

### Use Cases

- Quickly save an API response for subsequent testing
- Create a Mock based on a real response, then modify specific fields for testing
- Batch create Mock rules for multiple APIs

---

## Rule Management

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

**Document Version**: 1.0  
**Last Updated**: 2026-03-03
