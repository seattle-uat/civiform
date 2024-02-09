export const {
  BASE_URL = 'http://civiform:9000',
  LOCALSTACK_URL = 'http://localhost.localstack.cloud:4566',
  TEST_USER_AUTH_STRATEGY = '',
  TEST_USER_LOGIN = '',
  TEST_USER_PASSWORD = '',
  TEST_USER_DISPLAY_NAME = '',
  TEST_CIVIC_ENTITY_SHORT_NAME = 'TestCity',
  DISABLE_SCREENSHOTS = false,
  DISABLE_BROWSER_ERROR_WATCHER = false,
  // This value was extracted from the browser test env via:
  //
  // python -c "import requests;print(requests.Session().get('http://localhost:9999').cookies.get_dict()['PLAY_SESSION'])"
  FROZEN_PLAY_SESSION_COOKIE_VALUE = 'eyJhbGciOiJIUzI1NiJ9.eyJkYXRhIjp7InNlc3Npb25JZCI6IjE4Y2JkYTRhLWFjYmEtNDY5OS05ZTM0LWY2ZGI0OWQxZGNhYSIsInBhYzRqIjoiZnROVURWZFJJZWJrdkFldndadmdMN0RoZ0d4a0dPNUpxMTJkVUE0Sjl4RHQ5cDFnN0J4dXZ1YkUzQUVzdzNCQVQwNHZ0bGNLQ2plWjRHV1lTK295cDU5OUE0TGR4WEJvcnVLZ01tKzg2S3J3L0FmV3c5MkJYbjRRU2dzSjRnQ3VCWkJSMVRXaENVWUNBZStlWjd3d2h1R2g2MTlqcHpqWjlSNXZEbU04Z0Vod25samdQNml1VENsRVZ6OGZXRWo0QTdkWHMyWUxXY2dUSWp4RnplOEh3WjQ0TmVRN2R1d3U4d1gvNEZIRitqTFlMazdGdUtMbGtXVWs5L0o2QTlIejRma3ZRaDV1a1YxYktBc2tscmo0aVloSTg2a2Fodks0TTJlUFBaQXVCZm1SY1ZFSCtySXozZWdScFpZRFBNWFhpTDBpV0Q3STF4RElsUDZUSlFIcCtYL3k0QjlNWGJjT1p6elBjeVVwR0JqVWNwWWhQUjVZSzA4bVFhRk1BUkJNWkNXUWthNWJaS2JNR3NVQUZmM0kydjdDdjdtOUphTjI4UFRvNW12Nk8vSDIzTWRSNk0wZkd1aEVwa0pJZFpYUi9JQXRRWVFiMXo4NWNqdi9uNEFvNzBVSk50NS84a0pRdWZMR1lFUEh4ay8xNkVQc20yNlIvMGp4cWVEQWRBK2NZM0ZhZ0NzZjhxYVdPaURLblpkNFNObkQvMTk3ejdIWTFEbGdwcVdwNENzc1V6cWJpSjhZV2lKOGVPMXN5UEU2MUFBd3ZyN05abzIxOXlQTzJEVlRrZkhrNU5senQ4eGNEaXlaemQrUGsxSVVnVEt6QUFXVzJjb3IwMUJXTjU3VHV2eHYyYjFMRXgyUnNCc05ERFlJMU94VXprbmgvMGxjT1hDdFgxdy82TFVDa3ZlOUFQQ0NnZTNGa2NmcFgxWEdpS0pqNk5TYXp3RkNCZEVHblQxemNPeDM5SWtaTU04d010ZHMiLCJjc3JmVG9rZW4iOiIyNzJkYjEyMWFjMDc2MGEwNzRiNjg4N2EyYThhMjE4ODBhNjBmN2VlLTE3MDE0NTgyMjQxNDYtY2MxMzI4ZWFlMTkxNjI4NTJjMDE0MzhhIn0sIm5iZiI6MTcwMTQ1ODIyNCwiaWF0IjoxNzAxNDU4MjI0fQ.ifm74kjDkb-eEUw2nLJY9f3TaQe2QHHztW_1M1W1-zM',
} = process.env
