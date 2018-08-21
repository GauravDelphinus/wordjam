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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.ezeeideas.wordjam.GameDialog.GameDialogListener;
import com.ezeeideas.wordjam.db.GamesDbAdapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.google.ads.*;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * BaseGameActivity - the base class for ALL Game Activities
 */
/**
 * Starting a Game
 * 
 * A: A Game can be started by creating an Intent object and pass the corresponding *GameActivity with these intent values:
 * 
 * 1) KEY_GAME_NUM must be set to the game number (if existing), or -1 if new game
 * 2) KEY_IS_PLAY must be true for Play and false for Practice
 * 3) KEY_PLAY_TYPE must be set to one of Constants.PLAY_TYPE_*
 * 4) KEY_LEVEL must be set to the level
 * For Practice type, you must pass these additional values:
 * 5) KEY_PRACTICE_TYPE must be set to one of Constants.PRACTICE_TYPE_*
 * 6) PRACTICE_GAMES_COMPLETED must be set to total no. of practice games completed so far in the session (0 for first game)
 * 7) PRACTICE_TIME_SPENT must be set to the total time spent in practice session so far (0 for first game)
 * 8) PRACTICE_HINTS_USED must be set to the total no. of hints used in the practice session so far (0 for first game)
 * 9) PRACTICE_POINTS_EARNED must be set to the total no. of points earned in the practice session so far (0 for first game)
 *
 * B: Game Restored after Orientation Change.  This *could* be a game that is new (i.e., not stored in DB)
 * 
 * C: Game Restored from the DB.  This *cannot* be a new game.  All values must be restored from the DB.
 *
 */
