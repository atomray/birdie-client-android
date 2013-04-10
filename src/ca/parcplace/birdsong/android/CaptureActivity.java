package ca.parcplace.birdsong.android;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import ca.parcplace.birdsong.R;

public class CaptureActivity extends Activity {

	private BirdServiceResponse birdie;
	private Location currentLocation;
	private SongRecorder recorder;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);

		registerLocation();
		
		ImageButton recordbutton = (ImageButton) findViewById(R.id.record);
		recordbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.i("start recording", "from location " + currentLocation);
				
				ImageButton button = (ImageButton) findViewById(R.id.record);
				button.setVisibility(View.GONE);
				// stop button goes away
				ImageButton stopbutton = (ImageButton) findViewById(R.id.stop);
				stopbutton.setVisibility(View.VISIBLE);

				recorder = new SongRecorder();
				recorder.execute(Environment.getExternalStorageDirectory().getPath() + "/test");
			}
		});
		ImageButton stopbutton = (ImageButton) findViewById(R.id.stop);
		stopbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.i("done recording", "from location " + currentLocation);
				
				ImageButton button = (ImageButton) findViewById(R.id.record);
				button.setVisibility(View.VISIBLE);
				// stop button goes away
				ImageButton stopbutton = (ImageButton) findViewById(R.id.stop);
				stopbutton.setVisibility(View.GONE);
				
				recorder.stopRecord();
				new BirdWebService(CaptureActivity.this, Environment.getExternalStorageDirectory().getPath() + "/test.wav").execute();
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
		return Uri.parse("http://xeno-canto.org/species/" + birdie.getGenus()
				+ "-" + birdie.getSpecies());
	}

	private Uri getAvibaseSearchUri() {
		return Uri
				.parse("http://www.bsc-eoc.org/avibase/avibase.jsp?pg=search&qstr="
						+ birdie.getGenus() + "%20" + birdie.getSpecies());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.capture, menu);
		return true;
	}

	private void registerLocation() {
		LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		for (String prov : mgr.getAllProviders()) {
			Log.i("location provider", getString(R.string.provider_found) + " "
					+ prov);
		}

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
