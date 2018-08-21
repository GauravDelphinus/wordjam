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

package com.ezeeideas.wordjam.db;

import android.database.Cursor;
import android.database.SQLException;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.util.ArrayList;

import com.ezeeideas.wordjam.Constants;
import com.ezeeideas.wordjam.GameUtils;
import com.ezeeideas.wordjam.AdvancedGameSelectionActivity;
import com.ezeeideas.wordjam.Utils;
import com.ezeeideas.wordjam.WordDbAdapter;

public class GamesDbAdapter {

	private static final String GAMES_DB_NAME = "games.db";
	private static final int GAMES_DB_VERSION = 1;
	private static String DATABASE_FULL_PATH;
	
	/**
	 * Table: english_games
	 * 
	 * This table stores all game data for the English Mega Challenge.
	 */
	//Database Tables
	
	private static final String TABLE_ENGLISH_GAMES = "english_games";
	public static final String KEY_GAME_ROWID = "_id";
	public static final String KEY_GAME_LEVEL = Constants.KEY_GAME_LEVEL; // the game level
	public static final String KEY_GAME_NUM = Constants.KEY_GAME_NUM; // the game number
	public static final String KEY_GAME_TYPE = Constants.KEY_GAME_TYPE; // the game type. One of Constants.GAME_TYPE_*
	public static final String KEY_GAME_STATE = Constants.KEY_GAME_STATE; // the state of the game.  One of Constants.GAME_STATE_*
	public static final String KEY_GAME_POINTS = Constants.KEY_GAME_POINTS; // points earned in this particular game so far
	public static final String KEY_GAME_HINTS_REMAINING = Constants.KEY_GAME_HINTS_REMAINING; // hints remaining in this particular game
	public static final String KEY_GAME_DATA = Constants.KEY_GAME_DATA; // saved game data, see details below
	public static final String KEY_GAME_LAST_PLAYED = Constants.KEY_GAME_LAST_PLAYED; // last played time of this game
	public static final String KEY_GAME_DURATION = Constants.KEY_GAME_DURATION; //time spent in the game so far
	/**
	 * Saved Game data formats:
	 * 
	 * Recall Words / Word Meaning Test:
	 * 
	 * @WORD@<word>@MEANING@<meaning>@WORD2MEANING@<0 or 1>@CHOICES#<num choices>@WORDS>@<word1>@<word2>@MEANINGS@<meaning1>@<meaning2>@STATES@<0 or 1>@<0 or 1>@ 
	 * 
	 * Match Words:
	 * 
	 * @WORDS#<word1>%<matchign meaning>%<chosen meaning index>#<word2>%<matchign meaning>%<chosen meaning index>@MEANINGS@<meaning1>#<meaning2>@
	 * 		FOUNDWORDS#<index1>#<index2>@
	 * 
	 * - Words are listed in the order of visual appearance in the UI (top to bottom)
	 * - Meanings are listed the order of visual appearance in the UI (top to bottom)
	 * - Next to each word are 2 numbers: First is the matching meaning index (visual order), and the second is the index to the meaning button the user has 'chosen' to match this word with, but is actually not the right meaning. this number is absent in case this word has been paired yet.
	 * - The FOUNDWORDS list contains the indexes to the words that are correctly found by the user
	 * - No. of words is same as no. of meanings
	 * 
	 * Hangman:
	 * 
	 * @WORD@<word>@MEANING@<meaning>@KEYCHARS@<26 digits either 0 or 1 depending on whether that char has been already used or not, 0 means used>@
	 * 
	 * - The keychars are nothing but the toggle state of each key depending on whether it has been used up already or not
	 * - The actual word and hangman's current state isn't explicitly mentioned in the format as that can be derived from the keystates above
	 * 
	 * Word Jumble:
	 * 
	 * @WORD@<word>@MEANING@<meaning>@JUMBLEDWORD@<jumbled word with '#'s in places of used/removed letters>@UNJUMBLEDWORD@<unjumbled word wiht '#'s in places of unfilled letters>@
	 * 
	 * - The jumbled word is of full word length, with '#' in place of already used up letters
	 * - The unjumbled word is of full word length, with '#' in place of non-filled letters
	 * 
	 * Word Search:
	 * 
	 * @ORDER@<order of matrix>@WORDTYPE@<word type>@WORDS#<no. of words>@<word1>@<word2>@WORDMAPPINGLIST#<size of list>@<index>#<direction>#<row>#<col>@<index>#<direction>#<row>#<col>
	 * 		@FOUNDWORDSLIST#<size of list>@<index1>@<index2>@CURRENTGRID@<char><char><char>@<char><char><char>@
	 * 
	 * - There are no meanings stored as meanings aren't shown in Word Search
	 * - The mFoundGrid isn't stored as it can be computed from mCurrentGrid, mWords and mFoundWordsList
	 * - WORDMAPPINGLIST: same mapping as mWordMappingList in WordSearchTest class
	 * - FOUNDWORDSLIST: index into the WORDMAPPINGLIST
	 * 
	 * Crossword:
	 * 
	 * @ORDER@<order of matrix>@WORDS#<no. of words>@<word1>@<word2>@MEANINGS@<meaning1>@<meaning2>@ACROSSLIST#<size>@<0>@<1>@<2>@<3>@<4>@<0>@<1>@<2>@<3>@<4>@DOWNLIST#<size>@<0>@<1>@<2>@<3>@<4>
	 * 		@FOUNDWORDSLIST#<size>@<listtype><index>@<listtype><index>@CURRENTMATRIX@<char><#><#><char>@<char><#><#><char>@<char><#><#><char>@
	 * 
	 * - The meanings list size is not required as it's the same as the word list size
	 * - ACROSSLIST: 5 integral values, refer mAcrossList in CrosswordTest class
	 * - DOWNLIST: similar to ACROSSLIST
	 * - FOUNDWORDSLIST: each item has two integers: listtype: 0 for Across, 1 for Down, index: Index into the corresponding list (ACROSSLIST/DOWNLIST)
	 * - CURRENTMATRIX: '#' represent unfillable cells (black)
	 */
	
