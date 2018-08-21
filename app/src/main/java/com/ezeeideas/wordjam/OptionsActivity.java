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

public class OptionsActivity extends Activity implements OnClickListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_options);
		
		setupViews();

		honorTheme();
	}
	
	private void setupViews()
	{
		mSettingsButton = (Button) findViewById(R.id.settings_button);
		mSettingsButton.setEnabled(true);
		mSettingsButton.setOnClickListener(this);
		updateWordDifficultyInButton(GameUtils.getWordDifficultyPreference(this));
		
		mThemesButton = (Button) findViewById(R.id.themes_button);
		mThemesButton.setEnabled(true);
		mThemesButton.setOnClickListener(this);
		updateThemeInButton(Themes.getTheme());
		
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mBackButton.setEnabled(true);
		mBackButton.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else if (v.getId() == R.id.settings_button)
    	{
    		Intent i = new Intent(this, PracticeLevelSelectionActivity.class);
    		startActivityForResult(i, Constants.KEY_WORD_DIFFICULTY);
    	}
    	else if (v.getId() == R.id.themes_button)
    	{
    		Intent i = new Intent(this, ThemesActivity.class);
    		startActivityForResult(i, Constants.KEY_THEME);
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
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Utils.Log("onActivityResult called, requestCode = " + requestCode + ", resultCode = " + resultCode);
    	if (requestCode == Constants.KEY_WORD_DIFFICULTY)
    	{	
    		if (resultCode != 0)
    		{
	    		//set the preference
	    		GameUtils.setWordDifficultyPreference(resultCode, this);
	    		
	    		updateWordDifficultyInButton(resultCode);
	    		
	    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_SELECTION_MADE, Analytics.ANALYTICS_LABEL_WORD_DIFFICULTY_SELECTION, resultCode);
    		}
    	}
    	else if (requestCode == Constants.KEY_THEME)
    	{
    		if (resultCode != 0)
    		{
    			updateThemeInButton(resultCode);
    			
    			Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_SELECTION_MADE, Analytics.ANALYTICS_LABEL_THEMES_SELECTION, resultCode);
    		}
    		
    		honorTheme();
    	}
    }
    
	private void honorTheme()
	{
		View view = (View) findViewById(R.id.main_layout);
		view.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
	}
    
    private void updateWordDifficultyInButton(int wordDifficulty)
    {
		if (wordDifficulty == 0)
		{
			//set the default difficulty
			wordDifficulty = GameUtils.getWordDifficultyPreference(this);
		}
		
		String settingsButtonText = getResources().getString(R.string.settings_button_label);
		settingsButtonText += " - " + GameUtils.getPracticeTypeName(wordDifficulty, this);
		mSettingsButton.setText(settingsButtonText);
		
		int drawableID = 0;
		switch (wordDifficulty)
		{
		case Constants.PRACTICE_TYPE_EASY:
			drawableID = R.drawable.smiley1;
			break;
		case Constants.PRACTICE_TYPE_MEDIUM:
			drawableID = R.drawable.smiley2;
			break;
		case Constants.PRACTICE_TYPE_HARD:
			drawableID = R.drawable.smiley3;
			break;
		case Constants.PRACTICE_TYPE_VERY_HARD:
			drawableID = R.drawable.smiley4;
			break;
		default:
			drawableID = R.drawable.smiley1;
			break;
		}
		
		mSettingsButton.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0, 0, 0);
    }
    
    private void updateThemeInButton(int theme)
    {
    	if (theme == 0)
    	{
    		theme = Themes.getTheme();
    	}
    	
		String themesButtonText = getResources().getString(R.string.themes_button_label);
		themesButtonText += " - " + Themes.getThemeNameFromID(theme);
		mThemesButton.setText(themesButtonText);
    }

	private Button mSettingsButton, mThemesButton;
	private ImageButton mBackButton;
	
	private WordJamApp mApp;
	private WordDbAdapter mWordDbAdapter;
	
	private static final int MAX_LEVELS = 6;
}

