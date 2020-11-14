/**
 * Copyright Ben Hardill (hardillb@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package uk.me.hardill.volley.multipart;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Request obejct to provide Multipart Mime POST support for the Volley framework.
 *
 * Created by hardillb on 25/04/17.
 */
public class MultipartRequest extends Request<NetworkResponse> {

    private Map<String, String> headers;
    private Response.Listener listener;
    private Response.ErrorListener errorListener;

    private final String boundary = Long.toHexString(System.currentTimeMillis());
    private final String twoDashes = "--";
    private final String newLine = "\r\n";

    private List<MultiPart> parts = new ArrayList<MultiPart>();

    /**
     *
     * @param url URL to make the POST to
     * @param headers A Map containing any headers that should be added to the request
     * @param listener A Volley Response.Listener to process any returned data
     * @param errorListener A Volley Response.ErrorListener to handle errors
     */
    public MultipartRequest(String url, Map<String,String> headers,
                            Response.Listener<NetworkResponse> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.headers = headers;
        this.listener = listener;
        this.errorListener = errorListener;

    }

    public MultipartRequest(int method, String url, Map<String,String> headers,
                            Response.Listener<NetworkResponse> listener,
                            Response.ErrorListener errorListener) {
        super(method,url,errorListener);
        this.headers = headers;
        this.listener = listener;
        this.errorListener = errorListener;

    }

    /**
     * Adds a new part to the request
     *
     * @param part
     */
    public void addPart(MultiPart part) {
        if (part != null) {
            parts.add(part);
        }
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);

        try {
            for (MultiPart part: parts) {
                dos.writeBytes(twoDashes + boundary + newLine);
                if (part instanceof FormPart) {
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + part.getName() + "\"" + newLine);
                    dos.writeBytes(newLine);
                    dos.write(part.getData());
                    dos.writeBytes(newLine);
                } else if (part instanceof FilePart) {
                    FilePart filePart = (FilePart) part;
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + part.getName()
                            + "\"; filename=\"" + filePart.getFilename() + "\"" + newLine);
                    dos.writeBytes("Content-type: " + filePart.getMimeType() + newLine);
                    dos.writeBytes(newLine);
                    dos.write(part.getData());
                    dos.writeBytes(newLine);
                }
            }

            //close out
            dos.writeBytes(twoDashes + boundary + twoDashes + newLine);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (headers != null) {
            return headers;
        } else {
            return super.getHeaders();
        }
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    /**
     * A generic part to add
     */
    protected static abstract class MultiPart {

        private String name;
        private String mimeType;

        public MultiPart(String name, String mimeType) {
            this.name = name;
            this.mimeType = mimeType;
        }

        public String getName() {
            return name;
        }

        public String getMimeType() {
            return mimeType;
        }

        public abstract byte[] getData();
    }

    /**
     * A class to represent a basic form field to be added to the request
     */
    public static class FormPart extends MultiPart {

        private String value;

        /**
         * Creates a form part with the supplied name and value
         * @param name form field name
         * @param value form field value
         */
        public FormPart(String name, String value) {
            super(name, "");
            this.value = value;
        }

        @Override
        public byte[] getData() {
            return value.getBytes();
        }
    }

    /**
     * A class representing a file to be added to the request
     */
    public static class FilePart extends MultiPart {

        private byte data[];
        private String filename;

        /**
         * Creates a file with the given values to add to the request
         * @param name form field name
         * @param mimeType mime type for part
         * @param filename filename (can be null)
         * @param data the content of the file
         */
        public FilePart(String name, String mimeType, String filename, byte data[]){
            super(name, mimeType);
            this.data = data;
            this.filename = filename;
        }

        public byte[] getData() {
            return data;
        }

        public String getFilename() {
            return filename;
        }
    }
}
