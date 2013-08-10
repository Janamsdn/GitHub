package com.jana.android.lab;



import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;





import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ShowRestaurantsActivity extends Activity implements LocationListener {
	
	// flag for Internet connection status
		Boolean isInternetPresent = false;

		double mLatitude=0;
		double mLongitude=0;
		// Alert Dialog Manager
		AlertDialogManager alert = new AlertDialogManager();

		// Google Places
		GooglePlaces googlePlaces;

		// Restaurant List
		RestaurantsList nearPlaces;
		//ArrayList<HashMap<String, String>> nearPlaces = new ArrayList<HashMap<String,String>>();
	  // Progress dialog
		ProgressDialog pDialog;
		
		// Restaurants Listview
		ListView lv;

		// GPS Location
		GPSTracker gps;

		// ListItems data
		ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();
		
		
		// KEY Strings
		public static String KEY_REFERENCE = "reference"; // id of the place
		public static String KEY_NAME = "name"; // name of the place
		public static String KEY_VICINITY = "vicinity"; // Place area name
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.show_restarants);


    	// Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location From GPS
        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null){
                onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(provider, 20000, 0, this);
        
		// Getting listview
		lv = (ListView) findViewById(R.id.list);
		/**
		 * ListItem click event
		 * On selecting a listitem SinglePlaceActivity is launched
		 * */
		lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	// getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
                
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);
                
                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                startActivity(in);
            }
        });
	
		// calling background Async task to load Google Places
				// After getting places from Google all the data is shown in listview
				new LoadPlaces().execute();
	}
	/**
	 * Background Async Task to Load Google places
	 * */
	class LoadPlaces extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ShowRestaurantsActivity.this);
			pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();
			
			try {
				// Separeate your place types by PIPE symbol "|"
				// If you want all types places make it as null
				// Check list of types supported by google
				// 
				String types = "restaurant"; // Listing places only  restaurants
				
				// Radius in meters - increase this value if you don't find any places
				double radius = 1000; // 1000 meters 
				//sb.append("location="+mLatitude+","+mLongitude);
				// get nearest Restaurants
				nearPlaces = googlePlaces.search(mLatitude,
						mLongitude, radius, types);
				

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * and show the data in UI
		 * Always use runOnUiThread(new Runnable()) to update UI from background
		 * thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					// Get json response status
					String status = nearPlaces.status;
					
					// Check for all possible status
					if(status.equals("OK")){
						// Successfully got places details
						if (nearPlaces.results != null) {
							// loop through each place
							for (Restaurants p : nearPlaces.results) {
								HashMap<String, String> map = new HashMap<String, String>();
								
								// Place reference won't display in listview - it will be hidden
								// Place reference is used to get "place full details"
								map.put(KEY_REFERENCE, p.reference);
								
								// Place name
								map.put(KEY_NAME, p.name);
								
								
								// adding HashMap to ArrayList
								placesListItems.add(map);
							}
							// list adapter
							ListAdapter adapter = new SimpleAdapter(ShowRestaurantsActivity.this, placesListItems,
					                R.layout.list_item,
					                new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
					                        R.id.reference, R.id.name });
							
							// Adding data into listview
							lv.setAdapter(adapter);
						}
					}
					else if(status.equals("ZERO_RESULTS")){
						// Zero results found
						alert.showAlertDialog(ShowRestaurantsActivity.this, "Near Restaurants",
								"Sorry no Restaurant found. Try to change the types of Restaurant",
								false);
					}
					else if(status.equals("UNKNOWN_ERROR"))
					{
						alert.showAlertDialog(ShowRestaurantsActivity.this, "Restaurant Error",
								"Sorry unknown error occured.",
								false);
					}
					else if(status.equals("OVER_QUERY_LIMIT"))
					{
						alert.showAlertDialog(ShowRestaurantsActivity.this, "Restaurant Error",
								"Sorry query limit to google places is reached",
								false);
					}
					else if(status.equals("REQUEST_DENIED"))
					{
						alert.showAlertDialog(ShowRestaurantsActivity.this, "Restaurant Error",
								"Sorry error occured. Request is denied",
								false);
					}
					else if(status.equals("INVALID_REQUEST"))
					{
						alert.showAlertDialog(ShowRestaurantsActivity.this, "Restaurant Error",
								"Sorry error occured. Invalid Request",
								false);
					}
					else
					{
						alert.showAlertDialog(ShowRestaurantsActivity.this, "Restaurant Error",
								"Sorry error occured.",
								false);
					}
				}
			});

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		
		
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
}