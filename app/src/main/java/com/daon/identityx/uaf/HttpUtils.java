package com.daon.identityx.uaf;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mello on 09/06/16.
 */
public abstract class HttpUtils {
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int READ_TIMEOUT = 20000;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE = "application/json";
    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";

    public static HttpResponse get(String url) throws NoSuchAlgorithmException, IOException, KeyManagementException {

        HttpURLConnection urlConnection = null;
//        try {
            urlConnection = createConnection(url, GET_METHOD, false);

            int httpResult = urlConnection.getResponseCode();
            String response;
            if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                response = readStream(urlConnection.getInputStream());
            } else {
                response = readStream(urlConnection.getErrorStream());
            }
            return new HttpResponse(response, httpResult);
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//            Log.d("CURL", "Security error initialising HTTPS connection");
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d("CURL", "Unable to connect to the server");
//        } finally {
//            if (urlConnection != null)
//                urlConnection.disconnect();
//        }
//        return new HttpResponse("", 404);
    }

    public static HttpResponse post(String url, String payload) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = createConnection(url, POST_METHOD, true);
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream(), "utf-8");
            out.write(payload);
            out.close();

            int httpResult = urlConnection.getResponseCode();
            String response;
            if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                response = readStream(urlConnection.getInputStream());
            } else {
                response = readStream(urlConnection.getErrorStream());
            }

            return new HttpResponse(response, httpResult);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Log.d("CURL", "Security error initialising HTTPS connection");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CURL", "Unable to connect to the server");
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return new HttpResponse("", 404);
    }

    protected static HttpURLConnection createConnection(String fullUrl, String method, boolean output) throws
            IOException, KeyManagementException, NoSuchAlgorithmException {
        URL url = new URL(fullUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(output);
        urlConnection.setRequestMethod(method);
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);
        urlConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
        urlConnection.connect();
        return urlConnection;
    }

    protected static String readStream(InputStream stream) throws IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    public static class HttpResponse {
        private final String payload;
        private final int httpStatusCode;

        public HttpResponse(String payload, int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            this.payload = payload;
        }

        public String getPayload() {
            return payload;
        }

        public int getHttpStatusCode() {
            return httpStatusCode;
        }
    }

}
