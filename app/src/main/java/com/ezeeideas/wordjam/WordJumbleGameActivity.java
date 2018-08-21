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
import android.view.ViewGroup;

import java.util.Vector;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.View.OnClickListener;

import java.util.Random;

import com.ezeeideas.wordjam.WordDbAdapter.WordShortInfo;

public class WordJumbleGameActivity extends BaseGameActivity implements OnClickListener
{
	@Override
	protected int getLayoutID() 
	{
		return (R.layout.activity_word_jumble_game);
	}

	@Override
	protected void doInitializeGame() 
	{
		mGameType = Constants.GAME_TYPE_WORD_JUMBLE;
		mGameClass = WordJumbleGameActivity.class;
	}

	@Override
	protected void doHonorTheme() 
	{   	

	}

	@Override
	protected void doRestoreGame() 
	{
		Utils.Log("doRestoreGame, mGameData = [" + mGameData + "]");
		if (mGameData == null)
		{
			return; //should never happen
		}
		
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
			else if (tokens[0].equals(KEY_DATA_JUMBLED_WORD))
			{
				mCurrentJumbled = tokens[1];
			}
			else if (tokens[0].equals(KEY_DATA_UNJUMBLED_WORD))
			{
				mCurrentOrdered = tokens[1];
			}
		}
		
		mGivenText.setText(mMeaning);
		
		mSanitizedWord = sanitizeWord(mWord);
		mSanitizedWord = Utils.sanitizeString(mSanitizedWord, true, false, true, false, false);
		