	/*
	 * We shouldn't really need these any more
	 */
	//public static final String KEY_GIVEN_TEXT = "givenText";
	//public static final String KEY_WORD = "word";
	//public static final String KEY_MEANING = "meaning";
	//public static final String KEY_NUM_HINTS_USED = "numHintsUsed";

	/**
	 * Table: english_levels
	 * 
	 * This table stores all the global level data for the English Mega Challenge
	 * Note that this table does not store things that are fixed values, such as total hints or total points associated
	 * with a given level.  Those values are hard coded in the app in GameUtils.java
	 */
	
	private static final String TABLE_ENGLISH_LEVELS = "english_levels";
	public static final String KEY_LEVEL_ROWID = "_rowid";
	public static final String KEY_LEVEL_NUMBER = Constants.KEY_LEVEL;
	public static final String KEY_LEVEL_STATE = Constants.KEY_LEVEL_STATE; //one of Constants.LEVEL_STATE_*
	public static final String KEY_LEVEL_HINTS_REMAINING = Constants.KEY_LEVEL_HINTS_REMAINING; //no. of hints remaining in the level
	public static final String KEY_LEVEL_HINTS_EARNED = Constants.KEY_LEVEL_HINTS_EARNED; //no. of hints earned in this level
	public static final String KEY_LEVEL_GAMES_REMAINING = Constants.KEY_LEVEL_GAMES_REMAINING; // no. of games remaining in the level to be completed
	
	//SQLiteMaster Table, and its column keys
	private static final String TABLE_SQLITE_MASTER = "sqlite_master";
	public static final String KEY_SQLITE_NAME = "name";
	public static final String KEY_SQLITE_TYPE = "type";
	
	private Context context;
	private SQLiteDatabase database;
	private GamesDbHelper dbHelper;
	
