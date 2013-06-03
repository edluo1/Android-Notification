package com.wanikani.wklib;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class Radical extends Item {

	private static class Factory implements Item.Factory<Radical> {

		public Radical deserialize (JSONObject obj)
			throws JSONException
		{
			return new Radical (obj);
		}
	}

	public final static Factory FACTORY = new Factory ();

	public Bitmap bitmap;
	
	public String image;
	
	public Radical (JSONObject obj)
		throws JSONException
	{
		super (obj, Item.Type.RADICAL);
		
		image = Util.getString (obj, "image");
	}	

	@Override
	protected boolean hasReading ()
	{
		return false;
	}
}
