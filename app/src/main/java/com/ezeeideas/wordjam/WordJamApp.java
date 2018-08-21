/******************************************************************************
*
* The MIT License (MIT)
*
* Copyright (c) 2013 - Present, Gaurav Jain
*
* (@gauravdelphinus, github.com/gauravdelphinus)
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*
*******************************************************************************/
package com.ezeeideas.wordjam;

import com.ezeeideas.wordjam.db.GamesDbAdapter;
import com.google.ads.Ad;
import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;

import android.app.Application;
import android.content.Context;

public class WordJamApp extends Application
{
  private WordJam.SetupDBTask mSetupDBTask;
  private WordJam mMainActivity;
  private boolean mDBCorrupt;


  @Override
  public void onCreate()
  {
	  super.onCreate();
	  mContext = this;
  }
  
  public WordJamApp()
  {
	  super();
  }
  public WordJam.SetupDBTask getSetupDBTask()
  {
	  return mSetupDBTask;
  }
  public void setSetupDBTask(WordJam.SetupDBTask setupDBTask)
  {
	  mSetupDBTask = setupDBTask;
  }
  
  public void setMainActivity(WordJam activity)
  {
	  mMainActivity = activity;
  }
  
  public WordJam getMainActivity()
  {
	  return mMainActivity;
  }
  
  public void setDBCorrupt(boolean iscorrupt)
  {
	  mDBCorrupt = iscorrupt;
  }
  
  public boolean getDBCorrupt()
  {
	  return mDBCorrupt;
  }
  
  /**
   * Get the Application Context
   * @return Context for the app
   */
  public static Context getContext()
  {
	  return mContext;
  }
  
  /**
   * Get or create the Word Database Adapter.
   */
  public WordDbAdapter getWordDbAdapter()
  {
	  if (mWordDbAdapter != null)
	  {
		  return mWordDbAdapter;
	  }
	  
	  //create a new adapter for the lifetime of the app
	  mWordDbAdapter = new WordDbAdapter(this);
	  
	  return mWordDbAdapter;
  }
  
  /**
   * Get or create the Games Database Adapter.
   */
  public GamesDbAdapter getGamesDbAdapter()
  {
	  if (mGamesDbAdapter != null)
	  {
		  return mGamesDbAdapter;
	  }
	  
	  //create a new adapter for the lifetime of this app
	  mGamesDbAdapter = new GamesDbAdapter(this);
	  
	  return mGamesDbAdapter;
  }
  
	public static String getShortNamesForType(int type)
	{
		switch (type)
		{
		case 1: return mContext.getResources().getString(R.string.word_type_verbs);
		case 2: return mContext.getResources().getString(R.string.word_type_adjectives);
		case 3: return mContext.getResources().getString(R.string.word_type_people);
		case 4: return mContext.getResources().getString(R.string.word_type_processes);
		case 5: return mContext.getResources().getString(R.string.word_type_attributes);
		case 6: return mContext.getResources().getString(R.string.word_type_objects);
		default:
			break;
		}
		
		return null;
	}
	
  private static Context mContext; //The Application Context
  private WordDbAdapter mWordDbAdapter; //Word DB Adapter
  private GamesDbAdapter mGamesDbAdapter; // Games DB Adapter
  
}