	/**
	 * Game Short Info required mostly when listing buttons that lead to games.
	 */
	public class GameShortInfo
	{
		public GameShortInfo() {}
		public int gameNum;
		public int gameType;
		public int gameState;
		public int gamePointsEarned;
	}
	
	/**
	 * Game Long Info containing remaining fields required to save or load a game
	 * to/from DB.
	 */
	public class GameLongInfo
	{
		public GameLongInfo() {}
		public int gameLevel;
		public int gameHintsRemaining;
		public String gameData;
		public long gameLastPlayed;
		public long gameDuration;
	}
	
	/**
	 * Game Full Info
	 */
	public class GameInfo
	{
		public GameInfo() {}
		public GameShortInfo gameShortInfo;
		public GameLongInfo gameLongInfo;
	}
	
	/**
	 * Level Info that contains all level information required to save/load a game
	 * to/from DB.
	 */
	public class LevelInfo
	{
		public LevelInfo() {}
		public int levelNum;
		public int levelState;
		public int levelHintsRemaining;
		public int levelHintsEarned;
		public int levelGamesRemaining;
	}
	
	public GamesDbAdapter(Context context)
	{
		this.context = context;
		DATABASE_FULL_PATH = context.getDatabasePath(GAMES_DB_NAME).getAbsolutePath();
		
		dbHelper = new GamesDbHelper(context, false, GAMES_DB_NAME, GAMES_DB_VERSION);
	}
	
	public boolean isSetUp()
	{
		if (database != null && database.isOpen())
			return true;
		
		return false;
	}
	
	public boolean open_dontuse() throws SQLException
	{
		Utils.Log("gaurav - GamesDbAdapter.open");
		if (database != null && database.isOpen())
			return true;
		
		//Get the database
		database = dbHelper.getWritableDatabase();
		
		setup();
		
		//return true if DB exists
		return database.isOpen();
	}

	public boolean setup()
	{
		SQLiteDatabase checkDB = null;
		Boolean dbExists = true;
		try {
			checkDB = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
			
			checkDB.close();
		}
		catch (SQLiteException e)
		{
			Utils.Log("games db adatpter, exception = " + e);
			//this could be a SQLiteDatabaseCorruptException, and we should just delete the DB file
			//if it exists so we can do a clean sweep the next time
			File newDBFile = new File(DATABASE_FULL_PATH);
			newDBFile.delete();
			
			dbExists = false;
		}
		
		//Get the database
		database = dbHelper.getWritableDatabase();
		
		// set up tables for the very first time, if they don't already exist
		setupEnglishTables(database);
		
		return true;
	}
	
