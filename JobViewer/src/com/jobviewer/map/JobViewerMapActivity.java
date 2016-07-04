package com.jobviewer.map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.PollutionActivity;
import com.lanesgroup.jobviewer.R;

public class JobViewerMapActivity extends FragmentActivity implements
		LocationListener, View.OnClickListener, OnMapClickListener {
	GoogleMap googleMap;
	ImageButton lineMarkerButton, mapOptionSelectorButton, polygonMarkerButton;
	LocationManager locationManager;
	ArrayList<LatLng> latLang = new ArrayList<LatLng>();
	ArrayList<IGeoPoint> listPoints = new ArrayList<IGeoPoint>();
	boolean isGeometryClosed = false;
	boolean isStartGeometry = false;
	Polygon polygon;
	Context context = JobViewerMapActivity.this;
	boolean isPolyDrawStarted = false;
	boolean isLineDrawStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isGooglePlayServicesAvailable()) {
			finish();
		}
		setContentView(R.layout.map_screen);
		SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.googleMap);
		initMap(supportMapFragment);

		lineMarkerButton = (ImageButton) findViewById(R.id.lineMarkerButton);
		lineMarkerButton.setOnClickListener(this);
		mapOptionSelectorButton = (ImageButton) findViewById(R.id.mapOptionSelectorButton);
		mapOptionSelectorButton.setOnClickListener(this);
		polygonMarkerButton = (ImageButton) findViewById(R.id.polygonMarkerButton);
		polygonMarkerButton.setOnClickListener(this);
	}

	private void initMap(SupportMapFragment supportMapFragment) {
		googleMap = supportMapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setMapToolbarEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
		googleMap.setOnMapClickListener(this);
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
		googleMap.clear();
		ImageButton snapShotButton = (ImageButton) findViewById(R.id.doneButton);
		snapShotButton.setOnClickListener(this);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		locationManager.removeUpdates(this);
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
		switch (v.getId()) {
		case R.id.doneButton:
			captureMapScreen();
			break;
		case R.id.mapOptionSelectorButton:
			openMapOptionsDialog();
			break;
		case R.id.polygonMarkerButton:
			if (!isPolyDrawStarted) {
				if (isLineDrawStarted) {
					isStartGeometry = false;
					isLineDrawStarted = false;
					lineMarkerButton.setImageDrawable(ContextCompat
							.getDrawable(context, R.drawable.map_lineoff));
					clearCanvas(v);
				}
				isStartGeometry = true;
				isPolyDrawStarted = true;
				polygonMarkerButton.setImageDrawable(ContextCompat.getDrawable(
						context, R.drawable.map_polyon));
			} else {
				isPolyDrawStarted = false;
				isStartGeometry = false;
				polygonMarkerButton.setImageDrawable(ContextCompat.getDrawable(
						context, R.drawable.map_polyoff));
				clearCanvas(v);
			}

			break;
		case R.id.lineMarkerButton:
			if (!isLineDrawStarted) {
				if (isPolyDrawStarted) {
					isPolyDrawStarted = false;
					isStartGeometry = false;
					polygonMarkerButton.setImageDrawable(ContextCompat
							.getDrawable(context, R.drawable.map_polyoff));
					clearCanvas(v);
				}
				isStartGeometry = true;
				isLineDrawStarted = true;
				lineMarkerButton.setImageDrawable(ContextCompat.getDrawable(
						context, R.drawable.map_lineon));
			} else {
				isStartGeometry = false;
				isLineDrawStarted = false;
				lineMarkerButton.setImageDrawable(ContextCompat.getDrawable(
						context, R.drawable.map_lineoff));
				clearCanvas(v);
			}

			break;
		default:
			break;
		}

	}

	public void openMapOptionsDialog() {

		View view = JobViewerMapActivity.this.getLayoutInflater().inflate(
				R.layout.map_options_dialog, null);

		ListView listView = (ListView) view.findViewById(R.id.list);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				Utils.mMapOptions) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text = (TextView) view
						.findViewById(android.R.id.text1);
				text.setTextColor(BLACK);
				return view;
			}
		};
		listView.setAdapter(adapter);

		final Dialog dialog = new Dialog(JobViewerMapActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow()
				.setBackgroundDrawable(new ColorDrawable(TRANSPARENT));
		dialog.setContentView(view);
		dialog.show();
		dialog.setCancelable(false);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:
					Log.i("Android", "" + position);
					googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					break;
				case 1:
					Log.i("Android", "" + position);
					googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					break;
				case 2:
					Log.i("Android", "" + position);
					googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
					break;
				case 3:
					Log.i("Android", "" + position);
					googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
					break;

				}
				dialog.dismiss();
			}
		});
	}

	@Override
	public void onMapClick(LatLng latlan) {

		if (isPolyDrawStarted) {
			drawPointAndLine(latlan);

			if (latLang.size() == 5) {
				Draw_Map();
				isGeometryClosed = true;
				isStartGeometry = false;
			}
		} else if (isLineDrawStarted) {
			drawPointAndLine(latlan);
		}

	}

	private void drawPointAndLine(LatLng latlan) {
		if (!isGeometryClosed && isStartGeometry) {
			latLang.add(latlan);
			GeoPoint point = new GeoPoint(latlan.latitude, latlan.longitude);
			listPoints.add((IGeoPoint) point);
			MarkerOptions marker = new MarkerOptions().position(latlan);
			googleMap.addMarker(marker);
			if (latLang.size() > 1) {

				PolylineOptions polyLine = new PolylineOptions().color(
						ContextCompat
								.getColor(context, R.color.headerBackColor))
						.width((float) 7.0);
				polyLine.add(latlan);
				LatLng previousPoint = latLang.get(latLang.size() - 2);
				polyLine.add(previousPoint);
				googleMap.addPolyline(polyLine);
			}
		}
	}

	public void Draw_Map() {
		PolygonOptions rectOptions = new PolygonOptions();
		rectOptions.addAll(latLang);
		rectOptions.strokeColor(ContextCompat.getColor(context,
				R.color.headerBackColor));
		// rectOptions.fillColor(Color.CYAN);
		rectOptions.strokeWidth(7);
		polygon = googleMap.addPolygon(rectOptions);
	}

	public void clearCanvas(View view) {
		try {
			googleMap.clear();
			latLang = new ArrayList<LatLng>();
			listPoints = new ArrayList<IGeoPoint>();
			isGeometryClosed = false;

		} catch (Exception e) {
		}

	}

}
