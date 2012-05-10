package org.misera.android.cavenav;

import java.io.IOException;
import java.io.InputStream;

import org.misera.android.cavenav.graph.Graph;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MapActivity extends Activity {
		
    private MapView mapView;
    private RayCastRendererView rayCastView;
    
    private boolean mapIsMainView = true;


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        	);
        load("caestert_negative.png");
	} 
       
	private void load(String filename) {	
		Bitmap pic = getBitmapFromAsset(filename);
		String json = getStringFromAsset("vertices.json");
		Graph graph = new Graph(json);
		
		mapView = new MapView(this, pic, graph);
		
		
		Bitmap map = getBitmapFromAsset("canny.png");

		rayCastView = new RayCastRendererView(this, map);

		setContentView(R.layout.main);
		LinearLayout main = (LinearLayout) findViewById(R.id.contentMain);
		main.addView(mapView);
		
		LinearLayout miniMap = (LinearLayout) findViewById(R.id.miniMap);
		miniMap.addView(rayCastView);
		
		mapView.setRayCaster(rayCastView);
		mapView.requestFocus();        

    }
	
	private void toggleSwitchView(){
		LinearLayout main = (LinearLayout) findViewById(R.id.contentMain);
		LinearLayout miniMap = (LinearLayout) findViewById(R.id.miniMap);
		
		if(mapIsMainView){
			main.removeView(mapView);
			miniMap.removeView(rayCastView);
			
			main.addView(rayCastView,0);
			miniMap.addView(mapView, 0);
		}
		else{
			main.removeView(rayCastView);
			miniMap.removeView(mapView);
			
			main.addView(mapView,0);
			miniMap.addView(rayCastView, 0);			
		}

		mapIsMainView = !mapIsMainView;
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
            case R.id.toggle_paths:
            	item.setChecked(!item.isChecked());
                mapView.togglePaths();
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
            case R.id.switch_views:
            	toggleSwitchView();
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
	
	
}