	private void setupEnglishTables(SQLiteDatabase database)
	{
		// Set up the Levels Table with default values to begin with
	    String CHECK_LEVELS_TABLE = "SELECT " + KEY_SQLITE_NAME + " FROM " + TABLE_SQLITE_MASTER + " WHERE " + KEY_SQLITE_TYPE + " ='table' AND " + KEY_SQLITE_NAME + "='" + TABLE_ENGLISH_LEVELS + "'";

		Cursor cursor = database.rawQuery(CHECK_LEVELS_TABLE, null);
		if (cursor.getCount() <= 0)
		{
			//create table
			String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ENGLISH_LEVELS + " (" + KEY_LEVEL_ROWID + " INTEGER PRIMARY KEY, " + KEY_LEVEL_NUMBER + " INTEGER, " + KEY_LEVEL_STATE + " INTEGER, " + KEY_LEVEL_HINTS_REMAINING + " INTEGER, " + KEY_LEVEL_HINTS_EARNED + " INTEGER, " + KEY_LEVEL_GAMES_REMAINING + " INTEGER)";
			database.execSQL(QUERY_CREATE_TABLE);
			
			//Insert default entries for levels
			String INSERT_LEVEL_ROW = "INSERT INTO " + TABLE_ENGLISH_LEVELS + " (" + KEY_LEVEL_NUMBER + "," + KEY_LEVEL_STATE + "," + KEY_LEVEL_HINTS_REMAINING + "," + KEY_LEVEL_HINTS_EARNED + "," + KEY_LEVEL_GAMES_REMAINING + ") " +
							" VALUES (" + 1 + "," + Constants.LEVEL_STATE_NOT_STARTED + "," + GameUtils.sLevelValues[GameUtils.LEVEL1][GameUtils.LEVEL_TOTAL_HINTS] + ",0," + GameUtils.sLevelValues[GameUtils.LEVEL1][GameUtils.LEVEL_TOTAL_GAMES] + ")";
			database.execSQL(INSERT_LEVEL_ROW);
		}
		
		// Set up the Games Table with default values to begin with
	    String CHECK_GAMES_TABLE = "SELECT " + KEY_SQLITE_NAME + " FROM " + TABLE_SQLITE_MASTER + " WHERE " + KEY_SQLITE_TYPE + " ='table' AND " + KEY_SQLITE_NAME + "='" + TABLE_ENGLISH_GAMES + "'";

		cursor = database.rawQuery(CHECK_GAMES_TABLE, null);
		if (cursor.getCount() <= 0)
		{
			//create table
			String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ENGLISH_GAMES + " (" + KEY_GAME_ROWID + " INTEGER PRIMARY KEY, " + KEY_GAME_LEVEL + " INTEGER, " + KEY_GAME_NUM + " INTEGER, " + KEY_GAME_TYPE + " INTEGER, " + KEY_GAME_STATE + " INTEGER, " + KEY_GAME_POINTS + " INTEGER, " + KEY_GAME_HINTS_REMAINING + " INTEGER, " + KEY_GAME_DATA + " TEXT, " + KEY_GAME_LAST_PLAYED + " TEXT, " + KEY_GAME_DURATION + " TEXT)";
			database.execSQL(QUERY_CREATE_TABLE);
		}
		
		cursor.close();
	}
	
	public void close()
	{
		dbHelper.close();
		if (database != null && database.isOpen())
		{
			database.endTransaction();
			database.close();
			database = null;
		}
		
	}
	
	public void create()
	{
		//Get the database, and build the cache
		database = dbHelper.getWritableDatabase();
	}
	
