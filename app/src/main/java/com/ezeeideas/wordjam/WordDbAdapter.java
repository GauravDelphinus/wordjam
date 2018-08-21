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

import android.database.sqlite.SQLiteStatement;
import android.database.Cursor;
import android.database.SQLException;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.ezeeideas.wordjam.db.GamesDbAdapter.GameShortInfo;

public class WordDbAdapter {

	private static final String DATABASE_NAME = "words.db.mp3"; // name of the Word DB
	private static final int DATABASE_VERSION = 2;
	private static String DATABASE_FULL_PATH; // full path to the DB after it has been copied to the correct location
	
	//Database Tables
	
	//Word Table, and the column keys
	private static final String TABLE_WORDS_ENGLISH = "words_english";
	private static final String TABLE_WORDS_GRE = "words_gre";
	private static final String TABLE_WORDS_SAT = "words_sat";
	private static final String TABLE_WORDS_KIDS = "words_kids";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_WORD = "word_name";
	public static final String KEY_TYPE = "word_type";
	public static final String KEY_MEANING = "word_meaning";
	public static final String KEY_DIFFICULTY = "word_difficulty";
	
	//Type Index Table, and its column keys
	private static final String TABLE_TYPEINDEX_ENGLISH = "typeindex_english";
	private static final String TABLE_TYPEINDEX_GRE = "typeindex_gre";
	private static final String TABLE_TYPEINDEX_SAT = "typeindex_sat";
	private static final String TABLE_TYPEINDEX_KIDS = "typeindex_kids";
	public static final String KEY_TYPEI = "type";
	public static final String KEY_LOWINDEX = "lowindex";
	public static final String KEY_HIGHINDEX = "highindex";
	
	// Crossword Table, and its column keys
	private static final String CROSSWORD_TABLE = "crossword_data";
	public static final String KEY_WORDS = "words";
	public static final String KEY_MEANINGS = "meanings";
	public static final String KEY_ACROSSLIST = "acrosslist";
	public static final String KEY_DOWNLIST = "downlist";
	
	//SQLiteMaster Table, and its column keys
	private static final String SQLITEMASTER_TABLE = "sqlite_master";
	public static final String KEY_NAME = "name";
	public static final String KEY_TYPES = "type";
	public static final String VALUE_TYPES = "value";
	
	private Context context;
	private SQLiteDatabase database;
	private WordDbHelper dbHelper;
	
	/**
	 * The typeCache stores the mapping from the types to the low and high range index
	 * for words of that type in the given table.
	 * Hash Key: integer array with two elements: 0 - Play Type, 1 = Word Type
	 * Hash Value: integer array with two elements: 0 - Low Index, 1 = High Index
	 */
	private static HashMap<int[], int[]> mTypeCache = new HashMap<int[], int[]>();
	
	public WordDbAdapter(Context context)
	{
		this.context = context;
		DATABASE_FULL_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
		
		dbHelper = new WordDbHelper(context, false, DATABASE_NAME, DATABASE_VERSION);
	}
	
	/**
	 * Check if the DB is set up, and the Cache is already built
	 * 
	 * @return true if DB was successfully opened.  false otherwise.
	 * @throws SQLException
	 */
	public boolean isSetUp() throws SQLException
	{
		if (database != null && database.isOpen())
			return true;
		
		return false;
	}
	
	/**
	 * Attempt to open the DB.
	 * 
	 * @return true if DB was successfully opened.  false otherwise.
	 * @throws SQLException
	 */
	public boolean open_dontuse() throws SQLException
	{
		if (database != null && database.isOpen())
			return true;
		
		boolean dbExists = isSetUp();
		
		//build the typecache in memory, if the DB already exists
		if (dbExists)
		{
			//Get the database
			database = dbHelper.getWritableDatabase();
			
			//build the cache from the database
			buildCache();
		}
		
		//return true if DB exists
		return dbExists;
	}
	
	/**
	 * Create the DB (by copying from assets folder), and build the cache
	 * for faster access.
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean setup()
	{
		boolean retval = true;
		
		SQLiteDatabase checkDB = null;
		Boolean dbExists = true;
		try {
			checkDB = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
			Utils.Log("Database Version = " + checkDB.getVersion());
			int dbVersion = checkDB.getVersion();
			
			checkDB.close();
			
			if (dbVersion == 1)
			{
				//remove and upgrade to new version
				File newDBFile = new File(DATABASE_FULL_PATH);
				newDBFile.delete();
				
				dbExists = false;
			}
		}
		catch (SQLiteException e)
		{
			//this could be a SQLiteDatabaseCorruptException, and we should just delete the DB file
			//if it exists so we can do a clean sweep the next time
			File newDBFile = new File(DATABASE_FULL_PATH);
			newDBFile.delete();
			
			//Log.d(null, "########### Doesn't exist " + e.toString());
			dbExists = false;
		}
		
		if (!dbExists) //if DB does not exist, create it now
		{
			/**
			 * Trying to address a user reported crash.
			 * Solution from http://www.anddev.org/networking-database-problems-f29/missing-table-in-sqlite-with-specific-version-of-desire-hd-t50364.html
			 * This will create an empty database, for overwriting later.
			 * This is really just a hack.
			 */
			SQLiteDatabase db_Read = null;
			db_Read = dbHelper.getReadableDatabase(); 
			db_Read.close();
			
