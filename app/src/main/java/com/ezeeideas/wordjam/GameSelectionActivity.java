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

import android.app.Activity;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameSelectionActivity extends Activity implements OnClickListener
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		setContentView(R.layout.activity_game_selection);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mIsPlay = extras.getBoolean(Constants.KEY_IS_PLAY);
			mPlayType = extras.getInt(Constants.KEY_PLAY_TYPE);
			
			if (mIsPlay)
			{
				mLevel = extras.getInt(Constants.KEY_LEVEL);
			}
			else
			{
				mPracticeType = extras.getInt(Constants.KEY_PRACTICE_TYPE);
			}
			
			Utils.Log("in game selection, mIsPlay = " + mIsPlay + ", mLevel = " + mLevel + ", mPracticeType = " + mPracticeType);

		}
		
		setupViews();		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	private void setupViews()
	{
		//TODO - don't show challenge name for Word Jam as we only have one
		TextView titleTV = (TextView) findViewById(R.id.game_selection_title_text_view);
		//titleTV.setText(GameUtils.getPlayTypeName(mPlayType));
		titleTV.setText(getResources().getString(R.string.game_selection_title));
		
		TextView subtitleTV = (TextView) findViewById(R.id.game_selection_subtitle_text_view);
		String subtitleText = null;
		if (mIsPlay)
		{
			subtitleText = String.format(getResources().getString(R.string.level_number_label), mLevel);
		}
		else
		{
			subtitleText = String.format(getResources().getString(R.string.practice_type_label), GameUtils.getPracticeTypeName(mPracticeType, this));
		}
		subtitleTV.setText(subtitleText);
				
		mRecallWordsButton = (Button) findViewById(R.id.recall_words_button);
		mRecallWordsButton.setOnClickListener(this);
		
		mMatchWordsButton = (Button) findViewById(R.id.match_words_button);
		mMatchWordsButton.setOnClickListener(this);
		
		mHangmanButton = (Button) findViewById(R.id.hangman_button);
		mHangmanButton.setOnClickListener(this);
		
		mCrosswordButton = (Button) findViewById(R.id.crossword_button);
		mCrosswordButton.setOnClickListener(this);
		
		mWordJumbleButton = (Button) findViewById(R.id.word_jumble_button);
		mWordJumbleButton.setOnClickListener(this);
		
		mWordSearchButton = (Button) findViewById(R.id.word_search_button);
		mWordSearchButton.setOnClickListener(this);
		
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this);
        
        mCurrentSelectedGame = Constants.GAME_TYPE_HANGMAN;
        
        honorTheme();	
	}
	
	private void honorTheme()
    {
    	RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
    	mainLayout.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    }
	
    public void onClick(View v) {
        
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else if (v.getId() == R.id.recall_words_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_RECALL_WORDS_BUTTON, null);
    		
    		startGame(Constants.GAME_TYPE_RECALL_WORDS);
    	}
    	else if (v.getId() == R.id.match_words_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_MATCH_WORDS_BUTTON, null);
    		
    		startGame(Constants.GAME_TYPE_MATCH_WORDS);
    	}
    	else if (v.getId() == R.id.hangman_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_HANGMAN_BUTTON, null);
    		
    		startGame(Constants.GAME_TYPE_HANGMAN);
    	}
    	else if (v.getId() == R.id.crossword_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_CROSSWORD_BUTTON, null);
    		
    		startGame(Constants.GAME_TYPE_CROSSWORD);
    	}
    	else if (v.getId() == R.id.word_jumble_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_WORD_JUMBLE_BUTTON, null);
    		
    		startGame(Constants.GAME_TYPE_WORD_JUMBLE);
    	}
    	else if (v.getId() == R.id.word_search_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_WORD_SEARCH_BUTTON, null);
    		
    		startGame(Constants.GAME_TYPE_WORD_SEARCH);
    	}
    }
    
    private void startGame(int gameType)
    {
    	Class c = GameUtils.getGameClassForGameType(gameType);
    	Intent i = new Intent(this, c); 	
    	
    	//Fill in the required values
    	i.putExtra(Constants.KEY_IS_PLAY, mIsPlay);
    	i.putExtra(Constants.KEY_GAME_NUM, -1); // indicates a new game
    	i.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
    	
    	if (mIsPlay)
    	{
    		i.putExtra(Constants.KEY_LEVEL, mLevel);
    	}
    	else
    	{
    		i.putExtra(Constants.KEY_PRACTICE_TYPE, mPracticeType);
    	}
    	
    	startActivityForResult(i, Constants.KEY_SOURCE_ACTIVITY);
    }
	
	Button mRecallWordsButton, mMatchWordsButton, mHangmanButton, mCrosswordButton, mWordJumbleButton, mWordSearchButton;
	ImageButton mBackButton;
	
	private boolean mIsPlay;
	int mLevel;
	int mPlayType;
	int mPracticeType;

	int mCurrentSelectedGame; //currently selected game in the spinner

	private static final String LOG_TAG = "gauravfb: ";
}