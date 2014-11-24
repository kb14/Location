package com.plateconstant.android;


import java.io.*;
import java.util.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

public class LocationActivity extends ActionBarActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
			GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	
	TextView myLocationText;
	
	
	/*
	 * New/updated variables
	 * Kangkan. November 17, 2014
	 */
	private GoogleMap myMap;	// Map object
	
	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    
    LocationClient mLocationClient;
    
    // Global variable to hold the current location
    Location mCurrentLocation;
    
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    
    boolean mUpdatesRequested;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	
	// Zoom flag
	private boolean alreadyZoomed = false;
	
	// Share button and comment edittext
	Button shareBtn;
	EditText commentField;
	
	// Selected Items Arraylist : Co-Ordinates/Address
	ArrayList<Integer> selectedItems;		// 0:Location, 1:Address
	// Multiple Items
	CharSequence[] items = {"Latitude/Longitude", "Address"};


	private AlertDialog alert;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		// Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
		
		/*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
        // Start with updates turned off
        mUpdatesRequested = true;
        
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        
        selectedItems = new ArrayList<Integer>();
        
        shareBtn = (Button) findViewById(R.id.shareBtn);
        commentField = (EditText) findViewById(R.id.commentText);
        
        shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	selectedItems.add(0);
            	showAlertDialogWithMultipleOptions();
            }
        });
		
	}
	
	@Override
    protected void onResume() {
		super.onResume();
		
		/*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", true);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
		
		setUpMapIfNeeded();		
	}
	
	@Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }
	
	/*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
    	
    	// If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
        	mLocationClient.removeLocationUpdates(this);
        }
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
	
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (myMap == null) {
	        myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMapFragment))
	                            .getMap();
	        // Check if we were successful in obtaining the map.
	        if (myMap != null) {
	            myMap.setMyLocationEnabled(true);
	            //myMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
	            alreadyZoomed = false;
	        }
	    }
	}
	
	public void showAlertDialogWithMultipleOptions(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Include...");
        builder.setMultiChoiceItems(items, new boolean[]{true, false}, new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
				if (isChecked) {
                    
                    selectedItems.add(indexSelected);
                } else if (selectedItems.contains(indexSelected)) {
                   
                    selectedItems.remove(Integer.valueOf(indexSelected));
                    
                }
				
				Button buttonDone = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				if(selectedItems.size() == 0){
                	
                	buttonDone.setEnabled(false);
                }
				else{
					buttonDone.setEnabled(true);
				}
				
			}
        	
        })
        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                 showShareLocationDialog();
            	 dialog.dismiss();
             }
         })
         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
               selectedItems.clear();
               dialog.dismiss();
             }
         });
        alert = builder.create();
        alert.show();
	}
	
	
	// Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }
     }
    
    public void showShareLocationDialog(){
    	
    	String shareText = "";
    	String fullText = myLocationText.getText().toString();
    	if(selectedItems.size() == 2){						// Share both lat/long and address
    		shareText = fullText;
    	}
    	else{
    		String splitItems[] = fullText.split("\n");
    		int len = splitItems.length;
    		if(len > 0){
    			if(selectedItems.contains(0)){					// Only lat/long case
        			shareText = splitItems[0];
        		}
        		else if(selectedItems.contains(1)){				// Only address case
        			for(int i=1; i<len; i++){
        				shareText = shareText + splitItems[i] + "\n";
        			}
        		}
    		
    		}
    	}
    	
    	Intent sendIntent = new Intent();
    	sendIntent.setAction(Intent.ACTION_SEND);
    	sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
    	sendIntent.setType("text/plain");
    	startActivity(Intent.createChooser(sendIntent, "Share Location"));
    	
    	selectedItems.clear();
    }
    
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason.
        // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }
        return false;
    }
    
    public void showErrorDialog(int errorCode){
    	Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment =
                    new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(),
                    "Location Updates");
        }
    }
    
	/*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }
	
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		// Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        
        // If already requested, start periodic updates
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
        
        mCurrentLocation = mLocationClient.getLastLocation();
		updateWithNewLocation(mCurrentLocation);
		
	}

	/*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		// Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
		
	}
	
	
		
	@Override
	public void onLocationChanged(Location location) {
		updateWithNewLocation(location);
	}
	
	
	
	private void updateWithNewLocation(Location location) {
		
		String latLongString;
		myLocationText = (TextView)findViewById(R.id.myLocationText);
		
		String addressString = "No address found";
		
		if (location != null) {
			
			
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			
			LatLng latLng = new LatLng(lat, lng);
			
			if(!alreadyZoomed){
				myMap.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 16.0f) );
				alreadyZoomed = true;
			}
			
			latLongString =  lat + "," + lng;
			
			Geocoder gc = new Geocoder(this, Locale.getDefault());
			try {
				List<Address> addresses = gc.getFromLocation(lat, lng, 1);
				
				StringBuilder sb = new StringBuilder();
				if (addresses.size() > 0) {
					Address address = addresses.get(0);
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
						
						if(address.getAddressLine(i) != null && !address.getAddressLine(i).equals("")){
							sb.append(address.getAddressLine(i)).append("\n");
						}	
					}
					
					if(address.getLocality() != null && !address.getLocality().equals("")){
						sb.append(address.getLocality()).append("\n");
					}
					
					if(address.getPostalCode()!=null && !address.getPostalCode().equals("")){
						sb.append(address.getPostalCode()).append("\n");
					}
					
					if(address.getCountryName() != null && !address.getCountryName().equals("")){
						sb.append(address.getCountryName());
					}
				}
				
				addressString = sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			
			latLongString = "No location found";
			
		}
		myLocationText.setText(latLongString+ "\n" + addressString);
	}

	
	

	
}