public abstract class BaseGameActivity extends FragmentActivity implements OnClickListener, GameDialogListener, AdListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
    	
		setContentView(getLayoutID());
		
		//Initialize Everything!
		initAll(savedInstanceState);
        
        honorTheme();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		if (mSetupGameAsyncTask != null && mSetupGameAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
		{
			mSetupGameAsyncTask.cancel(true);
			mSetupGameAsyncTask = null;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onBackPressed() 
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_BACK_BUTTON, null);
		
	    onStopButtonClicked();
	}
	
	private void honorTheme()
	{
		//honor theme for common elements
    	LinearLayout mainLayout = (LinearLayout) findViewById(R.id.game_main_layout);
    	mainLayout.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    	
    	View ruleHeader = (View) findViewById(R.id.rule_header);
    	ruleHeader.setBackgroundColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
    	
    	View ruleFooter = (View) findViewById(R.id.rule_footer);
    	ruleFooter.setBackgroundColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
    	
     	View ruleFooter2 = (View) findViewById(R.id.rule_footer2);
     	if (ruleFooter2 != null)
     	{
     		ruleFooter2.setBackgroundColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
     	}
		
		//honor theme for game specfic elements
		doHonorTheme();
	}
	
	/**
	 * The Uber Initialization routine.  Call this, and only this, from all games
	 * to ensure correct order of initialization.
	 */
	private void initAll(Bundle savedInstanceState)
	{
		commonInit();
			
		doInitializeGame();
		
		if (savedInstanceState != null && !savedInstanceState.isEmpty())
		{
			restoreSavedInstance(savedInstanceState);
		}
		else if (getIntent().getExtras() != null)
		{
			initializeFromExtras(getIntent().getExtras());
		}

		setupViews();
		
		setupInterstitialAds();
		
		setupGame();
		
		setupConnectors();
	}
	
	/**
	 * Common Initialization Routine.
	 */
	private void commonInit()
	{
		//open databases
		mApp = (WordJamApp)getApplication();
		
    	mWordDbAdapter = mApp.getWordDbAdapter();
    	mGamesDbAdapter = mApp.getGamesDbAdapter();    	
    	
    	
    	//TODO: Fix this
    	//mLevelPoints = mGamesDbAdapter.getEarnedPoints(mPlayType, mLevel);
    	
    	
    	mGameTimer = new GameTimer();
    	mGameTimerHandler = new Handler();
	}
	
	/**
	 * Restore the instance of the game from the saved Bundle.  This is usually
	 * when the device screen was rotated while in the middle of the game.
	 * 
	 * Note: The game may or may not have been stored in the DB.
	 * 
	 * @param savedInstanceState The bundle to restore state from
	 */
	private void restoreSavedInstance(Bundle savedInstanceState)
	{
		if (savedInstanceState != null && !savedInstanceState.isEmpty())
		{
			mIsPlay = savedInstanceState.getBoolean(Constants.KEY_IS_PLAY);
			mPlayType = savedInstanceState.getInt(Constants.KEY_PLAY_TYPE);
			mGameNumber = savedInstanceState.getInt(Constants.KEY_GAME_NUM);
			mIsNewGame = savedInstanceState.getBoolean(Constants.KEY_IS_NEW_GAME);
			
			if (mIsPlay)
			{
				mLevel = savedInstanceState.getInt(Constants.KEY_LEVEL);
				mNumGamesInLevel = savedInstanceState.getInt(Constants.KEY_LEVEL_NUM_GAMES);
				
				mLevelPoints = savedInstanceState.getInt(Constants.KEY_LEVEL_POINTS);
				mLevelState = savedInstanceState.getInt(Constants.KEY_LEVEL_STATE);
				mLevelGamesRemaining = savedInstanceState.getInt(Constants.KEY_LEVEL_GAMES_REMAINING);
				mLevelHintsRemaining = savedInstanceState.getInt(Constants.KEY_LEVEL_HINTS_REMAINING);
				mLevelHintsEarned = savedInstanceState.getInt(Constants.KEY_LEVEL_HINTS_EARNED);
			}
			else
			{
				mPracticeType = savedInstanceState.getInt(Constants.KEY_PRACTICE_TYPE);
				mPracticeGamesCompleted = savedInstanceState.getInt(Constants.PRACTICE_GAMES_COMPLETED);
				mPracticeTimeSpent = savedInstanceState.getInt(Constants.PRACTICE_TIME_SPENT);
				mPracticeHintsUsed = savedInstanceState.getInt(Constants.PRACTICE_HINTS_USED);
				mPracticePointsEarned = savedInstanceState.getInt(Constants.PRACTICE_POINTS_EARNED);
			}
						
			mGameState = savedInstanceState.getInt(Constants.KEY_GAME_STATE);
			mGameHintsRemaining = savedInstanceState.getInt(Constants.KEY_GAME_HINTS_REMAINING);
			mGameHintsUsed = GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_HINTS_TOTAL][mGameType] - mGameHintsRemaining;
			mGamePoints = savedInstanceState.getInt(Constants.KEY_GAME_POINTS);
			mGameTime = savedInstanceState.getLong(Constants.KEY_GAME_DURATION);
			mGameData = savedInstanceState.getString(Constants.KEY_GAME_DATA);
		}
	}
	
	/**
	 * Initialize the game from the bundle extras.  Note that the extras may be sent in these cases:
	 * 
	 * 1) A new game was started by the user (in this case, KEY_GAME_NUM is passed as -1 in case of Play, but actual no. incase of Practice)
	 * 2) An existing game was started/resumed by the user (in this case, KEY_GAME_NUM is the actual game number)
	 * 
	 * @param extras The bundle extras to initialize from
	 */
	private void initializeFromExtras(Bundle extras)
	{
		if (extras != null)
		{			
			//Get the mandatory fields
			mIsPlay = extras.getBoolean(Constants.KEY_IS_PLAY);
			mPlayType = extras.getInt(Constants.KEY_PLAY_TYPE);
			mGameNumber = extras.getInt(Constants.KEY_GAME_NUM);
			mIsNewGame = false;
						
			//Not get the play or practice related values
			if (mIsPlay)
			{
				mLevel = extras.getInt(Constants.KEY_LEVEL);
				mNumGamesInLevel = mGamesDbAdapter.getNumGames(mPlayType, mLevel);
				if (mGameNumber == -1)
				{
					mGameNumber = mNumGamesInLevel + 1;
					mIsNewGame = true;
					mNumGamesInLevel ++;
				}
				
				mLevelPoints = mGamesDbAdapter.getEarnedPoints(mPlayType, mLevel);
				mLevelState = mGamesDbAdapter.getLevelState(mPlayType, mLevel);
				mLevelGamesRemaining = mGamesDbAdapter.getNumGamesRemaining(mPlayType, mLevel);
				mLevelHintsRemaining = mGamesDbAdapter.getNumHintsRemaining(mPlayType, mLevel);
				mLevelHintsEarned = mGamesDbAdapter.getNumHintsEarned(mPlayType, mLevel);
			}
			else
			{
				mIsNewGame = true;
				if (mGameNumber == -1)
				{
					mGameNumber = 1;
				}
				
				mPracticeType = extras.getInt(Constants.KEY_PRACTICE_TYPE);
				mPracticeGamesCompleted = extras.getInt(Constants.PRACTICE_GAMES_COMPLETED);
				mPracticeTimeSpent = extras.getInt(Constants.PRACTICE_TIME_SPENT);
				mPracticeHintsUsed = extras.getInt(Constants.PRACTICE_HINTS_USED);
				mPracticePointsEarned = extras.getInt(Constants.PRACTICE_POINTS_EARNED);
			}
		}
		
		if (mIsNewGame)
		{
			mGameState = Constants.GAME_STATE_NOT_STARTED;
			Utils.Log("mGameHintsRemaining, mPlayType = " + mPlayType + ", mIsPlay = " + mIsPlay + ", mLevel = " + mLevel + ", mPracticeType = " + mPracticeType + ", mGameType = " + mGameType);
			mGameHintsRemaining = GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_HINTS_TOTAL][mGameType];
			Utils.Log("initializing mGameHintsRemaining to " + mGameHintsRemaining);
			mGameHintsUsed = 0;
			mGamePoints = 0;
			mGameTime = 0;
			mGameData = null;
		}
		else
		{
			//always for a Play game, as Practice is always NEW
			GamesDbAdapter.GameInfo gameInfo = mGamesDbAdapter.getGameInformation(mPlayType, mLevel, mGameNumber);
			mGameState = gameInfo.gameShortInfo.gameState;
			mGameHintsRemaining = gameInfo.gameLongInfo.gameHintsRemaining;
			mGameHintsUsed = GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_HINTS_TOTAL][mGameType] - mGameHintsRemaining;
			mGamePoints = gameInfo.gameShortInfo.gamePointsEarned;
			mGameTime = gameInfo.gameLongInfo.gameDuration;
			mGameData = gameInfo.gameLongInfo.gameData;
		}
		
		//game hints cannot be larger than the total level hints remaining, in case of Play
		if (mIsPlay)
		{
			mGameHintsRemaining = Math.min(mGameHintsRemaining, mLevelHintsRemaining);
		}
	}
	
	/**
	 * Print various key game attributes - for debugging purposes only.
	 */
	private void print()
	{
		Utils.Log("####################################################");
		Utils.Log("mGameNumber = " + mGameNumber);
		Utils.Log("mGameType = " + mGameType);
		Utils.Log("mGameState = " + mGameState);
		Utils.Log("mIsPlay = " + mIsPlay);
		Utils.Log("mPlayType = " + mPlayType);
		Utils.Log("mIsNewGame = " + mIsNewGame);
		Utils.Log("mLevel = " + mLevel);
		Utils.Log("mGameData = [" + mGameData + "]");
		Utils.Log("mGameTime = " + mGameTime);
		Utils.Log("####################################################");
	}
	
	/**
	 * Save the instance state of the current game to the bundle.  This is usually called when the 
	 * user rotates the device screen while playing the game.  It can be called at any time the system
	 * decides to "kill" the activity without user interaction where the activity is expected to
	 * "restore" itself at some later point.
	 */
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onSaveInstanceState(savedInstanceState);

		//cancel the async task, if it's running
		if (mSetupGameAsyncTask != null && mSetupGameAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
		{
			mSetupGameAsyncTask.cancel(true);
			
			mGameData = null;
		}
		
		savedInstanceState.putBoolean(Constants.KEY_IS_PLAY, mIsPlay);
		savedInstanceState.putBoolean(Constants.KEY_IS_NEW_GAME, mIsNewGame);
		savedInstanceState.putInt(Constants.KEY_GAME_NUM, mGameNumber);
		savedInstanceState.putInt(Constants.KEY_PLAY_TYPE, mPlayType);
		
		savedInstanceState.putInt(Constants.KEY_GAME_TYPE, mGameType);
		savedInstanceState.putInt(Constants.KEY_GAME_POINTS, mGamePoints);
		savedInstanceState.putInt(Constants.KEY_GAME_STATE, mGameState);
		savedInstanceState.putInt(Constants.KEY_GAME_HINTS_REMAINING, mGameHintsRemaining);
		savedInstanceState.putLong(Constants.KEY_GAME_DURATION, mGameTime);
		savedInstanceState.putLong(Constants.KEY_GAME_LAST_PLAYED, System.currentTimeMillis());
		
		if (mGameData != null)
		{
			//the game setup (via AsyncTask) already happened once, so generate current game state and save
			savedInstanceState.putString(Constants.KEY_GAME_DATA, generateGameData());
		}
		else
		{
			//the game setup never happened, so set game data to null so it is setup the next time it is restored
			savedInstanceState.putString(Constants.KEY_GAME_DATA, null);
		}
		
		if (mIsPlay)
		{
			savedInstanceState.putInt(Constants.KEY_LEVEL, mLevel);
			savedInstanceState.putInt(Constants.KEY_LEVEL_STATE, mLevelState);
			savedInstanceState.putInt(Constants.KEY_LEVEL_GAMES_REMAINING, mLevelGamesRemaining);
			savedInstanceState.putInt(Constants.KEY_LEVEL_HINTS_REMAINING, mLevelHintsRemaining);
			savedInstanceState.putInt(Constants.KEY_LEVEL_HINTS_EARNED, mLevelHintsEarned);
			savedInstanceState.putInt(Constants.KEY_LEVEL_NUM_GAMES, mNumGamesInLevel);
		}
		else
		{
			savedInstanceState.putInt(Constants.KEY_PRACTICE_TYPE, mPracticeType);
			savedInstanceState.putInt(Constants.PRACTICE_GAMES_COMPLETED, mPracticeGamesCompleted);
			savedInstanceState.putInt(Constants.PRACTICE_TIME_SPENT, mPracticeTimeSpent);
			savedInstanceState.putInt(Constants.PRACTICE_HINTS_USED, mPracticeHintsUsed);
			savedInstanceState.putInt(Constants.PRACTICE_POINTS_EARNED, mPracticePointsEarned);
		}
	}
	
	private void setupConnectors()
	{

	}
	/**
	 * Set up the game data, UI, etc. depending on whether it's a new game or a
	 * restored game.
	 */
	private void setupGame()
	{
		if (mGameData != null)
		{
			//restore an existing game
			doRestoreGame();
			loadBannerAd();
			if (mGameState == Constants.GAME_STATE_IN_PROGRESS)
			{
				startTimer();
			}
			refreshViews();
			return;
		}
		
		//start a new game!
		mSetupGameAsyncTask = new SetupGameAsyncTask(this);
		mSetupGameAsyncTask.execute((Void[])null);
	}
	
	/**
	 * The Game was successfully completed.  Do the points updates, etc. and then
	 * congratulate the user on completing the game!
	 * 
	 * This function is called from the actual game class that knows when the game
	 * is completed.
	 */
	protected void onGameCompleted()
	{
		Utils.Log("onGameCompleted, this is " + this.getClass());
		stopTimer();
		
		//do the math for the points/hints
		mGameState = Constants.GAME_STATE_COMPLETED;
	
		mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_COMPLETION][mGameType];
		mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_COMPLETION][mGameType];
		mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_COMPLETION][mGameType];

		if (mGameTime < 1000 * GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_FAST_COMPLETION_TIME][mGameType])
		{
			mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_BONUS_POINTS_ON_FAST_COMPLETION][mGameType];
			mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_BONUS_POINTS_ON_FAST_COMPLETION][mGameType];
			mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_BONUS_POINTS_ON_FAST_COMPLETION][mGameType];
		}

		mLevelGamesRemaining --;
		mPracticeGamesCompleted ++;
		
		mLevelHintsRemaining += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_HINTS_ON_COMPLETION][mGameType];
		mLevelHintsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_HINTS_ON_COMPLETION][mGameType];
		
		if (mIsPlay)
		{
			//save to the DB
			saveCurrentGame();
		}
		
		refreshViews();
		
		Utils.Log("showing the dialog");
		//show the dialog		
		GameCompletedDialog dialog = new GameCompletedDialog(this, mGamePoints);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();	
		Utils.Log("after dialog.show");
	}
	
	/**
	 * The Game was unsuccessfully completed (failed).  Do the points updates and then
	 * alert the user about the fact that he failed the game!
	 * 
	 * Called from the actual game class that knows exactly when the user failed the game.
	 */
	protected void onGameFailed()
	{
		stopTimer();
		
		//do the math for the points/hints
		mGameState = Constants.GAME_STATE_FAILED;
		mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_LOSING_GAME][mGameType];
		mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_LOSING_GAME][mGameType];
		mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_LOSING_GAME][mGameType];
		
		if (mIsPlay)
		{
			//save to the DB
			saveCurrentGame();
		}

		refreshViews();
		
		//show the dialog
		GameFailedDialog dialog = new GameFailedDialog(this);
		dialog.show();	
	}
	
	/**
	 * Show a little point animation that makes the user like to earn more points!
	 */
	private void showPointsBubble()
	{
		
	}
	
	/**
	 * A correct Word or Letter was chosen, not leading to game completion.
	 * 
	 * Called from the game class that knows when the right word or letter was selected.
	 */
	protected void onRightWordOrLetterFound()
	{
		Utils.Log("onRightWordOrLetterFound, mGameType = " + mGameType + ", mLevel = " + mLevel);
		//do the math for the points/hints
		mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType];
		mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType];
		mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType];
				
		refreshViews();
		
		showPointsBubble();
	}
	
	/**
	 * A wrong Word or Letter was chosen, not leading to game completion.
	 * 
	 * Called from the game class that knows when the right word or letter wasn't successfuly found.
	 */
	protected void onWrongWordOrLetterFound()
	{
		Utils.Log("onWrongWordOrLetterFound");
		//do the math for the points/hints
		mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_INCORRECT_WORD_OR_LETTER][mGameType];
		mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_INCORRECT_WORD_OR_LETTER][mGameType];
		mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_INCORRECT_WORD_OR_LETTER][mGameType];
						
		refreshViews();
				
		showPointsBubble();
	}
		
	/**
	 * Call this when you're ready to handle a game completion
	 */
	private void handleGameCompleted()
	{
		Utils.Log(LOG_TAG + "handleGameCompleted");
		doHandleGivenUp();

		refreshViews();

		//now move on to the next game directly				
		startNextGame(true);
	}
	
	private boolean checkLevelCompleted()
	{
		int levelGamesRemaining = mGamesDbAdapter.getNumGamesRemaining(mPlayType, mLevel);
		if (mGameState == Constants.GAME_STATE_COMPLETED)
		{
			levelGamesRemaining --;
		}
		
		if (levelGamesRemaining <= 0 && mLevelPoints >= GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_POINTS])
		{
			//Completed the level!
			return true;
		}
		
		return false;
	}
	
	/**
	 * Save the current game to the DB.  Only relevant for Play, not Practice.
	 */
	protected void saveCurrentGame()
	{
		GamesDbAdapter.GameShortInfo gameShortInfo = mGamesDbAdapter.new GameShortInfo();
		gameShortInfo.gameNum = mGameNumber;
		gameShortInfo.gameType = mGameType;
		gameShortInfo.gamePointsEarned = mGamePoints;
		gameShortInfo.gameState = mGameState;

		GamesDbAdapter.GameLongInfo gameLongInfo = mGamesDbAdapter.new GameLongInfo();
		gameLongInfo.gameLevel = mLevel;
		gameLongInfo.gameHintsRemaining = mGameHintsRemaining;
		gameLongInfo.gameDuration = mGameTime;
		gameLongInfo.gameLastPlayed = System.currentTimeMillis();
		gameLongInfo.gameData = generateGameData();

		mGamesDbAdapter.addOrUpdateGameData(mPlayType, gameShortInfo, gameLongInfo);

		GamesDbAdapter.LevelInfo levelInfo = mGamesDbAdapter.new LevelInfo();
		levelInfo.levelNum = mLevel;
		levelInfo.levelState = mLevelState;
		levelInfo.levelGamesRemaining = mLevelGamesRemaining;
		levelInfo.levelHintsRemaining = mLevelHintsRemaining;
		levelInfo.levelHintsEarned = mLevelHintsEarned;

		mGamesDbAdapter.addOrUpdateLevelData(mPlayType, levelInfo);
	}

	/**
	 * The next game needs to be opened (could be saved or a new game).
	 */
	protected void startNextGame(boolean checkAd)
	{
		if (checkAd && shouldShowInterstitialAd() && canShowInterstitialAd())
		{
			showInterstitialAd(AD_DISMISS_ACTION_START_NEXT_GAME);
			return;
		}
		
		Intent i = null; 
		
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
		int newGamePref = settings.getInt(Constants.PREFS_NEW_GAME_CHOICE, Constants.PREFS_NEW_GAME_SAME_TYPE);
		if (newGamePref == Constants.PREFS_NEW_GAME_SAME_TYPE)
		{
			i = new Intent(this, mGameClass);
		}
		else if (newGamePref == Constants.PREFS_NEW_GAME_RANDOM_TYPE)
		{
			int gameType = GameUtils.getRandomGameType();
			Class gameClass = GameUtils.getGameClassForGameType(gameType);
			
			i = new Intent(this, gameClass);
		}
		else //if (newGamePref == Constants.PREFS_NEW_GAME_ASK_ME)
		{
			i = new Intent(this, AdvancedGameSelectionActivity.class);
		}
		
		//in case this is Play type and not the last game in the level, the next game needs
		//to be restored from the DB, so retrieve the game class
		if (mIsPlay && (mGameNumber < mNumGamesInLevel))
		{
			int gameType = mGamesDbAdapter.getGameTypeForGame(mPlayType, mLevel, mGameNumber + 1);
			i = new Intent(this, GameUtils.getGameClassForGameType(gameType));
		}
		
		i.putExtra(Constants.KEY_IS_PLAY, mIsPlay);
		i.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
		if (mIsPlay)
		{
			if (mGameNumber == mNumGamesInLevel) //this is the last game, so start a new game
			{
				i.putExtra(Constants.KEY_GAME_NUM, -1);
			}
			else //this is not the last game and there are more saved games ahead of it
			{
				i.putExtra(Constants.KEY_GAME_NUM, mGameNumber + 1);
			}
		}
		else
		{
			i.putExtra(Constants.KEY_GAME_NUM, mGameNumber + 1);
		}
		 	
		if (mIsPlay)
		{
			i.putExtra(Constants.KEY_LEVEL, mLevel);
		}
		else
		{
			i.putExtra(Constants.KEY_PRACTICE_TYPE, mPracticeType);
			i.putExtra(Constants.PRACTICE_GAMES_COMPLETED, mPracticeGamesCompleted);
			i.putExtra(Constants.PRACTICE_TIME_SPENT, mPracticeTimeSpent);
			i.putExtra(Constants.PRACTICE_HINTS_USED, mPracticeHintsUsed);
			i.putExtra(Constants.PRACTICE_POINTS_EARNED, mPracticePointsEarned);
		}
		
		//finally, start the activity
		//Whenever the game activity is dismissed, we'd like to go directly to the Level Home
		Utils.Log("setting result in Activity to 1");
		setResult(Constants.ACTION_GO_TO_LEVEL_HOME);
		this.finish();
		
		startActivity(i);
	}
	
	/**
	 * The previous game needs to be opened.  This is only relevant for Play, not Practice.
	 */
	protected void startPreviousGame()
	{
		Intent i = null;

		int gameNum = mGameNumber - 1;
		int gameType = mGamesDbAdapter.getGameType(mPlayType, mLevel, gameNum);
		if (gameType != -1)
		{
			i = new Intent(this, GameUtils.getGameClassForGameType(gameType));
		}

		i.putExtra(Constants.KEY_IS_PLAY, mIsPlay);
		i.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
		i.putExtra(Constants.KEY_GAME_NUM, gameNum);
		i.putExtra(Constants.KEY_LEVEL, mLevel);


		//finally, start the activity
		Utils.Log("setting result in Activity to 1");
		setResult(Constants.ACTION_GO_TO_LEVEL_HOME);
		this.finish();
		startActivity(i);
	}
	
	/**
	 * Set up Views that are common to all Games
	 */
	protected void setupViews()
	{
		//Header
		TextView headerTextView = (TextView) findViewById(R.id.game_header_text_view);
		String headerText = String.format(getResources().getString(R.string.game_header), GameUtils.getPlayTypeName(mPlayType, this), mLevel);
		headerTextView.setText(headerText);
		
		//Game Name
		TextView gameHeadingTextView = (TextView) findViewById(R.id.game_title_text_view);
		String gameHeadingText = String.format(getResources().getString(R.string.game_heading), GameUtils.getGameNameFromGameType(mGameType, this), mGameNumber);
		gameHeadingTextView.setText(gameHeadingText);
    	
		//Points Text
		mPointsTextView = (TextView) findViewById(R.id.game_points_text_view);
		int points = (mIsPlay ? mLevelPoints : mPracticePointsEarned);
		String pointsText = String.format(getResources().getString(R.string.game_points_label), points);
		mPointsTextView.setText(pointsText);
		
		//Timer Text
		mGameTimeTextView = (TextView) findViewById(R.id.game_time_text_view);
		mGameTimeTextView.setText(Utils.convertTime(mGameTime));
		
        //Buttons at the bottom
        mStopButton = (ImageButton) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(this);
        
        mPauseButton = (ImageButton) findViewById(R.id.pause_button);
        mPauseButton.setOnClickListener(this);
        
        mGiveUpButton = (ImageButton) findViewById(R.id.giveup_button);
        mGiveUpButton.setOnClickListener(this);
        
        mHintButton = (ImageButton) findViewById(R.id.hint_button);
        mHintButton.setOnClickListener(this);
        
        mHelpButton = (ImageButton) findViewById(R.id.help_button);
        mHelpButton.setOnClickListener(this);
        
        //mShareButton = (ImageButton) findViewById(R.id.share_button);
        //mShareButton.setOnClickListener(this);
        
        mPreviousButton = (ImageButton) findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(this);
        if (mGameNumber == 1) //disable previous in case of first game
        {
        	mPreviousButton.setEnabled(false);
        }
        if (!mIsPlay) //Practice games don't have a Previous button
        {
        	LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.game_button_layout);
        	buttonLayout.removeView(mPreviousButton);
        	//mPreviousButton.setVisibility(ImageButton.INVISIBLE);
        	//TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0f);
        	//mPreviousButton.setLayoutParams(params);
        }
        
        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        
        //set up hint button with values, as required
        //setupHintButton();
        
        //set up the ads
        mBannerAdView = (AdView) findViewById(R.id.adView);
        mBannerAdView.setAdListener(this);
        
        //set up game specific views
        doSetupViews();
	}
	
	/**
	 * Refresh the views at run-time based on updated game state, such as hints
	 * used, points earned, time passed, etc.
	 */
	private void refreshViews()
	{
		//Points Text
		String pointsText = String.format(getResources().getString(R.string.game_points_label), (mIsPlay? mLevelPoints : mPracticePointsEarned));
		mPointsTextView.setText(pointsText);
		
		if (mGameState == Constants.GAME_STATE_NOT_STARTED)
		{
			mStopButton.setEnabled(true);
			mPauseButton.setEnabled(false);
			mGiveUpButton.setEnabled(false);
			mHintButton.setEnabled(false);
			mHelpButton.setEnabled(true);
		}
		else if (mGameState == Constants.GAME_STATE_COMPLETED)
		{
			mStopButton.setEnabled(true);
			mPauseButton.setEnabled(false);
			mGiveUpButton.setEnabled(false);
			mHintButton.setEnabled(false);
			mHelpButton.setEnabled(true);
		}
		else if (mGameState == Constants.GAME_STATE_IN_PROGRESS)
		{
			mStopButton.setEnabled(true);
			mPauseButton.setEnabled(true);
			mGiveUpButton.setEnabled(true);
			mHintButton.setEnabled(true);
			mHelpButton.setEnabled(true);
		}
		else if (mGameState == Constants.GAME_STATE_PAUSED)
		{
			mStopButton.setEnabled(false);
			mPauseButton.setEnabled(false);
			mGiveUpButton.setEnabled(false);
			mHintButton.setEnabled(false);
			mHelpButton.setEnabled(true);
		}
		else if (mGameState == Constants.GAME_STATE_GIVEN_UP)
		{
			mStopButton.setEnabled(true);
			mPauseButton.setEnabled(false);
			mGiveUpButton.setEnabled(false);
			mHintButton.setEnabled(false);
			mHelpButton.setEnabled(true);
		}
		else if (mGameState == Constants.GAME_STATE_FAILED)
		{
			mStopButton.setEnabled(true);
			mPauseButton.setEnabled(false);
			mGiveUpButton.setEnabled(false);
			mHintButton.setEnabled(false);
			mHelpButton.setEnabled(true);
		}
		
		//setupHintButton();
		
		doRefreshViews();
	}
	
	/**
	 * Set up the hint button to show the count of remaining hints in the game.
	 * 
	 * If there are > 1 hints remaining in the game, don't show a count
	 * If there are 1 or 0 hints remaining in the game, show a count
	 */
	private void setupHintButton()
	{		
		if (mGameHintsRemaining == 1)
		{
			mHintButton.setBackgroundResource(R.drawable.hint_button1);
		}
		else if (mGameHintsRemaining == 0)
		{
			mHintButton.setBackgroundResource(R.drawable.hint_button0);
		}
		else
		{
			mHintButton.setBackgroundResource(R.drawable.hint_button);
		}
	}
	
	/**
	 * The stop button was clicked
	 */
	private void onStopButtonClicked()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_STOP_BUTTON, null);
		
		stopTimer();
		
		//Update various values
		
		if (mIsPlay) // for play, save the game and show the entire level point summary
		{
			//save to the DB
			saveCurrentGame();
			
			//show the dialog
			GameUtils.LevelInfo levelInfo = new GameUtils.LevelInfo();
			levelInfo.levelNum = mLevel;
			levelInfo.levelGamesRemaining = mLevelGamesRemaining;
			levelInfo.levelHintsRemaining = mLevelHintsRemaining;
			levelInfo.levelHintsEarned = mLevelHintsEarned;
			levelInfo.levelPointsRemaining = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_POINTS] - mLevelPoints;
			levelInfo.levelTimeSpent = mGamesDbAdapter.getTotalTimeSpent(mPlayType, mLevel);
			levelInfo.levelTotalGames = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_GAMES];
			levelInfo.levelTotalHints = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_HINTS];
			levelInfo.levelTotalPoints = GameUtils.sLevelValues[mLevel - 1][GameUtils.LEVEL_TOTAL_POINTS];
			
			Utils.Log("passing to LevelPointSummaryDialog, levelInfo = " + levelInfo);
			LevelPointSummaryDialog dialog = new LevelPointSummaryDialog(this, levelInfo);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			levelInfo = null;
		}
		else
		{
			// for practice, show the practice summary
			GameUtils.PracticeInfo practiceInfo = new GameUtils.PracticeInfo();
			//practiceInfo.practiceGamesCompleted = mPracticeGamesCompleted;
			practiceInfo.practiceGamesCompleted = mGameNumber;
			practiceInfo.practiceHintsUsed = mPracticeHintsUsed;
			practiceInfo.practicePointsEarned = mPracticePointsEarned;
			practiceInfo.practiceTimeSpent = mPracticeTimeSpent;
			
			PracticePointSummaryDialog dialog = new PracticePointSummaryDialog(this, practiceInfo);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			practiceInfo = null;
		}
	}
	
	/**
	 * The pause button was clicked
	 */
	private void onPauseButtonClicked()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_PAUSE_BUTTON, null);
		
		//stop the timer
		stopTimer();
		
		GamePauseDialog dialog = new GamePauseDialog(this);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();	
	}
	
	/**
	 * The user opted to resume the current game
	 */
	private void onResumeGameSelected()
	{
		//resume the timer
		startTimer();
	}
	
	/**
	 * The give up button was clicked
	 */
	private void onGiveUpButtonClicked()
	{	
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_REVEAL_BUTTON, null);
		
		GameGiveUpDialog dialog = new GameGiveUpDialog(this, -GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_GIVING_UP][mGameType], mGameHintsRemaining);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
	
	/**
	 * Consume a hint for the current game
	 */
	private void consumeHint()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_HINT_USED, null);
		
		doHandleHintUsed();
		
		mGameHintsRemaining --;
		mGameHintsUsed ++;
		mLevelHintsRemaining --;
		mPracticeHintsUsed ++;
		
		mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_HINT_USED][mGameType];
		mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_HINT_USED][mGameType];
		mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_HINT_USED][mGameType];
	}

	/**
	 * The hint button was clicked
	 */
	private void onHintButtonClicked()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_HINT_BUTTON, null);
		
		Utils.Log("onHintbutton, mGameHintsRemaining = " + mGameHintsRemaining);
		if (mGameHintsRemaining > 0)
		{
			consumeHint();
			
			refreshViews();
		}
		else //hints exhausted!
		{	
			GameHintsExhaustedDialog dialog = new GameHintsExhaustedDialog(this);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
		}
	}
	
	/**
	 * The Help button was clicked
	 */
	private void onHelpButtonClicked()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_HELP_BUTTON, null);
		
		Intent intent = new Intent(this, HelpDialog.class);
		intent.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
		intent.putExtra(Constants.KEY_GAME_TYPE, mGameType);
		intent.putExtra(Constants.KEY_IS_PLAY, mIsPlay);
		intent.putExtra(Constants.KEY_GAME_LEVEL, mLevel);
		intent.putExtra(Constants.KEY_PRACTICE_TYPE, mPracticeType);
		
		startActivity(intent);
	}
	
	private void onShareButtonClicked()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_SHARE_BUTTON, null);
		
		GameShareDialog dialog = new GameShareDialog(this);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
	
	/**
	 * The previous game button was clicked
	 */
	private void onPreviousButtonClicked()
	{
		stopTimer();
		
		saveCurrentGame();
		startPreviousGame();
	}
	
	/**
	 * The next game button was clicked
	 */
	private void onNextButtonClicked()
	{
		Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_NEXT_GAME_BUTTON, null);
		
		stopTimer();
		
		if (mIsPlay)
		{
			saveCurrentGame();
		}
		
		startNextGame(false);
	}

	/**
	 * A button was clicked
	 */
	public void onClick(View v)
	{
		if (v.getId() == R.id.stop_button)
		{
			onStopButtonClicked();
		}
		else if (v.getId() == R.id.pause_button)
		{
			onPauseButtonClicked();
		}
		else if (v.getId() == R.id.giveup_button)
		{
			onGiveUpButtonClicked();
		}
		else if (v.getId() == R.id.hint_button)
		{
			onHintButtonClicked();
		}
		else if (v.getId() == R.id.help_button)
		{
			onHelpButtonClicked();
		}
		else if (v.getId() == R.id.previous_button)
		{
			onPreviousButtonClicked();
		}
		else if (v.getId() == R.id.next_button)
		{
			onNextButtonClicked();
		}
		else if (v.getId() == R.id.share_button)
		{
			onShareButtonClicked();
		}
		else
		{
			doHandleButtonClicked(v.getId());
		}
	}
	
	/**
	 * One of the options in the GameDialog instance was selected
	 */
	public void onDialogOptionSelected(Dialog dialog, int option)
	{
		if (dialog.getClass() == GamePauseDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				//Resume the game
				onResumeGameSelected();
			}
		}
		else if (dialog.getClass() == LevelPointSummaryDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				setResult(Constants.ACTION_GO_TO_LEVEL_HOME);
				this.finish();
			}
		}
		else if (dialog.getClass() == PracticePointSummaryDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_GAME_SUMMARY_SHARE_BUTTON, null);
	    		
				String storyTitle = String.format(getResources().getString(R.string.practice_point_summary_share_story_title), mGameNumber, GameUtils.getGameNameFromGameType(mGameType, this));
				String storyCaption = getResources().getString(R.string.practice_point_summary_share_story_caption);
				String storyDescription = String.format(getResources().getString(R.string.practice_point_summary_share_story_description), GameUtils.getGameNameFromGameType(mGameType, this), mGameNumber, mPracticePointsEarned, mPracticeTimeSpent/1000, mPracticeHintsUsed);

			}
			else
			{
				this.finish();
			}
			/*
			 * Dont' show ads for now
			 */
			/*
				if (canShowInterstitialAd())
				{
					showInterstitialAd(AD_DISMISS_ACTION_DISMISS_ACTIVITY);
				}
				else
				{
					this.finish();
				}
			 */
			//this.finish();
		}
		else if (dialog.getClass() == GameShareDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1) //Share Game
			{
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_SHARE_GAME_BUTTON, null);
			}
			else if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_2) //Invite Friends
			{
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_INVITE_FRIENDS_BUTTON, null);
	    		
				String storyTitle = getResources().getString(R.string.facebook_share_story_title);
				String storyCaption = getResources().getString(R.string.facebook_share_story_caption);
				String storyDescription = getResources().getString(R.string.facebook_share_story_description);
			}
		}
		else if (dialog.getClass() == GameGiveUpDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_REVEAL_GAME_BUTTON, null);
	    		
				stopTimer();
				
				doHandleGivenUp();
				
				mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_GIVING_UP][mGameType];
				mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_GIVING_UP][mGameType];
				mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_GIVING_UP][mGameType];
				
				mGameState = Constants.GAME_STATE_GIVEN_UP;
				
				refreshViews();
			}
			else if (option == GameDialog.GAME_DIALOG_ACTION_NEGATIVE_1)
			{
				//cancel giving up.  Do nothing
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_REVEAL_CANCELLED, null);
			}
			else if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_2)
			{
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_REVEAL_SHARE_BUTTON, null);
			}
		}
		else if (dialog.getClass() == GameCompletedDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{

			}
			else
			{
				handleGameCompleted();
			}
		}
		else if (dialog.getClass() == GameFailedDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{				
				doHandleGivenUp();
					
				//Update values
				mGamePoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_LOSING_GAME][mGameType];
				mLevelPoints += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_LOSING_GAME][mGameType];
				mPracticePointsEarned += GameUtils.getPointChart(mPlayType, mIsPlay, mLevel, mPracticeType)[GameUtils.GAME_POINTS_ON_LOSING_GAME][mGameType];
				
				refreshViews();
			}
		}
		else if (dialog.getClass() == GameHintsExhaustedDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				Analytics.trackEvent(this, Analytics.getAnalyticsCategoryFromGame(mGameType), Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_HINT_SHARE_BUTTON, null);
	    		

			}
		}
		else if (dialog.getClass() == GameHintConfirmationDialog.class)
		{
			if (option == GameDialog.GAME_DIALOG_ACTION_POSITIVE_1)
			{
				consumeHint();
				
				refreshViews();
			}
			else if (option == GameDialog.GAME_DIALOG_ACTION_NEGATIVE_1)
			{
				//status quo (don't use hint)
			}
		}

	}
	
	/************* Ads **************/
	
	/**
	 * Load the banner ad.  This should be done after
	 * the game data has been loaded so we can target
	 * ads based on game content
	 */
	private void loadBannerAd()
	{
		AdRequest adRequest = new AdRequest();
		Set<String> keywords = new HashSet<String>();
		Utils.addKeywords(keywords);
		adRequest.setKeywords(keywords);
		
		Utils.Log("gauravad: loadBannerAd with keywords");
		mBannerAdView.loadAd(adRequest);
	}
	/**
	 * Show a full-screen rich Interstitial Ad
	 */
	private void setupInterstitialAds()
	{
		Utils.Log("gauravad: setupInterstitialAds");
		// Create the interstitial
	    interstitial = new InterstitialAd(this, "a15281ef463ae86");

	    // Set Ad Listener to use the callbacks below
	    interstitial.setAdListener(this);
	    
	    // Initial Refresh
	    refreshInterstitialAds();
	}
	
	private void refreshInterstitialAds()
	{
		Utils.Log("gauravad: refreshInterstitialAds");
	    // Create ad request
	    AdRequest adRequest = new AdRequest();

	    // Begin loading your interstitial
	    interstitial.loadAd(adRequest);
	}
	
	public void onReceiveAd(Ad ad) 
	{
		if (ad == mBannerAdView)
		{
			Utils.Log("gauravad: onReceiveAd for Banner ad");
		}
		else if (ad == interstitial)
		{
			Utils.Log("gauravad: onReceiveAd for Interstitial ad");
		}
	}
	
	public void onFailedToReceiveAd (Ad ad, AdRequest.ErrorCode error)
	{	
		if (ad == mBannerAdView)
		{
			Utils.Log("gauravad: onFailedToReceiveAd for Banner ad");
		}
		else if (ad == interstitial)
		{
			Utils.Log("gauravad: onFailedToReceiveAd for Interstitial ad");
		}
	}
	public void onPresentScreen (Ad ad)
	{
		Utils.Log("gauravad: onPresentScreen");
		mShowingInterstitialAd = true;
		mTimeOfLastAd = System.currentTimeMillis();
	}
	public void onDismissScreen (Ad ad)
	{
		Utils.Log("gauravad: onDismissScreen");
		mShowingInterstitialAd = false;
		switch (mActionOnAdDismiss)
		{
		case AD_DISMISS_ACTION_DISMISS_ACTIVITY:
			this.finish();
			break;
		case AD_DISMISS_ACTION_START_NEXT_GAME:
			startNextGame(false);
			break;
		}
	}
	public void onLeaveApplication (Ad ad)
	{
		Utils.Log("gauravad: onLeaveApplication"); 
	}
  
	private boolean shouldShowInterstitialAd()
	{		
		//Don't show interstitials for now!
		/*
		long currentTime = System.currentTimeMillis();
		*/
		/**
		 * Set the initial value of mTimeOfLastAd if not already set
		 * this is to avoid showing the ad to the user as soon as he starts
		 * playing.
		 */
		
		/*
		if (mTimeOfLastAd == 0)
		{
			mTimeOfLastAd = currentTime;
		}
		if (currentTime - mTimeOfLastAd > MIN_INTERVAL_BETWEEN_ADS)
		{
			return true;
		}
		*/
		return false;
	}
	
	private boolean canShowInterstitialAd()
	{
		return interstitial.isReady();
	}
	
	private void showInterstitialAd(int actionOnDismiss)
	{
		interstitial.show();
		mActionOnAdDismiss = actionOnDismiss;
	}
	

	public static Bitmap getBitmapFromAsset(Context context, String strName) {
	    AssetManager assetManager = context.getAssets();

	    InputStream istr;
	    Bitmap bitmap = null;
	    try {
	        istr = assetManager.open(strName);
	        bitmap = BitmapFactory.decodeStream(istr);
	    } catch (IOException e) {
	        return null;
	    }

	    return bitmap;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		Utils.Log(LOG_TAG + "onActivityResult called");
		super.onActivityResult(requestCode, resultCode, data);

	}
	

	
	/************* Timer Related **************/
	
	/**
	 * Update the timer in the UI with the game time
	 */
	private void updateTimer()
	{
		mGameTimeTextView.setText(Utils.convertTime(mGameTime));
	}
	
	/**
	 * Start the timer either from the beginning (if new game)
	 * or from the point it is being resumed/restored.
	 */
	private void startTimer()
	{
		long currentTime = System.currentTimeMillis();
		if (mGameTime <= 0)
		{
			mGameTimeStart = currentTime;
			Utils.Log("Timer Started from 0");
		}
		else
		{
			mGameTimeStart = currentTime - mGameTime;
			Utils.Log("Timer Resumed");
		}
		
		//set timer value for practice time calculation
		mLastTimerValue = currentTime;
		
		mGameTimerHandler.removeCallbacks(mGameTimer);
		mGameTimerHandler.postDelayed(mGameTimer, 0); //start immediately
	}
	
	/**
	 * Stop the timer
	 */
	private void stopTimer()
	{
		mGameTimerHandler.removeCallbacks(mGameTimer);
		Utils.Log("Timer Stopped");
	}
	
	/********************* Abstract routines - Game implementations must implement these ******************/
	
	/**
	 * Return the main layout's ID that is used to call setContentView() on.
	 * 
	 * @return The Layout ID
	 */
	protected abstract int getLayoutID();
	
	/**
	 * Initialize Game specific values here, such as mGameType and mGameClass
	 * as well as allocate member variables and initialize them (e.g., Vectors, lists, etc.)
	 */
	protected abstract void doInitializeGame();
	
	/**
	 * Honor theme for game specific UI elements
	 */
	protected abstract void doHonorTheme();
	
	/**
	 * Implemented by the Game Activity to restore game-specific attributes.
	 */
	protected abstract void doRestoreGame();
	
	/**
	 * Set up game specific views
	 */
	protected abstract void doSetupViews();
	
	/**
	 * Refresh game specific views.
	 */
	protected abstract void doRefreshViews();
	
	/**
	 * Generate the Game Data for the current game for storing in the DB.
	 * Refer the GamesDbAdapter for format details for individual game types.
	 * 
	 * @return String representing the game data
	 */
	protected abstract String generateGameData();
	
	/**
	 * Generate game data from the DB for starting a fresh game.
	 * This is called in the context of an AsyncTask as the expectation is
	 * that it can take a while to successfully generate a good game
	 * from the DB.
	 */
	protected abstract String generateGameDataFromDB();
	
	/**
	 * Handle the user Giving up the current game.
	 */
	protected abstract void doHandleGivenUp();
	
	/**
	 * Handle the user using a hint in the current game.
	 */
	protected abstract void doHandleHintUsed();
	
	/**
	 * Generic button clicked, other than the standard buttons common
	 * to all games.
	 * 
	 * @param id The button ID
	 */
	protected abstract void doHandleButtonClicked(int id);
	
	/****************** Member Variables **********************/
	
	/**
	 * Common protected members
	 */
	private WordJamApp mApp;
	protected WordDbAdapter mWordDbAdapter;
	protected GamesDbAdapter mGamesDbAdapter;
	private SetupGameAsyncTask mSetupGameAsyncTask;
	
	/**
	 * Play and Practice - common members
	 */
	protected boolean mIsPlay; //This game's part of a Play, not a practice
	protected int mPlayType; //The Play or Challenge type.  One of Constants.PLAY_TYPE_*
	protected int mGameType; //The type of game.  One of Constants.GAME_TYPE_*
	protected int mGameState; //Type state of the game.  One of Constants.GAME_STATE_*
	protected Class mGameClass; //The Class for this game activity
	protected int mGameNumber; //This Game's no.  May be -1 in case of "new" game.  Sent from calling activity.
	protected int mGameHintsRemaining; //No. of hints remaining in this game.  Calculated for new game, looked up for resumed game.
	protected int mGameHintsUsed; //No. of hints consumed so far in the game.
	protected int mGamePoints; //Total points earned in this game so far
	protected boolean mStopGame; //Indication to the dialog interface to stop the game as soon as the dialog returns
	protected boolean mIsNewGame; //This is a new game, not already saved in the DB
	
	/**
	 * Play related members
	 */
	protected int mLevel; //This game's level.  Send from calling activity.
	protected int mLevelPoints; //Total points earned in this level, including the current game (i.e., inclusive of mGamePoints)
	protected int mNumGamesInLevel; //Total no. of games in the level excluding the current game
	protected int mLevelState; //State of this level at this very point.  One of Constants.LEVEL_STATE_*
	protected int mLevelGamesRemaining; //No.o f games remaining to be completed at this level
	protected int mLevelHintsRemaining; //No. of hints remaining to be used at this level
	protected int mLevelHintsEarned; //No. of hints earned at this level
	
	/**
	 * Practice (non-Play) related members
	 */
	protected int mPracticeType; //Type of practice.  One of Constants.PRACTICE_TYPE_*
	protected int mPracticeGamesCompleted; //Total no. of games successfully completed in this practice session.  Sent from calling activity.
	protected int mPracticeTimeSpent; //Total time spent in this practice session, including the current game.  Passed along to next practice game and added up for the session.
	protected int mPracticeHintsUsed; //Total no. of hints used in this practice session.  Passed along to next practice game and added up for the session.
	protected int mPracticePointsEarned; //Total. no. of points earned in this practice session.  Passed along to next practice game and added up for the session.
	
	/**
	 * Other members used for storing/restoring/status/etc.
	 */
	protected String mGameData; //The entire dump of game data in a single string.  For formats, see GamesDbAdapter
	protected boolean mGameDirty; //The game was actually started
	protected long mGameTime; //Time duration of the game before it was stopped/paused

	protected static final String LOG_TAG = "BaseGameActivity: ";

	/**
	 * Used to manage Timer values.
	 */
	protected long mLastTimerValue; //The "current time" snapshot the last time the timer was invoked.

	/**
	 * UI Elements
	 */
	protected TextView mPointsTextView; // shows the current total points earned in this level (including for the current game)
	protected TextView mGameTimeTextView; // shows the time elapsed since the beginning of the game
	private ProgressDialog mProgressDialog; //shows while the async task is setting up the game
	
	/**
	 * The Various action buttons at the bottom of each game.  Names are self-explanatory
	 */
	protected ImageButton mStopButton, mPauseButton, mGiveUpButton, mHintButton, mHelpButton, mPreviousButton, mNextButton, mShareButton;
	
	/**
	 * Interstitial Ad related
	 */
	private InterstitialAd interstitial;
	private boolean mShowingInterstitialAd = false; // tracks whether the interstitial ad is showing
	private int mActionOnAdDismiss = 0; // action to be taken after the interstitial ad is dismissed
	private static long mTimeOfLastAd = 0; // absolute time at which the last interstitial ad was shown to the user
	private static final int MIN_INTERVAL_BETWEEN_ADS = 5 * 60 * 1000; // 5 minutes (in milliseconds)
	private static final int AD_DISMISS_ACTION_START_NEXT_GAME = 1;
	private static final int AD_DISMISS_ACTION_DISMISS_ACTIVITY = 2;
	
	/**
	 * Banner Ad related
	 */
	private AdView mBannerAdView; // the banner ad view that's shown at the bottom of the game view
	
	/**
	 * The timer that manages the game time.
	 */
	private Runnable mGameTimer;
	private Handler mGameTimerHandler;
	private static final int TIMER_REFRESH_RATE = 1000; // milliseconds after which timer is to be refreshed
	private long mGameTimeStart;
	
	/**
	 * This class implements the Timer that is required to measure and display
	 * the game time (time spent playing a game)
	 */
	private class GameTimer implements Runnable
	{
		/**
		 * Callback called when the timer goes off.  Called every TIMER_REFRESH_RATE
		 * milliseconds
		 */
		public void run()
		{
			long currentTime = System.currentTimeMillis();
			mGameTime = currentTime - mGameTimeStart;
			updateTimer();
			mGameTimerHandler.postDelayed(this, TIMER_REFRESH_RATE);	
			
			//increment timer value, and set practice time
			mPracticeTimeSpent += (currentTime - mLastTimerValue);
			mLastTimerValue = currentTime;
		}
	}
	
	private class SetupGameAsyncTask extends AsyncTask<Void, Void, String>
	{
		private Context mContext;
		
		SetupGameAsyncTask(Context context)
		{
			mContext = context;
		}
		
		protected void onPreExecute()
		{
			Utils.Log("onPreExecute");
			//setProgressBarIndeterminateVisibility(true);
			//mProgressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.setupdb_progress_message), getResources().getString(R.string.setup_game_progress_title));
			mProgressDialog = ProgressDialog.show(mContext,  "",  "");
		}
		
		protected String doInBackground(Void... types)
		{	
			Utils.Log("doInBackground");
			return generateGameDataFromDB();
		}
		
		protected void onProgressUpdate(Void... progress)
		{			
		}
		
		protected void onPostExecute(String result)
		{
			Utils.Log("onPostExecute, result = [" + result + "]");
			mGameData = result;
			mGameState = Constants.GAME_STATE_IN_PROGRESS;
			startTimer();
			doRestoreGame();
			loadBannerAd();
			refreshViews();
			
			//setProgressBarIndeterminateVisibility(false);
  			if (mProgressDialog.isShowing()) {
  				//Log.d(null, "################# dialog was showing, dismissing it");
  	            mProgressDialog.dismiss();
  	        }
		}
		
		protected void onCancelled()
		{
			mGameData = null;
		}
	}
	
}
