package org.misera.android.cavenav;

import android.content.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class RayCastRendererView extends View
{

	private RayCaster rayCaster;
	private Bitmap mapImg;
	
	public RayCastRendererView(Context context, RayCaster rayCaster, Bitmap mapImg){
		super(context);
		this.rayCaster = rayCaster;
		this.mapImg = mapImg;
	}
		
	protected void onDraw(Canvas canvas){
		Ray[] rays = this.rayCaster.castRays();
		for(int i = 0; i < rays.length; i++){
			Ray ray = rays[i];
			int id = ray.id;
			double distance	= ray.distance;
			
		}
		
	}
}
