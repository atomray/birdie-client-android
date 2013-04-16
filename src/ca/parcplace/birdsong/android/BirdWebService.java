package ca.parcplace.birdsong.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

class BirdWebService extends AsyncTask<Object, Integer, BirdServiceResponse> {
	
	private String filename;
	private CaptureActivity activity;
	private BirdServiceResponse result;
	private ProgressDialog dialog;
	
	BirdWebService(CaptureActivity activity, String filename) {
		this.activity = activity;
		this.filename = filename;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = ProgressDialog.show(activity, "Please Be Patient", "Processing Song...", true, false);
	}
	
	@Override
	protected BirdServiceResponse doInBackground(Object... params) {
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpContext localContext = new BasicHttpContext();
		final HttpPost httpPost = new HttpPost("http://birdie.jelastic.servint.net/api");
		//final HttpPost httpPost = new HttpPost("http://192.168.1.110:8080/");
				
		InputStream inStream = null;
		try {
			final File file = new File(filename);
			Log.i("sending to server", "filename: " + filename + " size " + file.length());
			
			inStream = new FileInputStream(file);

			MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			InputStreamBody binBody = new InputStreamBody(inStream, "file") {
				@Override
				public long getContentLength() {
					return file.length();
				}
			};
			
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
			
			try { Thread.sleep(4000); } catch (Exception e) { }
			
			return result;
		} catch (IOException e) {
			Log.e("io", "error occurred: " + e.getMessage(), e);
		} catch (JSONException e) {
			Log.e("json", "error occurred: " + e.getMessage(), e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					Log.e("something", "could not close stream", e);
				}
			}
		}
		return BirdServiceResponse.UNKNOWN;
	}
	
	@Override
	protected void onPostExecute(BirdServiceResponse response) {
		super.onPostExecute(response);
		
		activity.updateModel(response);
		
		dialog.dismiss();
	}
	
	BirdServiceResponse getResult() {
		return result;
	}
}
