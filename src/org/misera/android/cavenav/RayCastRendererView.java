package org.misera.android.cavenav;

import android.content.*;
import android.graphics.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;

import java.util.*;

public class RayCastRendererView extends View
{

	public RayCaster rayCaster;
	private Bitmap mapImg;
	
	public RayCastRendererView(Context context, Bitmap mapImg){
		super(context);
		int[][] map = RayCaster.bitmapToMap(mapImg);
		
		this.rayCaster = new RayCaster(map);
		rayCaster.playerPos[0] = 100;
		rayCaster.playerPos[1] = 100;
		rayCaster.viewingAngle = 0;
		this.mapImg = mapImg;
		
		this.setOnClickListener(clickListener);
	}
		
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
	    canvas.save();
	    canvas.drawColor(Color.BLACK);
		Paint paint = new Paint();
		int resX = this.getWidth();
		int resY = this.getHeight();
		double maxDistance = 80;
		int maxColor = 200;
		
		int middle = resY / 2;
		
		int[] res = {resX, resY};
		
		this.rayCaster.setResolution(res);
		
		Ray[] rays = this.rayCaster.castRays();

		for(int i = 0; i < rays.length; i++){

			Ray ray = rays[i];
			int id = ray.id;
			int distance = (int) Math.min(ray.distance, maxDistance);
			
			int grayValue = (int) Math.floor(maxColor - ((maxColor / maxDistance) * distance));
			
			if(id != RayCaster.BOUNDARY){
				//Log.i("RayCaster", "Distance at x: " + i + " " + distance + " color:" + grayValue );
				int lineHeight = ray.sliceHeight;
				
				float lineStart = middle - (lineHeight / 2);
				float lineEnd = lineStart + lineHeight;
				
				int c = Color.rgb(grayValue, grayValue, grayValue);
				
				paint.setColor(c);
				canvas.drawLine(i, lineStart, i, lineEnd, paint);
			}
			else{
				paint.setColor(Color.RED);
				canvas.drawLine(i, 0, i, res[1], paint);
			}
		}
		
		canvas.restore();
		
	}
	
	private OnClickListener clickListener = new OnClickListener(){
		public void onClick(View v){
			float stepLength = 0.75f;
			float pixelLength = 0.5f;
			
			float dx = 1;
			float dy = -(stepLength / pixelLength);
			
			rayCaster.playerPos[0] = (int) (rayCaster.playerPos[0] + dx);
			rayCaster.viewingAngle += 10;
			//Log.i("RayCaster", "Player pos: (" + rayCaster.playerPos[0] + "|" + rayCaster.playerPos[1] + ")");
			
			invalidate();
			//return false;
		}
	};
}
