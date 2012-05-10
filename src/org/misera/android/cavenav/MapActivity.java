package org.misera.android.cavenav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MapActivity extends Activity {
		
    private MapView mapView;


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        	);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
        load("caestert_negative.png");
	} 
       
	private void load(String filename) {	
		Bitmap pic = getBitmapFromAsset(filename);
		ArrayList<Point> vertices = new ArrayList<Point>();
		ArrayList<Point[]> edges = new ArrayList<Point[]>();
		
		if (filename.contains("caestert")) {
			try {
				vertices = getVertices();
				//edges = getEdges(vertices);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mapView = new MapView(this, pic, vertices, edges);
		
		
		Bitmap map = getBitmapFromAsset("canny.png");

		RayCastRendererView rayCastView = new RayCastRendererView(this, map);

		setContentView(R.layout.main);
		LinearLayout main = (LinearLayout) findViewById(R.id.contentMain);
		main.addView(mapView);
		
		LinearLayout miniMap = (LinearLayout) findViewById(R.id.miniMap);
		miniMap.addView(rayCastView);
		
		mapView.setRayCaster(rayCastView);
		mapView.requestFocus();        

    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mapView.clearMarkers();
                return true;
            case R.id.click_stepping:
            	item.setChecked(!item.isChecked());
                mapView.toggleClickStepping();
                return true;
            case R.id.caestert_negative:
            	load("caestert_negative.png");
            	return true;
            case R.id.caestert:
            	load("caestert.png");
            	return true;
            case R.id.geogif:
            	load("geo.gif");
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	/**
	 * Helper Functions
	 */
    
	private Bitmap getBitmapFromAsset(String strName) {
	    AssetManager assetManager = getAssets();
	
	    InputStream istr = null;
		try {
			istr = assetManager.open(strName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Bitmap bitmap = BitmapFactory.decodeStream(istr);
	
	    return bitmap;
	}
	
	private String getStringFromAsset(String strName) {
		AssetManager assetManager = getAssets();
		
	    InputStream istr = null;
		try {
			istr = assetManager.open(strName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    try {
	        return new java.util.Scanner(istr).useDelimiter("\\A").next();
	    } catch (java.util.NoSuchElementException e) {
	        return "";
	    }
	}
	
	private ArrayList<Point> getVertices() throws JSONException {
		String str = getStringFromAsset("vertices.json");
		JSONObject json = new JSONObject(str);
		JSONObject nodes = json.getJSONObject("nodes");
		ArrayList<Point> out = new ArrayList<Point>();
		// there are 946 nodes/vertices
		for (int i=0; i<=946; i++) {
			JSONObject coords = nodes.getJSONObject(Integer.toString(i)).getJSONObject("coordinates");
			// translating the points because otherwise they seem off
			int x = coords.getInt("x") - 7;
			int y = coords.getInt("y") - 52;
			out.add(new Point(x, y));
		}
		return out;
	}
	

    private ArrayList<Point[]> getEdges(ArrayList<Point> vertices) throws JSONException {
    	String str = getStringFromAsset("vertices.json");
    	Log.d("FUCK", Integer.toString(str.length()));
		JSONObject json = new JSONObject(str);
		JSONObject edges = json.getJSONObject("edges");
		ArrayList<Point[]> out = new ArrayList<Point[]>();
		
		for (int i=0; i<=1621; i++) {
			JSONObject endpoints = edges.getJSONObject(Integer.toString(i)).getJSONObject("nodes");
	    	Log.d("FUCK", endpoints.toString());
			Point start = vertices.get(endpoints.getInt("start"));
			Point end = vertices.get(endpoints.getInt("end"));
			Point[] newEdge = {start, end};
			out.add(newEdge);
		}
		return out;
	}
	
}
