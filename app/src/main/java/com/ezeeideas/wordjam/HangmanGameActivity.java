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

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.Vector;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.Random;

import com.ezeeideas.wordjam.WordDbAdapter.WordShortInfo;

public class HangmanGameActivity extends BaseGameActivity
{
	@Override
	protected int getLayoutID() 
	{
		return (R.layout.activity_hangman_game);
	}

	@Override
	protected void doInitializeGame() 
	{
		mGameType = Constants.GAME_TYPE_HANGMAN;
		mGameClass = HangmanGameActivity.class;
		
		mKeyStates = new boolean[26];
	}

	@Override
	protected void doHonorTheme() 
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void doRestoreGame() 
	{
		Utils.Log("doRestoreGame, mGameData = [" + mGameData + "]");
		if (mGameData == null)
		{
			return; //should never happen
		}
		
		String keyChars = "";
		String[] sections = mGameData.split("@");
		for (String section : sections)
		{
			String[] tokens = section.split("#");
			if (tokens[0].equals(KEY_DATA_WORD))
			{
				mWord = tokens[1];
			}
			else if (tokens[0].equals(KEY_DATA_MEANING))
			{
				mMeaning = tokens[1];
			}
			else if (tokens[0].equals(KEY_DATA_KEYCHARS))
			{
				keyChars = tokens[1];
			}
		}
		
		mMeaning = Utils.sanitizeString(mMeaning, false, false, false, false, true);
		mGivenText.setText(mMeaning);
		mSanitizedWord = Utils.sanitizeString(mWord, true, false, true, true, false);
		mSanitizedWord = mSanitizedWord.toUpperCase();
		
		mCurrentPathLevel = 0;
		String alphabets = new String(mAlphabets);
		for (int i = 0; i < keyChars.length(); i++)
		{
			if (keyChars.charAt(i) == '0' && mSanitizedWord.indexOf(mAlphabets[i]) == -1)
			{
				mCurrentPathLevel ++;
			}
		}

		Utils.Log("mWord [" + mWord + "]");
		Utils.Log("mMeaning [" + mMeaning + "]");
		Utils.Log("mSanitizedWord [" + mSanitizedWord + "]");
		Utils.Log("keyChars [" + keyChars + "]");
		Utils.Log("mCurrentPathLevel = " + mCurrentPathLevel);
		Utils.Log("alphabets [" + alphabets + "]");
		mCurrentChars = new String();
		int i;
		for (i = 0; i < mSanitizedWord.length(); i++)
		{
			int index = alphabets.indexOf(mSanitizedWord.charAt(i));
			if (keyChars.charAt(index) == '1')
			{
				mCurrentChars = mCurrentChars.concat(Character.toString('#'));
			}
			else
			{
				mCurrentChars = mCurrentChars.concat(Character.toString((mSanitizedWord.charAt(i))));
			}
		}
		
		//set up the keypad button states
		for (i = 0; i < keyChars.length(); i++)
		{
			Button button = (Button)findViewById(KEYPAD_BUTTON_ID + i);
			if (keyChars.charAt(i) == '1')
			{
				button.setEnabled(true);
				mKeyStates[i] = true;
			}
			else
			{
				button.setEnabled(false);
				mKeyStates[i] = false;
			}
		}
		//The drawing panel can now be made visible for drawing
		mPanel.setVisibility(View.VISIBLE);
	}

	@Override
	protected void doSetupViews() 
	{
		mPanel = (HangmanPanel)findViewById(R.id.hangmanpanel);
		mPanel.setVisibility(View.INVISIBLE); //make sure the panel doesn't start rendering until we've restored the game
		mGivenText = (TextView) findViewById(R.id.given_text);
		mCurrentPathLevel = 0;
		
		setupKeypad();
	}
	
	@Override
	protected void doRefreshViews()
	{
		Utils.Log("dorefreshViews, setting mWord to " + mSanitizedWord);
		mPanel.updatePathLevel(mCurrentPathLevel);
		mPanel.setWord(mSanitizedWord);
		mPanel.updateCurrentChars(mCurrentChars);

		Utils.Log("doRefreshViews, mCurrentPathLevel = " + mCurrentPathLevel + ", mSanizitedWord = [" + mSanitizedWord + ", mCurrentChars = [" + mCurrentChars + "]");;
		
		if (mGameState == Constants.GAME_STATE_COMPLETED || mGameState == Constants.GAME_STATE_FAILED || mGameState == Constants.GAME_STATE_GIVEN_UP)
		{
			for (int i = 0; i < 26; i++)
			{
				Button button = (Button)findViewById(KEYPAD_BUTTON_ID + i);
				button.setEnabled(false);
			}
			
			//show all characters above the dashes
			mCurrentChars = mSanitizedWord;
			mPanel.updateCurrentChars(mCurrentChars);
		}
		
		mPanel.invalidate();
	}
	
