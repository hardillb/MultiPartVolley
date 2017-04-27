# MultipartRequest

A Volley Request implementation to support multipart/form-data HTTP Posts

```
MultipartRequest request = new MultipartRequest(url, headers, 
    new Response.Listener<NetworkResponse>() {
        @Override
        public void onResponse(NetworkResponse response) {
        }
    },
    new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    });
    
request.addPart(new FormPart(fieldName,value));
request.addPart(new FilePart(fileFieldName, mimeType, fileName, data);

requestQueue.add(request);
```

## Including in a Project

