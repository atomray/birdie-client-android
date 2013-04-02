package ca.parcplace.birdsong.android;

import android.app.Activity;
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
import android.widget.TextView;
import ca.parcplace.birdsong.R;

public class CaptureActivity extends Activity {
	
	private BirdServiceResponse birdie;
	private Location currentLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);

		registerLocation();

		Button captureButton = (Button) findViewById(R.id.captureButton);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("capturing song", "at location " + currentLocation);
				Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				startActivityForResult(intent, 1);
			}
		});
		
		Button xenoLinkButton = (Button) findViewById(R.id.xenoLinkButton);
		xenoLinkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri link = getXenoCantoUri();
				Log.i("external link", link.toString());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(link);
				startActivityForResult(intent, 2);
			}
		});	
		
		Button aviLinkButton = (Button) findViewById(R.id.aviLinkButton);
		aviLinkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri link = getAvibaseSearchUri();
				Log.i("external link", link.toString());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(link);
				startActivityForResult(intent, 2);
			}
		});	
	}
	
	private Uri getXenoCantoUri() {
		return Uri.parse("http://xeno-canto.org/species/" + birdie.getGenus() + "-" + birdie.getSpecies());
	}
	
	private Uri getAvibaseSearchUri() {
		return Uri.parse("http://www.bsc-eoc.org/avibase/avibase.jsp?pg=search&qstr=" + birdie.getGenus() + "%20" + birdie.getSpecies());
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
				new BirdWebService(this, getContentResolver(), audioUri).execute();
			}
		}
	}
	
	private void registerLocation() {
		LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		for (String prov : mgr.getAllProviders()) {
			Log.i("location provider", getString(R.string.provider_found) + " " + prov);
		}

		mgr.requestLocationUpdates(mgr.getAllProviders().get(1), 1000, 0,
			new LocationListener() {
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) { }

				@Override
				public void onProviderEnabled(String provider) { }

				@Override
				public void onProviderDisabled(String provider) { }

				@Override
				public void onLocationChanged(Location location) {
					currentLocation = location;
				}
			});
	}

	void updateModel(BirdServiceResponse birdie) {
		this.birdie = birdie;
		
		setText(R.id.genusResult, birdie.getGenus());
		setText(R.id.speciesResult, birdie.getSpecies());
		setText(R.id.subspeciesResult, birdie.getSubspecies());
		setText(R.id.englishResult, birdie.getEnglishName());
	}
	
	private void setText(int resource, String text) {
		TextView view = (TextView) findViewById(resource);
		view.setText(text);
	}
}
