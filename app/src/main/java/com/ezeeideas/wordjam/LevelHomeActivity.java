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

import java.util.ArrayList;
import java.util.Vector;

import com.ezeeideas.wordjam.GameDialog.GameDialogListener;
import com.ezeeideas.wordjam.db.GamesDbAdapter;
import com.ezeeideas.wordjam.db.GamesDbAdapter.GameShortInfo;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LevelHomeActivity extends Activity implements OnClickListener, GameDialogListener, PieButton.PieButtonListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		setContentView(R.layout.activity_level_home);
		
		//open databases
		mApp = (WordJamApp) getApplication();
		mGamesDbAdapter = mApp.getGamesDbAdapter();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mPlayType = extras.getInt(Constants.KEY_PLAY_TYPE);
			mLevel = extras.getInt(Constants.KEY_LEVEL);
		}
    	
		setupViews();

        honorTheme();
        
        if (mGamesDbAdapter.getNumGames(mPlayType, mLevel) <= 0)
        {
        	//first time entering level. Show Welcome Dialog
    		int levelTotalGames = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_GAMES];
    		int levelTotalHints = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_HINTS];
    		int levelTotalPoints = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_POINTS];
        	LevelWelcomeDialog dialog = new LevelWelcomeDialog(this, mLevel, levelTotalGames, levelTotalHints, levelTotalPoints);
        	dialog.show();
        }
	}
	
	private void setupViews()
	{
		mLevelChoicesView = (PieButton) findViewById(R.id.level_choices_view);
        Vector<Integer> buttonPressedResources = new Vector<Integer>();
        buttonPressedResources.add(R.drawable.level_options_button_points_summary);
        buttonPressedResources.add(R.drawable.level_options_button_view_saved_games);
        buttonPressedResources.add(R.drawable.level_options_button_resume_last_game);
        buttonPressedResources.add(R.drawable.level_options_button_start_new_game);
		mLevelChoicesView.setAttributes(0, R.drawable.level_options_button_normal, buttonPressedResources);
        
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this);
        
        TextView titleTV = (TextView) findViewById(R.id.activity_level_home_title_text_view);
        titleTV.setText(GameUtils.getPlayTypeName(mPlayType, this));
        
		TextView subtitleTV = (TextView) findViewById(R.id.activity_level_home_subtitle_text_view);
		String subtitleStr = String.format(getResources().getString(R.string.level_name), mLevel);
		subtitleTV.setText(subtitleStr);
	}
	
	private void honorTheme()
    {

    }
	
    public void onClick(View v) 
    {
        
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }
    
	public void onPieButtonSelected(PieButton button, int index) 
	{
		if (button == mLevelChoicesView)
		{
			switch (index)
			{
			case 0:
				//Lookup
		        {
		        	showPointsSummary();
		        }
		        break;
			case 1:
				//practice
		        {
		        	//show saved games
		        }
		        break;
			case 2:
				//play
			    {
			    	resumePreviousGame();
		        }
		        break;
			case 3:
				//play
			    {
			    	startNewGame();
		        }
		        break;
			}
		}
	} 
    
    
    private void showPointsSummary()
    {
		GameUtils.LevelInfo levelInfo = new GameUtils.LevelInfo();
		
		levelInfo.levelNum = mLevel;
		levelInfo.levelGamesRemaining = mGamesDbAdapter.getNumGamesRemaining(mPlayType, mLevel);
		levelInfo.levelHintsRemaining = mGamesDbAdapter.getNumHintsRemaining(mPlayType, mLevel);
		levelInfo.levelHintsEarned = mGamesDbAdapter.getNumHintsEarned(mPlayType, mLevel);
		levelInfo.levelPointsRemaining = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_POINTS] - mGamesDbAdapter.getEarnedPoints(mPlayType, mLevel);
		levelInfo.levelTimeSpent = mGamesDbAdapter.getTotalTimeSpent(mPlayType, mLevel);
		levelInfo.levelTotalGames = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_GAMES];
		levelInfo.levelTotalHints = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_HINTS];
		levelInfo.levelTotalPoints = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_POINTS];
		
		LevelPointSummaryDialog dialog = new LevelPointSummaryDialog(this, levelInfo);
		dialog.show();
    }
    private void startNewGame()
    {
    	Intent i = new Intent(this, AdvancedGameSelectionActivity.class);
    	
    	i.putExtra(Constants.KEY_GAME_NUM, -1);
    	i.putExtra(Constants.KEY_IS_PLAY, true);
    	i.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
    	i.putExtra(Constants.KEY_LEVEL, mLevel);
    	
    	startActivity(i);
    }

    private void resumePreviousGame()
    {
    	GameShortInfo gameShortInfo = mGamesDbAdapter.fetchLastResumableGame(mPlayType, mLevel);
    	
    	if (gameShortInfo != null)
    	{
    		//there's something to resume
	    	Intent i = new Intent(this, GameUtils.getGameClassForGameType(gameShortInfo.gameType));
	
	    	i.putExtra(Constants.KEY_GAME_NUM, gameShortInfo.gameNum);
	    	i.putExtra(Constants.KEY_IS_PLAY, true);
	    	i.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
	    	i.putExtra(Constants.KEY_LEVEL, mLevel);
	    	
	    	startActivity(i);
    	}
    	else
    	{
    		LevelResumeGameDialog dialog = new LevelResumeGameDialog(this);
    		dialog.show();
    	}
    }
    
	public void onDialogOptionSelected(Dialog dialog, int option) 
	{
		if (dialog.getClass() == LevelWelcomeDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
	        	startNewGame();
			}
		}
		else if (dialog.getClass() == LevelResumeGameDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				startNewGame();
			}
		}
		else if (dialog.getClass() == LevelPointSummaryDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				//status quo - do nothing
			}
		}
	}
    
    private void showAllGames()
    {
    	Intent i = new Intent(this, AllGamesList.class);
    	i.putExtra(Constants.KEY_LEVEL, mLevel);
    	
    	startActivity(i);
    }
    
    private void startTest(int testID)
    {
    	/*
    	int testNumber = 1;
    	int numTestsPassed = 0;
    	
    	
    	switch (testID) {
	        
	        case R.id.new_game_button:
	        {	
	        	
	        	
	        	GameSelection.startGame(this, mTestType, mLevel, -1);
	        }	
	        break;
	        
	        case R.id.recent_game_1_button:
	        case R.id.recent_game_2_button:
	        case R.id.recent_game_3_button:
	        case R.id.recent_game_4_button:
	        {
	        	
	        	
	        	int gameNum = -1;
	        	switch (testID)
	        	{
	        	case R.id.recent_game_1_button:
	        		gameNum = mRecentGames.get(0);
	        		break;
	        	case R.id.recent_game_2_button:
	        		gameNum = mRecentGames.get(1);
	        		break;
	        	case R.id.recent_game_3_button:
	        		gameNum = mRecentGames.get(2);
	        		break;
	        	case R.id.recent_game_4_button:
	        		gameNum = mRecentGames.get(3);
	        		break;
	        	default:
	        		gameNum = -1;
	        		break;
	        	}
	        	
	        	GameSelection.startGame(this, mTestType, mLevel, gameNum);
	        }
	        break;
        }
*/
    }
    
    
    public static Class getGameClass(int testType)
    {
    	/*
    	Class c = null;
    	if (testType == GameSelectionActivity.TEST_TYPE_HANGMAN)
    		c = HangmanTest.class;
    	else if (testType == GameSelectionActivity.TEST_TYPE_CROSSWORD)
    		c = CrosswordTest.class;
    	else if (testType == GameSelectionActivity.TEST_TYPE_WORD_JUMBLE)
    		c = JumbleTest.class;
    	else if (testType == GameSelectionActivity.TEST_TYPE_WORD_SEARCH)
    		c = WordSearchTest.class;
    	
    	return c;
    	*/
    	return null;
    }
    
    public static void startGame(Context context, int testType, int level, int gameNum)
    {
    	/*
    	illuIntent i = null;
    	if (testType == GameSelectionActivity.TEST_TYPE_HANGMAN)
    		i = new Intent(context, HangmanTest.class);
    	else if (testType == GameSelectionActivity.TEST_TYPE_CROSSWORD)
    		i = new Intent(context, CrosswordTest.class);
    	else if (testType == GameSelectionActivity.TEST_TYPE_WORD_JUMBLE)
    		i = new Intent(context, JumbleTest.class);
    	else if (testType == GameSelectionActivity.TEST_TYPE_WORD_SEARCH)
    		i = new Intent(context, WordSearchTest.class);
    	
    	i.putExtra(GameSelectionActivity.KEY_TEST_NUMBER, gameNum);
     	i.putExtra(GameSelectionActivity.KEY_LEVEL, level);
	     
    	context.startActivity(i);
    	*/
    }
	
	PieButton mLevelChoicesView; //the 4-way button to choose between the four choices
	ImageButton mBackButton;
	
	private WordJamApp mApp;
	private GamesDbAdapter mGamesDbAdapter;
	
	private ArrayList<Integer> mRecentGames; //most recent unfinished game number
	private ArrayList<Integer> mAllGames; // all games saved at this level
	
	int mLevel;
	int mPlayType;
}