package org.misera.android.cavenav.map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class POIComponent {
	
	private ArrayList<POI> poiList;
	private String mapName;
	
	public POIComponent(String mapName) {
		this.mapName = mapName;
		this.poiList = new ArrayList<POI>();
	}
	
	public void addPOI(int posX, int posY, String text) {
		POI poi = new POI(posX, posY, text);
		this.poiList.add(poi);
		writeFile();
	}
	
	public Canvas draw(Canvas canvas, MapScreen ms) {
		Paint paint = new Paint();
		paint.setColor(Color.CYAN);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(10 + ms.getScale());
		for (POI poi : poiList) {
			float[] coords = ms.mapToScreenCoords(poi.x, poi.y);
			canvas.drawCircle(coords[0], coords[1], ms.getScale(), paint);
			canvas.drawText(poi.text, coords[0]+5, coords[1]+3*ms.getScale(), paint);
		}
		return canvas;
	}
	
	public String toJsonString() {
		String out = "[";
		for (POI poi : this.poiList) {
			String s = String.format("{\"x\":%.0f, \"y\":%.0f, \"text\":\"%s\"}", poi.x, poi.y, poi.text);
			out += s + ", ";
		}
		out = out.substring(0, out.length()-2) + "]";
		return out;
	}
	
	public void readFile() {
	    try {
	    	String filename = "/sdcard/CaveNav/"+this.mapName+"/poi.json";
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
		    String text = "";

		    while(line != null) {
		    	text += line;
		        line = reader.readLine();
		    }
		    reader.close();
		    JSONArray json = new JSONArray(text);
		    setPOIs(json);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void writeFile() {
		try{
	    	String path = "/sdcard/CaveNav/"+this.mapName;
	        File directory = new File(path);
	        directory.mkdirs();
	        File file = new File(directory, "poi.json");
	        Writer output = null;
	        output = new BufferedWriter(new FileWriter(file));
	        output.write(this.toJsonString());
	        output.close();

	    }catch(Exception e){
	    }
	}
	
	private void setPOIs(JSONArray array) throws JSONException {
		ArrayList<POI> list = new ArrayList<POI>();
		for (int i=0; i<array.length(); i++) {
			JSONObject poi = array.getJSONObject(i);
			int x = poi.getInt("x");
			int y = poi.getInt("y");
			String text = poi.getString("text");
			POI newPOI = new POI(x, y, text);
			list.add(newPOI);
		}
		this.poiList = list;
	}
}
