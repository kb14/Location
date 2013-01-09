package apt.kangkan;


import java.io.*;
import java.util.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
public class LocationActivity extends MapActivity {
	Button send;
	String senderMail;
	String recvMail;
	EditText restaurantName;
	String rName;
	UserEmailFetcher umf = new UserEmailFetcher();
	AsyncTask<Void, Void, Void> sendEmailTask;
	TextView myLocationText;
	Context cxt;
	AlertDialogManager alert = new AlertDialogManager();
	@Override
	protected boolean isRouteDisplayed() {
		return false;
		}
	MapView myMapView = null;
	MapController mapController;
	MyLocationOverlay myLocationOverlay;
	//MyPositionOverlay positionOverlay;
	@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.main);
	
	cxt = getApplicationContext();
	//Get a reference to the MapView
	 myMapView = (MapView)findViewById(R.id.myMapView);
	 restaurantName = (EditText) findViewById(R.id.restaurantName);
	 
	//Get the Map View’s controller
	mapController = myMapView.getController();
	
	//Configure the map display options
	myMapView.setSatellite(false);
	
	//myMapView.setStreetView(true);
	myMapView.setBuiltInZoomControls(true);
	
	//Zoom in
	mapController.setZoom(17);
	
	// Add the MyPositionOverlay
	List<Overlay> overlays = myMapView.getOverlays();
	myLocationOverlay =new MyLocationOverlay(this, myMapView);
	overlays.add(myLocationOverlay);
	
	//myLocationOverlay.enableCompass();
	myLocationOverlay.enableMyLocation();

	LocationManager locationManager;
	String context = Context.LOCATION_SERVICE;
	locationManager = (LocationManager)getSystemService(context);
	
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	criteria.setAltitudeRequired(false);
	criteria.setBearingRequired(false);
	criteria.setCostAllowed(true);
	criteria.setPowerRequirement(Criteria.POWER_LOW);
	String provider = locationManager.getBestProvider(criteria, true);
	Location location =
	locationManager.getLastKnownLocation(provider);
	updateWithNewLocation(location);
	locationManager.requestLocationUpdates(provider, 2000, 10,
	locationListener);
	send = (Button) findViewById(R.id.send);
	 
   send.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
        	SqliteHandler db = new SqliteHandler(getApplicationContext());
            HashMap<String,String> user = db.getUserDetails();
            final String password = user.get("password");
        	rName = restaurantName.getText().toString();
    		recvMail = "kangkan_14@yahoo.co.in";
    		senderMail = UserEmailFetcher.getEmail(getApplicationContext());
    		sendEmailTask = new AsyncTask<Void, Void, Void>(){
            	
            	@Override
                protected Void doInBackground(Void... params) {
            		try {
            			
                        GMailSender sender = new GMailSender(senderMail, password);
                        sender.sendMail(rName,   
                                myLocationText.getText().toString(),   
                                senderMail,   
                                recvMail);   
                    } catch (Exception e) {   
                        Log.e("SendMail", e.getMessage(), e);   
                    } 
            		return null;
            	}
            	@Override
                protected void onPostExecute(Void result) {
            		//Toast.makeText(cxt, "Email sent", Toast.LENGTH_LONG).show();
            		alert.showAlertDialog(LocationActivity.this, "Location",
        	                "Email Sent!", true);
            	}
    		};	
    		
    		sendEmailTask.execute(null, null, null);
    		
    		
        }
    });
	}
	
private final LocationListener locationListener = new LocationListener() {
public void onLocationChanged(Location location) {
updateWithNewLocation(location);
}
public void onProviderDisabled(String provider){
updateWithNewLocation(null);
}
public void onProviderEnabled(String provider){ }
public void onStatusChanged(String provider, int status,
Bundle extras){ }
};
private void updateWithNewLocation(Location location) {
	
	String latLongString;
	myLocationText = (TextView)findViewById(R.id.myLocationText);
	
	String addressString = "No address found";
	
	if (location != null) {
		// Update my location marker
		//myLOverlay.setLocation(location);
		
		
		// Update the map location.
		Double geoLat = location.getLatitude()*1E6;
		Double geoLng = location.getLongitude()*1E6;
		GeoPoint point = new GeoPoint(geoLat.intValue(),
		geoLng.intValue());
		mapController.animateTo(point);
		
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		latLongString =  lat + "," + lng;
		
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		try {
		List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
		StringBuilder sb = new StringBuilder();
		if (addresses.size() > 0) {
		Address address = addresses.get(0);
		for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
			
			if(address.getAddressLine(i) != null){
				sb.append(address.getAddressLine(i)).append("\n");
			}	
		}
		
		if(address.getLocality() != null){
			sb.append(address.getLocality()).append("\n");
		}
		
		if(address.getPostalCode()!=null){
			sb.append(address.getPostalCode()).append("\n");
		}
		
		if(address.getCountryName() != null){
			sb.append(address.getCountryName());
		}
		}
		
		addressString = sb.toString();
		} catch (IOException e) {}
	} else {
		latLongString = "No location found";
	}
	myLocationText.setText(latLongString+ "\n" + addressString);
	}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
  if (keyCode == KeyEvent.KEYCODE_S) {
	  myMapView.setSatellite(!myMapView.isSatellite());
    return(true);
  }
  else if (keyCode == KeyEvent.KEYCODE_Z) {
	  myMapView.displayZoomControls(true);
    return(true);
  }
  return(super.onKeyDown(keyCode, event));
}

}