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
import java.util.Random;
import java.util.Vector;

import com.ezeeideas.wordjam.WordDbAdapter.WordShortInfo;

import android.database.Cursor;
import android.util.Pair;
import android.app.Activity;

public class WordSearch
{
	private static final int MAX_TRIES = 10;
	private static final int MIN_WORDS = 6;
	private static final int MAX_WORDS = 20;
	private static final int MIN_WORD_LEN = 3;
	private static final int MIN_WORDS_TO_FETCH = 200;
	
	//word directions, refer illustration below ('o' is the word starting point)
	/*   7 8 1
	      \|/
	     6-o-2
	      /|\ 
	     5 4 3
	 */
	public static final int DIR_TOP_RIGHT = 1; // word is diagonal (bottom-left to top-right)
	public static final int DIR_RIGHT = 2; // word is horizontal, left-to-right
	public static final int DIR_BOTTOM_RIGHT = 3; // word is diagonal (top-left to bottom-right)
	public static final int DIR_BOTTOM = 4; // word is vertical (top-to-bottom)
	public static final int DIR_BOTTOM_LEFT = 5;
	public static final int DIR_LEFT = 6;
	public static final int DIR_TOP_LEFT = 7;
	public static final int DIR_TOP = 8;
	
	private Vector<int[]> mWordMappingList; //list of all words. 0 = index into word list, 1 = direction, 2 = x, 3 = y
	private Vector<String> mWords;
	private int mSelectedType; //selected type of words
	private char[][] mCurrentGrid; // used as scratch pad for generating the word searches 

	private int mOrder;
	private int mMaxWords = MAX_WORDS;
	
	WordDbAdapter mWordDbAdapter;
	private Activity mContext;
	
	int mPlayType; //type of challenge, one of Constants.PLAY_TYPE*
	boolean mIsPlay; //true if play, false if practice
	int mLevelOrPracticeType; //level number in case of play, practice type (one of Constants.PRACTICE_TYPE*) in case of practice
	
	public WordSearch(Activity context, int order, WordDbAdapter dbAdapter, int playType, boolean isPlay, int levelOrPracticeType)
	{
		mOrder = order;
		mMaxWords = 2 * order; // at max twice the number of rows or columns
		mContext = context;
		
		mPlayType = playType;
		mIsPlay = isPlay;
		mLevelOrPracticeType = levelOrPracticeType;
		
		//open databases
    	mWordDbAdapter = dbAdapter;
    	
    	mWordMappingList = new Vector<int[]>();
    	mWords = new Vector<String>();
    	mCurrentGrid = new char[mOrder][mOrder];
    	int i, j;
    	for (i = 0; i < mOrder; i++)
    	{
    		for (j = 0; j < mOrder; j++)
    		{
    			mCurrentGrid[i][j] = '#';
    		}
    	}

		buildWordSearch();
	}
	
	public Vector<int[]> getWordMappingList()
	{
		return mWordMappingList;
	}
	
	public Vector<String> getWordList()
	{
		return mWords;
	}
	
	public int getSelectedType()
	{
		return mSelectedType;
	}
	
