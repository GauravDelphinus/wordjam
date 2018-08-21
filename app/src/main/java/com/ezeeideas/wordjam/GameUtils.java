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

import java.util.Random;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class GameUtils 
{	
	public static int[][] sLevelValues = {
		/*	Total Points to Earn		Total Games To Complete			Total Hints Available				*/
			{	750,						30,								40	},   // Level 1
			{	750,						30,								40	},   // Level 2
			{	750,						30,								40	},   // Level 3
			{	750,						30,								40	},   // Level 4
			{	750,						30,								40	},   // Level 5
			{	750,						30,								40	}    // Level 6
	};
	
	// rows
	public static final int LEVEL1 = 0;
	public static final int LEVEL2 = 1;
	public static final int LEVEL3 = 2;
	public static final int LEVEL4 = 3;
	public static final int LEVEL5 = 4;
	public static final int LEVEL6 = 5;
	
	// columns
	public static final int LEVEL_TOTAL_POINTS = 0;
	public static final int LEVEL_TOTAL_GAMES = 1;
	public static final int LEVEL_TOTAL_HINTS = 2;
	
	private static int[][] sGameValuesLevel1 = {
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		10,				20,				10,			7,				15,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	// rows
	public static final int GAME_POINTS_ON_COMPLETION = 0;
	public static final int GAME_BONUS_POINTS_ON_FAST_COMPLETION = 1;
	public static final int GAME_FAST_COMPLETION_TIME = 2;
	public static final int GAME_POINTS_ON_GIVING_UP = 3;
	public static final int GAME_POINTS_ON_LOSING_GAME = 4;
	public static final int GAME_POINTS_ON_CORRECT_WORD_OR_LETTER = 5;
	public static final int GAME_POINTS_ON_INCORRECT_WORD_OR_LETTER = 6;
	public static final int GAME_POINTS_ON_SHARE_ON_FACEBOOK = 7;
	public static final int GAME_POINTS_ON_HINT_USED = 8;
	public static final int GAME_HINTS_TOTAL = 9;
	public static final int GAME_HINTS_ON_COMPLETION = 10;
	public static final int GAME_HINTS_ON_SHARE_ON_FACEBOOK = 11;
	
	// columns
	public static final int GAME_TYPE_RECALL_WORDS = 0;
	public static final int GAME_TYPE_MATCH_WORDS = 1;
	public static final int GAME_TYPE_HANGMAN = 2;
	public static final int GAME_TYPE_WORD_JUMBLE = 3;
	public static final int GAME_TYPE_WORD_SEARCH = 4;
	public static final int GAME_TYPE_CROSSWORD = 5;
	
	private static int[][] sGameValuesLevel2 = {
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		10,				20,				10,			7,				15,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sGameValuesLevel3 = {
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		10,				20,				10,			7,				15,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sGameValuesLevel4 = {
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		10,				20,				10,			7,				15,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sGameValuesLevel5 = {
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		10,				20,				10,			7,				15,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sGameValuesLevel6 = {
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		10,				20,				10,			7,				15,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sPracticeValuesType1 = { /* VERY EASY */
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		5,				10,				10,			10,				45,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sPracticeValuesType2 = { /* MEDIUM */
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		5,				10,				10,			10,				45,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sPracticeValuesType3 = { /* HARD */
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		5,				10,				10,			10,				45,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static int[][] sPracticeValuesType4 = { /* VERY HARD */
		/* Level 1 */
		/* Recall Words		Match Words		Hangman		Word Jumble		Word Search		Crossword		*/
		{		20,				50,				40,			30,				70,				100	}, // Points on Game Completion
		{		10,				20,				10,			10,				20,				30	}, // Bonus Points on fast completion
		{		5,				10,				10,			10,				45,				120	}, // Fast Completion Time (seconds)
		{		-5,				-5,				-5,			-5,				-5,				-5	}, // Points on Giving Up
		{		0,				0,				0,			0,				0,				0	}, // Points on Lost Game (after trying)
		{		0,				10,				5,			5,				5,				5	}, // Points on Correct Word/Letter
		{		0,				-2,				0,			0,				0,				-2	}, // Points on Incorrect Word/Letter
		{		0,				0,				0,			0,				0,				0	}, // Points on Share on Facebook
		{		0,				0,				0,			0,				0,				0	}, // Points on Hint used
		{		2,				2,				2,			2,				3,				4	}, // Max Hints
		{		1,				1,				1,			1,				2,				4	}, // Hints on Game Completion
		{		5,				5,				5,			5,				5,				5	}, // Hints on Share on Facebook
	};
	
	private static Vector<int[][]> sGameValues; //point value chart for the Games
	private static Vector<int[][]> sPracticeValues; // point value chart for the Practice Games
	
	/**
	 * Get the Point Value table for the given type of game
	 * 
	 * @param playType One of Constants.PLAY_TYPE_*
	 * @param isPlay true if this is a Play, false if it is a practice
	 * @param gameLevel The level of the game, only used in case of play
	 * @param practiceLevel The difficult level of the practice game, only used in case of practice
	 * @return 2d Integer array containing the point chart
	 */
	public static int[][] getPointChart(int playType, boolean isPlay, int gameLevel, int practiceLevel)
	{
		if (isPlay)
		{
			return sGameValues.elementAt(gameLevel - 1);
		}
		else
		{
			return sPracticeValues.elementAt(practiceLevel - 1);
		}
	}
	
	public static void initialize(Context context)
	{
		if (!initialized)
		{
			sGameValues = new Vector<int[][]>();
			
			sGameValues.add(sGameValuesLevel1);
			sGameValues.add(sGameValuesLevel2);
			sGameValues.add(sGameValuesLevel3);
			sGameValues.add(sGameValuesLevel4);
			sGameValues.add(sGameValuesLevel5);
			sGameValues.add(sGameValuesLevel6);
			
			sPracticeValues = new Vector<int[][]>();
			
			sPracticeValues.add(sPracticeValuesType1);
			sPracticeValues.add(sPracticeValuesType2);
			sPracticeValues.add(sPracticeValuesType3);
			sPracticeValues.add(sPracticeValuesType4);
			
			initialized = true;
		}
	}
	
	/**
	 * Return the legal name for the Play Type
	 * 
	 * @param playType One of Constants.PLAY_TYPE_*
	 * 
	 * @return The string representing the legal name of the play type
	 */
	public static String getPlayTypeName(int playType, Context context)
	{
		Utils.Log("getPlayTypeName, mContext = " + context);
		switch (playType)
		{
			case Constants.PLAY_TYPE_ENGLISH_CLASSIC: return context.getResources().getString(R.string.english_challenge_label);
			case Constants.PLAY_TYPE_GRE: return context.getResources().getString(R.string.gre_challenge_label);
			case Constants.PLAY_TYPE_SAT: return context.getResources().getString(R.string.sat_challenge_label);
			case Constants.PLAY_TYPE_KIDS: return context.getResources().getString(R.string.kids_challenge_label);
			default: break;
		}
		
		return null;
	}
	
	public static String getPracticeTypeName(int practiceType, Context context)
	{
		Utils.Log("getPracticeTypeName, mContext = " + context);
		switch (practiceType)
		{
		case Constants.PRACTICE_TYPE_EASY: return context.getResources().getString(R.string.practice_type_easy);
		case Constants.PRACTICE_TYPE_MEDIUM: return context.getResources().getString(R.string.practice_type_medium);
		case Constants.PRACTICE_TYPE_HARD: return context.getResources().getString(R.string.practice_type_hard);
		case Constants.PRACTICE_TYPE_VERY_HARD: return context.getResources().getString(R.string.practice_type_very_hard);
		default:break;
		}
		
		return null;
	}

	/**
	 * Prepare the intent with values required to start game
	 * 
	 * @param i the intent
	 * @param isPlay true if this is a Play, else false in case of a Practice
	 * @param gameNum the game number to start.  -1 indicates start a new game
	 * @param playType one of Constants.PLAY_TYPE_*
	 * @param level the level
	 * @param practiceType one of Constants.PRACTICE_TYPE_*
	 */
	public static void prepareIntentExtras(Intent i, boolean isPlay, int gameNum, int playType, int level, int practiceType)
	{
    	//Fill in the required values
    	i.putExtra(Constants.KEY_IS_PLAY, isPlay);
    	i.putExtra(Constants.KEY_GAME_NUM, gameNum);
    	i.putExtra(Constants.KEY_PLAY_TYPE, playType);
    	
    	if (isPlay)
    	{
    		i.putExtra(Constants.KEY_LEVEL, level);
    	}
    	else
    	{
    		i.putExtra(Constants.KEY_PRACTICE_TYPE, practiceType);
    	}
	}
	
	/**
	 * Get a randomly selected game type.
	 * 
	 * @return One of Constants.GAME_TYPE_*
	 */
	public static int getRandomGameType()
	{
		Random rand = new Random();
		int i = rand.nextInt(6) + 1; // will return values between 1 - 6
		
		switch (i)
		{
		case 1:
			return Constants.GAME_TYPE_RECALL_WORDS;
		case 2:
			return Constants.GAME_TYPE_MATCH_WORDS;
		case 3:
			return Constants.GAME_TYPE_HANGMAN;
		case 4:
			return Constants.GAME_TYPE_WORD_JUMBLE;
		case 5:
			return Constants.GAME_TYPE_WORD_SEARCH;
		case 6:
			return Constants.GAME_TYPE_CROSSWORD;
		default:
			break;	
		}
		
		return -1;
	}
	
	/**
	 * Get the Class for the main activity corresponding to the given game type.
	 * 
	 * @param gameType One of Constants.GAME_TYPE_*
	 * 
	 * @return The Activity Class
	 */
	public static Class getGameClassForGameType(int gameType)
	{
		switch (gameType)
		{
		case Constants.GAME_TYPE_RECALL_WORDS: return RecallWordsGameActivity.class;
		case Constants.GAME_TYPE_MATCH_WORDS: return MatchWordsGameActivity.class;
		case Constants.GAME_TYPE_HANGMAN: return HangmanGameActivity.class;
		case Constants.GAME_TYPE_WORD_JUMBLE: return WordJumbleGameActivity.class;
		case Constants.GAME_TYPE_WORD_SEARCH: return WordSearchGameActivity.class;
		case Constants.GAME_TYPE_CROSSWORD: return CrosswordGameActivity.class;
		default: break;
		}
		
		return null;
	}
	
	public static String getGameNameFromGameType(int gameType, Context context)
	{
		switch (gameType)
		{
		case Constants.GAME_TYPE_RECALL_WORDS: return context.getResources().getString(R.string.recall_words_name);
		case Constants.GAME_TYPE_MATCH_WORDS: return context.getResources().getString(R.string.match_words_name);
		case Constants.GAME_TYPE_HANGMAN: return context.getResources().getString(R.string.hangman_name);
		case Constants.GAME_TYPE_WORD_JUMBLE: return context.getResources().getString(R.string.word_jumble_name);
		case Constants.GAME_TYPE_WORD_SEARCH: return context.getResources().getString(R.string.word_search_name);
		case Constants.GAME_TYPE_CROSSWORD: return context.getResources().getString(R.string.crossword_name);
		default: break;
		}
		
		return null;
	}
	
	/**
	 * Calculate the overall level progress by using some formula that returns
	 * a percentage progress.
	 * 
	 * @param levelInfo Structure containing all required level information
	 * @return The percent value of the progress
	 */
	public static double getLevelProgress(LevelInfo levelInfo)
	{		
		if (levelInfo.levelGamesRemaining <= 0 && levelInfo.levelPointsRemaining <= 0)
		{
			return 100;
		}
		
		double gameProgress = (1.0 * (levelInfo.levelTotalGames - levelInfo.levelGamesRemaining)) / levelInfo.levelTotalGames;
		double pointsProgress = (1.0 * (levelInfo.levelTotalPoints - levelInfo.levelPointsRemaining)) / levelInfo.levelTotalPoints;
	
		Utils.Log("gameProgress = " + gameProgress + ", pointsProgress = " + pointsProgress);
		double progress = (gameProgress * pointsProgress) * 100;
		
		return progress;
	}
	
    
	// Set the current theme to what user has chosen
	public static void setWordDifficultyPreference(int wordDifficulty, Context context)
	{	
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("word_difficulty", wordDifficulty);

		// Commit the edits!
		editor.commit();	
	}
	
	public static int getWordDifficultyPreference(Context context)
	{
		// Restore preferences
	    SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
	    int wordDifficulty = settings.getInt(Constants.PREF_WORD_DIFFICULTY, Constants.PRACTICE_TYPE_DEFAULT);
	    
	    return wordDifficulty;
	}
	
	/**
	 * LevelInfo - Structure that holds all points and hints information
	 * about this level
	 */
	public static class LevelInfo
	{
		public int levelNum;
		public int levelTotalPoints;
		public int levelPointsRemaining;
		public int levelTotalGames;
		public int levelGamesRemaining;
		public int levelTotalHints;
		public int levelHintsRemaining;
		public int levelHintsEarned;
		public int levelTimeSpent;
	};
	
	/**
	 * PracticeInfo - Structure that holds all points and hints information
	 * related to this practice session
	 */
	public static class PracticeInfo
	{
		public int practiceType; //One of Constants.PRACTICE_TYPE_*
		public int practiceGamesCompleted; //total no. of games completed in this session
		public int practiceTimeSpent; //total time spent in this session
		public int practiceHintsUsed; //total no. of hints used in this session
		public int practicePointsEarned; //total no. of points earned in this session
	}
	
	private static boolean initialized = false;
	//private static Context mContext; // the application context
}
