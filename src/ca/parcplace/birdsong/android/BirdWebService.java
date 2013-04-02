package ca.parcplace.birdsong.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

class BirdWebService implements Runnable {
	
	private ContentResolver resolver;
	private Uri audioUri;
	private BirdServiceResponse result;
	
	BirdWebService(ContentResolver resolver, Uri audioUri) {
		this.resolver = resolver;
		this.audioUri = audioUri;
	}
	
	@Override
	public void run() {
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpContext localContext = new BasicHttpContext();
		//final HttpPost httpPost = new HttpPost("http://birdsong.jelastic.servint.net/api");
		final HttpPost httpPost = new HttpPost("http://192.168.1.100:8080/");
		
		InputStream inStream = null;
		try {
			inStream = resolver.openInputStream(audioUri);

			
			MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			InputStreamBody binBody = new InputStreamBody(inStream, "file");
			mpEntity.addPart("file", binBody);
			
			httpPost.setEntity(mpEntity);

			HttpResponse response = httpClient.execute(httpPost, localContext);
			Log.i("response", response.toString());
			
			InputStream responseStream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
			StringBuilder buf = new StringBuilder();
			
			String str = reader.readLine();
			while (str != null) {
				Log.i("response", str);
				buf.append(str);
				str = reader.readLine();
			}
			
			Log.i("done", "done");
			
			String jsonStr = buf.toString();
			JSONObject json = (JSONObject) new JSONTokener(jsonStr).nextValue();
			
			result = new BirdServiceResponse();
			result.setGenus(json.getString("genus"));
			result.setSpecies(json.getString("species"));
			result.setEnglishName(json.getString("englishName"));
			result.setSubspecies(json.getString("subspecies"));
			
			Log.i("result", result.toString());
		} catch (IOException e) {
			Log.e("io", e.getMessage());
		} catch (JSONException e) {
			Log.e("json", e.getMessage());
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					Log.e("something", "could not close stream", e);
				}
			}
		}					
	}
	
	BirdServiceResponse getResult() {
		return result;
	}
}
