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
import java.util.Iterator;
import com.ezeeideas.wordjam.WordDbAdapter.WordShortInfo;

/**
 * This is the primary, core class the contains the core algorithm that
 * BUILDS the crossword.
 */
public class Crossword
{
	public Crossword(int order, WordDbAdapter dbAdapter, int playType, boolean isPlay, int levelOrPracticeType)
	{
		mOrder = order;
		
		mPlayType = playType;
		mIsPlay = isPlay;
		mLevelOrPracticeType = levelOrPracticeType;
		
		/**
		 * The minimum number of words to be filled (excluding fillers) for this crossword.
		 * This is based on experimentation.  One a 8x8 crossword, it's hard to find more
		 * than 6 words, so this is being set to 2 less than the order for now.
		 */
		mMinWords = Math.max(MIN_WORDS, order - 2);

		//open databases
    	mWordDbAdapter = dbAdapter;
    	
    	initializeValues();
    	
		buildCrossword();
		Utils.Log("################### Filled Ratio: " + getFilledRatio() + " ####################");
		
		if (mWords.size() < mMinWords || getFilledRatio() < MIN_FILLED_RATIO)
		{
			Utils.Log("Building the crossword again!!!");
			//we failed to build a good crossword the first time.  Try one more time
			initializeValues();
			buildCrossword();
			Utils.Log("################### New Filled Ratio: " + getFilledRatio() + " ####################");
		}
	}
	
	private void initializeValues()
	{
    	/**
    	 * Initialize values
    	 */
    	mAcrossList = new Vector<int[]>();
    	mDownList = new Vector<int[]>();
    	mWords = new Vector<String>();
    	mMeanings = new Vector<String>();
    	mCrosswordMatrix = new char[mOrder][mOrder];
    	mCrosswordMap = new int[mOrder][mOrder][4];
    	int i, j;
    	for (i = 0; i < mOrder; i++)
    	{
    		for (j = 0; j < mOrder; j++)
    		{
    			mCrosswordMatrix[i][j] = '#';
    			mCrosswordMap[i][j][0] = -1;
    			mCrosswordMap[i][j][2] = -1;
    		}
    	}
	}
	
	public Vector<int[]> getDownList()
	{
		return mDownList;
	}
	
	public Vector<int[]> getAcrossList()
	{
		return mAcrossList;
	}
	
	public Vector<String> getWordList()
	{
		return mWords;
	}
	
	public Vector<String> getMeaningList()
	{
		return mMeanings;
	}
	
	public int[][][] getCrosswordMap()
	{
		return mCrosswordMap;
	}
	
