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
import android.widget.TextView;

public class PracticeLevelSelectionActivity extends Activity implements OnClickListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_practice_level_selection);
		
		if (getIntent().getExtras() != null)
		{
			mPlayType = getIntent().getExtras().getInt(Constants.KEY_PLAY_TYPE);
		}
		
		mApp = (WordJamApp) getApplication();
		mWordDbAdapter = mApp.getWordDbAdapter();
		
		setupViews();
		
		honorTheme();
	}
	
	private void setupViews()
	{	
		mEasyButton = (Button) findViewById(R.id.easy_button);
		mEasyButton.setEnabled(true);
		mEasyButton.setOnClickListener(this);
		
		mMediumButton = (Button) findViewById(R.id.medium_button);
		mMediumButton.setEnabled(true);
		mMediumButton.setOnClickListener(this);
		
		mHardButton = (Button) findViewById(R.id.hard_button);
		mHardButton.setEnabled(true);
		mHardButton.setOnClickListener(this);
		
		mVeryHardButton = (Button) findViewById(R.id.very_hard_button);
		mVeryHardButton.setEnabled(true);
		mVeryHardButton.setOnClickListener(this);;
		
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mBackButton.setEnabled(true);
		mBackButton.setOnClickListener(this);
	}
	

	private void honorTheme()
	{
		View view = (View) findViewById(R.id.main_layout);
		view.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else if (v.getId() == R.id.easy_button)
    	{
    		returnAndExit(Constants.PRACTICE_TYPE_EASY);
    	}
    	else if (v.getId() == R.id.medium_button)
    	{
    		returnAndExit(Constants.PRACTICE_TYPE_MEDIUM);
    	}
    	else if (v.getId() == R.id.hard_button)
    	{
    		returnAndExit(Constants.PRACTICE_TYPE_HARD);
    	}
    	else if (v.getId() == R.id.very_hard_button)
    	{
    		returnAndExit(Constants.PRACTICE_TYPE_VERY_HARD);
    	}
	}
	
	private void startPractice(int practiceType)
	{
    	Intent i = new Intent(this, AdvancedGameSelectionActivity.class);
    	
        i.putExtra(Constants.KEY_IS_PLAY, false);
        i.putExtra(Constants.KEY_PRACTICE_TYPE, practiceType);
        i.putExtra(Constants.KEY_PLAY_TYPE, Constants.PLAY_TYPE_ENGLISH_CLASSIC);
        
    	startActivity(i);
	}
	
    
	// Set the current theme to what user has chosen
	public void returnAndExit(int wordDifficulty)
	{	      
	      // return the value to the calling activity
	      setResult(wordDifficulty);
	      this.finish();
	}

	private Button mEasyButton, mMediumButton, mHardButton, mVeryHardButton;
	private ImageButton mBackButton;
	
	private WordJamApp mApp;
	private WordDbAdapter mWordDbAdapter;
	private int mPlayType;
	
	private static final int MAX_LEVELS = 6;
}