	private boolean foundMatch(String word, int startRow, int startCol, int direction)
	{
		int i, j;
		boolean foundMatch = true;
		
		switch (direction)
		{
		case DIR_RIGHT:
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentGrid[startRow][startCol + j] != '#' && mCurrentGrid[startRow][startCol + j] != word.charAt(j))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_LEFT:
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentGrid[startRow][startCol - j] != '#' && mCurrentGrid[startRow][startCol - j] != word.charAt(j))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_BOTTOM:
			for (i = 0; i < word.length(); i++)
			{
				if (mCurrentGrid[startRow + i][startCol] != '#' && mCurrentGrid[startRow + i][startCol] != word.charAt(i))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_TOP:
			for (i = 0; i < word.length(); i++)
			{
				if (mCurrentGrid[startRow - i][startCol] != '#' && mCurrentGrid[startRow - i][startCol] != word.charAt(i))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_BOTTOM_RIGHT:
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentGrid[startRow + j][startCol + j] != '#' && mCurrentGrid[startRow + j][startCol + j] != word.charAt(j))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_TOP_RIGHT:
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentGrid[startRow - j][startCol + j] != '#' && mCurrentGrid[startRow - j][startCol + j] != word.charAt(j))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_TOP_LEFT:
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentGrid[startRow - j][startCol - j] != '#' && mCurrentGrid[startRow - j][startCol - j] != word.charAt(j))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		case DIR_BOTTOM_LEFT:
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentGrid[startRow + j][startCol - j] != '#' && mCurrentGrid[startRow + j][startCol - j] != word.charAt(j))
				{
					foundMatch = false;
					break;
				}
			}
			break;
		}
		
		return foundMatch;
	}
	
	private boolean buildNextWord(String nextWord, int direction)
	{
		int numTries = 0;
		int i, j;

		boolean foundMatch = false;
		
		if (nextWord.length() < MIN_WORD_LEN || mWords.indexOf(nextWord) != -1)
		{
			//word already chosen, so skip this
			return false;
		}
		
		int matchRow = -1, matchCol = -1;
		//Log.d(null, "About to enter while loop");
		while (!foundMatch && numTries < MAX_TRIES)
		{
			numTries ++;
			
			int wordlen = nextWord.length();
			if (direction == DIR_RIGHT)
			{
				//get the randomized lists for iterating the rows and columns in the rows
				int[] randListRows = Utils.getRandList(mOrder);
				int[] randListCols = Utils.getRandList(mOrder - wordlen + 1);
				
				for (i = 0; i < randListRows.length; i++)
				{
					for (j = 0; j < randListCols.length; j++)
					{
						if (foundMatch(nextWord, randListRows[i], randListCols[j], direction))
						{
							foundMatch = true;
							matchRow = randListRows[i];
							matchCol = randListCols[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_LEFT)
			{
				//get the randomized lists for iterating the rows and columns in the rows
				int[] randListRows = Utils.getRandList(mOrder);
				int[] randListCols = Utils.getRandList(mOrder - wordlen + 1);
				
				for (i = 0; i < randListRows.length; i++)
				{
					for (j = 0; j < randListCols.length; j++)
					{
						if (foundMatch(nextWord, randListRows[i], randListCols[j] + wordlen - 1, direction))
						{
							foundMatch = true;
							matchRow = randListRows[i];
							matchCol = randListCols[j] + wordlen - 1;
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_BOTTOM)
			{
				//get the randomized lists for iterating the rows and columns in the rows
				int[] randListCols = Utils.getRandList(mOrder);
				int[] randListRows = Utils.getRandList(mOrder - wordlen + 1);
				
				for (j = 0; j < randListCols.length; j++)
				{
					for (i = 0; i < randListRows.length; i++)
					{
						if (foundMatch(nextWord, randListRows[i], randListCols[j], direction))
						{
							foundMatch = true;
							matchRow = randListRows[i];
							matchCol = randListCols[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_TOP)
			{
				//get the randomized lists for iterating the rows and columns in the rows
				int[] randListCols = Utils.getRandList(mOrder);
				int[] randListRows = Utils.getRandList(mOrder - wordlen + 1);
				
				for (j = 0; j < randListCols.length; j++)
				{
					for (i = 0; i < randListRows.length; i++)
					{
						if (foundMatch(nextWord, randListRows[i] + wordlen - 1, randListCols[j], direction))
						{
							foundMatch = true;
							matchRow = randListRows[i] + wordlen - 1;
							matchCol = randListCols[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_BOTTOM_RIGHT)
			{
				/*
				 *  _____________ 
				 * /    0 1 2 3 4 5 6 7 
				 * |    _ _ _ _ _ _ _ _ 
				 * | 0 |_|_|_|_|_|_|_|_|
				 * | 1 |_|_|_|_|_|_|_|_|
				 * | 2 |_|_|_|_|_|_|_|_|
				 * | 3 |_|_|_|_|_|_|_|_|
				 * V 4 |_|_|_|_|_|_|_|_|
				 *   5 |_|_|_|_|_|_|_|_|
				 *   6 |_|_|_|_|_|_|_|_|
				 *   7 |_|_|_|_|_|_|_|_|
				 * 
				 * In the above grid, say we're looking to fit words of length wordlen = 4
				 * Then the following diagonals are possible for DIR_BOTTOM_RIGHT:
				 * 1 main diagonal starting at 0,0
				 * 4 diagonals starting at column 0, and rows 1, 2, 3 and 4
				 * 4 diagonals starting at row 0, and columns 1, 2, 3 and 4
				 * 
				 * In the logic below, what we're doing is:
				 * 1) Randomly traverse the set of 9 possible diagonals (order of indeces assumed to be as illustrated above)
				 * 2) In the chosen diagonal, depending on the number of possible positions of placement of the word, traverse a random list of that size and try fitting the word
				 */

				//get a random list of diagonals.  Total number of diagonals is 1 (main diagonal) + (mOrder - wordlen) each for diagonals starting
				//at row 0 and column 0 respectively
				//The order is assumed to start at largest order column down to column 1, then main diagonal at 0,0, and then rows starting at row 1 to mOrder-wordlen
				int[] randListDiagonals = Utils.getRandList(2 * (mOrder - wordlen) + 1); //the index will give row id of start of diagonal
				
				//for each diagonal starting for each row, we'll have a separate rand list with entries to be used for word traversals
				for (i = 0; i < randListDiagonals.length; i++)
				{
					int index = randListDiagonals[i];
					int diagonalLength = 0;
					int startRow = -1, startCol = -1;
					
					
					//midpoint is always going to be integer truncation, since the randListDiagonals.length is always going to be odd
					int midPoint = randListDiagonals.length / 2;
					
					//try and determine the actual length of the diagonal given the index
					if (index == midPoint)
					{
						//this is the middle entry, and belongs to the main diagonal
						diagonalLength = mOrder;
						startRow = 0;
						startCol = 0;
						
					}
					else if (index < midPoint)
					{
						//this is the set of diagonals starting at row 0, coming from right
						diagonalLength = wordlen + index;
						startRow = 0;
						startCol = midPoint - index;
					}
					else if (index > midPoint)
					{
						//this is the set of diagonals starting at column 0, from top to bottom
						diagonalLength = wordlen + (randListDiagonals.length - 1) - index;
						startCol = 0;
						startRow = index - midPoint;
						
					}
					
					int[] randList = Utils.getRandList(diagonalLength - wordlen + 1);
					for (j = 0; j < randList.length; j++)
					{
						if (foundMatch(nextWord, startRow + randList[j], startCol + randList[j], direction))
						{
							foundMatch = true;
							matchRow = startRow + randList[j];
							matchCol = startCol + randList[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_TOP_RIGHT)
			{
				/*

				 *      0 1 2 3 4 5 6 7 
				 *      _ _ _ _ _ _ _ _ 
				 *   0 |_|_|_|_|_|_|_|_|
				 *   1 |_|_|_|_|_|_|_|_|
				 *   2 |_|_|_|_|_|_|_|_|
				 *   3 |_|_|_|_|_|_|_|_|
				 * | 4 |_|_|_|_|_|_|_|_|
				 * | 5 |_|_|_|_|_|_|_|_|
				 * | 6 |_|_|_|_|_|_|_|_|
				 * | 7 |_|_|_|_|_|_|_|_|
				 * \____________>
				 *  
				 */

				int[] randListDiagonals = Utils.getRandList(2 * (mOrder - wordlen) + 1); //the index will give row id of start of diagonal
				
				//for each diagonal starting for each row, we'll have a separate rand list with entries to be used for word traversals
				for (i = 0; i < randListDiagonals.length; i++)
				{
					int index = randListDiagonals[i];
					int diagonalLength = 0;
					int startRow = -1, startCol = -1;
					
					
					//midpoint is always going to be integer truncation, since the randListDiagonals.length is always going to be odd
					int midPoint = randListDiagonals.length / 2;
					
					//try and determine the actual length of the diagonal given the index
					if (index == midPoint)
					{
						//this is the middle entry, and belongs to the main diagonal
						diagonalLength = mOrder;
						startRow = mOrder - 1;
						startCol = 0;
						
					}
					else if (index < midPoint)
					{
						//this is the set of diagonals starting at row 0, coming from right
						diagonalLength = wordlen + index;
						startRow = wordlen + index - 1;
						startCol = 0;
						
					}
					else if (index > midPoint)
					{
						//this is the set of diagonals starting at column 0, from top to bottom
						diagonalLength = wordlen + (randListDiagonals.length - 1) - index;
						startCol = index - midPoint;
						startRow = mOrder - 1;
						
					}
					
					int[] randList = Utils.getRandList(diagonalLength - wordlen + 1);
					for (j = 0; j < randList.length; j++)
					{
						if (foundMatch(nextWord, startRow - randList[j], startCol + randList[j], direction))
						{
							foundMatch = true;
							matchRow = startRow - randList[j];
							matchCol = startCol + randList[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_TOP_LEFT)
			{
				/*

				 *      0 1 2 3 4 5 6 7 
				 *      _ _ _ _ _ _ _ _ 
				 *   0 |_|_|_|_|_|_|_|_|
				 *   1 |_|_|_|_|_|_|_|_|
				 *   2 |_|_|_|_|_|_|_|_|
				 *   3 |_|_|_|_|_|_|_|_|
				 *   4 |_|_|_|_|_|_|_|_| ^
				 *   5 |_|_|_|_|_|_|_|_| |
				 *   6 |_|_|_|_|_|_|_|_| |
				 *   7 |_|_|_|_|_|_|_|_| |
				 *              ________/
				 *  
				 */

				int[] randListDiagonals = Utils.getRandList(2 * (mOrder - wordlen) + 1); //the index will give row id of start of diagonal
				
				//for each diagonal starting for each row, we'll have a separate rand list with entries to be used for word traversals
				for (i = 0; i < randListDiagonals.length; i++)
				{
					int index = randListDiagonals[i];
					int diagonalLength = 0;
					int startRow = -1, startCol = -1;
					
					
					//midpoint is always going to be integer truncation, since the randListDiagonals.length is always going to be odd
					int midPoint = randListDiagonals.length / 2;
					
					//try and determine the actual length of the diagonal given the index
					if (index == midPoint)
					{
						//this is the middle entry, and belongs to the main diagonal
						diagonalLength = mOrder;
						startRow = mOrder - 1;
						startCol = mOrder - 1;
						
					}
					else if (index < midPoint)
					{
						//this is the set of diagonals starting at row 0, coming from right
						diagonalLength = wordlen + index;
						startRow = mOrder - 1;
						startCol = wordlen + index - 1;
						
					}
					else if (index > midPoint)
					{
						//this is the set of diagonals starting at column 0, from top to bottom
						diagonalLength = wordlen + (randListDiagonals.length - 1) - index;
						startCol = mOrder - 1;
						startRow = mOrder - index + midPoint - 1;
						
					}
					
					int[] randList = Utils.getRandList(diagonalLength - wordlen + 1);
					for (j = 0; j < randList.length; j++)
					{
						if (foundMatch(nextWord, startRow - randList[j], startCol - randList[j], direction))
						{
							foundMatch = true;
							matchRow = startRow - randList[j];
							matchCol = startCol - randList[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}
			else if (direction == DIR_BOTTOM_LEFT)
			{
				/*
                                ________
				 *      0 1 2 3 4 5 6 7 \
				 *      _ _ _ _ _ _ _ _  |
				 *   0 |_|_|_|_|_|_|_|_| |
				 *   1 |_|_|_|_|_|_|_|_| |
				 *   2 |_|_|_|_|_|_|_|_| |
				 *   3 |_|_|_|_|_|_|_|_| V
				 *   4 |_|_|_|_|_|_|_|_|  
				 *   5 |_|_|_|_|_|_|_|_|   
				 *   6 |_|_|_|_|_|_|_|_|  
				 *   7 |_|_|_|_|_|_|_|_|  
				 *              
				 *  
				 */

				int[] randListDiagonals = Utils.getRandList(2 * (mOrder - wordlen) + 1); //the index will give row id of start of diagonal
				
				//for each diagonal starting for each row, we'll have a separate rand list with entries to be used for word traversals
				for (i = 0; i < randListDiagonals.length; i++)
				{
					int index = randListDiagonals[i];
					int diagonalLength = 0;
					int startRow = -1, startCol = -1;
					
					
					//midpoint is always going to be integer truncation, since the randListDiagonals.length is always going to be odd
					int midPoint = randListDiagonals.length / 2;
					
					//try and determine the actual length of the diagonal given the index
					if (index == midPoint)
					{
						//this is the middle entry, and belongs to the main diagonal
						diagonalLength = mOrder;
						startRow = 0;
						startCol = mOrder - 1;
						
					}
					else if (index < midPoint)
					{
						//this is the set of diagonals starting at row 0, coming from right
						diagonalLength = wordlen + index;
						startRow = 0;
						startCol = wordlen + index - 1;
						
					}
					else if (index > midPoint)
					{
						//this is the set of diagonals starting at column 0, from top to bottom
						diagonalLength = wordlen + (randListDiagonals.length - 1) - index;
						startCol = mOrder - 1;
						startRow = index - midPoint;
						
					}
					
					int[] randList = Utils.getRandList(diagonalLength - wordlen + 1);
					for (j = 0; j < randList.length; j++)
					{
						if (foundMatch(nextWord, startRow + randList[j], startCol - randList[j], direction))
						{
							foundMatch = true;
							matchRow = startRow + randList[j];
							matchCol = startCol - randList[j];
							break;
						}
					}
					
					if (foundMatch)
					{
						break;
					}
				}
			}

		}
		
		//Log.d(null, "after while loop, foundMatch = " + foundMatch);
		if (foundMatch)
		{
			//update mWords and mWordMappingList with new word
			mWords.add(nextWord);
			int[] val = new int[] {mWords.size() - 1, direction, matchRow, matchCol};
			
			mWordMappingList.add(val);
			
			//update the grid
			fillWordForDirection(nextWord, direction, matchRow, matchCol);
		}
		
		return foundMatch;
	}

	
	private void buildWordSearch()
	{
		int numWords = 0;
		String nextWord = new String();	
		
		int numToFetch = MIN_WORDS * MAX_TRIES;
		if (numToFetch < MIN_WORDS_TO_FETCH)
		{
			numToFetch = MIN_WORDS_TO_FETCH;
		}

		Utils.Log("buildWordSearch, mOrder = " + mOrder + ", numToFetch = " + numToFetch);
		Pair<ArrayList<WordShortInfo>,Integer> pair = mWordDbAdapter.fetchWordsOfCommonTypeNotLongerThan(mPlayType, mIsPlay, mLevelOrPracticeType, numToFetch, mOrder);
		ArrayList<WordShortInfo> wordList = pair.first;
		mSelectedType = pair.second;
		int i = 0;
			while (numWords < mMaxWords && i < wordList.size())
			{
				WordShortInfo wordInfo = wordList.get(i);
				
				//randomly choose a direction that we want to attempt to fit the next word in
				Random randWordOrMeaning = new Random();
				int dir = randWordOrMeaning.nextInt(8) + 1;
				
				//get the next word to try and fit into the puzzle
				nextWord = wordInfo.word.toUpperCase();
				nextWord = Utils.sanitizeString(nextWord, true, false, true, true, false);
				
				//build it!
				boolean retval = buildNextWord(nextWord, dir);
				if (retval)
				{
					numWords ++;
				}
				
				i++;
			}
	}
	
	private void fillWordForDirection(String word, int dir, int startRow, int startCol)
	{
		int i;
		switch (dir)
		{
		case WordSearch.DIR_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow][startCol + i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow][startCol - i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_BOTTOM:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow + i][startCol] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_TOP:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow - i][startCol] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_TOP_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow - i][startCol + i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_BOTTOM_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow + i][startCol + i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_BOTTOM_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow + i][startCol - i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_TOP_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				mCurrentGrid[startRow - i][startCol - i] = word.charAt(i);
			}
			break;
		}
	}
}