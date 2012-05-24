package org.misera.android.cavenav;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.misera.android.cavenav.MapView.Mode;
import org.misera.android.cavenav.map.MapBundle;
import org.misera.android.cavenav.raycaster.RayCastRendererView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MapActivity extends Activity {
		
    private MapView mapView;
    private MapBundle mapBundle;
    private RayCastRendererView rayCastView;
    
    private boolean mapIsMainView = true;
    private boolean hasFlashLight;
    private boolean flashLightEnabled = false;

    private OnFlashlightClickListener flashLightListener = new OnFlashlightClickListener(this);

    private ProgressDialog mProgressDialog;
	private String mapName;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        hasFlashLight = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        	);
        load("caestert");
	} 
       
	private void load(String mapName) {
		this.mapName = mapName;
		String folder = mapName + "/";
		Bitmap map = getBitmapFromAsset(folder + "map.png");
		String graph = getStringFromAsset(folder + "graph.json");
		double pixelLength = 0.5;
		mapBundle = new MapBundle(this.mapName, map, pixelLength);
		mapBundle.initGraph(graph);
		
		mapView = new MapView(this, mapBundle);
		mapView.setMode(Mode.NORMAL);
		
		Bitmap canny = getBitmapFromAsset(folder + "canny.png");
		rayCastView = new RayCastRendererView(this, canny);

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
            case R.id.caestert:
            	load("caestert");
            	return true;
            case R.id.ternaaien:
            	load("ternaaien");
            	return true;
            case R.id.switch_views:
            	toggleSwitchView();
            	return true;
            case R.id.sync_poi:
            	//first, upload any new POIs
            	String url = "http://cavenav.android.misera.org/poi/" + mapName;
    		    String text = mapBundle.poi.toJsonString();
				UploadFile upload = new UploadFile();
				upload.execute(url, text);
				// second, download merged list of POIs
            	DownloadFile download = new DownloadFile();
            	download.execute(url);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                URL url = new URL(sUrl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();
                
                // create a File object for the parent directory
                File directory = new File("/sdcard/CaveNav/" + mapName);
                // have the object build the directory structure, if needed.
                directory.mkdirs();
                // create a File object for the output file
                File outputFile = new File(directory, "poi.json");
                
                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(outputFile);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            	Log.e("CaveNav", e.getMessage());
            }
            return null;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setProgress(progress[0]);
        } 
        
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
			mapBundle.poi.readFile();
            Toast.makeText(getApplicationContext(), "Download complete.", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private class UploadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... input) {
            try {
                String url = input[0];
                String data = input[1];
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpParams myParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(myParams, 10000);
                HttpConnectionParams.setSoTimeout(myParams, 10000);
                // to make the request faster, use http1.1
                myParams.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

                HttpPost httppost = new HttpPost(url);
                httppost.setHeader("Content-type", "application/json");

                StringEntity se = new StringEntity(data); 
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se); 

                HttpResponse response = httpclient.execute(httppost);
                String temp = EntityUtils.toString(response.getEntity());
                Log.i("CaveNav", temp);

            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            } catch (Exception e) {
            }
            return null;
        }
        
        
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);  
            Toast.makeText(getApplicationContext(), "Upload complete.", Toast.LENGTH_SHORT).show();
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
		    Bitmap bitmap = BitmapFactory.decodeStream(istr);
		    return bitmap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
