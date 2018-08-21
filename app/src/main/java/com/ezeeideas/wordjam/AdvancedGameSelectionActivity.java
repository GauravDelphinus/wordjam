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

import android.app.Activity;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AdvancedGameSelectionActivity extends Activity implements OnClickListener, PieButton.PieButtonListener, SpinnerButton.SpinnerButtonListener
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		setContentView(R.layout.activity_advanced_game_selection);
		
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
	
	private void setupViews()
	{
		int numPoints = 0; //no. of points a game will fetch
		String pointsLabel = "";

		TextView titleTV = (TextView) findViewById(R.id.game_selection_title_text_view);
		titleTV.setText(GameUtils.getPlayTypeName(mPlayType, this));
		
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
		
		TextView actionTV = (TextView) findViewById(R.id.game_selection_action_text_view);
		if (mIsPlay)
		{
			actionTV.setText(getResources().getString(R.string.game_selection_action_text_play));
		}
		else
		{
			actionTV.setText(getResources().getString(R.string.game_selection_action_text_practice));
		}
				
		mGameOptionsSpinnerButton = (SpinnerButton) findViewById(R.id.game_options_spinner_button);
        Vector<Integer> buttonPressedResources = new Vector<Integer>();
        buttonPressedResources.add(R.drawable.game_options_button_recall);
        buttonPressedResources.add(R.drawable.game_options_button_search);
        buttonPressedResources.add(R.drawable.game_options_button_jumble);
        buttonPressedResources.add(R.drawable.game_options_button_match);
        buttonPressedResources.add(R.drawable.game_options_button_hangman);
        buttonPressedResources.add(R.drawable.game_options_button_crossword);
        mGameOptionsSpinnerButton.setAttributes(0, R.drawable.game_options_button_normal, buttonPressedResources, R.drawable.game_options_button_pressed);
        
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this);
        
        mPlayGameButton = (ImageButton) findViewById(R.id.game_play_button);
        mPlayGameButton.setOnClickListener(this);
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
    	else if (v.getId() == R.id.game_play_button)
    	{
    		startGame(mCurrentSelectedGame);
    	}
    }
    
    private int getGameTypeFromLeverIndex(int index)
    {
    	int gameType = -1;
    	
		switch (index)
		{
		case 0: //Recall words
			gameType = Constants.GAME_TYPE_RECALL_WORDS;
			break;
		case 1: //Word Search
			gameType = Constants.GAME_TYPE_WORD_SEARCH;
			break;
		case 2: //Word Jumble
			gameType = Constants.GAME_TYPE_WORD_JUMBLE;
			break;
		case 3: //Match Words
			gameType = Constants.GAME_TYPE_MATCH_WORDS;
			break;
		case 4: //Hangman
			gameType = Constants.GAME_TYPE_HANGMAN;
			break;
		case 5: //Crossword
			gameType = Constants.GAME_TYPE_CROSSWORD;
			break;
		}
		
		return gameType;
    }
    
    private void startGame(int gameType)
    {    	
    	int testNumber = 1;
    	int numTestsPassed = 0;
    	
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Utils.Log("onActivityResult called, requestCode = " + requestCode + ", resultCode = " + resultCode);
    	if (requestCode == Constants.KEY_SOURCE_ACTIVITY)
    	{
    		if (resultCode == Constants.ACTION_GO_TO_LEVEL_HOME)
    		{
    			Utils.Log("Finishing this activity");
    			//we want to move the user to the Level Home, so dismiss this activity
    			this.finish();
    		}
    	}
    }
    
    
	
	public void onPieButtonSelected(PieButton button, int index) 
	{
		if (button == mGameOptionsSpinnerButton)
		{
			switch (index)
			{
			case 0: //Recall words
				startGame(Constants.GAME_TYPE_RECALL_WORDS);
				break;
			case 1: //Word Search
				startGame(Constants.GAME_TYPE_WORD_SEARCH);
				break;
			case 2: //Word Jumble
				startGame(Constants.GAME_TYPE_WORD_JUMBLE);
				break;
			case 3: //Match Words
				startGame(Constants.GAME_TYPE_MATCH_WORDS);
				break;
			case 4: //Hangman
				startGame(Constants.GAME_TYPE_HANGMAN);
				break;
			case 5: //Crossword
				startGame(Constants.GAME_TYPE_CROSSWORD);
				break;
			}
		}
		
	}
	
	public void onSpinnerStartedRotating()
	{
		mPlayGameButton.setEnabled(false);
		mCurrentSelectedGame = -1;
	}
	
	public void onSpinnerStoppedRotating(int leverIndex) 
	{
		int gameType = getGameTypeFromLeverIndex(leverIndex);
		switch (gameType)
		{
		case Constants.GAME_TYPE_CROSSWORD:
			mPlayGameButton.setImageResource(R.drawable.play_crossword_button);
			break;
		case Constants.GAME_TYPE_HANGMAN:
			mPlayGameButton.setImageResource(R.drawable.play_hangman_button);
			break;
		case Constants.GAME_TYPE_WORD_SEARCH:
			mPlayGameButton.setImageResource(R.drawable.play_word_search_button);
			break;
		case Constants.GAME_TYPE_WORD_JUMBLE:
			mPlayGameButton.setImageResource(R.drawable.play_word_jumble_button);
			break;
		case Constants.GAME_TYPE_RECALL_WORDS:
			mPlayGameButton.setImageResource(R.drawable.play_recall_words_button);
			break;
		case Constants.GAME_TYPE_MATCH_WORDS:
			mPlayGameButton.setImageResource(R.drawable.play_match_words_button);
			break;
		}
		
		mCurrentSelectedGame = gameType;
		mPlayGameButton.setEnabled(true);
	}
	
	//Button mRecallWordsButton, mMatchWordsButton, mHangmanButton, mCrosswordButton, mWordJumbleButton, mWordSearchButton;
	ImageButton mBackButton;
	SpinnerButton mGameOptionsSpinnerButton;
	ImageButton mPlayGameButton;
	
	private boolean mIsPlay;
	int mLevel;
	int mPlayType;
	int mPracticeType;

	int mCurrentSelectedGame; //currently selected game in the spinner

	
}