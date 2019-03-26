package lib.doldory;

import java.util.Map;

import com.google.gson.Gson;

public class Demo {

	public static void main(String[] args) {
//		String url = "http://localhost:8085/mofs/db_upload.do";
//		String sampleFile = "C:\\Temp\\mofs.db";
		
//		HttpBuilder
//			.multipart()
//			.url(url)
//			.addParameter("json", "{\"msg\":\"hello doldory!!\"}")
//			.addHeader("auth", "12341234")
//			.addFile("database", new File(sampleFile))
//			.async(new HttpCallback<String>() {
//				public void call(String result) {
//					System.out.println(result);
//				}
//			});
		
		HttpBuilder
			.get()
			.url("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json")
			.addParameter("key", "6a0af2228d3496f811047d334826535b")
			.addParameter("targetDt", "20190108")
			.addParameter("weekGb", "0")
			.async(new HttpCallback<String>() {
				
				public void call(String result) {
					System.out.println(new Gson().fromJson(result, Map.class));
					
				}
			});
		
//		HttpBuilder
//			.post()
//			.url("http://localhost:8085/mofs/app_update.do")
//			.charset("EUC-KR")
//			.addHeader("Connection", "close")
//			.addHeader("Content-Type", "application/json")
//			.addHeader("Accept", "application/json")
//			.addHeader("Accept-Charset", "euc-kr")
//			.addBody("{\"tbDevice\":{\"app_version\":\"1.2.7\",\"mac_address\":\"F4:42:8F:60:7D:A9\"}}")
//			.async(new HttpCallback<String>() {
//				
//				public void call(String result) {
//					System.out.println(new Gson().fromJson(result, Map.class));
//					
//				}
//			});
		
		
//		HttpBuilder
//			.post()
//			.url("http://210.92.79.30/mofs/init_db_download.do")
//			.url("https://mofs.dxline.co.kr/mofs/init_db_download.do")
//			.url("http://localhost:8085/mofs/init_db_download.do")
//			.charset("EUC-KR")
//			.addHeader("Connection", "close")
//			.addHeader("Content-Type", "application/json")
//			.addHeader("Accept", "application/json")
//			.addHeader("Accept-Charset", "euc-kr")
//			.addBody("{\"tbDevice\":{\"db_version\":\"DB2016111302\",\"mac_address\":\"F4:42:8F:60:7D:A9\"}}")
//			.asyncDownload(new HttpCallback<String>() {
//				
//				public void call(String result) {
//					System.out.println(result);
//					
//				}
//			}, "D:\\temp2\\test.dat");		
		
		
		System.out.println("thread runn ==> " + Thread.currentThread().getName());
		
		try {
			Thread.sleep(3000 * 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("system exit...");
		
	}
}
