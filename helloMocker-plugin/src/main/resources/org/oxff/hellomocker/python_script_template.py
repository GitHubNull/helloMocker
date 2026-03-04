def handle_request(request):
    """
    Handle HTTP request and return response.
    
    Args:
        request: dict with the following fields:
            - request['url']: Full URL (e.g., 'http://example.com/api/user?id=123')
            - request['method']: HTTP method (e.g., 'GET', 'POST')
            - request['headers']: Request headers dict
            - request['body']: Request body string
            - request['path']: URL path (e.g., '/api/user')
            - request['query']: Query string (e.g., 'id=123&name=test')
            - request['host']: Hostname (e.g., 'example.com')
            - request['port']: Port number (e.g., 80, 443)
            - request['protocol']: Protocol (e.g., 'http', 'https')
    
    Returns:
        dict with status, headers, body
    """
    
    # Example 1: Check URL path
    if '/api/admin' in request['url']:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": '{"code": 0, "message": "admin access", "data": {}}'
        }
    
    # Example 2: Check HTTP method
    if request['method'] == 'POST':
        return {
            "status": 201,
            "headers": {"Content-Type": "application/json"},
            "body": '{"code": 0, "message": "created"}'
        }
    
    # Example 3: Access query parameters
    query = request.get('query', '')
    if 'debug=1' in query:
        return {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "body": '{"debug": true, "url": "' + request['url'] + '"}'
        }
    
    # Default response
    return {
        "status": 200,
        "headers": {"Content-Type": "application/json"},
        "body": '{"code": 0, "message": "success", "path": "' + request['path'] + '"}'
    }