	public GameShortInfo fetchLastResumableGame(int playType, int level)
	{
		GameShortInfo gameShortInfo = null;
		
		String query = "SELECT " + KEY_GAME_NUM + "," + KEY_GAME_TYPE + "," + KEY_GAME_STATE + "," + KEY_GAME_POINTS + " from " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_STATE + " ='" + Constants.GAME_STATE_IN_PROGRESS + "' AND " + KEY_GAME_LEVEL + " = '" + level + "' ORDER BY " + KEY_GAME_LAST_PLAYED + " DESC LIMIT 1";
		Cursor cursor = database.rawQuery(query, null);
		if (cursor.getCount() > 0)
		{
			gameShortInfo = new GameShortInfo();
			
			cursor.moveToFirst();
			gameShortInfo.gameNum = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.KEY_GAME_NUM));
			gameShortInfo.gameType = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.KEY_GAME_TYPE));
			gameShortInfo.gameState = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.KEY_GAME_STATE));
			gameShortInfo.gamePointsEarned = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.KEY_GAME_POINTS));
		}
		cursor.close();
		
		return gameShortInfo;
	}
	
	public ArrayList<Integer> fetchLastFewGamesDontUse(int level, int testType)
	{
		ArrayList<Integer> lastGames = new ArrayList<Integer>();
		/*
		//String tableName = GameSelectionActivity.getTableName(context, testType);
		
		String query = "SELECT " + KEY_GAME_NUM + " from " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_STATE + " IN (" + GameSelectionActivity.TEST_STATE_NOT_STARTED + "," + GameSelectionActivity.TEST_STATE_IN_PROGRESS + ") AND " + KEY_GAME_LEVEL + " = '" + level + "' ORDER BY " + KEY_GAME_LAST_PLAYED + " DESC LIMIT 4";
		Utils.Log("gaurav - fetchLastGame query [" + query + "]");
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		Utils.Log("gaurav - fetchLastFewGames, cursor.getCount() is " + cursor.getCount());
		if (cursor.getCount() > 0)
		{
			while (!cursor.isAfterLast())
			{
				lastGames.add(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_NUM)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		Utils.Log("gaurav - fetchLastFewGames returned " + lastGames.size());
		*/
		return lastGames;
	}
	
	/**
	 * Fetch all saved games in this level.  Extract the GameShortInfo structure, which contains information
	 * required to navigate to that game.  E.g., from the 'All Games' view.
	 * 
	 * @param level The level being queried
	 * 
	 * @return An ArrayList of all games fetched
	 */
	public ArrayList<GameShortInfo> fetchAllSavedGames(int level)
	{	
		String query = "SELECT " + KEY_GAME_NUM + ", " + KEY_GAME_STATE + ", " + KEY_GAME_TYPE + ", " + KEY_GAME_POINTS + " from " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_LEVEL + " = '" + level + "' ORDER BY " + KEY_GAME_NUM + "";
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		ArrayList<GameShortInfo> gameList = new ArrayList<GameShortInfo>();
		if (cursor.getCount() > 0)
		{
			while (!cursor.isAfterLast())
			{
				GameShortInfo info = new GameShortInfo();
				info.gameNum = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_NUM));
				info.gameState = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_STATE));
				info.gameType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_TYPE));
				info.gamePointsEarned = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_POINTS));
				
				gameList.add(info);
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		return gameList;
	}
	
	/**
	 * Returns the state of the given level in the given challenge type.
	 * 
	 * @param playType One of Constants.PLAY_TYPE_*
	 * @param level The Level Number
	 * 
	 * @return One of Constants.LEVEL_STATE_*
	 */
	public int getLevelState(int playType, int level)
	{
		int state = Constants.LEVEL_STATE_NOT_STARTED;
		
	    String QUERY_GET_MATCHING_LEVELS = "SELECT " + KEY_LEVEL_STATE +" FROM " + TABLE_ENGLISH_LEVELS + " WHERE " + KEY_LEVEL_NUMBER + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_LEVELS, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			state = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL_STATE));
		}
		
		cursor.close();
		
		return state;
	}
	
	/**
	 * Returns the no. of earned points in a given level for a specific challenge.
	 * 
	 * @param playType One of Constants.PLAY_TYPE_*
	 * @param level The Level Number
	 * 
	 * @return Current Earned Points
	 * 
	 * @note Total Points in a level are burned into the app, in GameUtils
	 */
	public int getEarnedPoints(int playType, int level)
	{
		int earnedPoints = 0;
		
	    String QUERY_SUM_OF_EARNED_POINTS = "SELECT SUM(" + KEY_GAME_POINTS +") AS " + KEY_GAME_POINTS + " FROM " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_LEVEL + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_SUM_OF_EARNED_POINTS, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			earnedPoints = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_POINTS));
		}
		
		cursor.close();
		
		return earnedPoints;
	}
	
	/**
	 * Returns the no. of games already played in this level for this specific challenge.
	 * 
	 * @param playType The challenge type.  One of Constants.PLAY_TYPE_*
	 * @param level The level.
	 * @return the no. of games already played.
	 */
	public int getNumGames(int playType, int level)
	{
		int numGames = 0;
		
	    String QUERY_NUM_GAMES = "SELECT COUNT(" + KEY_GAME_NUM +") FROM " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_LEVEL + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_NUM_GAMES, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			numGames = cursor.getInt(0);
		}
		
		cursor.close();
		
		return numGames;
	}
	
	/**
	 * Returns the no. of games already completed in this level for this specific challenge.
	 * 
	 * @param playType The challenge type.  One of Constants.PLAY_TYPE_*
	 * @param level The level.
	 * @return the no. of games already completed.
	 */
	public int getNumGamesCompleted(int playType, int level)
	{
		int numGames = 0;
		
	    String QUERY_NUM_GAMES = "SELECT COUNT(" + KEY_GAME_NUM +") FROM " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_LEVEL + " = '" + level + "' AND " + KEY_GAME_STATE + " = '" + Constants.GAME_STATE_COMPLETED + "'";

		Cursor cursor = database.rawQuery(QUERY_NUM_GAMES, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			numGames = cursor.getInt(0);
		}
		
		cursor.close();
		
		return numGames;
	}
	
	/**
	 * Get the number of games remaining to be completed at this level.
	 * 
	 * @param playType Challenge type.  One of Constants.PLAY_TYPE_*
	 * @param level level no.
	 * 
	 * @return Number of games remaining at this level
	 */
	public int getNumGamesRemaining(int playType, int level)
	{
		int gamesRemaining = GameUtils.sLevelValues[level - 1][GameUtils.LEVEL_TOTAL_GAMES];
		
	    String QUERY_GET_MATCHING_LEVELS = "SELECT " + KEY_LEVEL_GAMES_REMAINING +" FROM " + TABLE_ENGLISH_LEVELS + " WHERE " + KEY_LEVEL_NUMBER + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_LEVELS, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			gamesRemaining = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL_GAMES_REMAINING));
		}
		
		cursor.close();
		
		return gamesRemaining;
	}
	
	/**
	 * Get the number of hints remaining/available at thi slevel
	 * 
	 * @param playType Challenge type.  One of Constants.PLAY_TYPE_*
	 * @param level level no.
	 * 
	 * @return Number of hints remaining at this level
	 */
	public int getNumHintsRemaining(int playType, int level)
	{
		int hintsRemaining = GameUtils.sLevelValues[level - 1][GameUtils.LEVEL_TOTAL_HINTS];
		
	    String QUERY_GET_MATCHING_LEVELS = "SELECT " + KEY_LEVEL_HINTS_REMAINING +" FROM " + TABLE_ENGLISH_LEVELS + " WHERE " + KEY_LEVEL_NUMBER + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_LEVELS, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			hintsRemaining = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL_HINTS_REMAINING));
		}
		
		cursor.close();
		
		return hintsRemaining;
	}
	
	public int getNumHintsEarned(int playType, int level)
	{
		int hintsEarned = 0;
		
	    String QUERY_GET_MATCHING_LEVELS = "SELECT " + KEY_LEVEL_HINTS_EARNED +" FROM " + TABLE_ENGLISH_LEVELS + " WHERE " + KEY_LEVEL_NUMBER + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_LEVELS, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			hintsEarned = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL_HINTS_EARNED));
		}
		
		cursor.close();
		
		return hintsEarned;
	}
	
	private boolean gameTableContainsRow(int playType, int level, int gameNumber)
	{
		int count = 0;
		String query = "SELECT " + KEY_GAME_NUM + " FROM " + TABLE_ENGLISH_GAMES + " WHERE ( " + KEY_GAME_NUM + " = '" + gameNumber + "' AND " + KEY_GAME_LEVEL + " = '" + level + "')";
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		count = cursor.getCount();
		cursor.close();
		
		return (count > 0);
	}
	
	public int getGameTypeForGame(int playType, int level, int gameNumber)
	{
		int gameType = -1;
		String query = "SELECT " + KEY_GAME_TYPE + " FROM " + TABLE_ENGLISH_GAMES + " WHERE ( " + KEY_GAME_NUM + " = '" + gameNumber + "' AND " + KEY_GAME_LEVEL + " = '" + level + "')";
		Cursor cursor = database.rawQuery(query, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			gameType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_TYPE));
		}
		cursor.close();
		
		return gameType;
	}
	
	private boolean levelTableContainsRow(int playType, int level)
	{
		int count = 0;
		String query = "SELECT " + KEY_LEVEL_NUMBER + " FROM " + TABLE_ENGLISH_LEVELS + " WHERE " + KEY_LEVEL_NUMBER + " = '" + level + "'";
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		count = cursor.getCount();
		cursor.close();
		
		return (count > 0);
	}
	
	/**
	 * Add or Update game data to the DB.
	 * 
	 * @param playType Challenge type.  One of Constants.PLAY_TYPE_*
	 * @param gameShortInfo Game Short Information (refer class description)
	 * @param gameLongInfo Game Long Information (refer class description)
	 */
	public void addOrUpdateGameData(int playType, GamesDbAdapter.GameShortInfo gameShortInfo, GamesDbAdapter.GameLongInfo gameLongInfo)
	{
		if (gameTableContainsRow(playType, gameLongInfo.gameLevel, gameShortInfo.gameNum))
		{
			String QUERY_UPDATE_ROW = "UPDATE " + TABLE_ENGLISH_GAMES + " SET " + KEY_GAME_LEVEL + "='" + gameLongInfo.gameLevel + "', " + KEY_GAME_TYPE + "='" + gameShortInfo.gameType + "', " + KEY_GAME_STATE + "='" + gameShortInfo.gameState + "', " + KEY_GAME_POINTS + "='" + gameShortInfo.gamePointsEarned + "', " + KEY_GAME_HINTS_REMAINING + "='" + gameLongInfo.gameHintsRemaining + "', " + KEY_GAME_DATA + "='" + gameLongInfo.gameData + "', " + KEY_GAME_LAST_PLAYED + "='" + gameLongInfo.gameLastPlayed + "', " + KEY_GAME_DURATION + "='" + gameLongInfo.gameDuration + "' WHERE " + KEY_GAME_NUM + "='" + gameShortInfo.gameNum + "'";
			database.execSQL(QUERY_UPDATE_ROW);
			Utils.Log("addOrUpdateGameData, running query [" + QUERY_UPDATE_ROW + "]");
		}
		else
		{
			String QUERY_INSERT_INTO_TABLE = "INSERT INTO " + TABLE_ENGLISH_GAMES + " (" + KEY_GAME_LEVEL + ", " + KEY_GAME_NUM + ", " + KEY_GAME_TYPE + ", " + KEY_GAME_STATE + ", " + KEY_GAME_POINTS + ", " + KEY_GAME_HINTS_REMAINING + ", " + KEY_GAME_DATA + ", " + KEY_GAME_LAST_PLAYED + ", " + KEY_GAME_DURATION + ") VALUES ('" + 
					gameLongInfo.gameLevel + "', '" + gameShortInfo.gameNum + "', '" + gameShortInfo.gameType + "', '" + gameShortInfo.gameState + "', '" + gameShortInfo.gamePointsEarned + "', '" + gameLongInfo.gameHintsRemaining + "', '" + gameLongInfo.gameData + "', '" + gameLongInfo.gameLastPlayed + "', '" + gameLongInfo.gameDuration + "')";
			database.execSQL(QUERY_INSERT_INTO_TABLE);
			Utils.Log("addOrUpdateGameData, running query [" + QUERY_INSERT_INTO_TABLE + "]");
		}
	}
	
	/**
	 * Add or Update level data to the DB.
	 * 
	 * @param playType Challenge type.  One of Constants.PLAY_TYPE_*
	 * @param levelInfo Level Information (refer class description)
	 */
	public void addOrUpdateLevelData(int playType, GamesDbAdapter.LevelInfo levelInfo)
	{
		if (levelTableContainsRow(playType, levelInfo.levelNum))
		{
			String QUERY_UPDATE_ROW = "UPDATE " + TABLE_ENGLISH_LEVELS + " SET " + KEY_LEVEL_STATE + "='" + levelInfo.levelState + "', " + KEY_LEVEL_HINTS_REMAINING + "='" + levelInfo.levelHintsRemaining + "', " + KEY_LEVEL_HINTS_EARNED + "='" + levelInfo.levelHintsEarned + "', " + KEY_LEVEL_GAMES_REMAINING + "='" + levelInfo.levelGamesRemaining + "' WHERE " + KEY_LEVEL_NUMBER + "='" + levelInfo.levelNum + "'";
			database.execSQL(QUERY_UPDATE_ROW);
			Utils.Log("addOrUpdateLevelData, running query [" + QUERY_UPDATE_ROW + "]");
		}
		else
		{
			String QUERY_INSERT_INTO_TABLE = "INSERT INTO " + TABLE_ENGLISH_LEVELS + " (" + KEY_LEVEL_NUMBER + ", " + KEY_LEVEL_STATE + ", " + KEY_LEVEL_HINTS_REMAINING + ", " + KEY_LEVEL_HINTS_EARNED + ", " + KEY_LEVEL_GAMES_REMAINING +") VALUES ('" + 
					levelInfo.levelNum + "', '" + levelInfo.levelState + "', '" + levelInfo.levelHintsRemaining + "', '" + levelInfo.levelHintsEarned + "', '" + levelInfo.levelGamesRemaining + "')";
			database.execSQL(QUERY_INSERT_INTO_TABLE);
			Utils.Log("addOrUpdateLevelData, running query [" + QUERY_INSERT_INTO_TABLE + "]");
		}
	}
	
	public GameInfo getGameInformation(int playType, int level, int gameNum)
	{
		GameInfo gameInfo = new GameInfo();
		gameInfo.gameShortInfo = new GameShortInfo();
		gameInfo.gameLongInfo = new GameLongInfo();
		
		String QUERY_SELECT_ROW = "SELECT * FROM " + TABLE_ENGLISH_GAMES + " WHERE (" + KEY_GAME_NUM + "='" + gameNum + "' AND " + KEY_GAME_LEVEL + "='" + level + "')";
		Cursor cursor = database.rawQuery(QUERY_SELECT_ROW, null);

		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			
			gameInfo.gameShortInfo.gameNum = gameNum;
			gameInfo.gameShortInfo.gamePointsEarned = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_POINTS));
			gameInfo.gameShortInfo.gameState = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_STATE));
			gameInfo.gameShortInfo.gameType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_TYPE));
			
			gameInfo.gameLongInfo.gameData = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GAME_DATA));
			gameInfo.gameLongInfo.gameDuration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_DURATION));
			gameInfo.gameLongInfo.gameHintsRemaining = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_HINTS_REMAINING));
			gameInfo.gameLongInfo.gameLastPlayed = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_LAST_PLAYED));
			gameInfo.gameLongInfo.gameLevel = level;
		}
		
		return gameInfo;
	}
	
	/**
	 * Fetch the type of a specific game in the DB.
	 * 
	 * @param playType Challenge type.  One of Constants.PLAY_TYPE_*
	 * @param gameNum The game number
	 * @return The type of game.  One of Constants.GAME_TYPE_*
	 */
	public int getGameType(int playType, int level, int gameNum)
	{
		int gameType = -1;
		
	    String QUERY_GET_GAME_TYPE = "SELECT " + KEY_GAME_TYPE +" FROM " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_LEVEL + " = '" + level + "' AND " + KEY_GAME_NUM + " = '" + gameNum + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_GAME_TYPE, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			gameType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_TYPE));
		}
		
		cursor.close();
		
		return gameType;
	}
	
	/**
	 * Get the total time spent on a given level by adding time spent on all games that are
	 * saved in the DB.
	 */
	public int getTotalTimeSpent(int playType, int level)
	{
		int timeSpent = 0;
		
	    String QUERY_SUM_OF_TIME_SPENT = "SELECT SUM(" + KEY_GAME_DURATION +") AS " + KEY_GAME_DURATION + " FROM " + TABLE_ENGLISH_GAMES + " WHERE " + KEY_GAME_LEVEL + " = '" + level + "'";

		Cursor cursor = database.rawQuery(QUERY_SUM_OF_TIME_SPENT, null);
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			timeSpent = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GAME_DURATION));
		}
		
		cursor.close();
		
		return timeSpent;
	}
}