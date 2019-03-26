package lib.doldory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpBuilder {

	private HttpHelper httpHelper;
	private String url;
	private HttpMethod method;
	private int connectionTimeout;
	private int readTimeout;
	private Map<String, String> header;
	private Map<String, String> parameter;
	private Map<String, File> fileMap;
	private String jsonBody;
	private boolean isAsync;
	private String charset = "UTF-8";
	private Thread thread;
	
	public static HttpBuilder get() {
		HttpBuilder builder = new HttpBuilder();
		return builder.setMethod(HttpMethod.GET);
		
	}
	
	public static HttpBuilder post() {
		HttpBuilder builder = new HttpBuilder();
		return builder.setMethod(HttpMethod.POST);
	}
	
	public static HttpBuilder multipart() {
		HttpBuilder builder = new HttpBuilder();
		return builder.setMethod(HttpMethod.MULTIPART_POST);
	}
	
	private HttpBuilder setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}
	
	public HttpBuilder url(String url) {
		this.url = url;
		return this;
	}
	
	public HttpBuilder connectionTimeout(int timeout) {
		this.connectionTimeout = timeout;
		return this;
	}
	
	public HttpBuilder readTimeout(int timeout) {
		this.readTimeout = timeout;
		return this;
	}
	
	public HttpBuilder addHeader(String key, String value) {
		if (header == null) header = new HashMap<String, String>();
		header.put(key, value);
		return this;
	}
	
	public HttpBuilder addParameter(String key, String value) {
		if (parameter == null) parameter = new HashMap<String, String>();
		parameter.put(key, value);
		return this;
	}
	
	public HttpBuilder addBody(String value) {
		this.jsonBody = value;
		return this;
	}
	
	public HttpBuilder addFile(String name, File file) {
		if (fileMap == null) fileMap = new HashMap<String, File>();
		fileMap.put(name, file);
		return this;
	}
	
	public HttpBuilder charset(String charset) {
		this.charset = charset;
		return this;
	}
	
	private HttpHelper build() throws IOException {
		if (method == HttpMethod.GET) {
			if (url.indexOf("?") < 0) {
				url += "?";
			}
			if (url.indexOf("&") > 0) {
				url += "&";
			}
			if (parameter != null) {
				for (Entry<String, String> entry: parameter.entrySet()) {
					url += (entry.getKey() + "=" + entry.getValue() + "&") ;
				}
			}
		}
		HttpHelper http = new HttpHelper(method, url, charset, header);
		if (fileMap != null) {
			for (Entry<String, File> entry: fileMap.entrySet()) {
				http.addFilePart(entry.getKey(), entry.getValue());
			}
		}
		if (parameter != null) {
			for (Entry<String, String> entry: parameter.entrySet()) {
				http.addFormField(entry.getKey(), entry.getValue());
			}
		}
		if (this.jsonBody != null && jsonBody.length() > 0) {
			http.addBody(jsonBody);
		}
		return http;
	}
	
	public String sync() throws IOException {
		this.isAsync = false;
		this.httpHelper = build();
		return httpHelper.finish();
	}
	
	public String syncDownload(String fullFilePath) throws IOException {
		this.isAsync = false;
		this.httpHelper = build();
		return httpHelper.finish(fullFilePath);
	}
	
	
	public HttpBuilder async(@SuppressWarnings("rawtypes") final HttpCallback callback) {
		this.isAsync = true;
		this.thread = new Thread(new Runnable() {
			
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					System.out.println("thread runn ==> " + Thread.currentThread().getName());
					HttpBuilder.this.httpHelper = build();
					if (!Thread.interrupted())
						callback.call(HttpBuilder.this.httpHelper.finish());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}, "http-thread");
		this.thread.setDaemon(true);
		this.thread.start();
		return this;
	}
	
	public HttpBuilder asyncDownload(@SuppressWarnings("rawtypes") final HttpCallback callback, final String fullFilePath) {
		this.isAsync = true;
		this.thread = new Thread(new Runnable() {
			
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					System.out.println("thread runn ==> " + Thread.currentThread().getName());
					HttpBuilder.this.httpHelper = build();
					if (!Thread.interrupted())
						callback.call(HttpBuilder.this.httpHelper.finish(fullFilePath));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}, "http-thread");
		this.thread.setDaemon(true);
		this.thread.start();
		return this;
	}
	
	public void cancel() {
		if (this.thread != null && this.thread.isAlive()) {
			this.thread.interrupt();
		}
	}
	
}