	/**
	 * Build the next word given the word list.
	 * 
	 * @param direction Word Direction
	 * @param wordList The list of words that needs to be used for building the next word
	 * @param startIndex The start index into the word from where scanning must start
	 * @return Two values - 0: the index into the wordList that corresponds to the found word, 1: the new start index into the wordList for subsequent calls
	 */
	private int[] buildNextWord(int direction, ArrayList<WordShortInfo> wordList, int startIndex)
	{
		int[] retVals = new int[2];
		int numTries = 0;
		String nextWord = new String();
		String nextMeaning = new String();
		boolean foundMatch = false;
		int matchRow = -1, matchCol = -1;
		int wordIndex = startIndex;
		while (!foundMatch && numTries < MAX_TRIES && wordIndex < wordList.size())
		{
			numTries ++;
						
			nextWord = wordList.get(wordIndex).word.toUpperCase();
			nextWord = Utils.sanitizeString(nextWord, true, false, true, true, false);
			
			nextMeaning = wordList.get(wordIndex).meaning;
			retVals[0] = wordIndex;
			wordIndex++;
			
			int wordlen = nextWord.length();
					
			if (direction == DIR_ACROSS)
			{
				if (mDownList.size() > 0)
				{
					Iterator<int[]> it = mDownList.iterator();
					while (!foundMatch && it.hasNext())
					{
						int[] val = (int [])it.next();
						int x = val[1];
						int y = val[2];
						String word = mWords.elementAt(val[3]);
						int i;
						for (i = 0; i < word.length(); i++)
						{
							int row = x + i;
							int col = y;
							//int low = 0;
							
							int start = Math.max(0, col - wordlen + 1);
							int end = Math.min(mOrder - wordlen, col);
							
							int k;
							for (k = start; k <= end; k++)
							{
								if (goodMatch(nextWord, row, k, direction))
								{
									foundMatch = true;
									matchRow = row;
									matchCol = k;
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
				else if (mAcrossList.size() > 0)
				{
					int k;
					for (k = 0; k < mOrder; k++)
					{
						if (goodMatch(nextWord, k, 0, direction))
						{
							foundMatch = true;
							matchRow = k;
							matchCol = 0;
							break;
						}
					}
				}
				else
				{
					// no words yet
					foundMatch = true;
					matchRow = 0;
					matchCol = 0;
					break;
				}
			}
			else if (direction == DIR_DOWN)
			{
				if (mAcrossList.size() > 0)
				{
					Iterator<int[]> it = mAcrossList.iterator();
					while (!foundMatch && it.hasNext())
					{
						int[] val = (int [])it.next();
						int x = val[1];
						int y = val[2];
						String word = mWords.elementAt(val[3]);
						int i;
						for (i = 0; i < word.length(); i++)
						{
							int row = x;
							int col = y + i;
							
							int start = Math.max(0, row - wordlen + 1);
							int end = Math.min(mOrder - wordlen, row);
							
							int k;
							for (k = start; k <= end; k++)
							{
								if (goodMatch(nextWord, k, col, direction))
								{
									foundMatch = true;
									matchRow = k;
									matchCol = col;
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
				else if (mDownList.size() > 0)
				{
					int k;
					for (k = 0; k < mOrder; k++)
					{
						if (goodMatch(nextWord, 0, k, direction))
						{
							foundMatch = true;
							matchRow = 0;
							matchCol = k;
							break;
						}
					}
				}
				else
				{
					// No words yet
					foundMatch = true;
					matchRow = 0;
					matchCol = 0;
					break;
				}
			}
		}
		
		if (foundMatch)
		{
			mWords.add(nextWord);
			
			nextMeaning = Utils.sanitizeString(nextMeaning, false, false, false, false, true);
			mMeanings.add(nextMeaning);
			
			if (direction == DIR_ACROSS)
			{
				
				int index = -1;

				int[] newEntry = {index, matchRow, matchCol, mWords.size() - 1, mMeanings.size() - 1};
				mAcrossList.add(newEntry);
				int k;
				for (k = 0; k < nextWord.length(); k++)
				{
					mCrosswordMatrix[matchRow][matchCol + k] = nextWord.charAt(k);
					mCrosswordMap[matchRow][matchCol + k][0] = mAcrossList.size() - 1;
					mCrosswordMap[matchRow][matchCol + k][1] = k;
				}
			}
			else
			{
				int index = -1;

				int[] newEntry = {index, matchRow, matchCol, mWords.size() - 1, mMeanings.size() - 1};
				mDownList.add(newEntry);
				int k;
				for (k = 0; k < nextWord.length(); k++)
				{
					mCrosswordMatrix[matchRow + k][matchCol] = nextWord.charAt(k);
					mCrosswordMap[matchRow + k][matchCol][2] = mDownList.size() - 1;
					mCrosswordMap[matchRow + k][matchCol][3] = k;
				}
			}
		}
		else
		{
			nextWord = "";
		}

		retVals[1] = wordIndex;
		return retVals;
	}
	
	/**
	 * Build the next "Filler".  Fillers are words that are used to fill empty spaces in an
	 * already partially built crossword by using regular expression matches.
	 * 
	 * @param direction The word direction
	 * @param row Row of the word start
	 * @param col Column of the word start
	 * @param chosenWords The list of chosen words
	 * @return The next filler word chosen
	 */
	private String buildNextFiller(int direction, int row, int col, Vector<String> chosenWords)
	{
		String nextWord = new String();
		String nextMeaning = new String();
		boolean foundMatch = false;
		int matchRow = row, matchCol = col;
		boolean foundCrossing = false;
		String regex = new String();
		
		if (direction == DIR_ACROSS)
		{
			if (col > 0 && mCrosswordMatrix[row][col-1] != '#')
			{
				//invalid start
				return nextWord;
			}
			
			//boolean condition = true;
			int j = col;
		
			while (j < mOrder)
			{
				if ((mCrosswordMatrix[row][j] == '#') && (row == 0 || mCrosswordMatrix[row-1][j] == '#') && (row == mOrder-1 || mCrosswordMatrix[row+1][j] == '#'))
				{
					//all's good
					regex = regex.concat("_");
				}
				else if ((mCrosswordMatrix[row][j] != '#') 
						&& (j == mOrder-1 || mCrosswordMatrix[row][j+1] == '#') 
						&& ((row == 0 && mCrosswordMatrix[row+1][j] != '#') 
								|| (row == mOrder-1 && mCrosswordMatrix[row-1][j] != '#') 
								|| (row > 0 && row < mOrder-1 && (mCrosswordMatrix[row-1][j] != '#' || mCrosswordMatrix[row+1][j] != '#'))))
				{
					//alls's good
					foundCrossing = true;
					regex = regex.concat(Character.toString(mCrosswordMatrix[row][j]));
				}
				else
				{
					break;
				}
				
				j++;
			}
			
			if (j == mOrder)
			{
				//Log.d(null, "reached end of regex, so regex is now [" + regex + "]");
				// regex is good and till the end
			}
			//TODO: is this required?
			/*
			else if ((j - col >= MIN_WORD_LEN) && (mCrosswordMatrix[row][j-1] == '#'))
			{
				regex = regex.substring(0, j-1-col);
				Log.d(null, "cutting down regex to [" + regex + "]");
			}*/
			
			if (regex.length() >= MIN_WORD_LEN && j < mOrder && mCrosswordMatrix[row][j] != '#') // step back 2 steps
			{
				regex = regex.substring(0, regex.length() - 1);
			}
			
		}
		else if (direction == DIR_DOWN)
		{
			if (row > 0 && mCrosswordMatrix[row - 1][col] != '#')
			{
				//invalid start
				return nextWord;
			}
			
			//boolean condition = true;
			int i = row;
			
			while (i < mOrder)
			{
				if ((mCrosswordMatrix[i][col] == '#') && (col == 0 || mCrosswordMatrix[i][col-1] == '#') && (col == mOrder-1 || mCrosswordMatrix[i][col+1] == '#'))
				{					
					//all's good
					regex = regex.concat("_");
				}
				else if ((mCrosswordMatrix[i][col] != '#') 
						&& (i == mOrder-1 || mCrosswordMatrix[i+1][col] == '#') 
						&& ((col == 0 && mCrosswordMatrix[i][col+1] != '#') 
								|| (col == mOrder-1 && mCrosswordMatrix[i][col-1] != '#') 
								|| (col > 0 && col < mOrder-1 && (mCrosswordMatrix[i][col-1] != '#' || mCrosswordMatrix[i][col+1] != '#'))))
				{
					//alls's good
					foundCrossing = true;
					regex = regex.concat(Character.toString(mCrosswordMatrix[i][col]));
				}
				else
				{
					break;
				}
				
				i++;
			}
			
			if (i == mOrder)
			{
				//Log.d(null, "reached end of regex, so regex is now [" + regex + "]");
				// regex is good and till the end
			}
			//TODO: is this required?
			/*
			else if ((j - col >= MIN_WORD_LEN) && (mCrosswordMatrix[row][j-1] == '#'))
			{
				regex = regex.substring(0, j-1-col);
				Log.d(null, "cutting down regex to [" + regex + "]");
			}
			*/	
			
			if (regex.length() >= MIN_WORD_LEN && i < mOrder && mCrosswordMatrix[i][col] != '#') // step back 2 steps
			{
				regex = regex.substring(0, regex.length() - 1);
			}
		}
		
		//Only validate word if it crosses at least one existing word, and it's longer than 2 characters
		if (foundCrossing && regex.length() >= MIN_WORD_LEN)
		{
			WordDbAdapter.WordShortInfo wordInfo = mWordDbAdapter.fetchRegexWordNotInAndNotLongerThan(mPlayType, mIsPlay, mLevelOrPracticeType, regex, chosenWords, mOrder);
		
			if (wordInfo != null)
			{
				nextWord = wordInfo.word.toUpperCase();
				if (nextWord.matches("^[a-zA-Z]+$"))
				{
					foundMatch = true;
					matchRow = row;
					matchCol = col;
					nextMeaning = wordInfo.meaning;
				}
			}
		}
		
		if (foundMatch)
		{
			nextWord = Utils.sanitizeString(nextWord, true, false, true, false, false);
			mWords.add(nextWord);
			
			nextMeaning = Utils.sanitizeString(nextMeaning, false, false, false, false, true);
			mMeanings.add(nextMeaning);
			
			if (direction == DIR_ACROSS)
			{
				int index = -1;
				
				int[] newEntry = {index, matchRow, matchCol, mWords.size() - 1, mMeanings.size() - 1};
				mAcrossList.add(newEntry);
				int k;
				for (k = 0; k < nextWord.length(); k++)
				{
					mCrosswordMatrix[matchRow][matchCol + k] = nextWord.charAt(k);
					mCrosswordMap[matchRow][matchCol + k][0] = mAcrossList.size() - 1;
					mCrosswordMap[matchRow][matchCol + k][1] = k;
				}
			}
			else if (direction == DIR_DOWN)
			{
				int index = -1;
				
				int[] newEntry = {index, matchRow, matchCol, mWords.size() - 1, mMeanings.size() - 1};
				mDownList.add(newEntry);
				int k;
				for (k = 0; k < nextWord.length(); k++)
				{
					mCrosswordMatrix[matchRow + k][matchCol] = nextWord.charAt(k);
					mCrosswordMap[matchRow + k][matchCol][2] = mDownList.size() - 1;
					mCrosswordMap[matchRow + k][matchCol][3] = k;
				}
			}
		}
		return nextWord;
	}
	
	/**
	 * Check to see if the given word "fits" in the given position on the crossword
	 * for consideration as a "good match".
	 * 
	 * @param word The given word
	 * @param row The row to start the word
	 * @param col The column to start the word
	 * @param direction The direction
	 * @return true if the word matches, false otherwise
	 */
	private boolean goodMatch(String word, int row, int col, int direction)
	{
		boolean success = true;
		int i;
		
		if (word.length() < MIN_WORD_LEN)
		{
			return false;
		}
		
		if (direction == DIR_ACROSS)
		{
			for (i = 0; i < word.length(); i++)
			{
					if (mCrosswordMatrix[row][i + col] == '#')
					{
						if ((row > 0 && mCrosswordMatrix[row-1][i+col] != '#') || (row < mOrder - 1 && mCrosswordMatrix[row+1][i+col] != '#'))
						{
							success = false;
							break;
						}
						if (i == 0 && col > 0 && mCrosswordMatrix[row][i + col - 1] != '#')
						{
							success = false;
							break;
						}
						else if (i == word.length() - 1 && col + i < mOrder - 1 && mCrosswordMatrix[row][i+col+1] != '#')
						{
							success = false;
							break;
						}
					}
					else
					{
						if (mCrosswordMatrix[row][i+col] != word.charAt(i))
						{
							success = false;
							break;
						}
						
						if ((i+col) > 0 && mCrosswordMatrix[row][i+col-1] != '#')
						{
							success = false;
							break;
						}
						
						if ((i+col) < (mOrder-1) && mCrosswordMatrix[row][i+col+1] != '#')
						{
							success = false;
							break;
						}
					}
			}
		}
		else if (direction == DIR_DOWN)
		{
			for (i = 0; i < word.length(); i++)
			{
					if (mCrosswordMatrix[i + row][col] == '#')
					{
						if ((col > 0 && mCrosswordMatrix[i+row][col-1] != '#') || (col < mOrder - 1 && mCrosswordMatrix[i+row][col+1] != '#'))
						{
							success = false;
							break;
						}
						if (i == 0 && row > 0 && mCrosswordMatrix[i+row-1][col] != '#')
						{
							success = false;
							break;
						}
						else if (i == word.length() - 1 && row + i < mOrder - 1 && mCrosswordMatrix[i+row+1][col] != '#')
						{
							success = false;
							break;
						}
					}
					else
					{
						if (mCrosswordMatrix[i+row][col] != word.charAt(i))
						{
							success = false;
							break;
						}
						
						if ((i+row) > 0 && mCrosswordMatrix[i+row-1][col] != '#')
						{
							success = false;
							break;
						}
						
						if ((i+row) < (mOrder-1) && mCrosswordMatrix[i+row+1][col] != '#')
						{
							success = false;
							break;
						}
					}
			}
		}
		return success;
	}
	

	/**
	 * This determines how sparce the crossword is.  This is used as a 
	 * measure to see if we need to build more words into the crossword or not.
	 * @return The filled percentage
	 */
	public int getFilledRatio()
	{
		int numFilled = 0;
		int total = mOrder * mOrder;
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				if (mCrosswordMatrix[i][j] != '#')
				{
					numFilled ++;
				}
			}
		}
		
		return (numFilled * 100 / total);
	}
	
	/**
	 * This is the big daddy of all other routines in this class.  This actually
	 * "builds" the crossword by calling into all other utility functions.
	 * The result is stored in the various member variables.
	 */
	private void buildCrossword()
	{
		int numTries = 0;
		int numWords = 0;
		Vector<String> chosenWords = new Vector<String>();
		
		String nextWord = new String();
		
		int dir;
		Random randWordOrMeaning = new Random();
		if (randWordOrMeaning.nextBoolean())
		{
			dir = DIR_ACROSS;
		}
		else
		{
			dir = DIR_DOWN;
		}
		
		int numToFetch = mMinWords * MAX_TRIES;
		ArrayList<WordShortInfo> wordList  = mWordDbAdapter.fetchRandomWordsNotLongerThan(mPlayType, mIsPlay, mLevelOrPracticeType, mOrder, numToFetch);
		
		int wordIndex = 0;
		int startIndex = 0; //the index in wordList to start scanning for words for this run
		while (numWords < mMinWords && numTries < MAX_TRIES && wordIndex < wordList.size())
		{
			int[] nextWordVals = buildNextWord(dir, wordList, startIndex);
			nextWord = wordList.get(nextWordVals[0]).word;
			startIndex = nextWordVals[1];
			
			if (nextWord.length() > 0)
			{
				Utils.Log("buildNextWord returned nextWord [" + nextWord + "]");
				chosenWords.add(nextWord);
				numWords ++;
				//numTries ++;
			}
			else
			{
				//Utils.Log("buildNextWord returned NO WORD");
			}
			if (dir == DIR_ACROSS)
			{
				dir = DIR_DOWN;
			}
			else
			{
				dir = DIR_ACROSS;
			}
			
			numTries++;
			wordIndex++;
		}
		
		//try the "fillers"
		int numRuns = MAX_FILLER_RUNS;
		
		while (--numRuns >= 0)
		{
			//check fillers for across
			int i, j;
			for (i = 0; i < mOrder; i++)
			{
				for (j = 0; j < mOrder; j++)
				{
					nextWord = buildNextFiller(DIR_ACROSS, i, j, chosenWords);
					numTries ++;
					
					if (nextWord.length() > 0)
					{
						Utils.Log("buildNextFiller returned nextWord [" + nextWord + "]");
						chosenWords.add(nextWord);
					}
					else
					{
						//Utils.Log("buildNextFiller returned NO WORD");
					}
				}
			}
			
			//and now check fillers for down
			for (j = 0; j < mOrder; j++)
			{
				for (i = 0; i < mOrder; i++)
				{
					nextWord = buildNextFiller(DIR_DOWN, i, j, chosenWords);
					numTries ++;
					
					if (nextWord.length() > 0)
					{
						Utils.Log("buildNextFiller returned nextWord [" + nextWord + "]");
						chosenWords.add(nextWord);
					}
					else
					{
						//Utils.Log("buildNextFiller returned NO WORD");
					}
				}
			}
		}
		
		//done building the crossword
		//adjust the numbered for each word
		int i, j;
		int index = 1;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				if ((mCrosswordMap[i][j][0] != -1 && mCrosswordMap[i][j][1] == 0) || (mCrosswordMap[i][j][2] != -1 && mCrosswordMap[i][j][3] == 0))
				{
					if ((mCrosswordMap[i][j][0] != -1) && (mCrosswordMap[i][j][1] == 0))
					{
						int[] val = mAcrossList.elementAt(mCrosswordMap[i][j][0]);
						val[0] = index;
						mAcrossList.setElementAt(val, mCrosswordMap[i][j][0]);
					}
					if ((mCrosswordMap[i][j][2] != -1) && (mCrosswordMap[i][j][3] == 0))
					{
						int[] val = mDownList.elementAt(mCrosswordMap[i][j][2]);
						val[0] = index;
						mDownList.setElementAt(val, mCrosswordMap[i][j][2]);
					}
					index ++;
				}
			}
		}
	}
	
	/***************** Member Variables *******************/
	
	/**
	 * Related to number of tries to build the crossword.
	 */
	private static final int MAX_TRIES = 10; //maximum number of tries to find a 'word' (excluding fillers)
	private static final int MAX_FILLER_RUNS = 5; //Maximum number of tries to fit it 'fillers' (beyond the actual words)
	
	/**
	 * Word lengths.  Max word length is the same as the order of the crossword
	 */
	private static final int MIN_WORD_LEN = 3; //Minimum word length
	
	/**
	 * Quality metrics - minimum number of words as well as minimum filled ratio
	 */
	private static final int MIN_WORDS = 6; //minimum number of words in any crossword.  Refer mMinWords for more details.
	private static final int MIN_FILLED_RATIO = 50; //minimum filled % of the crossword to be called as useful
	
	/**
	 * Explanation of Various Data Structures used to hold crossword data.
	 * 
	 * mAcrossList:
	 * - List used to store the Across Hints for the Crossword
	 * - Each item in the vector corresponds to an entry in this list
	 * - Each item is an integer array of size 5, and can be parsed like this:
	 * 		Index 0: word index (the number you see next to a word in the crossword)
	 * 		Index 1: row corresponding to the first character of the word in the crossword
	 * 		Index 2: col corresponding to the first character of the word in the crossword
	 * 		Index 3: index into the mWords vector pointing to the word
	 * 		Index 4: index into the mMeanings vector pointing to the associated meaning
	 * 
	 * mDownList:
	 * - Similar to mAcrossList
	 * 
	 * mCrosswordMatrix:
	 * - Two dimensional NxN char matrix (N = mOrder)
	 * - First dimension is row number, and second dimension is the column number
	 * - The value of mCrosswordMatrix[row][col] is set to '#' if it is non-fillable (Black box)
	 * - Otherwise, it is set to the char corresponding to that position in the crossword
	 * 
	 * mCrosswordMap:
	 * - Two dimensional NxN int array matrix (N = mOrder)
	 * - In other words, think of it as each element in the crossword matrix points to an int array (the third dimension)
	 * - The value of mCrosswordMap[row][col] is an int array of size 4, with these interpretations:
	 * 		mCrosswordMap[row][col][0] => index into mAcrossList for the word that includes this character.  -1 if none.
	 * 		mCrosswordMap[row][col][1] => index of this particular character within the across word (e.g., in word 'hello', 'o' would set this to 4)
	 * 		mCrosswordMap[row][col][2] => index into mDownList for the word that includes this character.  -1 if none.
	 * 		mCrosswordMap[row][col][3] => index of this particular character within the down word (e.g., in word 'hello', 'o' would set this to 4)
	 * 		
	 */
	
	private Vector<int[]> mAcrossList;
	private Vector<int[]> mDownList;
	private Vector<String> mWords;
	private Vector<String> mMeanings;
	
	private char[][] mCrosswordMatrix;
	private int[][][] mCrosswordMap; // third dim: 0 = index on across list, 1 = index into word for this char, 2 = index on down list, 3 = index into word for this char
	private int mOrder;
	
	WordDbAdapter mWordDbAdapter;
	private static final int DIR_ACROSS = 0;
	private static final int DIR_DOWN = 1;
	
	int mPlayType;
	boolean mIsPlay;
	int mLevelOrPracticeType;
	
	private static int mMinWords; //the minimum number of words that must be filled in for this crossword
}
