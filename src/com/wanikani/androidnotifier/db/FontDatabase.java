package com.wanikani.androidnotifier.db;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;

/* 
 *  Copyright (c) 2013 Alberto Cuda
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class FontDatabase {

	public enum WellKnownFont {
		
		SYSTEM {
			public String getName ()
			{
				return "System font";
			}			
			
			public String getURL ()
			{
				return null;
			}
			
			public int sinceVersion ()
			{
				return 1;
			}
		},
		
		MOTOYA {
			public String getName ()
			{
				return "Motoya Maruberi Rounded";
			}
			
			public String getURL ()
			{
				return "https://www.assembla.com/code/cristianadam/subversion/nodes/76/fonts/android.fonts/MTLmr3m.ttf";
			}
			
			public String getFilename ()
			{
				return "/system/fonts/MTLmr3m.ttf";
			}			
			
			public int sinceVersion ()
			{
				return 1;
			}			
		},
		
		DROID_SANS {
			public String getName ()
			{
				return "Droid Sans Japanese";
			}
			
			public String getURL ()
			{
				return "https://www.assembla.com/code/cristianadam/subversion/nodes/76/fonts/android.fonts/DroidSansJapanese.ttf";
			}
			
			public String getFilename ()
			{
				return null;
			}			
			
			public int sinceVersion ()
			{
				return 1;
			}			
		};
				
		public abstract String getName ();
		
		public abstract String getURL ();
		
		public abstract int sinceVersion ();
		
		public boolean is (FontEntry fe)
		{
			return fe.name.equals (getName ());
		}
		
		public String getFilename ()
		{
			return null;
		}		
	}
		
	public static class FontEntry {
		
		public int id;
		
		public String name;
		
		public String filename;
		
		public String url;
		
		public Typeface face;
		
		public boolean enabled;
		
		public boolean available;
		
		public boolean wellknown;
						
		public FontEntry (int id, String name, String filename, String url, 
						  boolean enabled, boolean available, boolean wellknown)
		{
			this.id = id;
			this.name = name;
			this.filename = filename;
			this.url = url;
			this.enabled = enabled;
			this.available = available;
			this.wellknown = wellknown;
		}
		
		public boolean canBeDeleted ()
		{
			return available && 
					(!wellknown || url != null); 
		}
		
		public boolean canBeDownloaded ()
		{
			return !available && url != null;
		}
		
		public Typeface load ()
		{
			try {
				if (filename != null && face == null)
					face = Typeface.createFromFile (filename);
			} catch (Exception e) {
				/* empty */
			}
			
			return face;
		}
	}
	
	public static class FontBox {
		
		int idx;
		
		List<FontEntry> fes;
		
		private FontBox (List<FontEntry> fes)
		{
			this.fes = fes;
		}
		
		public Typeface nextFont ()
		{
			Typeface ans;
			
			if (fes == null || fes.isEmpty ())
				return null;
			
			ans = fes.get (idx %= fes.size ()).load ();
			
			idx++;
			
			return ans;
		}
		
		public boolean isTrivial ()
		{
			return fes == null || fes.isEmpty () ||
			   (fes.size () == 1 && WellKnownFont.SYSTEM.is (fes.get (0)));
		}
	}
	
	public static class FontTable {

		private static final String TABLE = "font";
		
		private static final String C_ID = "_id";
		
		private static final String C_NAME = "name";

		private static final String C_FILENAME = "filename";
		
		private static final String C_URL = "url";

		private static final String C_ENABLED = "enabled";
		
		private static final String C_AVAILABLE = "available";
		
		private static final String C_WELL_KNOWN = "wellknown";

		/** The SQL create statement */
		private static final String SQL_CREATE = 
				"CREATE TABLE " + TABLE + " (" +
						C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						C_NAME + " TEXT UNIQUE NOT NULL," +
						C_FILENAME + " TEXT NULL, " +
						C_URL + " TEXT NULL," +
						C_ENABLED + " INTEGER NOT NULL," +
						C_AVAILABLE + " INTEGER NOT NULL," +
						C_WELL_KNOWN + " INTEGER NOT NULL" +
				")";

		/** The SQL drop statement */
		private static final String SQL_DROP = 
				"DROP TABLE IF EXISTS " + TABLE;
		
		private static final String SQL_WHERE_ID =
				C_ID + " = ?";
		
		private static final String SQL_WHERE_NAME =
				C_NAME + " = ?";

		/**
		 * Creates the table
		 * @param db the database
		 */
		public static void onCreate (SQLiteDatabase db)
		{
			db.execSQL (SQL_CREATE);
		}

		/**
		 * Drops the table
		 * @param db the database
		 */		
		public static void onDrop (SQLiteDatabase db)
		{
			db.execSQL (SQL_DROP);
		}
		
		public static List<FontEntry> getFonts (SQLiteDatabase db, String where)
			throws SQLException
		{
			List<FontEntry> ans;
			String cols [];
			Cursor c;
			
			cols = new String [] { C_ID, C_NAME, C_FILENAME, C_URL, C_ENABLED, C_AVAILABLE, C_WELL_KNOWN };
			ans = new Vector<FontEntry> ();
			
			c = null;
			try {
				c = db.query (TABLE, cols, where, null, null, null, C_WELL_KNOWN);
				while (c.moveToNext ())
					ans.add (new FontEntry (c.getInt (0), c.getString (1), c.getString (2), 
											c.getString (3), c.getInt (4) > 0,
											c.getInt (5) > 0, c.getInt (6) > 0));
			} finally {
				if (c != null)
					c.close ();
			}
					
			return ans;			
		}		
		
		public static List<FontEntry> getFonts (SQLiteDatabase db)
				throws SQLException
		{
			return getFonts (db, null);
		}

		public static List<FontEntry> getEnabledFonts (SQLiteDatabase db)
				throws SQLException
		{
			return getFonts (db, C_ENABLED);
		}

		public static void insertFont (SQLiteDatabase db, String name, String filename, 
		 					           String url, boolean enabled, boolean available, 
		 					           boolean wellknown)
			throws SQLException
		{
			ContentValues cv;
			
			cv = new ContentValues ();
			cv.put (C_NAME, name);
			cv.put (C_FILENAME, filename);
			cv.put (C_URL, url);
			cv.put (C_ENABLED, enabled ? 1 : 0);
			cv.put (C_AVAILABLE, available ? 1 : 0);
			cv.put (C_WELL_KNOWN, wellknown ? 1 : 0);
			
			if (db.insert (TABLE, null, cv) < 0)
				throw new SQLException ();
		}
		
		public static void setEnabled (SQLiteDatabase db, FontEntry fe, boolean enabled)
		{
			ContentValues cv;
			
			cv = new ContentValues ();
			cv.put (C_ENABLED, enabled ? 1 : 0);			
			db.update (TABLE, cv, SQL_WHERE_ID, new String [] { Integer.toString (fe.id) } );
		}
		
		public static void setAvailable (SQLiteDatabase db, FontEntry fe, boolean available)
		{
			ContentValues cv;
			
			cv = new ContentValues ();
			cv.put (C_FILENAME, fe.filename);
			cv.put (C_AVAILABLE, available ? 1 : 0);			
			db.update (TABLE, cv, SQL_WHERE_ID, new String [] { Integer.toString (fe.id) } );
		}

		public static void delete (SQLiteDatabase db, FontEntry fe)
		{
			db.delete (TABLE, SQL_WHERE_ID, new String [] { Integer.toString (fe.id) } );
		}
		
		public static boolean exists (SQLiteDatabase db, String name)
		{
			String cols [];
			String where [];
			boolean ans;
			Cursor c;
			
			cols = new String [] { C_ID };			
			where = new String [] { name };
			
			c = null;
			try {
				c = db.query (TABLE, cols, SQL_WHERE_NAME, where, null, null, C_WELL_KNOWN);
				ans = c.moveToNext ();
			} finally {
				if (c != null)
					c.close ();
			}
					
			return ans;			
		}		
	}
	
	/**
	 * The DB open helper.  
	 */
	static class OpenHelper extends SQLiteOpenHelper {
		
		/** DB Version */
		private static final int VERSION = 1;
		
		/** The db file */
		private static final String NAME = "fonts.db";

		/**
		 * Constructor
		 * @param ctxt the context
		 */
		OpenHelper (Context ctxt)
		{
			super (ctxt, NAME, null, VERSION);
		}
		
		@Override
		public void onCreate (SQLiteDatabase db)
		{
			List<FontEntry> fes;
			FontEntry self;
			
			FontTable.onCreate (db);
			
			upgradeFrom (db, 0);
			
			self = null;
			fes = FontTable.getFonts (db);
			for (FontEntry fe : fes) {
				if (WellKnownFont.MOTOYA.is (fe) && fe.filename != null)
					self = fe;
				else if (WellKnownFont.SYSTEM.is (fe) && self == null)
					self = fe;
			}
			
			if (self != null)
				FontTable.setEnabled (db, self, true);
		}
		
		@Override
		public void onUpgrade (SQLiteDatabase db, int oldv, int newv)
		{
			upgradeFrom (db, oldv);
		}
		
		private void upgradeFrom (SQLiteDatabase db, int oldv)
		{
			File file;
				
			for (WellKnownFont f : EnumSet.allOf (WellKnownFont.class)) {
				if (f.sinceVersion () > oldv) {
					if (f.getFilename () != null) {
						file = new File (f.getFilename ());
						if (file.exists () && file.canRead ())
							FontTable.insertFont (db, f.getName (), f.getFilename (), 
												  null, false, true, true);
						else
							FontTable.insertFont (db, f.getName (), null, 
												  f.getURL (), false, false, true);
					} else
						FontTable.insertFont (db, f.getName (), null, f.getURL (), 
											  false, f == WellKnownFont.SYSTEM, true);
				}
			}
		}
		
	}

	/** The DB helper */
	private OpenHelper helper;
	
	/** The database */
	private SQLiteDatabase db;
		
	public static final Object MUTEX = new Object ();
	
	/**
	 * Cosntructor
	 * @param ctxt the context
	 */
	public FontDatabase (Context ctxt)
	{
		helper = new OpenHelper (ctxt);		
	}	
	
	/**
	 * Opens the database in r/w mode. This method may be called multiple times
	 * on the same instance, provided that {@link #close()} is called
	 * beforehand.
	 */	
	private synchronized void openW ()
		throws SQLException
	{
		if (db == null)
			db = helper.getWritableDatabase ();	
	}
	
	/**
	 * Opens the database in r/o mode. This method may be called multiple times
	 * on the same instance, provided that {@link #close()} is called
	 * beforehand.
	 */	
	private synchronized void openR ()
		throws SQLException
	{
		if (db == null)
			db = helper.getReadableDatabase ();	
	}
	
	/**
	 * Closes the DB.
	 */
	private void close ()
		throws SQLException
	{
		helper.close ();
	}
	
	public static List<FontEntry> getFonts (Context ctxt)
	{
		List<FontEntry> ans;
		FontDatabase fdb;

		synchronized (MUTEX) {
			fdb = new FontDatabase (ctxt);
			fdb.openR ();
			try {
				ans = FontTable.getFonts (fdb.db); 
			} finally {
				if (fdb != null)
					fdb.close ();
			}
		}
		
		return ans;
	}
	
	public static List<FontEntry> getEnabledFonts (Context ctxt)
	{
		List<FontEntry> ans;
		FontDatabase fdb;

		synchronized (MUTEX) {
			fdb = new FontDatabase (ctxt);
			fdb.openR ();
			try {
				ans = FontTable.getEnabledFonts (fdb.db); 
			} finally {
				if (fdb != null)
					fdb.close ();
			}
		}
		
		return ans;
	}
	
	public static FontBox getFontBox (Context ctxt)
	{
		return new FontBox (getEnabledFonts (ctxt));
	}

	public static void delete (Context ctxt, FontEntry fe)
	{
		FontDatabase fdb;
		
		synchronized (MUTEX) { 
			fdb = new FontDatabase (ctxt);
			fdb.openW ();
			try {
				if (fe.wellknown)
					FontTable.setAvailable (fdb.db, fe, false);
				else
					FontTable.delete (fdb.db, fe);
			} finally {
				fdb.close ();
			}
		}
	}

	public static void setEnabled (Context ctxt, FontEntry fe, boolean enabled)
	{
		FontDatabase fdb;

		synchronized (MUTEX) {
			fdb = new FontDatabase (ctxt);
			fdb.openW ();
			try {
				FontTable.setEnabled (fdb.db, fe, enabled);
			} finally {
				fdb.close ();
			}
		}
	}

	public static void setAvailable (Context ctxt, FontEntry fe, boolean available)
	{
		FontDatabase fdb;

		synchronized (MUTEX) {
			fdb = new FontDatabase (ctxt);
			fdb.openW ();
			try {
				if (fe.id == -1)
					FontTable.insertFont (fdb.db, fe.name, fe.filename, fe.url, false, available, false);
				
				FontTable.setAvailable (fdb.db, fe, available);
			} finally {
				fdb.close ();
			}
		}
	}
	
	public static boolean insertFixDuplicates (Context ctxt, FontEntry fe, boolean available)
	{
		String bname;
		int i;
		
		synchronized (MUTEX) {
			if (!exists (ctxt, fe.name)) {
				setAvailable (ctxt, fe, available);
				return true;
			}
			bname = fe.name;
			for (i = 1; i < 20; i++) {
				fe.name = bname + " " + i;
				if (!exists (ctxt, fe.name)) {
					setAvailable (ctxt, fe, available);
					return true;
				} 
			}
		}
		
		return false;
	}
	
	public static boolean exists (Context ctxt, String name)
	{
		FontDatabase fdb;
		boolean ans;

		synchronized (MUTEX) {
			fdb = new FontDatabase (ctxt);
			fdb.openW ();
			try {
				ans = FontTable.exists (fdb.db, name);
			} finally {
				fdb.close ();
			}
		}
		
		return ans;
	}
}