			//Now copy from assets to the data/data folder
			try 
			{
				InputStream assetDB = context.getAssets().open(DATABASE_NAME);
	
				File newDBFile = new File(DATABASE_FULL_PATH);
				File parentDir = newDBFile.getParentFile();
				
				parentDir.mkdirs();
	
				OutputStream newDB = new FileOutputStream(newDBFile);
	
				// actually copy the bytes from assets to the actual DB location
				Utils.copyFile(assetDB, newDB);
				newDB.close();
	
			} catch (IOException e)
			{
				retval = false;
	
			}
		}
		
		//Get the database, and build the cache
		database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		buildCache();
		database.setTransactionSuccessful();
		database.endTransaction();
		
		return retval;
	}
	
	/**
	 * Delete the Database file.
	 */
	public void delete()
	{
		File newDBFile = new File(DATABASE_FULL_PATH);
		newDBFile.delete();
	}
	
	/**
	 * Close the DB.  Caution: Only do this when you no longer need the DB, typically
	 * at app exit time.
	 */
	public void close()
	{
		//Log.d(null, "############################ --- WordDbAdapter.close()");
		dbHelper.close();
		if (database != null && database.isOpen())
		{
			database.endTransaction();
			database.close();
			database = null;
			//Log.d(null, "********* closed hte database");
		}
		
	}
	
	/**
	 * Check if a given table exists in the Database
	 * @param tableName the table being looked up
	 * @return true if the table exists, false otherwise
	 */
	private boolean tableExists(String tableName)
	{
		String QUERY_TABLE_EXISTS = "SELECT " + KEY_NAME + " FROM " + SQLITEMASTER_TABLE + " WHERE " + KEY_TYPES + "='" + VALUE_TYPES + "' AND " + KEY_NAME + "='" + tableName + "'";
		Cursor typeIndexCursor = database.rawQuery(QUERY_TABLE_EXISTS, null);

		boolean retval = (typeIndexCursor.getCount() > 0);
		typeIndexCursor.close();
		
		return retval;
	}
	
	private void dropTableIfExists(String tableName)
	{
		String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + tableName;
		database.execSQL(QUERY_DROP_TABLE);
	}
	
	private String getWordTableForChallenge(int playType)
	{
		switch (playType)
		{
		case Constants.PLAY_TYPE_ENGLISH_CLASSIC: return TABLE_WORDS_ENGLISH;
		case Constants.PLAY_TYPE_GRE: return TABLE_WORDS_GRE;
		case Constants.PLAY_TYPE_SAT: return TABLE_WORDS_SAT;
		case Constants.PLAY_TYPE_KIDS: return TABLE_WORDS_KIDS;
		}
		
		return null;
	}
	
	private String getTypeindexTableForChallenge(int playType)
	{
		switch (playType)
		{
		case Constants.PLAY_TYPE_ENGLISH_CLASSIC: return TABLE_TYPEINDEX_ENGLISH;
		case Constants.PLAY_TYPE_GRE: return TABLE_TYPEINDEX_GRE;
		case Constants.PLAY_TYPE_SAT: return TABLE_TYPEINDEX_SAT;
		case Constants.PLAY_TYPE_KIDS: return TABLE_TYPEINDEX_KIDS;
		}
		
		return null;
	}
	
	/**
	 * Build the in-memory cache as well as the TypeIndex table, if required.
	 * Refer the detailed comments ahead of buildCacheFromWordDB for details.
	 */
	private void buildCache()
	{
		if (mTypeCache.isEmpty())
		{
			boolean doneCache = false;
			if (tableExists(TABLE_TYPEINDEX_ENGLISH) && tableExists(TABLE_TYPEINDEX_GRE) && tableExists(TABLE_TYPEINDEX_SAT) && tableExists(TABLE_TYPEINDEX_KIDS))
			{
				doneCache = buildCacheFromTypeIndexTables();
			}
			
			//if not yet built cache, let's do from scratch
			if (!doneCache)
			{
				buildCachesFromWordDB();
			}
		}
	}
	
	private boolean buildCacheFromTypeIndexTables()
	{
		return (buildCacheFromTypeIndexTable(Constants.PLAY_TYPE_ENGLISH_CLASSIC) && buildCacheFromTypeIndexTable(Constants.PLAY_TYPE_GRE) 
				&& buildCacheFromTypeIndexTable(Constants.PLAY_TYPE_SAT) && buildCacheFromTypeIndexTable(Constants.PLAY_TYPE_KIDS));
	}
	
	/**
	 * Build the in-memory cache from the TypeIndex tables.
	 * 
	 * @return true if building was successful, false otherwise.
	 */
	private boolean buildCacheFromTypeIndexTable(int playType)
	{
		boolean retval = false;
		
		String tableName = getTypeindexTableForChallenge(playType);
		String QUERY_GET_TYPEINDEX = "SELECT * FROM " + tableName;
		Cursor typesCursor = database.rawQuery(QUERY_GET_TYPEINDEX, null);
		
		typesCursor.moveToFirst();
		if (typesCursor.getCount() > 0)
		{
			while (!typesCursor.isLast())
			{
				int selectedType = typesCursor.getInt(typesCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_TYPE));
				int lowindex = typesCursor.getInt(typesCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_LOWINDEX));
				int highindex = typesCursor.getInt(typesCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_HIGHINDEX));
				
				int[] key = {playType, selectedType};
				int[] lowHigh = {lowindex, highindex};
				
			    mTypeCache.put(key, lowHigh);
			    
			    typesCursor.moveToNext();
			}
			
			retval = true;
		}
		
		typesCursor.close();
		
		return retval;
	}
	
	private void buildCachesFromWordDB()
	{
		buildCacheFromWordDB(Constants.PLAY_TYPE_ENGLISH_CLASSIC);
		buildCacheFromWordDB(Constants.PLAY_TYPE_GRE);
		buildCacheFromWordDB(Constants.PLAY_TYPE_SAT);
		buildCacheFromWordDB(Constants.PLAY_TYPE_KIDS);
	}
	/**
	 * Build the Cache from the Word DB.
	 * There are two types of cache: in memory (mTypeCache) and in the DB itself (typeindex table)
	 * 
	 * Note: This function assumes that in the Words Table in the DB all types are together such that
	 * the types form consecutive ranges of words with a low index and a high index.
	 * 
	 * Format of mTypeCache: mapping from 'type' to 'low index' and 'high index' for that type in the Word Table
	 * Format of typeindex table: columns 'type', 'lowindex' and 'highindex' with similar function as above.
	 */
	private void buildCacheFromWordDB(int playType)
	{
		String wordTableName = getWordTableForChallenge(playType);
		String typeindexTableName = getTypeindexTableForChallenge(playType);
		
		//clear the typeindex tables, in case they exist
		dropTableIfExists(typeindexTableName);
		
		if (mTypeCache.isEmpty())
		{	
			//now create the index table
			String QUERY_CREATE_TYPEINDEX_TABLE = "CREATE TABLE IF NOT EXISTS " + typeindexTableName + " (" + KEY_TYPEI + " INTEGER NOT NULL, " + KEY_LOWINDEX + " INTEGER NOT NULL, " + KEY_HIGHINDEX + " INTEGER NOT NULL, UNIQUE(" + KEY_TYPEI + "))";
			database.execSQL(QUERY_CREATE_TYPEINDEX_TABLE);
			
			//get the distinct type values
			String QUERY_GET_TYPES = "SELECT DISTINCT " + KEY_TYPE + " from " + wordTableName;
			Cursor typesCursor = database.rawQuery(QUERY_GET_TYPES, null);

			//now for each type, find it's low and high range index
			typesCursor.moveToFirst();
			while (!typesCursor.isLast())
			{
				int selectedType = typesCursor.getInt(typesCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_TYPE));

				int[] lowHigh = new int[2];
				
				String MAX_QUERY = "select max(" + KEY_ROWID + ") from " + wordTableName + " WHERE " + KEY_TYPE + " = '" + selectedType + "'";
				SQLiteStatement query = database.compileStatement(MAX_QUERY);
			    lowHigh[1] = (int) query.simpleQueryForLong();
			    query.close();

			    String MIN_QUERY = "select min(" + KEY_ROWID + ") from " + wordTableName + " WHERE " + KEY_TYPE + " = '" + selectedType + "'";
			    query = database.compileStatement(MIN_QUERY);
			    lowHigh[0] = (int) query.simpleQueryForLong();
			    query.close();
    
			    int[] key = {playType, selectedType};
			    
			    //add the low and high index for this type to the cache
			    mTypeCache.put(key, lowHigh);
			    
			    //and to the typeindex table
			    String QUERY_UPDATE_TYPEINDEX_TABLE = "INSERT INTO " + typeindexTableName + " (" + KEY_TYPEI + ", " + KEY_LOWINDEX + ", " + KEY_HIGHINDEX + ") VALUES ('" + selectedType + "', '" + lowHigh[0] + "', '" + lowHigh[1] + "')";
			    database.execSQL(QUERY_UPDATE_TYPEINDEX_TABLE);
			    
			    typesCursor.moveToNext();
			}	
			typesCursor.close();
		}
	}
	
	/**
	 * This function maps levels or practiceTypes to word difficulty levels so we
	 * can query the database for the appropriate words that suit the given level.
	 * 
	 * For Play Types
	 * Level 1 -> Difficulty = 1
	 * Level 2 -> Difficulty = 1 or 2
	 * Level 3 -> Difficulty = 2
	 * Level 4 -> Difficulty = 2 or 3
	 * Level 5 -> Difficulty = 3
	 * Level 6 -> Difficulty = 4
	 * 
	 * For Practice Types
	 * Easy -> Difficulty = 1
	 * Moderate -> Difficulty = 2
	 * Hard -> Difficulty = 3
	 * Very Hard -> Difficulty = 4
	 * 
	 * @param playType
	 * @param isPlay
	 * @param levelOrPracticeType
	 * @param minWordLength Use this to override the default
	 * @param maxWordLength Use this to override the default
	 * @return
	 */
	private String getDifficultySQLQuery(int playType, boolean isPlay, int levelOrPracticeType)
	{		
		String sqlQuery = " (";
		
		if (isPlay)
		{
			switch (levelOrPracticeType)
			{
			case Constants.LEVEL_1: sqlQuery += KEY_DIFFICULTY + " = '1'";break;
			case Constants.LEVEL_2: sqlQuery += "(" + KEY_DIFFICULTY + " = '1') OR (" + KEY_DIFFICULTY + " = '2')";break;
			case Constants.LEVEL_3: sqlQuery += KEY_DIFFICULTY + " = '2'";break;
			case Constants.LEVEL_4: sqlQuery += "(" + KEY_DIFFICULTY + " = '2') OR (" + KEY_DIFFICULTY + " = '3')";break;
			case Constants.LEVEL_5: sqlQuery += KEY_DIFFICULTY + " = '3'";break;
			case Constants.LEVEL_6: sqlQuery += KEY_DIFFICULTY + " = '4'";break;
			default:break;
			}
		}
		else
		{
			switch (levelOrPracticeType)
			{
			case Constants.PRACTICE_TYPE_EASY: 
				sqlQuery += KEY_DIFFICULTY + " = '1'";break;
			case Constants.PRACTICE_TYPE_MEDIUM: 
				sqlQuery += KEY_DIFFICULTY + " = '2'";break;
			case Constants.PRACTICE_TYPE_HARD: 
				sqlQuery += KEY_DIFFICULTY + " = '3'";break;
			case Constants.PRACTICE_TYPE_VERY_HARD: 
				sqlQuery += KEY_DIFFICULTY + " = '4'";break;
			default:break;
			}
		}
		
		sqlQuery += ") ";
		
		return sqlQuery;
	}
	
	/******************************** GENERAL UTIL APIS *******************************/
	
	/**
	 * Fetch all words in the words table.
	 * 
	 * @return the Cursor to be used to iterate through all the words.
	 */
	public Cursor fetchAllWords(int playType)
	{
		Cursor mCursor = database.query(getWordTableForChallenge(playType),
				new String[] {KEY_ROWID, KEY_WORD},
				null, null, null, null, null);
		
		return mCursor;
	}
	
	/**
	 * Fetch the word at the given row ID.
	 * 
	 * @param rowID the rowID for the row in the Words table
	 * @return the Cursor pointing to the word
	 * @throws SQLException
	 */
	public Cursor fetchWord(int playType, long rowID) throws SQLException
	{
		Cursor mCursor = database.query(true,
				getWordTableForChallenge(playType),
				new String[] {KEY_ROWID, KEY_WORD, KEY_TYPE, KEY_MEANING},
				KEY_ROWID + "=" + rowID,
				null, null, null, null, null);
		if (mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Fetch a specific word of the given type, and at the given position within that type range.
	 * 
	 * @param wordType Type of word being fetched
	 * @param position Index in the word type range
	 * @return Cursor to the word being fetched
	 * @throws SQLException
	 */
	public Cursor fetchWordOfType(int playType, int wordType, int position) throws SQLException
	{
		int[] lowHigh = (int [])mTypeCache.get(wordType);
		int rowId = lowHigh[0] + position;
		
	    String QUERY_GET_MATCHING_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_ROWID + " = '" + rowId + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_WORDS, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	/**
	 * Fetch all words of a given type.
	 * 
	 * @param wordType Type of word
	 * @return Cursor used to iterate through all the matching words.
	 * @throws SQLException
	 */
	public Cursor fetchWordsOfType(int playType, int wordType) throws SQLException
	{
	    String QUERY_GET_MATCHING_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_TYPE + " = '" + wordType + "'";

		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_WORDS, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	/**
	 * Search all words that match a given keyword.
	 * 
	 * @param keyword The keyword to match.
	 * @return Cursor used to iterate through all the matching words.
	 * @throws SQLException
	 */
	public Cursor searchWords(int playType, String keyword) throws SQLException
	{
	    String QUERY_SEARCH_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_WORD + " LIKE '%" + keyword + "%' ORDER BY " + KEY_WORD;
	    //Log.d(null, "query [" + QUERY_GET_MATCHING_WORDS + "]");
		Cursor cursor = database.rawQuery(QUERY_SEARCH_WORDS, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	/**
	 * Fetch a random word from the Word Table.
	 * 
	 * @return Cursor to the random word selected.
	 * @throws SQLException
	 */
	public Cursor fetchRandomWord(int playType) throws SQLException
	{
		String QUERY_GET_RANDOM_WORD = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_ROWID + " = (abs(random()) % (SELECT max(" + KEY_ROWID + " )+1 FROM " + getWordTableForChallenge(playType) + "))";
		
		Cursor cursor = database.rawQuery(QUERY_GET_RANDOM_WORD, null);
		
		int numRows = cursor.getCount();
		if (numRows > 1)
		{
			Random randInt = new Random();
			int index = randInt.nextInt(numRows);
			cursor.moveToPosition(index);
		
		}
		else if (numRows == 1)
		{
			cursor.moveToFirst();
		
		}
		else
		{
			//should never reach here
		}
		
		return cursor;
	}
	
	public int getRandomType(int playType)
	{
		int retVal = 0;
		ArrayList<Integer> typeList = new ArrayList<Integer>();
		
		String query = "SELECT DISTINCT " + KEY_TYPEI + " FROM " + getTypeindexTableForChallenge(playType);
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		
		if (cursor.getCount() > 0)
		{
			while (!cursor.isAfterLast())
			{
				int type = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPEI));
				typeList.add(type);
				
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		Random rand = new Random();
		int randIndex = rand.nextInt(typeList.size());
		
		return typeList.get(randIndex);
	}
	
	/**
	 * Fetch words of a common type for games such as Recall Words and Match Words.
	 * 
	 * @param playType Type of challenge, one of Constants.PLAY_TYPE_*
	 * @param isPlay true if this is a Play, false in case of Practice
	 * @param levelOrPracticeType The level in case of play (Constants.LEVEL_*), and practice type in case of Practice (Constants.PRACTICE_TYPE_*)
	 * @param count No. of words expected to be returned
	 * @return A list of words
	 */
	public ArrayList<WordShortInfo> fetchWordsOfCommonType(int playType, boolean isPlay, int levelOrPracticeType, int count)
	{	
		ArrayList<WordShortInfo> wordList = new ArrayList<WordShortInfo>();
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		int numTries = 0;
		while (true) //keep trying until we get a good value from the DB
		{
			int type = getRandomType(playType);
			
			String maxWordQuery = "";
			if (!isPlay && levelOrPracticeType == Constants.PRACTICE_TYPE_EASY)
			{
				maxWordQuery = " AND ( LENGTH(" + KEY_WORD + ") <= " + MAX_LENGTH_FOR_DIFFICULTY_1_CROSSWORD + ") ";
			}
			
			String maxMeaningQuery = " AND ( LENGTH(" + KEY_MEANING + ") <= " + MAX_LENGTH_OF_MEANING + ") ";
			
			String query = "SELECT " + KEY_WORD + ", " + KEY_MEANING + " from " + getWordTableForChallenge(playType) + " WHERE " + difficultyStrQuery + " AND " + KEY_TYPE + " = '" + type + "' " + maxWordQuery + maxMeaningQuery + " ORDER BY RANDOM() LIMIT " + count;
			Utils.Log("fetchWordsOfCommonType, numTries = " + numTries + ", Query = [" + query + "]");
			Cursor cursor = database.rawQuery(query, null);
			cursor.moveToFirst();
			
			Utils.Log("cursor.getcount = " + cursor.getCount());
			if (cursor.getCount() < count)
			{
				cursor.close();
				numTries ++;
			}
			else
			{
				while (!cursor.isAfterLast())
				{
					WordShortInfo info = new WordShortInfo();
					info.word = cursor.getString(cursor.getColumnIndexOrThrow(KEY_WORD));
					info.meaning = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEANING));
					Utils.Log("fetchWordsOfCommonType adding word [" + info.word + "], meaning [" + info.meaning + "]");
					wordList.add(info);
					cursor.moveToNext();
				}
				cursor.close();
				break;
			}
		}
		
		return wordList;
	}
	
	/**
	 * Fetch words of a common type for games such as Recall Words and Match Words.
	 * 
	 * @param playType Type of challenge, one of Constants.PLAY_TYPE_*
	 * @param isPlay true if this is a Play, false in case of Practice
	 * @param levelOrPracticeType The level in case of play (Constants.LEVEL_*), and practice type in case of Practice (Constants.PRACTICE_TYPE_*)
	 * @param count No. of words expected to be returned
	 * @param maxLength Maximum length of the words
	 * @return A list of words
	 */
	public Pair<ArrayList<WordShortInfo>,Integer> fetchWordsOfCommonTypeNotLongerThan(int playType, boolean isPlay, int levelOrPracticeType, int count, int maxLength)
	{	
		int selectedType = -1;
		ArrayList<WordShortInfo> wordList = new ArrayList<WordShortInfo>();
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		int numTries = 0;
		
		while (true) //keep trying until we get a good value from the DB
		{
			int type = getRandomType(playType);
			
			if (!isPlay && levelOrPracticeType == Constants.PRACTICE_TYPE_EASY)
			{
				maxLength = MAX_LENGTH_FOR_DIFFICULTY_1_CROSSWORD;
			}
			
			String query = "SELECT " + KEY_TYPE + ", " + KEY_WORD + ", " + KEY_MEANING + " from " + getWordTableForChallenge(playType) + " WHERE " + difficultyStrQuery + " AND " + KEY_TYPE + " = '" + type + "' AND LENGTH(" + KEY_WORD + ") <= " + maxLength + " ORDER BY RANDOM() LIMIT " + count;
			Utils.Log("fetchWordsOfCommonTypeNotLongerThan, numTries = " + numTries + ", Query = [" + query + "]");
			Cursor cursor = database.rawQuery(query, null);
			cursor.moveToFirst();
			
			Utils.Log("cursor.getcount = " + cursor.getCount());
			if (cursor.getCount() < count)
			{
				cursor.close();
				numTries ++;
			}
			else
			{
				while (!cursor.isAfterLast())
				{
					WordShortInfo info = new WordShortInfo();
					info.word = cursor.getString(cursor.getColumnIndexOrThrow(KEY_WORD));
					info.meaning = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEANING));
					selectedType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPE));
					
					wordList.add(info);
					cursor.moveToNext();
				}
				cursor.close();
				break;
			}
		}
		
		return new Pair<ArrayList<WordShortInfo>,Integer>(wordList, selectedType);
	}
	
	/**
	 * Return a random word from the DB of the tiven type and of the given length.
	 * 
	 * @param playType One of Constants.PLAY_TYPE_*
	 * @param isPlay true for Play, false for Practice
	 * @param levelOrPracticeType Level or Practice Type
	 * @param minWordLength Minimum word length requested
	 * @param maxWordLength Maximum word length requested
	 * @return
	 */
	public WordShortInfo fetchRandomWord(int playType, boolean isPlay, int levelOrPracticeType, int minWordLength, int maxWordLength)
	{	
		WordShortInfo wordInfo = new WordShortInfo();
		
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		int numTries = 0;
		
		if (!isPlay && levelOrPracticeType == Constants.PRACTICE_TYPE_EASY)
		{
			maxWordLength = MAX_LENGTH_FOR_DIFFICULTY_1;
		}
		String whereQuery = "";
		String minWordQuery = "( LENGTH(" + KEY_WORD + ") >= " + minWordLength + ") ";
		String maxWordQuery = "( LENGTH(" + KEY_WORD + ") <= " + maxWordLength + ") ";
		if (minWordLength > 0)
		{
			whereQuery += " AND " + minWordQuery;
		}
		if (maxWordLength > 0)
		{
			if (minWordLength > 0)
			{
				whereQuery += " AND ";
			}
			whereQuery += maxWordQuery;
		}
		while (true) //keep trying until we get a good value from the DB
		{			
			String query = "SELECT " + KEY_WORD + ", " + KEY_MEANING + " from " + getWordTableForChallenge(playType) + " WHERE " + difficultyStrQuery + whereQuery + " ORDER BY RANDOM() LIMIT 1";
			Utils.Log("fetchWordsOfCommonType, numTries = " + numTries + ", Query = [" + query + "]");
			Cursor cursor = database.rawQuery(query, null);
			cursor.moveToFirst();
			
			Utils.Log("cursor.getcount = " + cursor.getCount());
			if (cursor.getCount() < 1)
			{
				cursor.close();
				numTries ++;
			}
			else
			{
				while (!cursor.isAfterLast())
				{
					wordInfo.word = cursor.getString(cursor.getColumnIndexOrThrow(KEY_WORD));
					wordInfo.meaning = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEANING));
					
					cursor.moveToNext();
				}
				cursor.close();
				break;
			}
		}
		
		return wordInfo;
	}
	
	/**
	 * Fetch a set of words of a common type.
	 * 
	 * @param playType Type of challenge, one of Constants.PLAY_TYPE_*
	 * @param levelOrPracticeType The game level or practice type
	 * @param isPlay true if this is a Play, false in case of practice
	 * @param count The number of words being asked
	 * @return The list of words
	 */
	public Cursor fetchMatchingWords(int playType, boolean isPlay, int levelOrPracticeType, int count)
	{
		int numTypes = mTypeCache.size();
		Random randInt = new Random();
		int index = randInt.nextInt(numTypes);
		Object[] objects = mTypeCache.entrySet().toArray();
		@SuppressWarnings("unchecked")
		Entry<Integer, int[]> entry = (Entry<Integer, int[]>)objects[index];
		
		//SQLiteStatement query = database.compileStatement("select count(*) from" + getWordTableForChallenge(playType));
	    //query.close();
	    List<Integer> list = Utils.RandomSelection.getRandomList(count, entry.getValue()[0], entry.getValue()[1]);
	    
	    String QUERY_GET_MATCHING_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_ROWID + " IN (";
	    Iterator<Integer> it = list.iterator();
	    while (it.hasNext())
	    {
	    	QUERY_GET_MATCHING_WORDS += it.next();
	    	if (it.hasNext())
	    	{
	    		QUERY_GET_MATCHING_WORDS += ",";
	    	}
	    }
	    QUERY_GET_MATCHING_WORDS += ") ";
	    
		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_WORDS, null);
		
		return cursor;
	}
	
	/**
	 * Fetch words of the given type.
	 * 
	 * @param word The word that must be excluded from the returned list.
	 * @param type The type that all words must be of type.
	 * @param numToFetch The no. of words to fetch.
	 * @return Cursor used to iterate through the words.
	 * @throws SQLException
	 */
	public Cursor fetchSimilarWords(int playType, String word, String type, int numToFetch) throws SQLException
	{
		String QUERY_GET_SIMILAR_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE type='" + type + "' AND " + KEY_WORD + " != '" + word + "' ORDER BY RANDOM() LIMIT " + numToFetch;
		
		Cursor cursor = database.rawQuery(QUERY_GET_SIMILAR_WORDS, null);
		
		cursor.moveToFirst();
		
		return cursor;
	}
	
	/**
	 * Fetch random list of words not longer than a given length.
	 * 
	 * @param maxLength Max length of words to be matched.
	 * @param numToFetch No. of words to fetch.
	 * @return Cursor to iterate through the list.
	 * @throws SQLException
	 */
	public Cursor fetchRandomWordsNotLongerThanOld(int playType, boolean isPlay, int levelOrPracticeType, int maxLength, int numToFetch) throws SQLException
	{
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		
		String QUERY_GET_RANDOM_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + difficultyStrQuery + " AND LENGTH(" + KEY_WORD + ") <= " + maxLength + " ORDER BY RANDOM() LIMIT " + numToFetch;
		
		Cursor cursor = database.rawQuery(QUERY_GET_RANDOM_WORDS, null);
		
		cursor.moveToFirst();
		
		return cursor;
	}
	
	/**
	 * Fetch the list of words of a common type, and not longer than a given length.
	 * 
	 * @param maxLength Max. length of the matched words.
	 * @param count No. of words to return.
	 * @return 
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public Pair<Cursor, Integer> fetchRandomSimilarWordsNotLongerThan(int playType, int maxLength, int count) throws SQLException
	{
		int numTypes = mTypeCache.size();
		Random randInt = new Random();
		
		Object[] objects = mTypeCache.entrySet().toArray();
		List<Integer> list = new ArrayList<Integer>();
		Entry<Integer, int[]> entry = null;
		int listSize = 0;
		
		while (listSize <= 0)
		{
			int index = randInt.nextInt(numTypes);		
			entry = (Entry<Integer, int[]>)objects[index];
			list = Utils.RandomSelection.getRandomList(count, entry.getValue()[0], entry.getValue()[1]);
			listSize = list.size();
		}
		
	    String QUERY_GET_MATCHING_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_ROWID + " IN (";
	    Iterator<Integer> it = list.iterator();
	    while (it.hasNext())
	    {
	    	QUERY_GET_MATCHING_WORDS += it.next();
	    	if (it.hasNext())
	    	{
	    		QUERY_GET_MATCHING_WORDS += ",";
	    	}
	    }
	    QUERY_GET_MATCHING_WORDS += ") AND LENGTH(" + KEY_WORD + ") <= " + maxLength;
	    
		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_WORDS, null);
		cursor.moveToFirst();
		
		return new Pair<Cursor, Integer>(cursor, entry.getKey());
	}
	
	/**
	 * Fetch a random list of words that are not longer than the given length.
	 * 
	 * @param playType The challenge type.  One of Constants.PLAY_TYPE_*
	 * @param isPlay true for Play, false for Practice
	 * @param levelOrPracticeType The Play level or Practice Type Constants.PRACTICE_TYPE_*
	 * @param maxLength The max length of the words beign fetched
	 * @param numToFetch The number of words to fetch
	 * 
	 * @return The list of words
	 */
	public ArrayList<WordShortInfo> fetchRandomWordsNotLongerThan(int playType, boolean isPlay, int levelOrPracticeType, int maxLength, int numToFetch)
	{
		ArrayList<WordShortInfo> wordList = new ArrayList<WordShortInfo>();
		
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		
		if (!isPlay && levelOrPracticeType == Constants.PRACTICE_TYPE_EASY)
		{
			maxLength = MAX_LENGTH_FOR_DIFFICULTY_1_CROSSWORD;
		}
		
		String query = "SELECT * from " + getWordTableForChallenge(playType) + " WHERE " + difficultyStrQuery + " AND LENGTH(" + KEY_WORD + ") <= " + maxLength + " ORDER BY RANDOM() LIMIT " + numToFetch;
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast())
		{
			WordShortInfo info = new WordShortInfo();
			info.word = cursor.getString(cursor.getColumnIndexOrThrow(KEY_WORD));
			info.meaning = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEANING));
			
			wordList.add(info);
			cursor.moveToNext();
		}
		cursor.close();

		return wordList;
	}
	
	public Cursor fetchRandomWordsNotLongerThanOld2(int playType, boolean isPlay, int levelOrPracticeType, int maxLength, int numToFetch)
	{
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		
		String query = "SELECT * from " + getWordTableForChallenge(playType) + " WHERE " + difficultyStrQuery + " AND LENGTH(" + KEY_WORD + ") <= " + maxLength + " ORDER BY RANDOM() LIMIT " + numToFetch;
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		return cursor;
	}
	
	/**
	 * Fetch random list of words not longer than a given length.
	 * 
	 * @param maxLength Max length of words to be matched.
	 * @param numToFetch No. of words to fetch.
	 * @return Cursor to iterate through the list.
	 * @throws SQLException
	 */
	public Cursor fetchRandomWordsNotLongerThan(int playType, int maxLength, int numToFetch) throws SQLException
	{
		SQLiteStatement query = database.compileStatement("select count(*) from " + getWordTableForChallenge(playType));
	    int numRows = (int) query.simpleQueryForLong();
	    query.close();
	    Utils.Log("fetchRandomWordsNotLongerThan, numRows = " + numRows + ", numToFetch = " + numToFetch);
	    List<Integer> list = Utils.RandomSelection.getRandomList(numToFetch * 2, 0, numRows);
	    
	    String QUERY_GET_MATCHING_WORDS = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE " + KEY_ROWID + " IN (";
	    Iterator<Integer> it = list.iterator();
	    while (it.hasNext())
	    {
	    	QUERY_GET_MATCHING_WORDS += it.next();
	    	if (it.hasNext())
	    	{
	    		QUERY_GET_MATCHING_WORDS += ",";
	    	}
	    }
	    QUERY_GET_MATCHING_WORDS += ") AND LENGTH(" + KEY_WORD + ") <= " + maxLength;
	    Utils.Log("fetchRandomWordsNotLongerThan query is [" + QUERY_GET_MATCHING_WORDS + "]");
	    
		Cursor cursor = database.rawQuery(QUERY_GET_MATCHING_WORDS, null);
		
		return cursor;
	}
	
	/**
	 * Fetch a list of random words that are not in the given list, and not longer than the given length.
	 * 
	 * @param wordList List of words to not match with the returned words.
	 * @param maxLength Max length of the matching words.
	 * @return Cursor to iterate over the words.
	 */
	public Cursor fetchRandomWordNotInAndNotLongerThan(int playType, Vector<String> wordList, int maxLength)
	{
		int j;
		String notInQuery = new String("");
		
		if (wordList.size() > 0)
		{
			notInQuery = notInQuery.concat(" AND " + KEY_WORD + " NOT IN ( ");
		}
		int i;
		for (i = 0; i < wordList.size(); i++)
		{
			if (i == wordList.size() - 1)
			{
				notInQuery = notInQuery.concat("'" + wordList.elementAt(i).replaceAll("'", "''") + "')"); // replace single quote with two single quotes to escape, per SQL standards
			}
			else
			{
				notInQuery = notInQuery.concat("'" + wordList.elementAt(i).replaceAll("'", "''") + "',"); // replace single quote with two single quotes to escape, per SQL standards	
			}
		}
		
		String QUERY_GET_RANDOM_WORD = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE LENGTH(" + KEY_WORD + ") <= " + maxLength + " " + notInQuery + " ORDER BY RANDOM() LIMIT " + 1;

		Cursor cursor = database.rawQuery(QUERY_GET_RANDOM_WORD, null);
		
		cursor.moveToFirst();
		
		return cursor;
	}
	
	/**
	 * Fetch the words matching a given regular expression and not longer than a given length.
	 * 
	 * @param regex The regex to match.
	 * @param wordList The list of words that are to be excluded.
	 * @param maxLength The max. length of the returned words.
	 * @return Cursor used to iterate through the words.
	 */
	public WordShortInfo fetchRegexWordNotInAndNotLongerThan(int playType, boolean isPlay, int levelOrPracticeType, String regex, Vector<String> wordList, int maxLength)
	{
		WordShortInfo wordInfo = null;
		
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		
		String notInQuery = new String("");
		
		if (wordList.size() > 0)
		{
			notInQuery = notInQuery.concat(" AND " + KEY_WORD + " NOT IN ( ");
		}
		int i;
		for (i = 0; i < wordList.size(); i++)
		{
			if (i == wordList.size() - 1)
			{
				notInQuery = notInQuery.concat("'" + wordList.elementAt(i).replaceAll("'", "''") + "')"); // replace single quote with two single quotes to escape, per SQL standards
			}
			else
			{
				notInQuery = notInQuery.concat("'" + wordList.elementAt(i).replaceAll("'", "''") + "',"); // replace single quote with two single quotes to escape, per SQL standards
			}
		}
		if (!isPlay && levelOrPracticeType == Constants.PRACTICE_TYPE_EASY)
		{
			maxLength = MAX_LENGTH_FOR_DIFFICULTY_1_CROSSWORD;
		}
		
		String QUERY_GET_RANDOM_WORD = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE LENGTH(" + KEY_WORD + ") <= " + maxLength + " AND " + difficultyStrQuery + notInQuery + " AND " + KEY_WORD + " LIKE '" + regex + "'  LIMIT " + 1;

		Cursor cursor = database.rawQuery(QUERY_GET_RANDOM_WORD, null);

		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			
			wordInfo = new WordShortInfo();
			wordInfo.word = cursor.getString(cursor.getColumnIndexOrThrow(KEY_WORD));
			wordInfo.meaning = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEANING));
		}
		cursor.close();
		
		return wordInfo;
	}
	
	/**
	 * Fetch the words matching a given regular expression and not longer than a given length.
	 * 
	 * @param regex The regex to match.
	 * @param wordList The list of words that are to be excluded.
	 * @param maxLength The max. length of the returned words.
	 * @return Cursor used to iterate through the words.
	 */
	public Cursor fetchRegexWordNotInAndNotLongerThanOld(int playType, boolean isPlay, int levelOrPracticeType, String regex, Vector<String> wordList, int maxLength)
	{
		String difficultyStrQuery = getDifficultySQLQuery(playType, isPlay, levelOrPracticeType);
		
		int j;
		for (j = 0; j < wordList.size(); j++)
		{
			//Log.d(null, "### wordList[" + j + "] = [" + wordList.elementAt(j) + "]");
		}
		String notInQuery = new String("");
		
		if (wordList.size() > 0)
		{
			notInQuery = notInQuery.concat(" AND " + KEY_WORD + " NOT IN ( ");
		}
		int i;
		for (i = 0; i < wordList.size(); i++)
		{
			if (i == wordList.size() - 1)
			{
				notInQuery = notInQuery.concat("'" + wordList.elementAt(i).replaceAll("'", "''") + "')"); // replace single quote with two single quotes to escape, per SQL standards
			}
			else
			{
				notInQuery = notInQuery.concat("'" + wordList.elementAt(i).replaceAll("'", "''") + "',"); // replace single quote with two single quotes to escape, per SQL standards
			}
		}
		
		String QUERY_GET_RANDOM_WORD = "SELECT * FROM " + getWordTableForChallenge(playType) + " WHERE LENGTH(" + KEY_WORD + ") <= " + maxLength + " AND " + difficultyStrQuery + notInQuery + " AND " + KEY_WORD + " LIKE '" + regex + "'  LIMIT " + 1;

		//Log.d(null, "### SQL QUERY is [" + QUERY_GET_RANDOM_WORD + "]");
		Cursor cursor = database.rawQuery(QUERY_GET_RANDOM_WORD, null);
		
		cursor.moveToFirst();
		
		return cursor;
	}
	
	
	/******************************************* CROSSWORD RELATED APIS ****************************************/
	
	/**
	 * Get the number of crosswords available for a given order.
	 * 
	 * @param order the order of the crossword to be found.
	 * @return The no. of crosswords stored.
	 */
	public int getNumCrosswords(int order)
	{	
		SQLiteStatement query = database.compileStatement("SELECT count(*) from " + CROSSWORD_TABLE + " WHERE tableorder = '" + order + "'");
	    int numRows = (int) query.simpleQueryForLong();
	    query.close();
	    
	    return numRows;
	}
	
	/**
	 * Get the crossword data for the given order.
	 * 
	 * @param order The order of the crossword.
	 * @return The Cursor to iterate through the list of words.
	 */
	public Cursor getCrosswordData(int order)
	{				
		String QUERY_GET_FIRST_ENTRY = "SELECT * FROM " + CROSSWORD_TABLE + " WHERE tableorder = '" + order + "' LIMIT 1 ";

		Cursor crosswordCursor = database.rawQuery(QUERY_GET_FIRST_ENTRY, null);
		crosswordCursor.moveToFirst();
		
		return crosswordCursor;
	}
	
	/**
	 * Insert a crossword row in the DB.
	 * 
	 * @param order the order of the crossword.
	 * @param words List of words for this entry.
	 * @param meanings List of meanings for this entry.
	 * @param acrosslist The list of across hints
	 * @param downlist The list of down hints.
	 */
	public void insertCrosswordRow(int order, String words, String meanings, String acrosslist, String downlist)
	{		
		String QUERY_UPDATE_CROSSWORD_TABLE = "INSERT INTO " + CROSSWORD_TABLE + " (tableorder, words, meanings, acrosslist, downlist) VALUES ('" + order + "', '" + words + "', '" + meanings + "', '" + acrosslist + "', '" + downlist + "')";
	    database.execSQL(QUERY_UPDATE_CROSSWORD_TABLE);
	}
	
	/**
	 * Delete a crossword row.
	 * @param rowid The rowid to be deleted.
	 */
	public void deleteCrosswordRow(int rowid)
	{
		String QUERY_DELETE_ROW = "DELETE FROM " + CROSSWORD_TABLE + " WHERE " + KEY_ROWID + " = '" + rowid + "'";
		database.execSQL(QUERY_DELETE_ROW);
	}
	
	public class WordShortInfo
	{
		public String word;
		public String meaning;
	}
	
	private static final int MAX_DB_TRIES = 50; //maximum number of times to try a query with the DB before quitting
	
	/**
	 * Values used to tune the kind of words picked up based
	 * on difficulty level
	 */
	private static final int MAX_LENGTH_FOR_DIFFICULTY_1 = 4; // max length of words of difficulty 1
	private static final int MAX_LENGTH_FOR_DIFFICULTY_1_CROSSWORD = 5; // max length of words of difficulty 1 for crossword
	private static final int MAX_LENGTH_OF_MEANING = 40; // max length of meanings to avoid overlapping in match words and recall words

}