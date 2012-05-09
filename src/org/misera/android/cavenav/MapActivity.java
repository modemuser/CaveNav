package org.misera.android.cavenav;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MapActivity extends Activity {
	
	Bitmap pic;
	View view;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		try {
			pic = getBitmapFromAsset("caestert_negative.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SensorManager mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		view = new MapView(this, pic, mSensorManager);
		

		setContentView(R.layout.main);
		LinearLayout v = (LinearLayout) findViewById(R.id.contentMain);
		v.addView(view);
		
		view.requestFocus();        

    }
    
    /**
	 * Helper Function
	 * @throws IOException 
	 */
	private Bitmap getBitmapFromAsset(String strName) throws IOException
	{
	    AssetManager assetManager = getAssets();
	
	    InputStream istr = assetManager.open(strName);
	    Bitmap bitmap = BitmapFactory.decodeStream(istr);
	
	    return bitmap;
	}
}
