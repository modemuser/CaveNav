package org.misera.android.cavenav;

import java.io.IOException;
import java.io.InputStream;

import org.misera.android.cavenav.MapView.Mode;
import org.misera.android.cavenav.map.MapBundle;
import org.misera.android.cavenav.raycaster.RayCastRendererView;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MapActivity extends Activity {
		
    private MapView mapView;
    private RayCastRendererView rayCastView;
    
    private boolean mapIsMainView = true;
    private boolean hasFlashLight;
    private boolean flashLightEnabled = false;

    private OnFlashlightClickListener flashLightListener = new OnFlashlightClickListener(this);

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        hasFlashLight = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        	);
        load("caestert_negative.png");
	} 
       
	private void load(String filename) {	
		Bitmap pic = getBitmapFromAsset(filename);
		String json = getStringFromAsset("caestert.json");
		
		double pixelLength = 0.5;
		MapBundle mb = new MapBundle(pic, pixelLength);
		mb.initGraph(json);
		
		mapView = new MapView(this, mb);
		mapView.setMode(Mode.NORMAL);
		
		Bitmap map = getBitmapFromAsset("caestert_canny.png");

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
	
	private class OnFlashlightClickListener implements OnMenuItemClickListener{

		private Camera cam;
		private Activity activity;
		
		public OnFlashlightClickListener(Activity activity){
			this.activity = activity;
		}
		
    	
		public boolean onMenuItemClick(MenuItem item) {
			flashLightEnabled = !flashLightEnabled;
			if(flashLightEnabled){
				cam = Camera.open();
				Parameters p = cam.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				cam.setParameters(p);
				cam.startPreview();
			}
			else{
				cam.stopPreview();
				cam.release();
			}
			
			activity.invalidateOptionsMenu();
			return false;
		}
		
	}
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        
        if(hasFlashLight){
            MenuItem flashLightItem;
            if(flashLightEnabled){
            	flashLightItem = menu.add("Light off");
            }else{
            	flashLightItem = menu.add("Light on");
            }
            
            flashLightItem.setOnMenuItemClickListener(flashLightListener);
            flashLightItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.mode_normal:
	        	item.setChecked(true);
	    		mapView.setMode(Mode.NORMAL);
	    		return true;
	    	case R.id.mode_waypoint:
	        	item.setChecked(true);
	    		mapView.setMode(Mode.WAYPOINT);
	    		return true;
	    	case R.id.mode_graph:
	        	item.setChecked(true);
	    		mapView.setMode(Mode.GRAPH);
	    		return true;
	    	case R.id.mode_poi:
	        	item.setChecked(true);
	    		mapView.setMode(Mode.POI);
	    		return true;
            case R.id.menu_clear:
                mapView.clear();
                return true;
            case R.id.click_stepping:
            	item.setChecked(!item.isChecked());
                mapView.toggleClickStepping();
                return true;
            case R.id.follow_edes:
            	item.setChecked(!item.isChecked());
            	mapView.toggleFollowEdges();
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