		createButtons(); //set up the jumbled and ordered buttons
	}

	@Override
	protected void doSetupViews() 
	{
		/**
		 * Set up the Jumbled and Ordered buttons
		 */
		mJumbledButtons = new Vector<Button>();
		mOrderedButtons = new Vector<Button>();
		
		/**
		 * Other UI elements
		 */
		mGivenText = (TextView) findViewById(R.id.given_text);
	}

	@Override
	protected void doRefreshViews() 
	{
		if (mGameState == Constants.GAME_STATE_COMPLETED || mGameState == Constants.GAME_STATE_FAILED || mGameState == Constants.GAME_STATE_GIVEN_UP)
		{
			refreshButtons(false);
		}
		else
		{
			refreshButtons(true);
		}
	}

	/**
	 * 
	 * @WORD#<word>@MEANING#<meaning>@JUMBLEDWORD#<jumbled word with '%'s in places of used/removed letters>@UNJUMBLEDWORD#<unjumbled word wiht '%'s in places of unfilled letters>@
	 * 
	 * - The jumbled word is of full word length, with '%' in place of already used up letters
	 * - The unjumbled word is of full word length, with '%' in place of non-filled letters
	 */
	@Override
	protected String generateGameData() 
	{
		String data = new String();
		
		data += "@" + KEY_DATA_WORD + "#" + mWord;
		data += "@" + KEY_DATA_MEANING + "#" + mMeaning;
		data += "@" + KEY_DATA_JUMBLED_WORD + "#" + mCurrentJumbled;
		data += "@" + KEY_DATA_UNJUMBLED_WORD + "#" + mCurrentOrdered;

		data += "@";
		
		data = Utils.encodeStringForDB(data);
		
		Utils.Log("generateGameData returning data = " + data);
		return data;
	}

	@Override
	protected String generateGameDataFromDB() 
	{
		WordShortInfo wordInfo = mWordDbAdapter.fetchRandomWord(mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType, MIN_WORD_LENGTH, MAX_WORD_LENGTH);
		String word = wordInfo.word.toUpperCase();
		String meaning = wordInfo.meaning;
		
		meaning = Utils.sanitizeString(meaning, false, false, false, false, true);
		
		String sanitizedWord = sanitizeWord(word);
		sanitizedWord = Utils.sanitizeString(sanitizedWord, true, false, true, false, false);
		
		
		String currentOrdered = new String();
		int i;
		for (i = 0; i < sanitizedWord.length(); i++)
		{
			currentOrdered = currentOrdered.concat(Character.toString('%')); //# means not yet filled
		}
		String currentJumbled = shuffleString(sanitizedWord);
		
		String data = new String();
		
		data += "@" + KEY_DATA_WORD + "#" + word;
		data += "@" + KEY_DATA_MEANING + "#" + meaning;
		data += "@" + KEY_DATA_JUMBLED_WORD + "#" + currentJumbled;
		data += "@" + KEY_DATA_UNJUMBLED_WORD + "#" + currentOrdered;

		data += "@";
		
		data = Utils.decodeStringFromDB(data);
		
		Utils.Log("generateGameDataFromDB returning data = " + data);
		return data;
	}

	@Override
	protected void doHandleGivenUp() 
	{
		//reset the ordered to the actual word, and the jumbled to empty
		mCurrentOrdered = mSanitizedWord;
		mCurrentJumbled = "";
		int i;
		for (i = 0; i < mSanitizedWord.length(); i++)
		{
			mCurrentJumbled = mCurrentJumbled.concat(String.valueOf('%'));
		}		
	}

	@Override
	protected void doHandleHintUsed() 
	{
		//let's find a hint

		//we start from the left of the current ordered list, and following these steps:
		//1. check if the current char is correctly placed.  if it is correct, move to the next character
		//2. if it isn't, look in the jumbled list for the right character.  if found, swap the two
		//3. if not found in the jumbled list, look in the remaining part of the ordered list, and swap the two if found
		//4. if the current is a blank space, do exactly as above (a space can also be 'swapped')
		//5. no more hints when there are only 2 character left in the jumbled list
		int length = mCurrentOrdered.length();
		int i;
		for (i = 0; i < length; i++)
		{
			if (mCurrentOrdered.charAt(i) != mSanitizedWord.charAt(i))
			{
				//let's reveal this letter
				//check in the jumbled list for the correct character
				int j;
				boolean found = false;
				for (j = 0; j < length; j++)
				{
					if (mCurrentJumbled.charAt(j) == mSanitizedWord.charAt(i))
					{
						//swap the two
						char jumC = mCurrentJumbled.charAt(j);
						char ordC = mCurrentOrdered.charAt(i);
						mCurrentOrdered = mCurrentOrdered.substring(0, i) + jumC + mCurrentOrdered.substring(i+1, length);
						mCurrentJumbled = mCurrentJumbled.substring(0, j) + ordC + mCurrentJumbled.substring(j+1, length);
						found = true;
						break;
					}
				}
				if (found == false)
				{
					for (j = i+1; j < length; j++)
					{
						if (mCurrentOrdered.charAt(j) == mSanitizedWord.charAt(i))
						{
							//swap the two
							char firstC = mCurrentOrdered.charAt(i);
							char secondC = mCurrentOrdered.charAt(j);
							mCurrentOrdered = mCurrentOrdered.substring(0, i) + secondC + mCurrentOrdered.substring(i+1,j) + firstC + mCurrentOrdered.substring(j+1, length);
							found = true;
							break;
						}
					}
				}

				break;
			}
		}
	}

	@Override
	protected void doHandleButtonClicked(int id) 
	{
		Utils.Log("doHandleButtonCLicked, id = " + id);
    	//jumbled key buttons
    	int jumbledButtonIndex = id - JUMBLED_BUTTON_ID;
    	
       	//hide the pressed jumbled button, and show the next ordered button
    	//to do this, we simply need to set the char for this jumbled button to '#' and set the char for the ordered button to the actual character
    	int length = mSanitizedWord.length();
    	for (int i = 0; i < mCurrentOrdered.length(); i++)
    	{
    		if (mCurrentOrdered.charAt(i) == '%')
    		{
    			mCurrentOrdered = mCurrentOrdered.substring(0,  i) + mCurrentJumbled.charAt(jumbledButtonIndex) + mCurrentOrdered.substring(i+1, length);
    			if (mSanitizedWord.charAt(i) == mCurrentJumbled.charAt(jumbledButtonIndex))
    			{
    				//correct letter!
    				onRightWordOrLetterFound();
    			}
    			break;
    		}
    	}
    	mCurrentJumbled = mCurrentJumbled.substring(0, jumbledButtonIndex) + '%' + mCurrentJumbled.substring(jumbledButtonIndex + 1, length);
    	refreshButtons(true);
    	
    	/**
    	 * Now, check for completion!
    	 */
    	if (mCurrentOrdered.indexOf('%') == -1)
    	{
    		//we're done, so let's check how we fared!
    		if (mCurrentOrdered.equals(mSanitizedWord))
    		{
    			onGameCompleted();
			}
			else
			{
				onGameFailed();
			}
		}
	}
	
	private String sanitizeWord(String word)
	{
		int i;
		char c;
		String sanitizedString = new String();
		//Log.d(null, "start: sanitizedString [" + sanitizedString + "]");
		for (i = 0; i < word.length(); i++)
		{
			c = word.charAt(i);
			//Log.d(null, "i = " + i + ", c = " + c + ", isLetter = " + Character.isLetter(c));
			if (Character.isLetter(c))
			{
				sanitizedString = sanitizedString.concat(Character.toString(c));
				//Log.d(null, "sanitized string after concat: [" + sanitizedString + "]");
			}
		}
		
		//Log.d(null, "end - sanitizedString [" + sanitizedString + "]");
		return sanitizedString;
	}
		
	// Implementing Fisher Yates shuffle
	static String shuffleString(String str)
	{
		int length = str.length();
		int[] arr = new int[length];
		for (int i = 0; i < length; i++)
		{
			arr[i] = i;
		}
		Random rnd = new Random();
	    for (int i = length - 1; i >= 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = arr[index];
	      arr[index] = arr[i];
	      arr[i] = a;
	    }
	    
	    String outString = new String();
	    for (int i = 0; i < length; i++)
	    {
	    	outString = outString.concat(Character.toString(str.charAt(arr[i])));
	    }
	    
	    return outString;
	}
	
	private void createButtons()
	{
		int length = mSanitizedWord.length();
		
		////////////////////////////////////
		//create the jumbled buttons layout
		
		LinearLayout ll = (LinearLayout)findViewById(R.id.jumbled_layout);
		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) ll.getLayoutParams();
		params.width = ViewGroup.LayoutParams.FILL_PARENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		LayoutParams buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout row = new LinearLayout(this);
		int i;
		for (i = 0; i < length; i++)
		{
			Button button = new Button(this);
			button.setId(JUMBLED_BUTTON_ID + i);
			button.setOnClickListener(this);
			button.setPadding(0, 0, 0, 0);
			buttonParams.weight = (float)(1.0/8.0);
			button.setLayoutParams(buttonParams);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ui_button_text_size));
			button.setTextAppearance(this, R.style.boldText);
			row.addView(button);
			mJumbledButtons.add(button);
		}

		row.setGravity(Gravity.CENTER);
		ll.addView(row, params);

		////////////////////////////////////
		//create the ordered buttons layout

		ll = (LinearLayout)findViewById(R.id.ordered_layout);
		params = (ViewGroup.LayoutParams) ll.getLayoutParams();
		params.width = ViewGroup.LayoutParams.FILL_PARENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		row = new LinearLayout(this);
		for (i = 0; i < length; i++)
		{
			Button button = new Button(this);
			button.setId(ORDERED_BUTTON_ID + i);
			//button.setOnClickListener(this); orderd buttons need not be clickable.
			button.setPadding(0, 0, 0, 0);
			buttonParams.weight = (float)(1.0/8.0);
			button.setLayoutParams(buttonParams);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ui_button_text_size));
			button.setTextAppearance(this, R.style.boldText);
			row.addView(button);
			mOrderedButtons.add(button);
		}

		row.setGravity(Gravity.CENTER);
		ll.addView(row, params);
	}
	
	private void refreshButtons(boolean enableButtons)
	{
		//update text and visibility of buttons according to the current values of mCurrentJumbled and mCurrentOrdered
		int i;
		for (i = 0; i < mSanitizedWord.length(); i++)
		{
			//update jumbled buttons
			Button button = mJumbledButtons.elementAt(i);
			if (mCurrentJumbled.charAt(i) == '%')
			{
				//# means the button is hidden
				button.setVisibility(Button.INVISIBLE);
			}
			else
			{
				button.setText(Character.toString(mCurrentJumbled.charAt(i)));
				button.setVisibility(Button.VISIBLE);
				button.setEnabled(enableButtons);
			}
			
			//update ordered buttons
			button = mOrderedButtons.elementAt(i);
			if (mCurrentOrdered.charAt(i) == '%')
			{
				//# means the button is hidden
				button.setVisibility(Button.INVISIBLE);
			}
			else
			{
				button.setText(Character.toString(mCurrentOrdered.charAt(i)));
				button.setVisibility(Button.VISIBLE);
				button.setEnabled(enableButtons);
			}
		}
	}

	/********************** Member Variables ******************/
	
	private TextView mGivenText;
	
	private String mWord; //given word
	private String mMeaning; //given meaning
	private String mSanitizedWord; //sanitized form of the word
	
	private String mCurrentJumbled; //current jumbled version of the word
	private String mCurrentOrdered; //current ordered version of the word
	
	private static final int JUMBLED_BUTTON_ID = 100, ORDERED_BUTTON_ID = 200;
	
	
	/**
	 * Constants
	 */
	private static final int MAX_WORD_LENGTH = 10; // word length must be 10 or lower for usable experience
	private static final int MIN_WORD_LENGTH = 4;
	
	/**
	 * UI elements
	 */
	Vector<Button> mJumbledButtons, mOrderedButtons; //the jumbled and ordered buttons
	
	/**
	 * Data Keys used for storing/restoring the game.
	 */
	private static final String KEY_DATA_WORD = "WORD";
	private static final String KEY_DATA_MEANING = "MEANING";
	private static final String KEY_DATA_JUMBLED_WORD = "JUMBLEDWORD";
	private static final String KEY_DATA_UNJUMBLED_WORD = "UNJUMBLEDWORD";
}