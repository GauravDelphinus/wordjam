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

import java.util.Vector;

import com.ezeeideas.wordjam.R;
import com.ezeeideas.wordjam.db.GamesDbAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;

public class SettingsActivity extends Activity implements OnClickListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		setupViews();
		
		honorTheme();
	}
	
	private void setupViews()
	{
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
		int newGamePref = settings.getInt(Constants.PREFS_NEW_GAME_CHOICE, Constants.PREFS_NEW_GAME_SAME_TYPE);
		int restoreAppPref = settings.getInt(Constants.PREFS_RESTORE_APP_CHOICE, Constants.PREFS_RESTORE_APP_HOME_SCREEN);
		
		//moving to the next game setting
		mSameGameTypeRadioButton = (RadioButton) findViewById(R.id.same_game_type_radio_button);
		mSameGameTypeRadioButton.setOnClickListener(this);
		mSameGameTypeRadioButton.setChecked(newGamePref == Constants.PREFS_NEW_GAME_SAME_TYPE);

		mRandomGameTypeRadioButton = (RadioButton) findViewById(R.id.random_game_type_radio_button);
		mRandomGameTypeRadioButton.setOnClickListener(this);
		mRandomGameTypeRadioButton.setChecked(newGamePref == Constants.PREFS_NEW_GAME_RANDOM_TYPE);
		
		mAlwaysAskRadioButton = (RadioButton) findViewById(R.id.ask_me_radio_button);
		mAlwaysAskRadioButton.setOnClickListener(this);
		mAlwaysAskRadioButton.setChecked(newGamePref == Constants.PREFS_NEW_GAME_ASK_ME);
		
		//restore of the app setting
		mHomeScreenRadioButton = (RadioButton) findViewById(R.id.home_screen_button);
		mHomeScreenRadioButton.setOnClickListener(this);
		mHomeScreenRadioButton.setChecked(restoreAppPref == Constants.PREFS_RESTORE_APP_HOME_SCREEN);
		
		mRecentGameRadioButton = (RadioButton) findViewById(R.id.restore_game_button);
		mRecentGameRadioButton.setOnClickListener(this);
		mRecentGameRadioButton.setChecked(restoreAppPref == Constants.PREFS_RESTORE_APP_RECENT_GAME);
		
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mBackButton.setEnabled(true);
		mBackButton.setOnClickListener(this);
	}
	

	private void honorTheme()
	{
		//TBD
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else if (v.getId() == R.id.same_game_type_radio_button)
    	{
    		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);  
  	      	SharedPreferences.Editor editor = settings.edit();
  	      	
  	      	editor.putInt(Constants.PREFS_NEW_GAME_CHOICE, Constants.PREFS_NEW_GAME_SAME_TYPE);

  	      	// Commit the edits!
  	      	editor.commit();	
    	}
    	else if (v.getId() == R.id.random_game_type_radio_button)
    	{
    		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);  
  	      	SharedPreferences.Editor editor = settings.edit();
  	      	
  	      	editor.putInt(Constants.PREFS_NEW_GAME_CHOICE, Constants.PREFS_NEW_GAME_RANDOM_TYPE);

  	      	// Commit the edits!
  	      	editor.commit();
    	}
    	else if (v.getId() == R.id.ask_me_radio_button)
    	{
    		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);  
  	      	SharedPreferences.Editor editor = settings.edit();
  	      	
  	      	editor.putInt(Constants.PREFS_NEW_GAME_CHOICE, Constants.PREFS_NEW_GAME_ASK_ME);

  	      	// Commit the edits!
  	      	editor.commit();
    	}
    	else if (v.getId() == R.id.home_screen_button)
    	{
    		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);  
  	      	SharedPreferences.Editor editor = settings.edit();
  	      	
  	      	editor.putInt(Constants.PREFS_RESTORE_APP_CHOICE, Constants.PREFS_RESTORE_APP_HOME_SCREEN);

  	      	// Commit the edits!
  	      	editor.commit();
    	}
    	else if (v.getId() == R.id.restore_game_button)
    	{
    		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);  
  	      	SharedPreferences.Editor editor = settings.edit();
  	      	
  	      	editor.putInt(Constants.PREFS_RESTORE_APP_CHOICE, Constants.PREFS_RESTORE_APP_RECENT_GAME);

  	      	// Commit the edits!
  	      	editor.commit();
    	}
	}

	private RadioButton mSameGameTypeRadioButton, mRandomGameTypeRadioButton, mAlwaysAskRadioButton;
	private RadioButton mHomeScreenRadioButton, mRecentGameRadioButton;
	private ImageButton mBackButton;
}

