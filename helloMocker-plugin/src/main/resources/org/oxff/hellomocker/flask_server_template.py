#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Flask Server Template for HelloMocker Proxy Forward

This server receives HTTP requests forwarded by HelloMocker plugin
and prints detailed request information for debugging purposes.

========================================
REQUIRED DEPENDENCIES - MUST INSTALL FIRST
========================================

Install required packages:
    pip install flask

If you don't have pip, install it first:
    python -m ensurepip --upgrade
    
Or use your system package manager:
    Ubuntu/Debian: sudo apt-get install python3-pip
    CentOS/RHEL:   sudo yum install python3-pip
    macOS:         brew install python3

========================================
USAGE INSTRUCTIONS
========================================

1. Save this file as 'flask_receiver_server.py'

2. Install dependencies (see above)

3. Run the server:
    python flask_receiver_server.py
    
4. The server will start on http://127.0.0.1:8765

5. Configure HelloMocker rule:
    - Target Host: 127.0.0.1
    - Target Port: 8765
    - Use SSL: False

6. Enable the Mock rule and test it!

========================================
FEATURES
========================================
    - Receives all HTTP methods (GET, POST, PUT, DELETE, etc.)
    - Prints detailed request information (URL, Headers, Body)
    - Returns JSON response
    - Health check endpoint at /health
"""

# =============================================================================
# IMPORTANT: INSTALL DEPENDENCIES BEFORE RUNNING
# =============================================================================
# This script requires 'flask' package.
#
# Install it using pip:
#     pip install flask
#
# Or if pip is not available:
#     python -m pip install flask
#
# The script will check for dependencies at startup and exit if they're missing.
# =============================================================================

from flask import Flask, request, jsonify
import json

app = Flask(__name__)


@app.route('/', defaults={'path': ''}, methods=['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS', 'TRACE'])
@app.route('/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS', 'TRACE'])
def catch_all(path):
    """
    Catch-all endpoint that receives all HTTP requests
    """
    # Get request details
    method = request.method
    url = request.url
    headers = dict(request.headers)
    
    # Read body
    try:
        body = request.get_data(as_text=True)
    except Exception as e:
        body = f"Error reading body: {str(e)}"
    
    # Print request information
    print("=" * 80)
    print(f"[REQUEST RECEIVED]")
    print("=" * 80)
    print(f"Method: {method}")
    print(f"URL: {url}")
    print(f"Path: /{path}")
    print("-" * 80)
    print("Headers:")
    for key, value in headers.items():
        print(f"  {key}: {value}")
    print("-" * 80)
    print(f"Body:\n{body}")
    print("=" * 80)
    print()
    
    # Return response
    response_data = {
        "code": 0,
        "message": "Request received successfully",
        "data": {
            "method": method,
            "url": url,
            "path": path,
            "headers_count": len(headers)
        }
    }
    
    return jsonify(response_data), 200


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "service": "flask-receiver"}), 200


@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    return jsonify({
        "code": 404,
        "message": "Endpoint not found",
        "data": None
    }), 404


if __name__ == "__main__":
    print("=" * 80)
    print("HelloMocker Flask Receiver Server")
    print("=" * 80)
    print()
    
    # Check if dependencies are installed
    try:
        import flask
        print("✓ Dependencies check passed: flask is installed")
    except ImportError as e:
        print("✗ ERROR: Missing dependencies!")
        print(f"  {e}")
        print()
        print("Please install required packages:")
        print("  pip install flask")
        print()
        print("Or if you don't have pip:")
        print("  python -m pip install flask")
        print()
        exit(1)
    
    print()
    print("Starting server on http://127.0.0.1:8765")
    print()
    print("Press Ctrl+C to stop")
    print()
    print("Configure HelloMocker rule:")
    print("  - Target Host: 127.0.0.1")
    print("  - Target Port: 8765")
    print("  - Use SSL: False")
    print()
    print("=" * 80)
    
    # Run Flask server
    # debug=False to avoid auto-reload issues
    app.run(host='127.0.0.1', port=8765, debug=False)
