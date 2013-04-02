package ca.parcplace.birdsong.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import ca.parcplace.birdsong.R;

public class CaptureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);

		registerLocation();

		Button captureButton = (Button) findViewById(R.id.captureButton);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("capturing", "sound - I'm at " + getLocation());
				Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				startActivityForResult(intent, 1); // intent and requestCode of 1
			}
		});		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.capture, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 1) {
			// is the resultCode OK?
			if (resultCode == RESULT_OK) {
				final Uri audioUri = intent.getData();
				Log.i("captured", audioUri.getEncodedPath());

				new Thread(new BirdWebService(getContentResolver(), audioUri)).start();
			}
		}
	}

	private void registerLocation() {
		LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		for (String prov : mgr.getAllProviders()) {
			Log.i("hi", getString(R.string.provider_found) + " " + prov);
		}

		// get handle for LocationManager
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// connect to the GPS location service
		Location loc = lm.getLastKnownLocation("gps");
		Log.i("testing", Boolean.toString(loc == null));

		mgr.requestLocationUpdates(mgr.getAllProviders().get(1), 1000, 0,
				new LocationListener() {
					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
					}

					@Override
					public void onProviderEnabled(String provider) {
					}

					@Override
					public void onProviderDisabled(String provider) {
					}

					@Override
					public void onLocationChanged(Location location) {
						Log.i("new loc", location.toString());
					}
				});
	}

	private Location getLocation() {
		LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		for (String prov : mgr.getAllProviders()) {
			Log.i("hi", getString(R.string.provider_found) + " " + prov);
		}

		// get handle for LocationManager
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// connect to the GPS location service
		Location loc = lm.getLastKnownLocation("gps");

		return loc;
	}
}
