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

import android.os.Bundle;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Button;
import android.graphics.LightingColorFilter;

/**
 * This game provides the user with a word or meaning, and the user needs to
 * select the right matching meaning or word from a set of 4 options
 */
public class RecallWordsGameActivity extends BaseGameActivity
{	
	@Override
	protected int getLayoutID() 
	{
		return (R.layout.activity_recall_words_game);
	}
	
	protected void doHonorTheme()
    {
    }
	
	protected void doInitializeGame()
	{		
		mGameType = Constants.GAME_TYPE_RECALL_WORDS;
		mGameClass = RecallWordsGameActivity.class;
	}

	protected void doSetupViews()
	{		
    	mGivenText = (TextView) findViewById(R.id.given_text);
    	mChoiceButtons = new Button[4];
    	mChoiceButtons[0] = (Button) findViewById(R.id.choice1_button);
    	mChoiceButtons[1] = (Button) findViewById(R.id.choice2_button);
    	mChoiceButtons[2] = (Button) findViewById(R.id.choice3_button);
    	mChoiceButtons[3] = (Button) findViewById(R.id.choice4_button);
    	
    	int i;
    	for (i = 0; i < 4; i++)
    	{
    		mChoiceButtons[i].setOnClickListener(this);
    	}
        
        mEligibleHintButtons = new Vector<Integer>();
	}
	
	protected void doRefreshViews()
	{
		if (mGameState == Constants.GAME_STATE_GIVEN_UP || mGameState == Constants.GAME_STATE_FAILED || mGameState == Constants.GAME_STATE_COMPLETED)
		{
			Utils.Log("gamenumber = " + mGameNumber + ", mGameState = " + mGameState);;
			//show the correct answer highlighted
	    	mChoiceButtons[mCorrectAnswer].getBackground().setColorFilter(new LightingColorFilter(getResources().getColor(R.color.choice_button_correct_color_mul), getResources().getColor(R.color.choice_button_correct_color_add)));
	    	
	    	if (mGameState == Constants.GAME_STATE_FAILED)
	    	{
		    	if (mSelectedButton >= 0 && mSelectedButton <= NUM_CHOICES)
		    	{
		    		mChoiceButtons[mSelectedButton].getBackground().setColorFilter(new LightingColorFilter(getResources().getColor(R.color.choice_button_incorrect_color_mul), getResources().getColor(R.color.choice_button_incorrect_color_add)));
		    	}
	    	}
		}
	}
	
	/**
	 * Generate the Game Data archival format.  Format is:
	 * @WORD#<word>@MEANING#<meaning>@WORD2MEANING#<0 or 1>@CORRECTANSWER#<index of right answer>@CHOICES#<no. of choices>#<word or meaning>%<state 0 or 1>#<word or meaning>%<state 0 or 1>@
	 */
	@Override
	protected String generateGameData()
	{
		String data = new String();
		
		data += "@" + KEY_DATA_WORD + "#" + mTestWord;
		data += "@" + KEY_DATA_MEANING + "#" + mMeaning;
		data += "@" + KEY_DATA_WORD2MEANING + "#" + (mWord2Meaning ? "1" : "0");
		data += "@" + KEY_DATA_CORRECTANSWER + "#" + mCorrectAnswer;
		data += "@" + KEY_DATA_CHOICES + "#" + NUM_CHOICES;
		for (int i = 0; i < NUM_CHOICES; i++)
		{
			data += "#" + mChoiceButtons[i].getText() + "%" + (mChoiceButtons[i].isEnabled() ? "1" : "0");
		}
		data += "@";
		
		data = Utils.encodeStringForDB(data);
		
		return data;
	}
	
