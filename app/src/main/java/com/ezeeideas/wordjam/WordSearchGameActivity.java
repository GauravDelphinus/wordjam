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

import android.util.DisplayMetrics;

import java.util.Random;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Vector;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import com.ezeeideas.wordjam.MatchWordsGameActivity.MessageHandler;

public class WordSearchGameActivity extends BaseGameActivity implements OnClickListener, OnTouchListener
{
	@Override
	protected int getLayoutID() 
	{
		return (R.layout.activity_word_search_game);
	}

	@Override
	protected void doInitializeGame() 
	{
		mGameType = Constants.GAME_TYPE_WORD_SEARCH;
		mGameClass = WordSearchGameActivity.class;
		
		mEnableFill = true;
		
		mWords = new Vector<String>();
		
		mFoundWordsList = new Vector<Integer>();
		mWordMappingList = new Vector<int[]>();
		
		mMessageHandler = new MessageHandler();
	}

	@Override
	protected void doHonorTheme() 
	{
		
	}

	@Override
	protected void doRestoreGame() 
	{
		if (mGameData == null)
		{
			return; //should never happen
		}
		
		String[] sections = mGameData.split("@");
		for (String section : sections)
		{
			String[] tokens = section.split("#");
			if (tokens[0].equals(KEY_DATA_ORDER))
			{
				mOrder = Integer.valueOf(tokens[1]);
			}
			else if (tokens[0].equals(KEY_DATA_WORD_TYPE))
			{
				mSelectedType = Integer.valueOf(tokens[1]);
			}
			else if (tokens[0].equals(KEY_DATA_WORDS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					mWords.add(tokens[i]);
				}
			}
			else if (tokens[0].equals(KEY_DATA_WORD_MAPPING_LIST))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					String[] valuesFound = tokens[i].split("%");
					int[] values = new int[4];
					values[0] = Integer.valueOf(valuesFound[0]);
					values[1] = Integer.valueOf(valuesFound[1]);
					values[2] = Integer.valueOf(valuesFound[2]);
					values[3] = Integer.valueOf(valuesFound[3]);
					
					mWordMappingList.add(values);
				}
			}
			else if (tokens[0].equals(KEY_DATA_FOUND_WORDS_LIST))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					mFoundWordsList.add(Integer.valueOf(tokens[i]));
				}
			}
			else if (tokens[0].equals(KEY_DATA_CURRENT_GRID))
			{
				if (mCurrentGrid == null)
				{
					mCurrentGrid = new char[mOrder][mOrder];
					mFoundGrid = new boolean[mOrder][mOrder];
				}
				
				for (int i = 1; i < tokens.length; i++)
				{
					mCurrentGrid[i-1] = tokens[i].toCharArray();
				}
			}
		}
		
		/**
		 * set up the found grid
		 */
		for (int i = 0; i < mFoundWordsList.size(); i++)
		{
			updateFoundGrid(mFoundWordsList.get(i));
		}
		
		/**
		 * Set up the panel with the data
		 */
		mPanel.setOrder(mOrder);
		mPanel.setData(mCurrentGrid, mWordMappingList, mFoundWordsList, mWords, mFoundGrid);
		mPanel.updateCurrentDragPoints(null, -1, -1);
		
		/**
		 * Set up the list views with the new data
		 */
		setupListViews();
		
		//The drawing panel can now be made visible for drawing
		mView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void doSetupViews() 
	{
		mPanel = (WordSearchPanel)findViewById(R.id.wordsearchpanel);
		//mPanel.setOnTouchListener(this);
		
		mView = (View) findViewById(R.id.wordsearchview);
		mView.setOnTouchListener(this);
		mView.setVisibility(View.INVISIBLE); //don't make this visible just yet - do it after the game has been fully restored

		//word list heading
		mWordListHeadingView = (TextView) findViewById(R.id.wordlist_heading);
		
		/**
		 * List Views
		 */
		mLeftLV = (ListView) findViewById(R.id.wordlist_left);
		mRightLV = (ListView) findViewById(R.id.wordlist_right);
	}

	@Override
	protected void doRefreshViews() 
	{
		if (mGameState == Constants.GAME_STATE_COMPLETED || mGameState == Constants.GAME_STATE_FAILED || mGameState == Constants.GAME_STATE_GIVEN_UP)
		{
			//don't allow filling now
			mEnableFill = false;
		}

		//refresh the panel with the current values, and force redraw
		mPanel.setData(mCurrentGrid, mWordMappingList, mFoundWordsList, mWords, mFoundGrid);
		mPanel.invalidate();
	}

	/**
	 * @ORDER#<order of matrix>@WORDTYPE#<word type>@WORDS#<word1>#<word2>@WORDMAPPINGLIST#<index>%<direction>%<row>%<col>#<index>%<direction>%<row>%<col>
	 * 		@FOUNDWORDSLIST#<index1>#<index2>@CURRENTGRID#<char><char><char>#<char><char><char>@
	 * 
	 * - There are no meanings stored as meanings aren't shown in Word Search
	 * - The mFoundGrid isn't stored as it can be computed from mCurrentGrid, mWords and mFoundWordsList
	 * - WORDMAPPINGLIST: same mapping as mWordMappingList in WordSearchTest class
	 * - FOUNDWORDSLIST: index into the WORDMAPPINGLIST
	 * 
	 */
	@Override
	protected String generateGameData() 
	{
		String data = new String();
		
		data += "@" + KEY_DATA_ORDER + "#" + mOrder;
		data += "@" + KEY_DATA_WORD_TYPE + "#" + mSelectedType;
		
		data += "@" + KEY_DATA_WORDS;
		for (int i = 0; i < mWords.size(); i++)
		{
			data += "#" + mWords.elementAt(i);
		}

		data += "@" + KEY_DATA_WORD_MAPPING_LIST;
		for (int i = 0; i < mWordMappingList.size(); i++)
		{
			int[] value = mWordMappingList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]) + "%" + String.valueOf(value[2]) + "%"
					+ String.valueOf(value[3]);
		}
		
		data += "@" + KEY_DATA_FOUND_WORDS_LIST;
		for (int i = 0; i < mFoundWordsList.size(); i++)
		{
			data += "#";
			
			data += String.valueOf(mFoundWordsList.get(i));
		}
		
		data += "@" + KEY_DATA_CURRENT_GRID;
		for (int i = 0; i < mOrder; i++)
		{
			data += "#";
			data += String.valueOf(mCurrentGrid[i]);
		}

		data += "@";
		
		data = Utils.encodeStringForDB(data);
		
		return data;
	}

	@Override
	protected String generateGameDataFromDB() 
	{
		int order = determineOrder();
	
		/**
		 * Generate the new word search information from the DB
		 */
		WordSearch wordSearch = new WordSearch(mContext, order, mWordDbAdapter, mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType);
		Vector<String> words = wordSearch.getWordList();
		Vector<int[]> wordMappingList = wordSearch.getWordMappingList();
		int selectedType = wordSearch.getSelectedType();
		
		/**
		 * Set up the current grid.
		 */
		char[][] currentGrid = new char[order][order];
		for (int i = 0; i < order; i++)
		{
			for (int j = 0; j < order; j++)
			{
				currentGrid[i][j] = '#';
			}
		}
		
		//fill the words in the grid
		for (int i = 0; i < wordMappingList.size(); i++)
		{
			int[] val = (int [])wordMappingList.elementAt(i);
			int wordIndex = val[0];
			int dir = val[1];
			int startRow = val[2];
			int startCol = val[3];
			fillWordForDirection(words.elementAt(wordIndex), dir, startRow, startCol, currentGrid);
		}
		
		//fill the remaining cells with random fillers
		for (int i = 0; i < order; i++)
		{
			for (int j = 0; j < order; j++)
			{
				if (currentGrid[i][j] == '#')
				{
					Random rand = new Random();
					int randIndex = rand.nextInt(mAlphabets.length);
					currentGrid[i][j] = mAlphabets[randIndex];
				}
			}
		}
		
		/**
		 * Now start filling the data
		 */
		String data = new String();
		
		data += "@" + KEY_DATA_ORDER + "#" + order;
		data += "@" + KEY_DATA_WORD_TYPE + "#" + selectedType;
		
		data += "@" + KEY_DATA_WORDS;
		for (int i = 0; i < words.size(); i++)
		{
			data += "#" + words.elementAt(i);
		}

		data += "@" + KEY_DATA_WORD_MAPPING_LIST;
		for (int i = 0; i < wordMappingList.size(); i++)
		{
			int[] value = wordMappingList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]) + "%" + String.valueOf(value[2]) + "%"
					+ String.valueOf(value[3]);
		}
		
		data += "@" + KEY_DATA_FOUND_WORDS_LIST; //empty found words list when the game starts
		
		data += "@" + KEY_DATA_CURRENT_GRID;
		for (int i = 0; i < order; i++)
		{
			data += "#";
			data += String.valueOf(currentGrid[i]);
		}

		data += "@";
		
		data = Utils.decodeStringFromDB(data);
		Utils.Log("generateGameDataFromDB returned [" + data + "]");
		return data;
	}

	@Override
	protected void doHandleGivenUp() 
	{
		mFoundWordsList.clear();
		int i;
		for (i = 0; i < mWordMappingList.size(); i++)
		{
			int val[] = mWordMappingList.elementAt(i);
			mFoundWordsList.add(i);
			updateWordListView(mWords.elementAt(val[0]));
			updateFoundGrid(i);
		}
	}

	@Override
	protected void doHandleHintUsed() 
	{
		int[] notFoundWordsList = new int[mWords.size() - mFoundWordsList.size()];
		
		/**
		 * HACK: Need to check why the game wasn't marked as completed even
		 * though mFoundWordsList includes all words
		 */
		if (notFoundWordsList.length <= 0)
		{
			return;
		}
		int i, j = 0;
		for (i = 0; i < mWordMappingList.size(); i++)
		{
			if (mFoundWordsList.indexOf(i) == -1)
			{
				//word ont yet found
				notFoundWordsList[j++] = i;
			}
		}
		
		//pick a random on of the not found words
		Random rand = new Random();
		int notfoundindex = rand.nextInt(notFoundWordsList.length);
		int notfoundmappingindex = notFoundWordsList[notfoundindex];
		
		mFoundWordsList.add(notfoundmappingindex);
		int val[] = mWordMappingList.elementAt(notfoundmappingindex);
		updateWordListView(mWords.elementAt(val[0]));
		
		String remainingText = String.format(getResources().getString(R.string.number_remaining), mWordMappingList.size() - mFoundWordsList.size());
		mWordListHeadingView.setText(remainingText);
		
		updateFoundGrid(notfoundmappingindex);
	}

	@Override
	protected void doHandleButtonClicked(int id) 
	{
		// TODO Auto-generated method stub
		
	}
	
	public boolean onTouch(View v, MotionEvent event)
	{	
		//only allow drags if we're in the fill mode
		if (!mEnableFill)
		{
			return true;
		}
		Point p = new Point((int)event.getRawX(), (int)event.getRawY());
		int[] cell = mPanel.getSelectedCell(p, false);
		int row = cell[0];
		int col = cell[1];
		
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			if (row != -1 && col != -1)
			{
				//record that we're starting to drag
				mDragInProgress = true;
				mCurrentWordStartRow = row;
				mCurrentWordStartCol = col;
				
				Point[] dragPoints = new Point[2];
				dragPoints[0] = mPanel.getCenterPointForCell(row, col);
				dragPoints[1] = new Point(-1, -1);
				mPanel.updateCurrentDragPoints(dragPoints, row, col);
				mPanel.invalidate();
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			if (mDragInProgress)
			{
				//drag in motion, set the current drag points and force redraw of the panel
				Point[] dragPoints = new Point[2];
				dragPoints[0] = mPanel.getCenterPointForCell(mCurrentWordStartRow, mCurrentWordStartCol);
				
				//offset the raw point for the panel location, as we're dealing with panel coordinates
				Rect r = new Rect(0, 0, 0, 0);
				mPanel.getGlobalVisibleRect(r, null);
				dragPoints[1] = new Point(p.x - r.left, p.y - r.top);
				
				mPanel.updateCurrentDragPoints(dragPoints, mCurrentWordStartRow, mCurrentWordStartCol);
				mPanel.invalidate();
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			if (row != -1 && col != -1)
			{
				String foundWord = getFoundWord(mCurrentWordStartRow, mCurrentWordStartCol, row, col);
				if (foundWord.length() > 0)
				{
					boolean wordFound = false;
					//this is a correct word, so add it to the found words list
					int i;
					for (i = 0; i < mWordMappingList.size(); i++)
					{
						int[] val = mWordMappingList.elementAt(i);
						String reverseWord = new StringBuffer(foundWord).reverse().toString();
						if (mWords.elementAt(val[0]).equals(foundWord) || mWords.elementAt(val[0]).equals(reverseWord))
						{
							if (mFoundWordsList.indexOf(i) == -1)
							{
								mFoundWordsList.add(i);
								//Utils.Log("wordsearchtest: Found word [" + foundWord + "], drag vals [" + mCurrentWordStartRow + ", " + mCurrentWordStartCol + "] -> [" + row + ", " + col + "], stored vals [" + );
								updateWordListView(mWords.elementAt(val[0]));
								
								///
								
								/**
								 * Updating the word stored with the word actually found.  This is required
								 * to make sure that if there are multiple instances of the same word on the grid,
								 * the one that the user actually draws is the one that is rendered (which means
								 * that's the one that's updated on the mWordMappingList).
								 */
								int startRow = mCurrentWordStartRow;
								int startCol = mCurrentWordStartCol;
								int endRow = row;
								int endCol = col;
								if (mWords.elementAt(val[0]).equals(reverseWord))
								{
									startRow = row;
									startCol = col;
									endRow = mCurrentWordStartRow;
									endCol = mCurrentWordStartCol;
								}
								
								int dir = getDirection(startRow, startCol, endRow, endCol);
								int newvals[] = new int[] {val[0], dir, startRow, startCol};
								mWordMappingList.set(i, newvals);
								
								////
								/*
								
								dir = val[1];
								startRow = val[2];
								startCol = val[3];
								endRow = val[2];
								endCol = val[3];
								int wordLen = mWords.elementAt(val[0]).length();
						    	switch (dir)
						    	{
						    	case WordSearch.DIR_TOP_RIGHT:
						    		endRow -= (wordLen - 1);
						    		endCol += (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_RIGHT:
						    		endCol += (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_BOTTOM_RIGHT:
						    		endRow += (wordLen - 1);
						    		endCol += (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_BOTTOM:
						    		endRow += (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_BOTTOM_LEFT:
						    		endRow += (wordLen - 1);
						    		endCol -= (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_LEFT:
						    		endCol -= (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_TOP_LEFT:
						    		endRow -= (wordLen - 1);
						    		endCol -= (wordLen - 1);
						    		break;
						    	case WordSearch.DIR_TOP:
						    		endRow -= (wordLen - 1);
						    		break;
						    	}
						    	String actualWordStored = getFoundWord(startRow, startCol, endRow, endCol);
						    	Utils.Log("gaurav GWS: ");
								Utils.Log("gaurav GWS: index into mWords = " + val[0] + ", storedWord [" + actualWordStored + "], foundWord [" + foundWord + "]");
								Utils.Log("gaurav GWS: original vals: 0 = " + val[0] + ", " + val[1] + ", " + val[2] + ", " + val[3]);
								Utils.Log("gaurav GWS: updated vals: 0 = " + newvals[0] + ", " + newvals[1] + ", " + newvals[2] + ", " + newvals[3]);
								int[] val2 = mWordMappingList.elementAt(i);
								Utils.Log("gaurav GWS: updated vals2: 0 = " + val2[0] + ", " + val2[1] + ", " + val2[2] + ", " + val2[3]);
								Utils.Log("gaurav GWS: ");
							
								
								*/
								///
								
								updateFoundGrid(i);
								
								wordFound = true;
								break;
							}
						}
					}
					
					if (wordFound)
					{
						onRightWordOrLetterFound();
						
						//title for list view
						String remainingText = String.format(getResources().getString(R.string.number_remaining), mWordMappingList.size() - mFoundWordsList.size());
						mWordListHeadingView.setText(remainingText);
						
						/**
						 * Check for game completed
						 */
						if (mFoundWordsList.size() == mWords.size())		
						{
							/**
							 * Game is completed, so send a game completed message.
							 * We need to do this so we can asynchronously handle the
							 * showing of a dialog and dismissal of this activity
							 * rather than doing it in the context of a touch event handler
							 */
							Message msg = Message.obtain();
							msg.what = MESSAGE_CHECK_GAME_COMPLETED;
							mMessageHandler.sendMessage(msg);
						}
					}
					else
					{
						onWrongWordOrLetterFound();
					}
				}
				
				//update the panel with the new data, and force redraw
				mPanel.setData(mCurrentGrid, mWordMappingList, mFoundWordsList, mWords, mFoundGrid);
				
				Point[] dragPoints = new Point[2];
				dragPoints[0] = new Point(-1, -1);
				dragPoints[1] = new Point(-1, -1);
				mPanel.updateCurrentDragPoints(dragPoints, -1, -1);
				
				mPanel.invalidate();
				
				//record that the drag has finished
				mDragInProgress = false;
				mCurrentWordStartRow = mCurrentWordStartCol = -1;
			}
			else
			{
				//the finger was lifted outside the grid, so just dismiss the current drag
				mPanel.updateCurrentDragPoints(null, -1, -1);
				mCurrentWordStartRow = mCurrentWordStartCol = -1;
				mPanel.invalidate();
			}
		}
		
		return true;
		//return super.onTouchEvent(event);
	}
	
	private int getDirection(int startRow, int startCol, int endRow, int endCol)
	{
		if (startRow > endRow && startCol < endCol)
		{
			return WordSearch.DIR_TOP_RIGHT;
		}
		else if (startRow == endRow && startCol < endCol)
		{
			return WordSearch.DIR_RIGHT;
		}
		else if (startRow < endRow && startCol < endCol)
		{
			return WordSearch.DIR_BOTTOM_RIGHT;
		}
		else if (startRow < endRow && startCol == endCol)
		{
			return WordSearch.DIR_BOTTOM;
		}
		else if (startRow < endRow && startCol > endCol)
		{
			return WordSearch.DIR_BOTTOM_LEFT;
		}
		else if (startRow == endRow && startCol > endCol)
		{
			return WordSearch.DIR_LEFT;
		}
		else if (startRow > endRow && startCol > endCol)
		{
			return WordSearch.DIR_TOP_LEFT;
		}
		else if (startRow > endRow && startCol == endCol)
		{
			return WordSearch.DIR_TOP;
		}
		return -1;
	}
	
	private String getFoundWord(int startRow, int startCol, int endRow, int endCol)
	{
		String outString = new String();
	
		if (startRow < 0 || startCol < 0 || endRow < 0 || endCol < 0)
		{
			return outString;
		}
		
		if (startRow >= mOrder || startCol >= mOrder || endRow >= mOrder || endCol >= mOrder)
		{
			return outString;
		}
		
		int i, j;
		if (startRow > endRow && startCol < endCol) //1 top-right
		{
			for (i = startRow, j = startCol; i >= endRow && j <= endCol; i--, j++)
			{
				outString = outString + mCurrentGrid[i][j];
			}
		}
		else if (startRow == endRow && startCol < endCol) //1 right
		{
			for (j = startCol; j <= endCol; j++)
			{
				outString = outString + mCurrentGrid[startRow][j];
			}
		}
		else if (startRow < endRow && startCol < endCol) //3 bottom-right
		{
			for (i = startRow, j = startCol; i <= endRow && j <= endCol; i++, j++)
			{
				outString = outString + mCurrentGrid[i][j];
			}
		}
		else if (startRow < endRow && startCol == endCol) //4 bottom
		{
			for (i = startRow; i <= endRow; i++)
			{
				outString = outString + mCurrentGrid[i][startCol];
			}
		}
		else if (startRow < endRow && startCol > endCol) //5 bottom-left
		{
			for (i = startRow, j = startCol; i <= endRow && j >= endCol; i++, j--)
			{
				outString = outString + mCurrentGrid[i][j];
			}
		}
		else if (startRow == endRow && startCol > endCol) //6 left
		{
			for (j = startCol; j >= endCol; j--)
			{
				outString = outString + mCurrentGrid[startRow][j];
			}
		}
		else if (startRow > endRow && startCol > endCol) //7 top-left
		{
			for (i = startRow, j = startCol; i >= endRow && j >= endCol; i--, j--)
			{
				outString = outString + mCurrentGrid[i][j];
			}
		}
		else if (startRow > endRow && startCol == endCol) //8 top
		{
			for (i = startRow; i >= endRow; i--)
			{
				outString = outString + mCurrentGrid[i][startCol];
			}
		}

		return outString;
	}
	
	private int determineOrder()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int dpi = metrics.densityDpi;
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		
		int wordSearchLengthPixels = Math.min(screenWidth,  screenHeight) * 3/4;
		int minCellPixelSize = (int) (MIN_CELL_SIZE_INCHES * dpi);
		
		int order = wordSearchLengthPixels / minCellPixelSize;
		if (order < MIN_GRID_SIZE)
		{
			order = MIN_GRID_SIZE;
		}
		if (order > MAX_GRID_SIZE)
		{
			order = MAX_GRID_SIZE;
		}

		return order;
	}
	
	private void updateFoundGrid(int mappingIndex)
	{
		int[] val = mWordMappingList.elementAt(mappingIndex);
		int dir = val[1];
		String word = mWords.elementAt(val[0]);
		int startRow = val[2];
		int startCol = val[3];
		int i;
		switch (dir)
		{
		case WordSearch.DIR_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow][startCol + i] = true;
			}
			break;
		case WordSearch.DIR_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow][startCol - i] = true;
			}
			break;
		case WordSearch.DIR_BOTTOM:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow + i][startCol] = true;
			}
			break;
		case WordSearch.DIR_TOP:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow - i][startCol] = true;
			}
			break;
		case WordSearch.DIR_TOP_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow - i][startCol + i] = true;
			}
			break;
		case WordSearch.DIR_BOTTOM_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow + i][startCol + i] = true;
			}
			break;
		case WordSearch.DIR_BOTTOM_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow + i][startCol - i] = true;
			}
			break;
		case WordSearch.DIR_TOP_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				mFoundGrid[startRow - i][startCol - i] = true;
			}
			break;
		}
	}
	
	//class used to manage the list views
	private class WordItem
	{
		String word;
		boolean mFound;
		
		public WordItem(String name, int number, boolean found)
		{
			word = name;
			mFound = found;
		}
		
		@Override
		public String toString()
		{
			return word;
		}
	}
	
	//comparator for each word item
	public class WordItemComparator implements Comparator<WordItem>{
		 
		public int compare(WordItem h1, WordItem h2)
		{
			return ((h1.word.compareTo(h2.word) > 0) ? 1 : ((h1.word.compareTo(h2.word) == 0) ? 0 : -1));
		}
	}
	
	//adapter for the list views
	public class WordAdapter extends ArrayAdapter<WordItem>
	{
		private List<WordItem> mWords;
		
		public WordAdapter(Context context, int textViewResourceId, List<WordItem> words)
		{
			super(context, textViewResourceId, words);
			mWords = words;
		}
		
		public void updateWordFound(String word)
		{
			int i;
			for (i = 0; i < mWords.size(); i++)
			{
				if (mWords.get(i).word.equals(word))
				{
					mWords.get(i).mFound = true;
					break;
				}
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Log.d(null, "################ getView called with position " + position);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.wordsearch_list_item, null);
            }
            
            TextView wordSearchView = (TextView) v.findViewById(R.id.wordsearchrow);
            wordSearchView.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_WORDSEARCH_LIST_ITEM));
            if (mWords.get(position).mFound)
            {
            	wordSearchView.setPaintFlags(wordSearchView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else
            {
            	wordSearchView.setPaintFlags(wordSearchView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
                    if (wordSearchView != null) {
                    	//Log.d(null, "### setting hint Number to [" + Integer.toString(mHints.get(position).hintNumber) + "]");
                    	wordSearchView.setText(mWords.get(position).word);
                    }
            
            return v;
		}
	}
	
	
	private void setupListViews()
	{
		ArrayList<WordItem> fullWordList = new ArrayList<WordItem>(); //temp list that will be sorted, and then left and right lists will be created for list adapters
		int i;
		for (i = 0; i < mWordMappingList.size(); i++)
		{
			int[] val = (int [])mWordMappingList.elementAt(i);
			String word = mWords.elementAt(val[0]);
			fullWordList.add(new WordItem(word, i, false));
		}
		
		Utils.Log("fullWordList size " + fullWordList.size() + ", wordmappinglist size " + mWordMappingList.size());
		Collections.sort(fullWordList, new WordItemComparator());
		
		//title for list view
		String remainingText = String.format(getResources().getString(R.string.number_remaining), fullWordList.size());
		mWordListHeadingView.setText(remainingText);
		
		//set up the left list view
		mLeftList = new ArrayList<WordItem>();
		int numInLeftList = fullWordList.size() / 2;
		int numInRightList = fullWordList.size() - numInLeftList;
		
		for (i = 0; i < numInLeftList; i++)
		{
			mLeftList.add(fullWordList.get(i));
		}
		
		mLeftListAdapter = new WordAdapter(getBaseContext(), R.layout.wordsearch_list_item, mLeftList);
		mLeftLV.setAdapter(mLeftListAdapter);
		
		//set up the right list view
		mRightList = new ArrayList<WordItem>();
		
		for (i = 0; i < numInRightList; i++)
		{
			mRightList.add(fullWordList.get(numInLeftList + i));
		}
		
		
		mRightListAdapter = new WordAdapter(getBaseContext(), R.layout.wordsearch_list_item, mRightList);
		mRightLV.setAdapter(mRightListAdapter);
		
		//if there are already found words, update the list views
		for (i = 0; i < mFoundWordsList.size(); i++)
		{
			int mappingIndex = mFoundWordsList.elementAt(i);
			int val[] = mWordMappingList.elementAt(mappingIndex);
			updateWordListView(mWords.elementAt(val[0]));	
		}
	}
	
	private void updateWordListView(String word)
	{
		mLeftListAdapter.updateWordFound(word);
		mRightListAdapter.updateWordFound(word);
		mLeftListAdapter.notifyDataSetChanged();
		mRightListAdapter.notifyDataSetChanged();
	}
	
	private void fillWordForDirection(String word, int dir, int startRow, int startCol, char[][] currentGrid)
	{
		int i;
		switch (dir)
		{
		case WordSearch.DIR_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow][startCol + i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow][startCol - i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_BOTTOM:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow + i][startCol] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_TOP:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow - i][startCol] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_TOP_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow - i][startCol + i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_BOTTOM_RIGHT:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow + i][startCol + i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_BOTTOM_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow + i][startCol - i] = word.charAt(i);
			}
			break;
		case WordSearch.DIR_TOP_LEFT:
			for (i = 0; i < word.length(); i++)
			{
				currentGrid[startRow - i][startCol - i] = word.charAt(i);
			}
			break;
		}
	}
	
	/**
	 * Handler required to process events asynchronously.
	 * For example, we need to check for game completed
	 * and show a dialog and dismiss the activity in the context
	 * that doesn't block touch events, so we do that in a handler
	 */
	
	class MessageHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == MESSAGE_CHECK_GAME_COMPLETED)
			{
				//the game has completed
				onGameCompleted();
			}
		}
	}
	
	/****************** Member Variables *****************/
	
	/**
	 * Constants
	 */
	private static final int MIN_GRID_SIZE = 8;
	private static final int MAX_GRID_SIZE = 8;
	
	private static final double MIN_CELL_SIZE_INCHES = 0.20;  //big enough to allow comfortable selection with the average human finger tip size
	
	/**
	 * The various data structures that hold the 'state' of the Word Search game.
	 */
	private Vector<int[]> mWordMappingList; //list of all words. 0 = index into word list, 1 = direction, 2 = row, 3 = col.  The direction is one of 8 values WordSearch.DIR_*
	private Vector<String> mWords;
	private Vector<Integer> mFoundWordsList; //indeces into the mWordMappingList for words that were found by user
	private int mSelectedType; // type of words selected
	private char[][] mCurrentGrid; // filled with current set of characters.
	private boolean[][] mFoundGrid; // grid with cells where it's set to true if the letter has been 'found', false otherwise	
	private int mOrder; // the order
	
	/**
	 * UI Elements
	 */
	private WordSearchPanel mPanel;
	private View mView; // actual view on which drawing happens
	private TextView mWordListHeadingView;
	
	/**
	 * List View related
	 */
	private ListView mLeftLV, mRightLV; //list views that hold the words	
	private WordAdapter mLeftListAdapter, mRightListAdapter; //list adapters for the left and right lists
	ArrayList<WordItem> mLeftList, mRightList;
	char[] mAlphabets = {'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z'};
	
	private Activity mContext;

	/**
	 * State related
	 */
	private boolean mEnableFill; // user is allowed to fill only if this is set to true
	private boolean mDragInProgress; //drag is in progress
	private int mCurrentWordStartRow, mCurrentWordStartCol; //start row,col for the currently dragged word

	/**
	 * Message handling related.
	 * Note: we need the message handler so we can asynchrnously do something in the context of a touch
	 * event callback without breaking the call hierarchy.  Refer class MessageHandler for more details
	 */
	private MessageHandler mMessageHandler; // this is required to asynchronously process events such as check for game completed
	private static final int MESSAGE_CHECK_GAME_COMPLETED = 100; //id used to identify the message between sender and receiver
	
	/**
	 * Data Keys used for storing/restoring the game.  Refer generateGameDataFromDB for format details.
	 */
	private static final String KEY_DATA_ORDER = "ORDER";
	private static final String KEY_DATA_WORD_TYPE = "WORDTYPE";
	private static final String KEY_DATA_WORDS = "WORDS";
	private static final String KEY_DATA_WORD_MAPPING_LIST = "WORDMAPPINGLIST";
	private static final String KEY_DATA_FOUND_WORDS_LIST = "FOUNDWORDSLIST";
	private static final String KEY_DATA_CURRENT_GRID = "CURRENTGRID";
}