package com.jobviewer.map;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.PollutionActivity;
import com.lanesgroup.jobviewer.R;

public class JobViewerMapActivity extends FragmentActivity implements
		LocationListener, View.OnClickListener {
	GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// show error dialog if GoolglePlayServices not available
		if (!isGooglePlayServicesAvailable()) {
			finish();
		}
		setContentView(R.layout.map_screen);
		SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.googleMap);
		googleMap = supportMapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String bestProvider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		if (googleMap != null) {
			googleMap.addMarker(new MarkerOptions()
					.position(
							new LatLng(location.getLatitude(), location
									.getLongitude())).title("pollution")
					.snippet("This area is polluted."));
			// captureMapScreen();
		}
		if (location != null) {
			onLocationChanged(location);
		}

		// locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

		//
	}

	private void captureMapScreen() {
		SnapshotReadyCallback callback = new SnapshotReadyCallback() {
			Bitmap bitmap;
			private File fn;

			@Override
			public void onSnapshotReady(Bitmap snapshot) {
				bitmap = snapshot;
				try {
					Log.i("Android",
							snapshot.getHeight() + "    " + snapshot.getWidth());
					File sampleDir = new File(
							Environment.getExternalStorageDirectory(),
							"/MapScreenShots");
					// Created directory if not exist
					if (!sampleDir.exists()) {
						sampleDir.mkdirs();
					}
					Date d = new Date();
					fn = new File(sampleDir + "/" + "Map" + d.getTime()
							+ ".png");
					FileOutputStream out = new FileOutputStream(fn);
					bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
					JobViewerMapActivity.this.finish();
					PollutionActivity.setMapAnnotationImage(Utils
							.bitmapToBase64String(bitmap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		googleMap.snapshot(callback);

	}

	@Override
	public void onLocationChanged(Location location) {
		Button snapShotButton = (Button) findViewById(R.id.doneButton);
		snapShotButton.setOnClickListener(this);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		googleMap.addMarker(new MarkerOptions().position(latLng));
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		captureMapScreen();

	}
}