	protected String generateGameDataFromDB()
	{
		String data = new String();
		
		ArrayList<WordShortInfo> wordList = mWordDbAdapter.fetchWordsOfCommonType(mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType, NUM_CHOICES);

		Random randCorrectAnswer = new Random();
		int correctAnswer = randCorrectAnswer.nextInt(4);
		
		boolean word2Meaning = false;
		
		//if we're on an LDPI device, such as Samsung Galaxy Mini, the resolution may not allow for
		//buttons to hold the meanings, so always use single meaning, 4 words in such cases
		//if (Themes.getScreenSize() == Themes.SCREEN_SIZE_SMALL)

		/**
		 * TODO.  Always do meaning to work to conserve space
		 */
		word2Meaning = false;
		/*
		else
		{
			Random randWordOrMeaning = new Random();
			if (randWordOrMeaning.nextBoolean())
			{
				// given word, select meaning
			
				word2Meaning = true;
			}
			else
			{
				// given meaning, select word
				
				word2Meaning = false;
			}
		}
		*/
		
		data += "@" + KEY_DATA_WORD + "#" + wordList.get(correctAnswer).word;
		data += "@" + KEY_DATA_MEANING + "#" + wordList.get(correctAnswer).meaning;
		data += "@" + KEY_DATA_WORD2MEANING + "#" + (word2Meaning ? "1" : "0");
		data += "@" + KEY_DATA_CORRECTANSWER + "#" + correctAnswer;
		data += "@" + KEY_DATA_CHOICES + "#" + NUM_CHOICES;
		for (int i = 0; i < NUM_CHOICES; i++)
		{
			if (word2Meaning)
			{
				data += "#" + wordList.get(i).meaning + "%" + "1";
			}
			else
			{
				data += "#" + wordList.get(i).word + "%" + "1";
			}
		}
		data += "@";
		
		data = Utils.decodeStringFromDB(data);
		
		return data;
	}
	
	/*
	protected void doSetupGame()
	{
		Random randCorrectAnswer = new Random();
		mCorrectAnswer = randCorrectAnswer.nextInt(4);
		
		//Cursor testWordCursor = mWordDbAdapter.fetchMatchingWords(mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType, 4);
		//startManagingCursor(testWordCursor);
		ArrayList<WordShortInfo> wordList = mWordDbAdapter.fetchWordsOfCommonType(mPlayType, mIsPlay, mIsPlay ? mLevel : mPracticeType, NUM_CHOICES);

		mWord2Meaning = false;
		
		//if we're on an LDPI device, such as Samsung Galaxy Mini, the resolution may not allow for
		//buttons to hold the meanings, so always use single meaning, 4 words in such cases
		if (Themes.getScreenSize() == Themes.SCREEN_SIZE_SMALL)
		{
			mWord2Meaning = false;
		}
		else
		{
			Random randWordOrMeaning = new Random();
			if (randWordOrMeaning.nextBoolean())
			{
				// given word, select meaning
			
				mWord2Meaning = true;
			}
			else
			{
				// given meaning, select word
				
				mWord2Meaning = false;
			}
		}
		
		int i;
		for (i = 0; i < wordList.size(); i++)
		{
			//testWordCursor.moveToPosition(i);
			if (mWord2Meaning)
			{
				//String buttonText = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_MEANING));
				String buttonText = wordList.get(i).meaning;
				buttonText = Utils.sanitizeString(buttonText, false, false, false, false, true);
				mChoiceButtons[i].setText(buttonText);
				if (mCorrectAnswer == i)
				{
					//mTestWord = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_WORD));
					//mMeaning = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_MEANING));
					//String givenText = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_WORD));
					mTestWord = wordList.get(i).word;
					mMeaning = wordList.get(i).meaning;
					String givenText = wordList.get(i).word;
					givenText = Utils.sanitizeString(givenText, true, false, true, false, false);
					mGivenText.setText(givenText);
				}
			}
			else
			{
				//String buttonText = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_WORD));
				String buttonText = wordList.get(i).word;
				buttonText = Utils.sanitizeString(buttonText, true, false, true, false, false);
				mChoiceButtons[i].setText(buttonText);
				if (mCorrectAnswer == i)
				{
					mTestWord = wordList.get(i).word;
					mMeaning = wordList.get(i).meaning;
					//mTestWord = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_WORD));
					//mMeaning = testWordCursor.getString(testWordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_MEANING));
					String givenText = Utils.sanitizeString(mMeaning, false, false, false, false, true);
					mGivenText.setText(givenText);
				}
			}
			
			if (mCorrectAnswer != i)
			{
				mEligibleHintButtons.add(i);
			}
		}
		
		//done with the cursor
		//stopManagingCursor(testWordCursor);
		//testWordCursor.close();
	}
	*/
	
