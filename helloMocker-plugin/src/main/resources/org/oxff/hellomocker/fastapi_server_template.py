#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
FastAPI Server Template for HelloMocker Proxy Forward

This server receives HTTP requests forwarded by HelloMocker plugin
and prints detailed request information for debugging purposes.

========================================
REQUIRED DEPENDENCIES - MUST INSTALL FIRST
========================================

Install required packages:
    pip install fastapi uvicorn

If you don't have pip, install it first:
    python -m ensurepip --upgrade
    
Or use your system package manager:
    Ubuntu/Debian: sudo apt-get install python3-pip
    CentOS/RHEL:   sudo yum install python3-pip
    macOS:         brew install python3

========================================
USAGE INSTRUCTIONS
========================================

1. Save this file as 'fastapi_receiver_server.py'

2. Install dependencies (see above)

3. Run the server:
    python fastapi_receiver_server.py
    
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
# This script requires 'fastapi' and 'uvicorn' packages.
#
# Install them using pip:
#     pip install fastapi uvicorn
#
# Or if pip is not available:
#     python -m pip install fastapi uvicorn
#
# The script will check for dependencies at startup and exit if they're missing.
# =============================================================================

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import uvicorn
import json

app = FastAPI(title="HelloMocker Receiver Server")


@app.api_route("/{path:path}", methods=["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"])
async def catch_all(request: Request, path: str):
    """
    Catch-all endpoint that receives all HTTP requests
    """
    # Get request details
    method = request.method
    url = str(request.url)
    headers = dict(request.headers)
    
    # Read body
    try:
        body_bytes = await request.body()
        body = body_bytes.decode('utf-8') if body_bytes else ""
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
    
    return JSONResponse(content=response_data, status_code=200)


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "code": 0,
        "message": "HelloMocker FastAPI Receiver Server is running",
        "endpoints": {
            "catch_all": "/{any_path} - Receives all HTTP requests"
        }
    }


@app.get("/health")
async def health():
    """Health check endpoint"""
    return {"status": "healthy", "service": "fastapi-receiver"}


if __name__ == "__main__":
    print("=" * 80)
    print("HelloMocker FastAPI Receiver Server")
    print("=" * 80)
    print()
    
    # Check if dependencies are installed
    try:
        import fastapi
        import uvicorn
        print("✓ Dependencies check passed: fastapi and uvicorn are installed")
    except ImportError as e:
        print("✗ ERROR: Missing dependencies!")
        print(f"  {e}")
        print()
        print("Please install required packages:")
        print("  pip install fastapi uvicorn")
        print()
        print("Or if you don't have pip:")
        print("  python -m pip install fastapi uvicorn")
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
    
    uvicorn.run(app, host="127.0.0.1", port=8765, log_level="info")
