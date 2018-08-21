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
import android.graphics.Point;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Vector;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class CrosswordGameActivity extends BaseGameActivity implements OnClickListener, OnTouchListener
{
	@Override
	protected int getLayoutID()
	{
		return (R.layout.activity_crossword_game);
	}

	@Override
	protected void doInitializeGame() 
	{
		mGameType = Constants.GAME_TYPE_CROSSWORD;
		mGameClass = CrosswordGameActivity.class;
		
		mEnableFill = true;

		mCurrentMatrix = null; //this will be initialized in doRestoreGame only after we know the 'order'
		mCurrentFoundMatrix = null;
		
		mCrosswordMap = null; // this will be initialized in doRestoreGame only after we know the 'order'

		
		mWords = new Vector<String>();
		mMeanings = new Vector<String>();
		
		mAcrossList = new Vector<int[]>();
		mDownList = new Vector<int[]>();
		mFoundWordsList = new Vector<int[]>();
		
		mCurrentRow = mCurrentCol = mCurrentDirection = -1;
		mSelectedWord = new int[] {-1, -1};
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
			else if (tokens[0].equals(KEY_DATA_WORDS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					mWords.add(tokens[i]);
				}
			}
			else if (tokens[0].equals(KEY_DATA_MEANINGS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					mMeanings.add(tokens[i]);
				}
			}
			else if (tokens[0].equals(KEY_DATA_ACROSS_LIST))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					String[] valuesFound = tokens[i].split("%");
					int[] values = new int[5];
					values[0] = Integer.valueOf(valuesFound[0]);
					values[1] = Integer.valueOf(valuesFound[1]);
					values[2] = Integer.valueOf(valuesFound[2]);
					values[3] = Integer.valueOf(valuesFound[3]);
					values[4] = Integer.valueOf(valuesFound[4]);
					
					mAcrossList.add(values);
				}
			}
			else if (tokens[0].equals(KEY_DATA_DOWN_LIST))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					String[] valuesFound = tokens[i].split("%");
					int[] values = new int[5];
					values[0] = Integer.valueOf(valuesFound[0]);
					values[1] = Integer.valueOf(valuesFound[1]);
					values[2] = Integer.valueOf(valuesFound[2]);
					values[3] = Integer.valueOf(valuesFound[3]);
					values[4] = Integer.valueOf(valuesFound[4]);
					
					mDownList.add(values);
				}
			}
			else if (tokens[0].equals(KEY_DATA_FOUND_WORDS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					String[] valuesFound = tokens[i].split("%");
					int[] values = new int[2];
					values[0] = Integer.valueOf(valuesFound[0]);
					values[1] = Integer.valueOf(valuesFound[1]);

					mFoundWordsList.add(values);
				}
			}
			else if (tokens[0].equals(KEY_DATA_CURRENT_MATRIX))
			{
				if (mCurrentMatrix == null)
				{
					mCurrentMatrix = new char[mOrder][mOrder];
					mCurrentFoundMatrix = new boolean[mOrder][mOrder];
				}
				
				for (int i = 1; i < tokens.length; i++)
				{
					mCurrentMatrix[i-1] = tokens[i].toCharArray();
				}
			}
		}
		
		//set up the found matrix
		for (int i = 0; i < mFoundWordsList.size(); i++)
		{
			int values[] = mFoundWordsList.elementAt(i);
			if (values[0] == 0) //across list
			{
				int vals[] = mAcrossList.elementAt(values[1]);
				int row = vals[1];
				int col = vals[2];
				String word = mWords.elementAt(vals[3]);
				for (int j = 0; j < word.length(); j++)
				{
					mCurrentFoundMatrix[row][col + j] = true;
				}
			}
			else if (values[0] == 1) //down list
			{
				int vals[] = mDownList.elementAt(values[1]);
				int row = vals[1];
				int col = vals[2];
				String word = mWords.elementAt(vals[3]);
				for (int j = 0; j < word.length(); j++)
				{
					mCurrentFoundMatrix[row + j][col] = true;
				}
			}
		}
		
		//now initialize the maps
    	mCrosswordMap = new int[mOrder][mOrder][4];
    	
		for (int i = 0; i < mOrder; i++)
		{
			for (int j = 0; j < mOrder; j++)
			{
				mCrosswordMap[i][j][0] = -1;
				mCrosswordMap[i][j][2] = -1;
			}
		}
		
		for (int i = 0; i < mAcrossList.size(); i++)
		{
			int[] values = mAcrossList.elementAt(i);
			
			String word = mWords.elementAt(values[3]);
			int k;
			int row = values[1];
			int col = values[2];
			for (k = 0; k < word.length(); k++)
			{
				mCrosswordMap[row][col + k][0] = i;
				mCrosswordMap[row][col + k][1] = k;
			}
		}
		
		for (int i = 0; i < mDownList.size(); i++)
		{
			int[] values = mDownList.elementAt(i);
			
			String word = mWords.elementAt(values[3]);
			int k;
			int row = values[1];
			int col = values[2];
			for (k = 0; k < word.length(); k++)
			{
				mCrosswordMap[row + k][col][2] = i;
				mCrosswordMap[row + k][col][3] = k;
			}
		}
		
		//Now initialize the panel with the fixed values for the game
		mPanel.initWithOrder(mOrder);
		mPanel.setCrosswordData(mAcrossList, mDownList, mWords, mMeanings, mCrosswordMap);

		setupHints(); // set up the hints views
		
		//The drawing panel can now be made visible for drawing
		mPanel.setVisibility(View.VISIBLE);
	}

	@Override
	protected void doSetupViews() 
	{
		mPanel = (CrosswordPanel)findViewById(R.id.crosswordpanel);
		mPanel.setOnTouchListener(this);
		mPanel.setVisibility(View.INVISIBLE);
		
		mViewFlipper = (ViewFlipper) findViewById(R.id.crossword_view_flipper);
		mViewFlipper.setVisibility(View.VISIBLE);
		
		mFillDirectionImage = (ImageView) findViewById(R.id.fill_direction_image);
		mFillDirectionImage.setOnClickListener(this);
		
		mCloseKeypadButton = (ImageButton) findViewById(R.id.close_keypad_button);
		mCloseKeypadButton.setOnClickListener(this);
		
		mCurrentHintTextView = (TextView) findViewById(R.id.selected_hint_text_view);
		
		//setup listeners on the keypad buttons
		int keypadButtonID = KEYPAD_BUTTON_ID_INDEX;
		LinearLayout ll1 = (LinearLayout) findViewById(R.id.key_buttons_layout1);
		int numButtons = ll1.getChildCount();
		for (int i = 0; i < numButtons; i++)
		{
			View v = ll1.getChildAt(i);
			if (v.getClass() == Button.class)
			{
				v.setId(keypadButtonID++);
				v.setOnClickListener(this);
			}
		}
		LinearLayout ll2 = (LinearLayout) findViewById(R.id.key_buttons_layout2);
		numButtons = ll2.getChildCount();
		for (int i = 0; i < numButtons; i++)
		{
			View v = ll2.getChildAt(i);
			if (v.getClass() == Button.class)
			{
				v.setId(keypadButtonID++);
				v.setOnClickListener(this);
			}
		}
		LinearLayout ll3 = (LinearLayout) findViewById(R.id.key_buttons_layout3);
		numButtons = ll3.getChildCount();
		for (int i = 0; i < numButtons; i++)
		{
			View v = ll3.getChildAt(i);
			if (v.getClass() == Button.class)
			{
				v.setId(keypadButtonID++);
				v.setOnClickListener(this);
			}
		}
		
		LinearLayout ll4 = (LinearLayout) findViewById(R.id.key_buttons_layout4);
		if (ll4 != null)
		{
			numButtons = ll4.getChildCount();
			for (int i = 0; i < numButtons; i++)
			{
				View v = ll4.getChildAt(i);
				if (v.getClass() == Button.class)
				{
					v.setId(keypadButtonID++);
					v.setOnClickListener(this);
				}
			}
		}
		
		//the hints list views
		ListView acrossLV = (ListView) findViewById(R.id.across_hints);
		ListView downLV = (ListView) findViewById(R.id.down_hints);
		acrossLV.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{	
				HintItem item = mAcrossHints.get(position);
				int[] selectedWordData = new int[] {0, item.indexIntoList};
				mPanel.updateSelectedWordData(selectedWordData);
				mPanel.invalidate();
			}
			
		}
		);
		downLV.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{	
				HintItem item = mDownHints.get(position);
				int[] selectedWordData = new int[] {1, item.indexIntoList};
				mPanel.updateSelectedWordData(selectedWordData);
				mPanel.invalidate();
			}
			
		}
		);
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
		mPanel.updateSelectedCell(mCurrentRow, mCurrentCol);
		mPanel.updateCurrentMatrix(mCurrentMatrix, mCurrentFoundMatrix);
		mPanel.updateSelectedWordData(mSelectedWord);
		mPanel.invalidate();
	}

	/**
	 * Generate the game data for the current crossword.
	 * 
	 * @ORDER#<order of matrix>@WORDS#<word1>#<word2>@MEANINGS#<meaning1>#<meaning2>@ACROSSLIST#<0>%<1>%<2>%<3>%<4>#<0>%<1>%<2>%<3>%<4>@DOWNLIST#<0>%<1>%<2>%<3>%<4>
	 * 		@FOUNDWORDS#<listtype>%<index>#<listtype>%<index>@CURRENTMATRIX@<char><%><%><char>#<char><%><%><char>#<char><%><%><char>@
	 * 
	 * - The meanings list size is not required as it's the same as the word list size
	 * - ACROSSLIST: 5 integral values, refer mAcrossList in CrosswordTest class
	 * - DOWNLIST: similar to ACROSSLIST
	 * - FOUNDWORDS: each item has two integers: listtype: 0 for Across, 1 for Down, index: Index into the corresponding list (ACROSSLIST/DOWNLIST)
	 * - CURRENTMATRIX: '%' represent unfillable cells (black), and '*' represents fillable, empty cells.
	 */
	@Override
	protected String generateGameData() 
	{
		String data = new String();
		
		data += "@" + KEY_DATA_ORDER + "#" + mOrder;
		data += "@" + KEY_DATA_WORDS;
		for (int i = 0; i < mWords.size(); i++)
		{
			data += "#" + mWords.elementAt(i);
		}
	
		data += "@" + KEY_DATA_MEANINGS;
		for (int i = 0; i < mMeanings.size(); i++)
		{
			data += "#" + mMeanings.elementAt(i);
		}

		data += "@" + KEY_DATA_ACROSS_LIST;
		for (int i = 0; i < mAcrossList.size(); i++)
		{
			int[] value = mAcrossList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]) + "%" + String.valueOf(value[2]) + "%"
					+ String.valueOf(value[3]) + "%" + String.valueOf(value[4]);
		}
		
		data += "@" + KEY_DATA_DOWN_LIST;
		for (int i = 0; i < mDownList.size(); i++)
		{
			int[] value = mDownList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]) + "%" + String.valueOf(value[2]) + "%"
					+ String.valueOf(value[3]) + "%" + String.valueOf(value[4]);
		}
		
		data += "@" + KEY_DATA_FOUND_WORDS;
		for (int i = 0; i < mFoundWordsList.size(); i++)
		{
			int[] value = mFoundWordsList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]);
		}
		
		data += "@" + KEY_DATA_CURRENT_MATRIX;
		for (int i = 0; i < mOrder; i++)
		{
			data += "#";
			data += String.valueOf(mCurrentMatrix[i]);
		}

		data += "@";
		
		data = Utils.encodeStringForDB(data);
		
		return data;
	}

	@Override
	protected String generateGameDataFromDB() 
	{
		int order = determineOrder();
		Crossword crossword = new Crossword(order, mWordDbAdapter, mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType);
		Vector<String> meanings = crossword.getMeaningList();
		Vector<String> words = crossword.getWordList();
		Vector<int[]> acrossList = crossword.getAcrossList();
		Vector<int[]> downList = crossword.getDownList();
		int[][][] crosswordMap = crossword.getCrosswordMap();
		char[][] currentMatrix = new char[order][order];
		for (int i = 0; i < order; i++)
		{
			for (int j = 0; j < order; j++)
			{
				if (crosswordMap[i][j][0] == -1 && crosswordMap[i][j][2] == -1)
				{
					//non-fillable cell
					currentMatrix[i][j] = '%';
				}
				else
				{
					currentMatrix[i][j] = '*'; //fillable cell
				}
			}
		}
		
		String data = new String();
		
		data += "@" + KEY_DATA_ORDER + "#" + order;
		data += "@" + KEY_DATA_WORDS;
		for (int i = 0; i < words.size(); i++)
		{
			data += "#" + words.elementAt(i);
		}
	
		data += "@" + KEY_DATA_MEANINGS;
		for (int i = 0; i < meanings.size(); i++)
		{
			data += "#" + meanings.elementAt(i);
		}

		data += "@" + KEY_DATA_ACROSS_LIST;
		for (int i = 0; i < acrossList.size(); i++)
		{
			int[] value = acrossList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]) + "%" + String.valueOf(value[2]) + "%"
					+ String.valueOf(value[3]) + "%" + String.valueOf(value[4]);
		}
		
		data += "@" + KEY_DATA_DOWN_LIST;
		for (int i = 0; i < downList.size(); i++)
		{
			int[] value = downList.elementAt(i);
			data += "#";
			
			data += String.valueOf(value[0]) + "%" + String.valueOf(value[1]) + "%" + String.valueOf(value[2]) + "%"
					+ String.valueOf(value[3]) + "%" + String.valueOf(value[4]);
		}
		
		data += "@" + KEY_DATA_FOUND_WORDS; //empty found words list
		
		data += "@" + KEY_DATA_CURRENT_MATRIX;
		for (int i = 0; i < order; i++)
		{
			data += "#";
			data += String.valueOf(currentMatrix[i]);
		}

		data += "@";
		
		data = Utils.decodeStringFromDB(data);
		
		return data;
	}

	@Override
	protected void doHandleGivenUp() 
	{
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				char c = mCurrentMatrix[i][j];
				if (c != '%')
				{
					int acrossIndex = mCrosswordMap[i][j][0];
					int downIndex = mCrosswordMap[i][j][2];
					char checkChar = '*';
					if (acrossIndex != -1)
					{
						int val[] = mAcrossList.elementAt(acrossIndex);
						int wordIndex = val[3];
						int charIndex = mCrosswordMap[i][j][1];
						String word = mWords.elementAt(wordIndex);
						checkChar = word.charAt(charIndex);
					}
					else if (downIndex != -1)
					{
						int val[] = mDownList.elementAt(downIndex);
						int wordIndex = val[3];
						int charIndex = mCrosswordMap[i][j][3];
						String word = mWords.elementAt(wordIndex);
						checkChar = word.charAt(charIndex);
					}
					if (c != checkChar)
					{
						mCurrentMatrix[i][j] = checkChar;
					}
				}	
			}
		}
	}

	@Override
	protected void doHandleHintUsed() 
	{
		Vector<int[]> incorrectWords = new Vector<int[]>(); // 0 = 0 if across, 1 if down, 1 = index into the corresponding list

		int i;
		for (i = 0; i < mAcrossList.size(); i++)
		{
			int val[] = mAcrossList.elementAt(i);
			int wordIndex = val[3];
			String word = mWords.elementAt(wordIndex);
			int row = val[1];
			int col = val[2];
			int j;
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentMatrix[row][col + j] != word.charAt(j))
				{
					incorrectWords.add(new int[] {0, i});
					break;
				}
			}
		}
		for (i = 0; i < mDownList.size(); i++)
		{
			int val[] = mDownList.elementAt(i);
			int wordIndex = val[3];
			String word = mWords.elementAt(wordIndex);
			int row = val[1];
			int col = val[2];
			int j;
			for (j = 0; j < word.length(); j++)
			{
				if (mCurrentMatrix[row + j][col] != word.charAt(j))
				{
					incorrectWords.add(new int[] {1, i});
					break;
				}
			}
		}
		
		if (incorrectWords.size() > 1)
		{
			// can show hints
			Random rand = new Random();
			int incorrectWordIndex = rand.nextInt(incorrectWords.size());
			
			int value[] = incorrectWords.elementAt(incorrectWordIndex);
			if (value[0] == 0)
			{
				//across list
				int val[] = mAcrossList.elementAt(value[1]);
				int wordIndex = val[3];
				String word = mWords.elementAt(wordIndex);
				int row = val[1];
				int col = val[2];
				for (i = 0; i < word.length(); i++)
				{
					mCurrentMatrix[row][col + i] = word.charAt(i);
					mCurrentFoundMatrix[row][col+i] = true;
				}
				int foundVals[] = {0, value[1]};
				mFoundWordsList.add(foundVals);
			}
			else
			{
				//down list
				int val[] = mDownList.elementAt(value[1]);
				int wordIndex = val[3];
				String word = mWords.elementAt(wordIndex);
				int row = val[1];
				int col = val[2];
				for (i = 0; i < word.length(); i++)
				{
					mCurrentMatrix[row + i][col] = word.charAt(i);
					mCurrentFoundMatrix[row+i][col] = true;
				}
				int foundVals[] = {1, value[1]};
				mFoundWordsList.add(foundVals);
			}
		}
	}

	@Override
	protected void doHandleButtonClicked(int id) 
	{
		if (!mKeypadActive)
			return;
		
		if (id == R.id.close_keypad_button)
		{
			closeKeypad();
			doRefreshViews();
		}
		else //keypad
		{
			Button keypadButton = (Button) findViewById(id);
			char selectedChar = keypadButton.getText().charAt(0);
			
			processClick(mCurrentRow, mCurrentCol, selectedChar);
		}
	}
	
	public boolean onTouch(View v, MotionEvent event)
	{	
		if (mEnableFill)
		{
			Point p = new Point((int)event.getRawX(), (int)event.getRawY());
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				int[] cell = mPanel.getSelectedCell(p);
				int row = cell[0];
				int col = cell[1];
				if (row != -1 && col != -1)
				{
					if (mCurrentFoundMatrix[row][col] == false)
					{
						//check to see if the click happened on a non-black (fillable) cell
						if (mCrosswordMap[row][col][0] != -1 || mCrosswordMap[row][col][2] != -1)
						{
							if (row == mCurrentRow && col == mCurrentCol)
							{
								//re-tapping on already selected cell will toggle the word direction, if it can
								if (mCrosswordMap[row][col][0] != -1 && mCrosswordMap[row][col][2] != -1)
								{
									//existing word has both directions
									if (mSelectedWord[0] == 0)
									{
										mSelectedWord[0] = 1; //toggle across to downlist
										mSelectedWord[1] = mCrosswordMap[row][col][2];
										mCurrentDirection = WORD_DIRECTION_VERTICAL;
									}
									else
									{
										mSelectedWord[0] = 0; //toggle down to across list
										mSelectedWord[1] = mCrosswordMap[row][col][0];
										mCurrentDirection = WORD_DIRECTION_HORIZONTAL;
									}
								}
							}
							else
							{
								//tapping for the first time
								if (mCrosswordMap[row][col][0] != -1 && mCrosswordMap[row][col][2] != -1)
								{
									//if both directions are possible, choose across over down except when
									//the character tapped is the first letter of the word
									if ((row == 0 && col > 0) || (row > 0 && mCurrentMatrix[row-1][col] == '%'))
									{
										//choose vertical
										mSelectedWord[0] = 1; //down list
										mSelectedWord[1] = mCrosswordMap[row][col][2];
										mCurrentDirection = WORD_DIRECTION_VERTICAL;
									}
									else
									{
										//choose horizontal
										mSelectedWord[0] = 0; //across list
										mSelectedWord[1] = mCrosswordMap[row][col][0];
										mCurrentDirection = WORD_DIRECTION_HORIZONTAL;
									}
								}
								else if (mCrosswordMap[row][col][0] != -1)
								{
									mSelectedWord[0] = 0; //across list
									mSelectedWord[1] = mCrosswordMap[row][col][0];
									mCurrentDirection = WORD_DIRECTION_HORIZONTAL;
								}
								else
								{
									mSelectedWord[0] = 1; //down list
									mSelectedWord[1] = mCrosswordMap[row][col][2];
									mCurrentDirection = WORD_DIRECTION_VERTICAL;
								}

							}
							
							mCurrentRow = row;
							mCurrentCol = col;
							showOrUpdateKeypad(row, col);
							doRefreshViews();
						}
					}
				}
			}
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * Check to see if the word is "filled", not necessarily "complete" yet.
	 * 
	 * @param i row
	 * @param j column
	 * @param dir Direction of word, one of WORD_DIRECTION_*
	 * @return true if word is filled, false if not yet filled
	 */
	private boolean isWordFilled(int i, int j, int dir)
	{
		boolean isFilled = false;
		
		if (dir == WORD_DIRECTION_HORIZONTAL)
		{
			int acrossIndex = mCrosswordMap[i][j][0];	
			if (acrossIndex != -1)
			{
				int val[] = mAcrossList.elementAt(acrossIndex);
				int wordIndex = val[3];
				int charIndex = mCrosswordMap[i][j][1];
				String word = mWords.elementAt(wordIndex);
				
				boolean filled = true;
				for (int k = 0; k < word.length(); k++)
				{
					if (mCurrentMatrix[i][j - charIndex + k] == '*')
					{
						filled = false;
						break;
					}
				}
				if (filled)
				{
					isFilled = true;
				}
			}
		}
		else if (dir == WORD_DIRECTION_VERTICAL)
		{
			int downIndex = mCrosswordMap[i][j][2];
			if (downIndex != -1)
			{
				int val[] = mDownList.elementAt(downIndex);
				int wordIndex = val[3];
				int charIndex = mCrosswordMap[i][j][3];
				String word = mWords.elementAt(wordIndex);
				
				boolean filled = true;
				for (int k = 0; k < word.length(); k++)
				{
					if (mCurrentMatrix[i - charIndex + k][j] == '*')
					{
						filled = false;
						break;
					}
				}
				if (filled)
				{
					isFilled = true;
				}
			}
		}
		
		return isFilled;
	}
	
	/**
	 * Check to see if the word at the given position has been completed
	 * successfully.  Note that it could be either vertical or horizontal or both.
	 * 
	 * @param i Row of the letter
	 * @param j Column of the letter
	 * @param dir Direction, one of WORD_DIRECTION_*
	 * 
	 * @return true if the word is complete, false otherwise
	 */
	private boolean isWordComplete(int i, int j, int dir)
	{	
		boolean wordComplete = false;
		
		char c = mCurrentMatrix[i][j];
		if (c != '%')
		{
			if (dir == WORD_DIRECTION_HORIZONTAL)
			{
				int acrossIndex = mCrosswordMap[i][j][0];	
				if (acrossIndex != -1)
				{
					int val[] = mAcrossList.elementAt(acrossIndex);
					int wordIndex = val[3];
					int charIndex = mCrosswordMap[i][j][1];
					String word = mWords.elementAt(wordIndex);
					
					boolean match = true;
					for (int k = 0; k < word.length(); k++)
					{
						if (word.charAt(k) != mCurrentMatrix[i][j-charIndex+k])
						{
							match = false;
							break;
						}
					}
					if (match)
					{
						wordComplete = true;
					}
				}
			}
			else if (dir == WORD_DIRECTION_VERTICAL)
			{
				int downIndex = mCrosswordMap[i][j][2];
				if (downIndex != -1)
				{
					int val[] = mDownList.elementAt(downIndex);
					int wordIndex = val[3];
					int charIndex = mCrosswordMap[i][j][3];
					String word = mWords.elementAt(wordIndex);
					
					boolean match = true;
					for (int k = 0; k < word.length(); k++)
					{
						if (word.charAt(k) != mCurrentMatrix[i-charIndex+k][j])
						{
							match = false;
							break;
						}
					}
					if (match)
					{
						wordComplete = true;
					}
				}
			}
		}	
		
		return wordComplete;
	}
	
	/**
	 * Mark the word as complete so it can no longer be allowed to be modified.
	 * 
	 * @param i row
	 * @param j column
	 * @param dir Direction of word, one of WORD_DIRECTION_*
	 */
	private void markWordComplete(int i, int j, int dir)
	{
		if (dir == WORD_DIRECTION_HORIZONTAL)
		{
			int acrossIndex = mCrosswordMap[i][j][0];	
			if (acrossIndex != -1)
			{
				int val[] = mAcrossList.elementAt(acrossIndex);
				int wordIndex = val[3];
				int charIndex = mCrosswordMap[i][j][1];
				String word = mWords.elementAt(wordIndex);
				
				for (int k = 0; k < word.length(); k++)
				{
					mCurrentFoundMatrix[i][j-charIndex+k] = true;
				}
			}
		}
		else if (dir == WORD_DIRECTION_VERTICAL)
		{
			int downIndex = mCrosswordMap[i][j][2];
			if (downIndex != -1)
			{
				int val[] = mDownList.elementAt(downIndex);
				int wordIndex = val[3];
				int charIndex = mCrosswordMap[i][j][3];
				String word = mWords.elementAt(wordIndex);
				
				for (int k = 0; k < word.length(); k++)
				{
					mCurrentFoundMatrix[i-charIndex+k][j] = true;
				}
			}
		}
	}
	
	/**
	 * Check to see if the crossword has been completely filled.
	 * It may or may not be correct, but this is only checking to see if it is "filled".
	 * 
	 * @return true if the crossword is filled, false otherwise
	 */
	public boolean isCrosswordFilled()
	{
		boolean filled = true;
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				if (mCurrentMatrix[i][j] == '*')
				{
					//found empty fillable cell
					filled = false;
					break;
				}
			}
		}
		
		return filled;
	}
	
	/**
	 * Check to see if the crossword is now complete.
	 * 
	 * @return true if complete, false otherwise
	 */
	private boolean isCrosswordComplete()
	{
		boolean correct = true;
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				char c = mCurrentMatrix[i][j];
				if (c != '%')
				{
					int acrossIndex = mCrosswordMap[i][j][0];
					int downIndex = mCrosswordMap[i][j][2];
					char checkChar = '*';
					if (acrossIndex != -1)
					{
						int val[] = mAcrossList.elementAt(acrossIndex);
						int wordIndex = val[3];
						int charIndex = mCrosswordMap[i][j][1];
						String word = mWords.elementAt(wordIndex);
						checkChar = word.charAt(charIndex);
					}
					else if (downIndex != -1)
					{
						int val[] = mDownList.elementAt(downIndex);
						int wordIndex = val[3];
						int charIndex = mCrosswordMap[i][j][3];
						String word = mWords.elementAt(wordIndex);
						checkChar = word.charAt(charIndex);
					}
					if (c != checkChar)
					{
						correct = false;
						break;
					}
				}	
			}
			if (!correct)
			{
				break;
			}
		}
		
		return correct;
	}
	
	/**
	 * Process the click on the keypad.
	 * 
	 * @param row The row of the cell selected
	 * @param col The column of the cell selected
	 * @param key The new character user chose on the keypad for this cell
	 */
	public void processClick(int row, int col, char key)
	{	
		if (key == ' ')
		{
			mCurrentMatrix[row][col] = '*';
		}
		else
		{
			mCurrentMatrix[row][col] = key;
		}
				
		if (isWordFilled(row, col, WORD_DIRECTION_HORIZONTAL))
		{
			if (isWordComplete(row, col, WORD_DIRECTION_HORIZONTAL))
			{
				markWordComplete(row, col, WORD_DIRECTION_HORIZONTAL);
				onRightWordOrLetterFound();
				int index = mCrosswordMap[row][col][0];
				mFoundWordsList.add(new int[]{0, index});
			}
			else
			{
				onWrongWordOrLetterFound();
			}
		}
		if (isWordFilled(row, col, WORD_DIRECTION_VERTICAL))
		{
			if (isWordComplete(row, col, WORD_DIRECTION_VERTICAL))
			{
				markWordComplete(row, col, WORD_DIRECTION_VERTICAL);
				onRightWordOrLetterFound();
				int index = mCrosswordMap[row][col][2];
				mFoundWordsList.add(new int[]{1, index});
			}
			else
			{
				onWrongWordOrLetterFound();
			}
		}

		if (isCrosswordFilled())
		{
			if (isCrosswordComplete())
			{
				onGameCompleted();
			}
			else
			{
				onGameFailed();
			}
		}
		else
		{
			//skip over and move the selected cell to the next available cell
			if (mCurrentDirection == WORD_DIRECTION_HORIZONTAL)
			{
				while (mCurrentCol < (mOrder - 1) && mCurrentMatrix[mCurrentRow][mCurrentCol + 1] != '%' && mCurrentFoundMatrix[mCurrentRow][mCurrentCol+1] == true)
				{
					mCurrentCol ++;
				}
				if (mCurrentCol < (mOrder - 1) && mCurrentMatrix[mCurrentRow][mCurrentCol + 1] != '%')
				{
					mCurrentCol ++;
				}
				else
				{
					//close the keypad - done typing the word
					closeKeypad();
				}
			}
			else if (mCurrentDirection == WORD_DIRECTION_VERTICAL)
			{
				while (mCurrentRow < (mOrder - 1) && mCurrentMatrix[mCurrentRow + 1][mCurrentCol] != '%' && mCurrentFoundMatrix[mCurrentRow+1][mCurrentCol] == true)
				{
					mCurrentRow ++;
				}
				if (mCurrentRow < (mOrder - 1) && mCurrentMatrix[mCurrentRow + 1][mCurrentCol] != '%')
				{
					mCurrentRow ++;
				}
				else
				{
					//close the keypad
					closeKeypad();
				}
			}
		}
		
		doRefreshViews();
	}
	
	/**
	 * The keypad needs to be closed.
	 */
	private void closeKeypad()
	{
		mCurrentDirection = -1;
		mCurrentRow = -1;
		mCurrentCol = -1;
		mSelectedWord[0] = -1;
		mSelectedWord[1] = -1;
		
		if (mViewFlipper.getDisplayedChild() == 1) //currently showing the keypad
		{
			//Animate to the hints lists
			mViewFlipper.setInAnimation(this, R.anim.in_from_left);
			mViewFlipper.setOutAnimation(this, R.anim.out_to_right);
			
			// Show The Previous Screen
			mViewFlipper.showPrevious();
			
			mKeypadActive = false;
		}		
	}
	
	/**
	 * Show the keypad for the first time, and/or update it
	 * 
	 * @param row The row the user selected
	 * @param col The column the user selected
	 */
	private void showOrUpdateKeypad(int row, int col)
	{
		if (mViewFlipper.getDisplayedChild() == 0) //currently showing the hints list views.
		{
			//Animate to the keypad
			mViewFlipper.setInAnimation(this, R.anim.in_from_right);
			mViewFlipper.setOutAnimation(this, R.anim.out_to_left);
			
			// Show The Next Screen
			mViewFlipper.showNext();
			
			mKeypadActive = true;
		}

		//update the keypad direction image
		if (mCurrentDirection == WORD_DIRECTION_HORIZONTAL)
		{
			mFillDirectionImage.setImageResource(R.drawable.fill_direction_horizontal);
		}
		else if (mCurrentDirection == WORD_DIRECTION_VERTICAL)
		{
			mFillDirectionImage.setImageResource(R.drawable.fill_direction_vertical);
		}

		//update the hint text in the keypad view
		if (mSelectedWord[0] == 0)
		{
			int vals[] = mAcrossList.elementAt(mSelectedWord[1]);
			mCurrentHintTextView.setText(mMeanings.elementAt(vals[4]));
		}
		else if (mSelectedWord[0] == 1) //down
		{
			int vals[] = mDownList.elementAt(mSelectedWord[1]);
			mCurrentHintTextView.setText(mMeanings.elementAt(vals[4]));
		}
		else
		{
			mCurrentHintTextView.setText("");
		}
	}
	
	/**
	 * Determine an appropriate order for the crossword based
	 * on the current screen size attributes.
	 * 
	 * @return The detected order
	 */
	private int determineOrder()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int dpi = metrics.densityDpi;
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		
		int crosswordLengthPixels = Math.min(screenWidth,  screenHeight) * 3/4;
		int minCellPixelSize = (int) (MIN_CELL_SIZE_INCHES * dpi);
		
		int order = crosswordLengthPixels / minCellPixelSize;
		if (order < MIN_CROSSWORD_SIZE)
		{
			order = MIN_CROSSWORD_SIZE;
		}
		if (order > MAX_CROSSWORD_SIZE)
		{
			order = MAX_CROSSWORD_SIZE;
		}

		return order;
	}
	
	/**
	 * This class implements the comparator for the hints (crossword meanings) list
	 * The hints are ordered on the basis of the "hintNumber" field described below.
	 */
	public class HintItemComparator implements Comparator<HintItem>
	{	 
		public int compare(HintItem h1, HintItem h2)
		{
			return (h1.hintNumber > h2.hintNumber ? 1 : (h1.hintNumber == h2.hintNumber ? 0 : -1));
		}
	}
	
	/**
	 * This class implements the hints (i.e., meanings) that are shown to the user.
	 */
	private class HintItem
	{
		String hintText; //the word meaning
		int hintNumber; //the hint number - this is the number that is shown next to each hint
		int indexIntoList; //index into the mAcrossList or mDownList
		
		public HintItem(String name, int number, int index)
		{
			hintText = name;
			hintNumber = number;
			indexIntoList = index;
		}
		
		@Override
		public String toString()
		{
			return hintText;
		}
	}
	
	/**
	 * Set up the hints, and the hints list views
	 */
	private void setupHints()
	{		
		mAcrossHints = new ArrayList<HintItem>();
		
		int i;
		for (i = 0; i < mAcrossList.size(); i++)
		{
			int[] val = (int [])mAcrossList.elementAt(i);
			int meaningIndex = val[4];
			mAcrossHints.add(new HintItem(mMeanings.elementAt(meaningIndex), val[0], i)); 
		}
		Collections.sort(mAcrossHints, new HintItemComparator());
		
		ListView acrossLV = (ListView) findViewById(R.id.across_hints);
		acrossLV.setAdapter(new HintAdapter(getBaseContext(), R.layout.crossword_hint_item, mAcrossHints));
		
		mDownHints = new ArrayList<HintItem>();
		
		for (i = 0; i < mDownList.size(); i++)
		{
			int[] val = (int [])mDownList.elementAt(i);
			int meaningIndex = val[4];
			mDownHints.add(new HintItem(mMeanings.elementAt(meaningIndex), val[0], i));
		}
		Collections.sort(mDownHints, new HintItemComparator());

		ListView downLV = (ListView) findViewById(R.id.down_hints);
		downLV.setAdapter(new HintAdapter(getBaseContext(), R.layout.crossword_hint_item, mDownHints));
	}
	
	/**
	 * This class is the adapter for storing and managing the hints in the list views
	 */
	public class HintAdapter extends ArrayAdapter<HintItem>
	{
		private List<HintItem> mHints;
		
		public HintAdapter(Context context, int textViewResourceId, List<HintItem> hints)
		{
			super(context, textViewResourceId, hints);
			mHints = hints;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
            View v = convertView;
            if (v == null) 
            {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.crossword_hint_item, null);
            }
            
            TextView hintNumView = (TextView) v.findViewById(R.id.hintnumberrow);
            hintNumView.setText(Integer.toString(mHints.get(position).hintNumber));
            
            TextView hintTextView = (TextView) v.findViewById(R.id.hinttextrow);
            hintTextView.setText(mHints.get(position).hintText);
            
            return v;
		}
	}
	
	/******************* Member Variables ************************/
	
	/**
	 * The minimum and maximum sizes for any crossword.  The actual size chosen for a
	 * given device among this range would depend on the device dimensions, resolution, etc.
	 * Refer the 'determineOrder' function for more details.  Also, the MIN_CELL_SIZE_INCHES
	 * value always needs to be honored, so that plays a part in this decision.
	 */
	private static final int MIN_CROSSWORD_SIZE = 8;
	private static final int MAX_CROSSWORD_SIZE = 8;
	
	private static final double MIN_CELL_SIZE_INCHES = 0.25;  //big enough to allow comfortable selection with the average human finger tip size
		
	/**
	 * The core data structures that hold information related to the crossword and the panel.
	 * Some of these are described in more detail ahead of the class 'Crossword'
	 */
	private Vector<String> mWords; // all words selected for this crossword
	private Vector<String> mMeanings; // all meanings selected for this crossword
	
	private List<HintItem> mAcrossHints; // the list of across hints that are actually displayed on the screen
	private List<HintItem> mDownHints; // the list of down hints that are actually displayed on the screen
	
	private Vector<int[]> mAcrossList; // 0 = word index, 1 = starting row, 2 = starting column, 3 = index into mWords list, 4 = index into mMeanings list
	private Vector<int[]> mDownList; //// 0 = word index, 1 = starting row, 2 = starting column, 3 = index into mWords list, 4 = index into mMeanings list
	
	private int[][][] mCrosswordMap; // third dim: 0 = index on across list, 1 = index into word for this char, 2 = index on down list, 3 = index into word for this char
	private char[][] mCurrentMatrix; // filled with current set of characters.  '%' indicates non-fillable cell, and '*' indicates fillable cell that's not yet filled by user
	private boolean[][] mCurrentFoundMatrix; //filled with true for letters that were found, and false for those not yet found
	
	private Vector<int[]> mFoundWordsList; //list of words found. each element is an integer array of 2 elements.  First element is 0 for Across list, and 1 for down list.  Second element is the index into the corresponding list.
	private int mOrder; //the determined order for this crossword
	
	/**
	 * "Current" Values that manage the state of the crossword as it is being filled.
	 */
	private boolean mEnableFill; // user is allowed to fill only if this is set to true
	private boolean mKeypadActive; //this is required, as during hte viewflipper animation, we'd like to disable key inputs into the keypad
	private int mCurrentRow, mCurrentCol; //currently selected row or col. -1 if none selected
	private int mCurrentDirection; //the direction of currently typed-in word.  -1 if none selected
	private int[] mSelectedWord; //currently selected word, if any.  First element: 0 for across list, 1 for down list, Second element: index into corresponding list
	
	/**
	 * Views, buttons, etc.
	 */
	private CrosswordPanel mPanel; //the main panel/layout that is used to draw the Crossword
	private ViewFlipper mViewFlipper; //the view flipper flips between the Hints view (Across + Down hints list views), and the Keypad view
	private ImageButton mCloseKeypadButton; //the close button on the keypad view
	private ImageView mFillDirectionImage; //the direction image on the keypad view
	private TextView mCurrentHintTextView; //text view in the keypad layout that shows the currently selected word's hint (meaning)
		
	/**
	 * Static constants
	 */
	//direction of word
	private static final int WORD_DIRECTION_HORIZONTAL = 0;
	private static final int WORD_DIRECTION_VERTICAL = 1;
	
	private static final int KEYPAD_BUTTON_ID_INDEX = 10000; //base index of the keypad buttons
	
	/**
	 * Data Keys used for storing/restoring the game.  Refer generateGameDataFromDB for format details.
	 */
	private static final String KEY_DATA_ORDER = "ORDER";
	private static final String KEY_DATA_WORDS = "WORDS";
	private static final String KEY_DATA_MEANINGS = "MEANINGS";
	private static final String KEY_DATA_ACROSS_LIST = "ACROSSLIST";
	private static final String KEY_DATA_DOWN_LIST = "DOWNLIST";
	private static final String KEY_DATA_FOUND_WORDS = "FOUNDWORDS";
	private static final String KEY_DATA_CURRENT_MATRIX = "CURRENTMATRIX";
}