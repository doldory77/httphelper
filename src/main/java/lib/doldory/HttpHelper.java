package lib.doldory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class HttpHelper {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private int SOCK_CONNECT_TIMEOUT = 1000 * 3; //1000 * 15;
    private int SOCK_READ_TIMEOUT = 1000 * 10; //1000 * 60;
    private URLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private boolean isHttps = false;
    private HttpMethod method;

    public HttpHelper(HttpMethod method, String requestURL, String charset, Map<String, String> header) throws IOException {
    	this.method = method;
        this.charset = charset;
        URL url = new URL(requestURL);
        httpConn = requestURL.startsWith("https") ?  (HttpsURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection();
        if (requestURL.startsWith("https")) {
            isHttps = true;
            httpConn = (HttpsURLConnection) url.openConnection();
            ((HttpsURLConnection) httpConn).setHostnameVerifier(new HostnameVerifier() {
				
				public boolean verify(String hostname, SSLSession session) {
					
					return true;
				}
			});
        } else {
            httpConn = (HttpURLConnection) url.openConnection();
        }
        httpConn.setConnectTimeout(SOCK_CONNECT_TIMEOUT);
        httpConn.setReadTimeout(SOCK_READ_TIMEOUT);
        httpConn.setUseCaches(false);
        boundary = "====" + System.currentTimeMillis() + "===";
        
        
        if (method != HttpMethod.MULTIPART_POST) {
        	if (header != null) {
        		for (Entry<String, String> entry: header.entrySet()) {
        			httpConn.setRequestProperty(entry.getKey(), entry.getValue());
        		}
        	}
        }
        
        if (method != HttpMethod.GET) {
        	if (isHttps) {
        		((HttpsURLConnection) httpConn).setRequestMethod("POST");
        	} else {
        		((HttpURLConnection) httpConn).setRequestMethod("POST");
        	}
        	httpConn.setDoOutput(true);
        	httpConn.setDoInput(true);
        	if (method == HttpMethod.MULTIPART_POST) {
        		httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        	}
        	outputStream = httpConn.getOutputStream();
        	writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        	if (method == HttpMethod.MULTIPART_POST) {
        		if (header != null) {
        			for (Entry<String, String> entry: header.entrySet()) {
        				writer.append(entry.getKey() + ": " + entry.getValue()).append(LINE_FEED);
        	    		writer.flush();
        			}
        		}
        	}
        } else {
        	if (isHttps) {
        		((HttpsURLConnection) httpConn).setRequestMethod("GET");
        	} else {
        		((HttpURLConnection) httpConn).setRequestMethod("GET");
        	}
        }
        
    }
    
    public HttpHelper setConnectTimeout(int timeout) {
    	this.httpConn.setConnectTimeout(timeout);
    	return this;
    }
    
    public HttpHelper setReadTimeout(int timeout) {
    	this.httpConn.setReadTimeout(timeout);
    	return this;
    }

    public void addHeaderField(String name, String value) {
    	if (method == HttpMethod.MULTIPART_POST) {
    		writer.append(name + ": " + value).append(LINE_FEED);
    		writer.flush();
    		return;
    	}
    	httpConn.setRequestProperty(name, value);
    }

    public void addFormField(String name, String value) {
    	if (method == HttpMethod.GET) {
    		return;
    	} else if (method == HttpMethod.MULTIPART_POST) {
    		writer.append("--" + boundary).append(LINE_FEED);
    		writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
    		writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
    		writer.append(LINE_FEED);
    		writer.append(value).append(LINE_FEED);
    	} else if (method == HttpMethod.POST) {
    		writer.append(name + "=" + value + "&");
    	}
        writer.flush();
    }
    
    public void addBody(String strBody) {
    	if (method == HttpMethod.POST) {
    		writer.append(strBody).append(LINE_FEED);
    		writer.flush();
    	}
    }

    public void addFilePart(String fieldName, File uploadFile) throws IOException {
    	if (method != HttpMethod.MULTIPART_POST) return;
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int byteRead = -1;
        while ((byteRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, byteRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED);
        writer.flush();
    }
    
    public String finish(String fullFilePath) throws IOException {
    	BufferedOutputStream bos = null;
    	BufferedInputStream bis = null;
    	try {
    		int status = isHttps ? ((HttpsURLConnection) httpConn).getResponseCode() : ((HttpURLConnection) httpConn).getResponseCode();
    		if (status == HttpURLConnection.HTTP_OK) {
    			String tmp = fullFilePath.substring(0, fullFilePath.lastIndexOf(File.separator));
    			File downloadFolder = new File(tmp);
    			if (!downloadFolder.exists()) downloadFolder.mkdirs();
    			
    			bos = new BufferedOutputStream(new FileOutputStream(fullFilePath));
    			bis = new BufferedInputStream(httpConn.getInputStream());
    			byte[] buffer = new byte[2048];
    			int readCount = 0;
    			while ((readCount = bis.read(buffer)) != -1) {
    				bos.write(buffer, 0, readCount);
    			}
    			bos.flush();
    			
    		}
    	} catch (IOException e) {
    		throw e;
    	} finally {
    		if (httpConn != null) {
	        	if (isHttps) {
	        		((HttpsURLConnection) httpConn).disconnect();
	        	} else {
	        		((HttpURLConnection) httpConn).disconnect();
	        	}
	        	if (bos != null) bos.close();
	        	if (bis != null) bis.close();
    		}
    			
    	}
    	return fullFilePath;
    }

    public String finish() throws IOException {
        StringBuilder response = new StringBuilder();
        if (method == HttpMethod.MULTIPART_POST) {
        	writer.append(LINE_FEED).flush();
        	writer.append("--" + boundary + "--").append(LINE_FEED);
        	writer.close();
        }

        try {
        
	        int status = isHttps ? ((HttpsURLConnection) httpConn).getResponseCode() : ((HttpURLConnection) httpConn).getResponseCode();
	        if (status == HttpURLConnection.HTTP_OK) {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                response.append(line);
	            }
	            reader.close();
	        } else {
	            throw new IOException("Server returned non-OK status: " + status);
	        }
        
        } catch (IOException e) {
        	throw e;
        } finally {
        	if (httpConn != null)
	        	if (isHttps) {
	        		((HttpsURLConnection) httpConn).disconnect();
	        	} else {
	        		((HttpURLConnection) httpConn).disconnect();
	        	}
        	
        }

        return response.toString();

    }
}