	protected void doRestoreGame() //game data already exists
	{		
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
				mTestWord = tokens[1];
			}
			else if (tokens[0].equals(KEY_DATA_MEANING))
			{
				mMeaning = tokens[1];
			}
			else if (tokens[0].equals(KEY_DATA_WORD2MEANING))
			{
				mWord2Meaning = (Integer.parseInt(tokens[1]) == 1);
				if (mWord2Meaning)
				{
					String givenText = Utils.sanitizeString(mTestWord, true, false, true, false, false);
					givenText = Utils.decodeStringFromDB(givenText);
					mGivenText.setText(givenText);
				}
				else
				{
					String givenText = Utils.sanitizeString(mMeaning, true, false, false, false, false);
					givenText = Utils.decodeStringFromDB(givenText);
					mGivenText.setText(givenText);
				}
			}
			else if (tokens[0].equals(KEY_DATA_CORRECTANSWER))
			{
				mCorrectAnswer = Integer.parseInt(tokens[1]);
			}
			else if (tokens[0].equals(KEY_DATA_CHOICES))
			{
				int numChoices = Integer.parseInt(tokens[1]);
				for (int i = 0; i < numChoices; i++)
				{
					String[] choiceTokens = tokens[2 + i].split("%");
					Utils.Log("tokens[2+" + i + "] = [" + tokens[2+i] + "]");
					Utils.Log("size of choiceTokens = " + choiceTokens.length);
					
					String buttonText  = Utils.sanitizeString(choiceTokens[0], false, false, false, false, true);
					buttonText = Utils.decodeStringFromDB(buttonText);
					mChoiceButtons[i].setText(buttonText);
					if (Integer.parseInt(choiceTokens[1]) == 1)
					{
						mChoiceButtons[i].setEnabled(true);
					}
					else
					{
						mChoiceButtons[i].setEnabled(false);
					}

					if (mCorrectAnswer != i)
					{
						mEligibleHintButtons.add(i);
					}
				}
			}
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle bundle)
	{
		super.onRestoreInstanceState(bundle);
	}
	
	protected void doHandleButtonClicked (int id) {
		Boolean testPassed = false;
	    switch (id) {
	        case R.id.choice1_button:
	        	if (mCorrectAnswer == 0)
	        	{
	        		testPassed = true;
	        	}
	        	else
	        	{
	        		testPassed = false;
	        	}
	        	mSelectedButton = 0;
	        	break;
	        case R.id.choice2_button:
	        	if (mCorrectAnswer == 1)
	        	{
	        		testPassed = true;
	        	}
	        	else
	        	{
	        		testPassed = false;
	        	}
	        	mSelectedButton = 1;
	        	break;
	        case R.id.choice3_button:
	        	if (mCorrectAnswer == 2)
	        	{
	        		testPassed = true;
	        	}
	        	else
	        	{
	        		testPassed = false;
	        	}
	        	mSelectedButton = 2;
	        	break;
	        case R.id.choice4_button:
	        	if (mCorrectAnswer == 3)
	        	{
	        		testPassed = true;
	        	}
	        	else
	        	{
	        		testPassed = false;
	        	}
	        	mSelectedButton = 3;
	        	break;
	        }
	    
	    if (id == R.id.choice1_button || id == R.id.choice2_button || id == R.id.choice3_button || id == R.id.choice4_button)
	    {	
		    if (testPassed)
		    {
		    	onGameCompleted();
		    }
		    else
		    {
		    	//Utils.showTestResultFailed(this, );
		    	onGameFailed();
		    }

	    }
	    
	    mGameDirty = true;
	    
	}
	
	protected void doHandleHintUsed()
	{   	
	    Random rand = new Random();
		int eligibleButton = rand.nextInt(mEligibleHintButtons.size());
		int eligibleButtonIndex = mEligibleHintButtons.elementAt(eligibleButton);
			
		mChoiceButtons[eligibleButtonIndex].setEnabled(false);

		mEligibleHintButtons.remove(eligibleButton);
	}
	
	protected void doHandleGivenUp()
	{
    	int i;
    	for (i = 0; i < 4; i++)
    	{
    		mChoiceButtons[i].setEnabled(false);
    	}
    	
    	mChoiceButtons[mCorrectAnswer].getBackground().setColorFilter(new LightingColorFilter(getResources().getColor(R.color.choice_button_correct_color_mul), getResources().getColor(R.color.choice_button_correct_color_add)));
	}
	
	private ArrayList<WordShortInfo> mWordList; //list of words generated from the DB used to build a new game
	private int mCorrectAnswer = -1;
	private String mTestWord = "";
	private String mMeaning = "";
	private boolean mWord2Meaning = false;
	
	private static final int NUM_CHOICES = 4;
	private TextView mGivenText;
	private Button[] mChoiceButtons;
	private int mSelectedButton; //button that was selected by the user
	
	Vector<Integer> mEligibleHintButtons;

	/**
	 * Data Keys used for storing/restoring the game.
	 */
	private static final String KEY_DATA_WORD = "WORD";
	private static final String KEY_DATA_MEANING = "MEANING";
	private static final String KEY_DATA_WORD2MEANING = "WORD2MEANING";
	private static final String KEY_DATA_CORRECTANSWER = "CORRECTANSWER";
	private static final String KEY_DATA_CHOICES = "CHOICES";

}