	/**
	 * Set up the keypad
	 */
	private void setupKeypad()
	{
		LinearLayout ll = (LinearLayout)findViewById(R.id.keypad_layout);
		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) ll.getLayoutParams();
		params.width = ViewGroup.LayoutParams.FILL_PARENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		
		LayoutParams buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout row = new LinearLayout(this);
		int i;
		for (i = 0; i < 8; i++)
		{
			Button button = new Button(this);
			button.setId(KEYPAD_BUTTON_ID + i);
			button.setText(Character.toString(mAlphabets[i]));
			button.setOnClickListener(this);
			button.setPadding(0, 0, 0, 0);
			buttonParams.weight = (float)(1.0/8.0);
			button.setLayoutParams(buttonParams);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ui_button_text_size));
			if (i == 0 || i == 4)
			{
				//set bold for vowels
				button.setTypeface(null, Typeface.ITALIC);
			}
			row.addView(button);
		}
		
		row.setGravity(Gravity.CENTER);
		ll.addView(row, params);
		
		row = new LinearLayout(this);
		for (i = 8; i < 17; i++)
		{
			Button button = new Button(this);
			button.setId(KEYPAD_BUTTON_ID + i);
			button.setText(Character.toString(mAlphabets[i]));
			button.setOnClickListener(this);
			button.setPadding(0, 0, 0, 0);
			buttonParams.weight = (float)(1.0/9.0);
			button.setLayoutParams(buttonParams);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ui_button_text_size));
			if (i == 8 || i == 14)
			{
				//set bold for vowels
				button.setTypeface(null, Typeface.ITALIC);
			}
			row.addView(button);	
		}
		
		row.setGravity(Gravity.CENTER);
		ll.addView(row, params);
		
		row = new LinearLayout(this);
		for (i = 17; i < 26; i++)
		{
			Button button = new Button(this);
			button.setId(KEYPAD_BUTTON_ID + i);
			button.setText(Character.toString(mAlphabets[i]));
			button.setOnClickListener(this);
			button.setPadding(0, 0, 0, 0);
			buttonParams.weight = (float)(1.0/9.0);
			button.setLayoutParams(buttonParams);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ui_button_text_size));
			if (i == 20)
			{
				//set bold for vowels
				button.setTypeface(null, Typeface.ITALIC);
			}
			row.addView(button);	
		}
		
		row.setGravity(Gravity.CENTER);
		ll.addView(row, params);
	}

	/**
	 * 
	 * @WORD#<word>@MEANING#<meaning>@KEYCHARS#<26 digits either 0 or 1 depending on whether that char has been already used or not, 0 means used>@
	 * 
	 * - The keychars are nothing but the toggle state of each key depending on whether it has been used up already or not
	 * - The actual word and hangman's current state isn't explicitly mentioned in the format as that can be derived from the keystates above
	 */
	@Override
	protected String generateGameData() 
	{
		String data = new String();
		
		data += "@" + KEY_DATA_WORD + "#" + mWord;
		data += "@" + KEY_DATA_MEANING + "#" + mMeaning;
		data += "@" + KEY_DATA_KEYCHARS + "#";

		for (int i = 0; i < mAlphabets.length; i++)
		{
			if (mKeyStates[i] == true)
			{
				data += "1";
			}
			else
			{
				data += "0";
			}
		}
		data += "@";
		
		data = Utils.encodeStringForDB(data);
		
		Utils.Log("generateGameData returning data = " + data);
		return data;
	}

	@Override
	protected String generateGameDataFromDB() 
	{
		WordShortInfo wordInfo = mWordDbAdapter.fetchRandomWord(mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType, MIN_WORD_LENGTH, MAX_WORD_LENGTH);
		String data = new String();
		
		data += "@" + KEY_DATA_WORD + "#" + wordInfo.word;
		data += "@" + KEY_DATA_MEANING + "#" + wordInfo.meaning;
		data += "@" + KEY_DATA_KEYCHARS + "#";

		for (int i = 0; i < mAlphabets.length; i++)
		{
			data += "1";
		}
		data += "@";
		
		data = Utils.decodeStringFromDB(data);
		
		return data;
	}

	@Override
	protected void doHandleGivenUp() 
	{
		mCurrentChars = new String(mSanitizedWord);
		mPanel.updateCurrentChars(mCurrentChars);
	
		int i;
		for (i = 0; i < 26; i++)
		{
			Button button = (Button)findViewById(KEYPAD_BUTTON_ID + i);
			button.setEnabled(false);
		}	
	}

	@Override
	protected void doHandleHintUsed() 
	{
		Vector<Integer> spacesArray = new Vector<Integer>();
		int i;
		for (i = 0; i < mCurrentChars.length(); i++)
		{
			if (mCurrentChars.charAt(i) == '#')
			{
				spacesArray.add(i);
			}
		}
		if (spacesArray.size() <= 1)
		{
			//no more hints
			String hintText = getResources().getString(R.string.hangmantest_nomorehinttext);
	    	Utils.showGenericToast(this, hintText);
		}
		else
		{
			Random rand = new Random();
			int index = rand.nextInt(spacesArray.size());
			int selectedIndex = spacesArray.elementAt(index);
			char selectedChar = mSanitizedWord.charAt(selectedIndex);
		
			char c;
			Boolean changed = false;
			String newCurrentChars = new String();
			for (i = 0; i < mSanitizedWord.length(); i++)
			{
				c = mSanitizedWord.charAt(i);
				if (c == selectedChar)
				{
					newCurrentChars = newCurrentChars.concat(Character.toString(c));
					changed = true;
				}
				else
				{
					newCurrentChars = newCurrentChars.concat(Character.toString(mCurrentChars.charAt(i)));
				}
			}
			if (changed)
			{
				for (i = 0; i < mAlphabets.length; i++)
				{
					if (selectedChar == mAlphabets[i])
					{
						break;
					}
				}
				Button button = (Button)findViewById(KEYPAD_BUTTON_ID + i);
				button.setEnabled(false);
				
				mCurrentChars = newCurrentChars;
				mPanel.updateCurrentChars(mCurrentChars);
			}
		}
		
	}
	
	/**
	 * The keypad key was tapped.  Handle that.
	 * 
	 * @param id The button id of the keypad key
	 */
	private void handleKeypadClicked(int id)
	{
		Utils.Log("handleKeypadClicked, id =" + id);
		//keypad clicked
				
		Button button = (Button) findViewById(id);
		button.setEnabled(false);
		mKeyStates[id - KEYPAD_BUTTON_ID] = false;
		
		char selectedChar = mAlphabets[id - KEYPAD_BUTTON_ID];
		int i;
		char c;
		Boolean changed = false;
		String newCurrentChars = new String();
		for (i = 0; i < mSanitizedWord.length(); i++)
		{
			c = mSanitizedWord.charAt(i);
			if (c == selectedChar)
			{
				newCurrentChars = newCurrentChars.concat(Character.toString(c));
				changed = true;
			}
			else
			{
				newCurrentChars = newCurrentChars.concat(Character.toString(mCurrentChars.charAt(i)));
			}
		}
		if (changed)
		{
			mCurrentChars = newCurrentChars;
			onRightWordOrLetterFound();
		}
		else
		{
			mCurrentPathLevel ++;
			onWrongWordOrLetterFound();
			Utils.Log("handleKeypad, updating mCurrentPathLevel to " + mCurrentPathLevel);
		}
		
		//check if the game is completed
		if (mCurrentChars.indexOf('#') == -1)
		{
			onGameCompleted();
		}
		else if (mCurrentPathLevel == MAX_PATHS)
		{
			onGameFailed();
		}
		
		doRefreshViews();
	}

	@Override
	protected void doHandleButtonClicked(int id) 
	{
		if (id >= KEYPAD_BUTTON_ID && id < (KEYPAD_BUTTON_ID + mAlphabets.length))
		{
			handleKeypadClicked(id);
		}
	}
	
	/*********** Member Variables *********/
	
	private static final int KEYPAD_BUTTON_ID = 1000; //id of the first keypad button
	private char[] mAlphabets = {'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z'};
	
	//Game specific data
	private HangmanPanel mPanel; //the panel that is used to draw the hangman game, including the characters found and dashes
	
	private TextView mGivenText;
	private String mWord;
	private String mMeaning;
	private String mSanitizedWord;
	
	private String mCurrentChars; //the currently selected characters.  # represents characters not yet found
	private boolean[] mKeyStates; //state of the keypad keys
	private int mCurrentPathLevel; //the current path level.  Refer HangmanPanel class for more information
	private static int MAX_PATHS = 7; //the maximum number of paths possible, this is used to decide when the game is over
	
	private static final int MAX_WORD_LENGTH = 12; //the maximum length of a word to be selected for the game
	private static final int MIN_WORD_LENGTH = 4; //the minimum length of a word to be selected for the game

	/**
	 * Data Keys used for storing/restoring the game.
	 */
	private static final String KEY_DATA_WORD = "WORD";
	private static final String KEY_DATA_MEANING = "MEANING";
	private static final String KEY_DATA_KEYCHARS = "KEYCHARS";
}