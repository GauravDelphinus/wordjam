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

import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.view.MotionEvent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

/**
 * This class implements the 'Match Words' game.  It assumes 4 pairs of words and meanings
 * that are shown to the user and he then tries to match words to meanings by drawing lines
 * connecting them.  MatchWordsPanel class is the layout that implements the actual drawing
 * of the lines, though the words and meanings themselves are drawn in buttons managed by
 * this activity class.
 *
 */
public class MatchWordsGameActivity extends BaseGameActivity implements OnTouchListener, OnClickListener
{
	@Override
	protected int getLayoutID() 
	{
		return (R.layout.activity_match_words_game);
	}

	@Override
	protected void doInitializeGame() 
	{
		mGameType = Constants.GAME_TYPE_MATCH_WORDS;
		mGameClass = MatchWordsGameActivity.class;
		
		//initialize members
		mCurrentStartPoint = new Point(0, 0);
		mCurrentEndPoint = new Point(0, 0);
		
		mChosenPairs = new Vector<int[]>();
		mFoundWords = new Vector<Integer>();
		mEligibleHintButtons = new Vector<Integer>();
		
		mWordToMeaningMap = new int[NUM_WORDS];
		mMeaningToWordMap = new int[NUM_WORDS];
		
		mWords = new Vector<String>();
		mMeanings = new Vector<String>();
		
		mMessageHandler = new MessageHandler();
		
		//start with this state
		mMatchState = STATE_MATCH_ENDED;
		mEnableButtons = true;
	}

	@Override
	protected void doHonorTheme() 
	{
		View view = (View) findViewById(R.id.vertical_line1);
		view.setBackgroundColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_LINE));
		
		view = (View) findViewById(R.id.vertical_line2);
		view.setBackgroundColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_LINE));
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
			if (tokens[0].equals(KEY_DATA_WORDS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					String[] vals = tokens[i].split("%");
					mWords.add(vals[0]);
					mWordToMeaningMap[i-1] = Integer.valueOf(vals[1]);
					if (vals.length > 2)
					{
						//chosen pair value index present
						mChosenPairs.add(new int[] {i - 1, Integer.valueOf(vals[2])});
					}
				}
			}
			else if (tokens[0].equals(KEY_DATA_MEANINGS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					mMeanings.add(tokens[i]);
				}
			}
			else if (tokens[0].equals(KEY_DATA_FOUND_WORDS))
			{
				for (int i = 1; i < tokens.length; i++)
				{
					mFoundWords.add(Integer.valueOf(tokens[i]));
				}
			}
		}
		
		mMeaningToWordMap = new int[mWords.size()];
		for (int i = 0; i < mMeaningToWordMap.length; i++)
		{
			mMeaningToWordMap[mWordToMeaningMap[i]] = i;
		}
		
		for (int i = 0; i < mWords.size(); i++)
		{
			String word = mWords.get(i);
			word = Utils.sanitizeString(word, true, false, true, false, false);
			mWordButtons[i].setText(word);

			String meaning = mMeanings.get(i);
			meaning = Utils.sanitizeString(meaning, false, false, false, false, true);
			mMeaningButtons[i].setText(meaning);
		}
		
		for (int i = 0; i < mWords.size(); i++)
		{
			boolean alreadyChosen = false;
			for (int j = 0; j < mFoundWords.size(); j++)
			{
				if (mFoundWords.get(j) == i)
				{
					alreadyChosen = true;
					break;
				}
			}
			if (!alreadyChosen)
			{
				mEligibleHintButtons.add(i);
			}
		}
		
		//The drawing panel can now be made visible for drawing
		mPanel.updateData(mCurrentStartPoint, mCurrentEndPoint, mChosenPairs, mFoundWords);
		mPanel.invalidate();
		refreshButtons();
		mPanel.setVisibility(View.VISIBLE);
	}

	@Override
	protected void doSetupViews() 
	{
		mPanel = (MatchWordsPanel)findViewById(R.id.matchwordspanel);
		mPanel.setVisibility(View.INVISIBLE);
		
		mWordButtons = new Button[NUM_WORDS];
		mWordButtons[0] = (Button) findViewById(R.id.word1_button);
		mWordButtons[1] = (Button) findViewById(R.id.word2_button);
		mWordButtons[2] = (Button) findViewById(R.id.word3_button);
		mWordButtons[3] = (Button) findViewById(R.id.word4_button);

		mMeaningButtons = new Button[NUM_WORDS];
		mMeaningButtons[0] = (Button) findViewById(R.id.meaning1_button);
		mMeaningButtons[1] = (Button) findViewById(R.id.meaning2_button);
		mMeaningButtons[2] = (Button) findViewById(R.id.meaning3_button);
		mMeaningButtons[3] = (Button) findViewById(R.id.meaning4_button);
		
		for (int i = 0; i < NUM_WORDS; i++)
		{
			mWordButtons[i].setOnTouchListener(this);
			mMeaningButtons[i].setOnTouchListener(this);

			//mWordButtons[i].getBackground().setColorFilter(new LightingColorFilter(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON), Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON)));
			//mMeaningButtons[i].getBackground().setColorFilter(new LightingColorFilter(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON), Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON)));
			
			mWordButtons[i].setTextColor(Color.BLACK);
			mMeaningButtons[i].setTextColor(Color.BLACK);
		}
	}

	@Override
	protected void doRefreshViews() 
	{
		if (mGameState == Constants.GAME_STATE_GIVEN_UP || mGameState == Constants.GAME_STATE_FAILED || mGameState == Constants.GAME_STATE_COMPLETED)
		{
			mEnableButtons = false;
		}
		
		refreshPanel();
	}

	/**
	 * Refresh the word and meaning buttons to fix the look depending on whether
	 * they have already been 'matched' or not.
	 */
	private void refreshButtons()
	{
		
		float[] radii = new float[] {10, 10, 10, 10, 10, 10, 10, 10};
		
		for (int i = 0; i < NUM_WORDS; i++)
		{		
			/*
			if (isWordFound(i))
			{
				GradientDrawable gd = new GradientDrawable();
				//footerBackground.setShape(new RoundRectShape(radii, null, null));
				gd.setCornerRadius(10);
				gd.setColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_FOUND_BUTTON));
				gd.setStroke(1, Themes.getColorFor(Themes.COLOR_MATCH_WORDS_LINE));
				mWordButtons[i].setBackgroundDrawable(gd);
				mMeaningButtons[mWordToMeaningMap[i]].setBackgroundDrawable(gd);
			}
			else
			{
				GradientDrawable gd = new GradientDrawable();
				//footerBackground.setShape(new RoundRectShape(radii, null, null));
				gd.setCornerRadius(10);
				gd.setColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON));
				gd.setStroke(1, Themes.getColorFor(Themes.COLOR_MATCH_WORDS_LINE));
				mWordButtons[i].setBackgroundDrawable(gd);
				mMeaningButtons[mWordToMeaningMap[i]].setBackgroundDrawable(gd);
			}
			*/
			
			if (isWordFound(i))
			{
				mWordButtons[i].setBackgroundColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_FOUND_BUTTON));
				mMeaningButtons[mWordToMeaningMap[i]].setBackgroundColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_FOUND_BUTTON));
			}
			else
			{
				mWordButtons[i].setBackgroundColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON));
				mMeaningButtons[mWordToMeaningMap[i]].setBackgroundColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON));
			}
						
			mWordButtons[i].setTextColor(Color.BLACK);
			mMeaningButtons[i].setTextColor(Color.BLACK);
		}
	}
	
	@Override
	protected String generateGameData() 
	{
		String data = "";
		data += "@" + KEY_DATA_WORDS;
		
		for (int i = 0; i < mWords.size(); i++)
		{
			data += "#" + mWords.get(i) + "%" + mWordToMeaningMap[i];
			for (int j = 0; j < mChosenPairs.size(); j++)
			{
				int[] val = mChosenPairs.get(j);
				if (val[0] == i)
				{
					data += "%" + val[1];
					break;
				}
			}
		}
		
		data += "@" + KEY_DATA_MEANINGS;
		for (int i = 0; i < mMeanings.size(); i++)
		{
			data += "#" + mMeanings.get(i);
		}
		
		data += "@" + KEY_DATA_FOUND_WORDS;
		for (int i = 0; i < mFoundWords.size(); i++)
		{
			data += "#" + mFoundWords.get(i);
		}

		data += "@";
		
		data = Utils.encodeStringForDB(data);
		
		return data;
	}

	/**
	 *  @WORDS#<word1>%<matchign meaning>%<chosen meaning index>#<word2>%<matchign meaning>%<chosen meaning index>@MEANINGS#<meaning1>#<meaning2>@
	 * 		FOUNDWORDS#<index1>#<index2>@
	 * 
	 * - Words are listed in the order of visual appearance in the UI (top to bottom)
	 * - Meanings are listed the order of visual appearance in the UI (top to bottom)
	 * - Next to each word are 2 numbers: First is the matching meaning index (visual order), and the second is the index to the meaning button the user has 'chosen' to match this word with, but is actually not the right meaning. this number is absent in case this word has been paired yet.
	 * - The FOUNDWORDS list contains the indexes to the words that are correctly found by the user
	 * - No. of words is same as no. of meanings
	 */
	@Override
	protected String generateGameDataFromDB() 
	{
		ArrayList<WordShortInfo> wordList = mWordDbAdapter.fetchWordsOfCommonType(mPlayType, mIsPlay, (mIsPlay ? mLevel : mPracticeType), NUM_WORDS);

		int[] wordToMeaningMap = new int[wordList.size()];
		for (int i = 0; i < wordToMeaningMap.length; i++)
		{
			wordToMeaningMap[i] = i;
		}
		Utils.shuffleArray(wordToMeaningMap, NUM_WORDS);
		
		int[] meaningToWordMap = new int[wordList.size()];
		for (int i = 0; i < meaningToWordMap.length; i++)
		{
			meaningToWordMap[wordToMeaningMap[i]] = i;
		}
		
		String data = "";
		data += "@" + KEY_DATA_WORDS;
		
		for (int i = 0; i < wordList.size(); i++)
		{
			data += "#" + wordList.get(i).word + "%" + wordToMeaningMap[i];
		}
		
		data += "@" + KEY_DATA_MEANINGS;
		for (int i = 0; i < wordList.size(); i++)
		{
			data += "#" + wordList.get(meaningToWordMap[i]).meaning;
		}
		
		data += "@" + KEY_DATA_FOUND_WORDS;

		data += "@";
		
		data = Utils.decodeStringFromDB(data);
		return data;
	}

	@Override
	protected void doHandleGivenUp() 
	{
		/**
		 * Just add all words to the found words and chosen pairs
		 * lists as that will mark all pairs for the user.
		 */
		
		//first clear the lists
		mFoundWords.clear();
		mChosenPairs.clear();
		
		for (int i = 0; i < NUM_WORDS; i++)
		{		
			//Now add the word to the lists
			mFoundWords.add(i);
			mChosenPairs.add(new int[] {i, mWordToMeaningMap[i]});
		}
	}

	private void printVals0()
	{
		Utils.Log("gaurav: mWordToMeaningMap:");
		for (int i = 0; i < 4; i++)
		{
			Utils.Log("gaurav: " + i + " -> " + mWordToMeaningMap[i]);
		}
	}
	private void printVals(String header)
	{
		Utils.Log(header);
		Utils.Log("gaurav: mChosenPairs:");
		for (int i = 0; i < mChosenPairs.size(); i++)
		{
			Utils.Log("gaurav: " + i + ": 0 = " + mChosenPairs.elementAt(i)[0] + ", 1 = " + mChosenPairs.elementAt(i)[1]);
		}
		
		Utils.Log("gaurav: mFoundWords");
		for (int i = 0; i < mFoundWords.size(); i++)
		{
			Utils.Log("gaurav: " + i + ": " + mFoundWords.get(i));
		}
		
		Utils.Log("gaurav: mEligibleHintButtons");
		for (int i = 0; i < mEligibleHintButtons.size(); i++)
		{
			Utils.Log("gaurav: " + i + ": " + mEligibleHintButtons.get(i));
		}
	}
	@Override
	protected void doHandleHintUsed() 
	{
		//Utils.Log("\n\n#########################################");
		//printVals0();
		//printVals("gaurav: doHandleHintUsed, before:");
		/**
		 * When a hint needs to be consumed, we first check to see if there are some chosenPairs
		 * that are wrong matches, and try to consume the hint by matching one of these first
		 * ahead of revealing a new match.  This gives the user an opportunity to learn words
		 * that he marked incorrectly.
		 */
		boolean foundMisMatch = false;
		for (int j = 0; j < mChosenPairs.size(); j++)
		{
			int wordButton = mChosenPairs.elementAt(j)[0];
			int meaningButton = mChosenPairs.elementAt(j)[1];
			if (mWordToMeaningMap[wordButton] != meaningButton)
			{
				foundMisMatch = true;
				
				//fix this match
				int realMeaningIndex = mWordToMeaningMap[wordButton];
				mChosenPairs.set(j, new int[] {wordButton, realMeaningIndex});
				
				//now, remove any existing pair for this real meaning
				for (int i = 0; i < mChosenPairs.size(); i++)
				{
					//skip the entry we just added
					if ((mChosenPairs.elementAt(i)[0] != wordButton) && (mChosenPairs.elementAt(i)[1] == realMeaningIndex))
					{
						int wordIndex = mChosenPairs.elementAt(i)[0];
						
						mChosenPairs.remove(i);
						mEligibleHintButtons.add(wordIndex);
						break;
					}
				}
				
				//add this to the found list
				mFoundWords.add(wordButton);
				
				break;
			}
		}
		
		/**
		 * If no wrongly paired matches were present, we have no option but
		 * to reveal a fresh matching pair.
		 */
		if (foundMisMatch == false)
		{
			//check if there's scope for hints
			if (mChosenPairs.size() < (NUM_WORDS - 1) && mEligibleHintButtons.size() > 0)
			{
				//select any unchosen button and match it
		    	Random rand = new Random();
				int eligibleButton = rand.nextInt(mEligibleHintButtons.size());
				int eligibleButtonIndex = mEligibleHintButtons.elementAt(eligibleButton);
				
				//fix this match
				mChosenPairs.add(new int[] {eligibleButtonIndex, mWordToMeaningMap[eligibleButtonIndex]});
				mEligibleHintButtons.remove(eligibleButton);
				
				//add to the found list
				mFoundWords.add(eligibleButtonIndex);
			}
		}
		//printVals("gaurav: doHandleHintUsed, after:");
		//Utils.Log("#########################################\n\n");
	}

	@Override
	protected void doHandleButtonClicked(int id) 
	{
		//we don't need to handle button clicks for this game
	}
	
	/**
	 * Returns the word or meaning button that contains the given point, or -1 if none.
	 * @param p The point
	 * @param type MATCH_WORD for word button, MATCH_MEANING for meaning button
	 * @return The index to the button that contains the point, or -1 if none found
	 */
	int getButtonContainingPoint(Point p, int type)
	{
		Rect r = new Rect(0, 0, 0, 0);
		
		if (type == MATCH_WORD)
		{
			for (int i = 0; i < NUM_WORDS; i++)
			{
				mWordButtons[i].getGlobalVisibleRect(r, null);
			
				if (r.contains(p.x, p.y))
				{
					return i;
				}
			}
		}
		else
		{
			for (int i = 0; i < NUM_WORDS; i++)
			{
				mMeaningButtons[i].getGlobalVisibleRect(r, null);
			
				if (r.contains(p.x, p.y))
				{
					return i;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Gets the anchor points for the given chosen pair.  Refer getAnchorPointForButton
	 * for more information about anchor points.
	 * @param chosenPairs The chosen pairs.  First index is the index of the word button, second is the index of the meaning button
	 * @return a pair of points that are the anchor positions for the given pair of buttons
	 */
	Point[] getAnchorPoints(int[] chosenPairs)
	{
		Point buttonPoint = getAnchorPointForButton(chosenPairs[0], MATCH_WORD);
		Point meaningPoint = getAnchorPointForButton(chosenPairs[1], MATCH_MEANING);
		
		return new Point[] {buttonPoint, meaningPoint};
	}
	
	/**
	 * Get the anchor point for the given button.  The anchor point is the point from which
	 * the line is drawn for a given button, and is on one side of the button (right in case of
	 * word button and left in case of meaning button)
	 * @param buttonIndex The index to the button
	 * @param type The type of button, MATCH_WORD in case of word button, MATCH_MEANING in case of meaning button
	 * @return the Point correspondign to the anchor position
	 */
	Point getAnchorPointForButton(int buttonIndex, int type)
	{
		Rect r = new Rect(0, 0, 0, 0);
		Point p = new Point(0, 0);
		if (type == MATCH_WORD)
		{
			mWordButtons[buttonIndex].getGlobalVisibleRect(r, null);
			p.x = r.right + MatchWordsPanel.ANCHOR_RADIUS;
			p.y = (r.top + r.bottom)/2;
		}
		else
		{
			mMeaningButtons[buttonIndex].getGlobalVisibleRect(r, null);
			p.x = r.left - MatchWordsPanel.ANCHOR_RADIUS;
			p.y = (r.top + r.bottom)/2;
		}
		
		return p;
	}
	
	/**
	 * Check to see if the word corresponding to this button index has already been successfully paired.
	 * @param wordButtonIndex the index to the word button
	 * @return true if already found, false otherwise
	 */
	boolean isWordFound(int wordButtonIndex)
	{
		for (int i = 0; i < mFoundWords.size(); i++)
		{
			if (wordButtonIndex == mFoundWords.get(i))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check to see if the game has completed successfully.
	 * @return true if the game is completed, false otherwise
	 */
	boolean isGameCompleted()
	{
		boolean completed = true;
		for (int i = 0; i < mWords.size(); i++)
		{
			if (!isWordFound(i))
			{
				completed = false;
				break;
			}
		}
		
		return completed;
	}
	
	/**
	 * Refresh the panel with the latest state.
	 */
	private void refreshPanel()
	{
		mPanel.updateData(mCurrentStartPoint, mCurrentEndPoint, mChosenPairs, mFoundWords);
		mPanel.invalidate();
		refreshButtons();
	}
	
	/**
	 * This is the main handler that is called during the drag operation.
	 */
	public boolean onTouch(View v, MotionEvent event)
	{
		if (mEnableButtons == false) //ignore touch if the buttons aren't enabled
		{
			return false;
		}
		
		Point p = new Point((int)event.getRawX(), (int)event.getRawY());
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) //the drag has started
		{
			int buttonIndex = -1;
			if ((buttonIndex = getButtonContainingPoint(p, MATCH_WORD)) != -1 && !isWordFound(buttonIndex))
			{
				doTheMatch(buttonIndex, MATCH_WORD); //match the drag starting from a word button
				refreshPanel();
			}
			else if ((buttonIndex = getButtonContainingPoint(p, MATCH_MEANING)) != -1 && !isWordFound(mMeaningToWordMap[buttonIndex]))
			{
				doTheMatch(buttonIndex, MATCH_MEANING); //match the drag starting from a meaning button
				refreshPanel();
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE) //the drag is ongoing
		{
			if (mCurrentMatchIndex > -1) //there was an ongoing drag, so just draw it out
			{
				mCurrentEndPoint = p;
				refreshPanel();
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) //the drag has ended
		{
			int buttonIndex = -1;
			if ((buttonIndex = getButtonContainingPoint(p, MATCH_WORD)) != -1 && !isWordFound(buttonIndex))
			{
				doTheMatch(buttonIndex, MATCH_WORD); //match the drag ending on a word button
				refreshPanel();
			}
			else if ((buttonIndex = getButtonContainingPoint(p, MATCH_MEANING)) != -1 && !isWordFound(mMeaningToWordMap[buttonIndex]))
			{
				doTheMatch(buttonIndex, MATCH_MEANING); //match the drag ending on a meaning button
				refreshPanel();
			}
			else
			{
				if (mMatchState != STATE_MATCH_ENDED)
				{
					//reset if finger was lifted in the middle (non-button)
					mMatchState = STATE_MATCH_ENDED;
					
					if (mCurrentMatchIndex != -1)
					{
						mCurrentStartPoint = new Point(0, 0);
						mCurrentEndPoint = new Point(0, 0);
						mCurrentMatchIndex = -1;
						
						refreshPanel();
					}
				}
			}
		}

		return super.onTouchEvent(event);
	}
	
	/**
	 * This function is the core routine that tries to do the matching of a word to a meaning depending
	 * on the state of the drag.
	 * - When there is no drag, mMatchState is set to STATE_MATCHE_ENDED
	 * - When a drag begins from "inside" a button:
	 * 		a) mMatchState is set to STATE_MATCH_STARTED
	 * 		b) mCurrentMatchIndex is set to the index of the button from which the drag started
	 * 		c) mCurrentStartPoint is set to the anchor point for the button from which the drag started
	 * 		d) mWord2Meaning is set appropriately depending on the direction of the drag
	 * 		e) If this button already had an anchor point (i.e., if the button was listed in mChosenWords)
	 * 			1) Remove the button and its pair from the mChosenWords
	 * 			2) mCurrentMatchIndex is set to the index of the button that was paired with this in mChosenWords
	 * 			3) mCurrentStartPoint is set to the anchor point of the button that was paired with this in mChosenWords
	 * - When a drag ends inside a button:
	 * 		a) If the dropped button already has a pair in mChosenPairs, disallow the drop
	 * 		b) mMatchState is set to STATE_MATCH_ENDED
	 * 		c) mCurrentMatchIndex is reset to null values
	 * 		d) mCurrentStartPoint is also reset to null values
	 * 		e) This chosen pair is now added to mChosenPairs
	 * 		f) A check is performed to see if this was a actual "found match" or not, and if it was, if this
	 * 			resulted in the game to be completed or not, and the appropriate base class callback is invoked
	 * 
	 * @param buttonIndex The index of the word or meaning button where the drag is starting or ending
	 * @param type MATCH_WORD in case of word button, MATCH_MEANING in case of meaning button
	 */
	private void doTheMatch(int buttonIndex, int type)
	{
		if (mMatchState == STATE_MATCH_ENDED) //the drag has started
		{
			if (type == MATCH_WORD) //the drag started from a word button
			{
				//check if there's already an anchor point on this button
				boolean anchorPresent = false;
				for (int i = 0; i < mChosenPairs.size(); i++)
				{
					if (mChosenPairs.elementAt(i)[0] == buttonIndex)
					{						
						//treat this as changing the end point of an existing meaning2word map
						mCurrentMatchIndex = mChosenPairs.elementAt(i)[1];
						mCurrentStartPoint = getAnchorPointForButton(mChosenPairs.elementAt(i)[1], MATCH_MEANING);
						
						mWord2Meaning = false;
						
						//remove the chosen pair
						mChosenPairs.remove(i);
						
						anchorPresent = true;
						break;
					}
				}
				
				if (!anchorPresent)
				{
					mCurrentStartPoint = getAnchorPointForButton(buttonIndex, MATCH_WORD);
					mWord2Meaning = true;
					
					mCurrentMatchIndex = buttonIndex;
				}
			}
			else if (type == MATCH_MEANING) //the drag started from a meaning button
			{
				//check if there's already an anchor point on this button
				boolean anchorPresent = false;
				for (int i = 0; i < mChosenPairs.size(); i++)
				{
					if (mChosenPairs.elementAt(i)[1] == buttonIndex)
					{						
						//treat this as changing the end point of an existing meaning2word map
						mCurrentMatchIndex = mChosenPairs.elementAt(i)[0];
						mCurrentStartPoint = getAnchorPointForButton(mChosenPairs.elementAt(i)[0], MATCH_WORD);
						mWord2Meaning = true;
						
						//remove the chosen pair
						mChosenPairs.remove(i);
						
						anchorPresent = true;
						break;
					}
				}
				
				if (!anchorPresent)
				{
					mCurrentStartPoint = getAnchorPointForButton(buttonIndex, MATCH_MEANING);
					mWord2Meaning = false;
					
					mCurrentMatchIndex = buttonIndex;
				}
			}
			
			//the match has now started
			mMatchState = STATE_MATCH_STARTED;
		}
		else if (mMatchState == STATE_MATCH_STARTED) //the drag has ended
		{
			boolean allowDrop = true;
			
			/**
			 * Disallow drop if button is already part of a chosen pair.  The user must
			 * clear the existing chosen pair if he wishes to drop on this button.
			 */
			for (int i = 0; i < mChosenPairs.size(); i++)
			{
				if ((type == MATCH_WORD && mChosenPairs.elementAt(i)[0] == buttonIndex) || (type == MATCH_MEANING && mChosenPairs.elementAt(i)[1] == buttonIndex))
				{
					//already chosen, so don't allow this drop
					mCurrentStartPoint.x = 0;
					mCurrentStartPoint.y = 0;
					mCurrentEndPoint.x = 0;
					mCurrentEndPoint.y = 0;
					
					allowDrop = false;
					break;
				}
			}
			
			if (allowDrop)
			{
				if ((type == MATCH_WORD) && (mWord2Meaning == false)) //the user dropped from a meaning to a word button
				{					
					mCurrentStartPoint.x = 0;
					mCurrentStartPoint.y = 0;
					mCurrentEndPoint.x = 0;
					mCurrentEndPoint.y = 0;
					
					mChosenPairs.add(new int[]{buttonIndex, mCurrentMatchIndex});

					for (int i = 0; i < mEligibleHintButtons.size(); i++)
					{
						if (mEligibleHintButtons.elementAt(i) == buttonIndex)
						{
							mEligibleHintButtons.remove(i);
							break;
						}
					}	
					
					//check for matched pair
					if (mWordToMeaningMap[buttonIndex] == mCurrentMatchIndex)
					{
						//this is a match!
						onRightWordOrLetterFound();
						
						//mark it in the found list
						mFoundWords.add(buttonIndex);
						
						if (isGameCompleted())
						{
							Message msg = Message.obtain();
							msg.what = MESSAGE_CHECK_GAME_COMPLETED;
							mMessageHandler.sendMessage(msg);
						}
					}
					else
					{
						//wrong match
						onWrongWordOrLetterFound();
					}
					
					mCurrentMatchIndex = -1;
				}
				else if ((type == MATCH_MEANING) && (mWord2Meaning == true)) //the user dropped from a word to a meaning button
				{					
					mCurrentStartPoint.x = 0;
					mCurrentStartPoint.y = 0;
					mCurrentEndPoint.x = 0;
					mCurrentEndPoint.y = 0;
					
					mChosenPairs.add(new int[]{mCurrentMatchIndex, buttonIndex});

					for (int i = 0; i < mEligibleHintButtons.size(); i++)
					{
						if (mEligibleHintButtons.elementAt(i) == mCurrentMatchIndex)
						{
							mEligibleHintButtons.remove(i);
							break;
						}
					}
					
					//check for matched pair
					if (mWordToMeaningMap[mCurrentMatchIndex] == buttonIndex)
					{
						//this is a match!
						onRightWordOrLetterFound();
						
						//mark it in the found list
						mFoundWords.add(mCurrentMatchIndex);
						
						if (isGameCompleted())
						{
							Message msg = Message.obtain();
							msg.what = MESSAGE_CHECK_GAME_COMPLETED;
							mMessageHandler.sendMessage(msg);
						}
					}
					else
					{
						//wrong match
						onWrongWordOrLetterFound();
					}
					
					mCurrentMatchIndex = -1;
				}
				else
				{
					//can't drop on the same dirction					
					mCurrentStartPoint.x = 0;
					mCurrentStartPoint.y = 0;
					mCurrentEndPoint.x = 0;
					mCurrentEndPoint.y = 0;
				}
			}
			
			//reset the match state to ENDED
			mMatchState = STATE_MATCH_ENDED;
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

	/********************** Member Variables ***************/
	
	private static final int MATCH_WORD = 0; //used to indicate the match was for a word button
	private static final int MATCH_MEANING = 1; //used to indicate the match was for a meaning button
		
	private static final int STATE_MATCH_STARTED = 0; //this state is entered after the user starts dragging his finger from a button
	private static final int STATE_MATCH_ENDED = 1; //this state is the beginning state, or after a user stops dragging his finger
	
	private int mMatchState; //start with this stage
	private int mCurrentMatchIndex = -1; //this is the index of the word or meaning button from which the current line is being dragged
	private Boolean mWord2Meaning = false; //direction of drag.  true if from word to meaning, false if the other way
	
	private Vector<String> mWords; //words in visual button listing order
	private Vector<String> mMeanings; //meanings in visual button listing order
	
	private Point mCurrentStartPoint; // the 'current' start point of the drag
	private Point mCurrentEndPoint; //the 'current' end point of the drag
	
	private Vector<int[]> mChosenPairs; // chosen pairs, including "matched" pairs.  Found words/pairs are tracked in mFoundWords
	private Vector<Integer> mFoundWords; // indexes into the mWords list for all "correctly found" words
	private Vector<Integer> mEligibleHintButtons; // word buttons eligible for hints
	
	private int[] mWordToMeaningMap; //mapping from mWords list to mMeanings list for correctly matching pairs
	private int[] mMeaningToWordMap; //mapping from mMeanings list to mWords list for correctly matching pairs
	
	private boolean mEnableButtons; //whether the word and meaning buttons need to be enabled or not
	
	/**
	 * UI elements
	 */
	private Button[] mWordButtons; //the actual word buttons
	private Button[] mMeaningButtons; //the actual meaning buttons
	private MatchWordsPanel mPanel; //the panel that hosts the drawing of matching lines between words and meanings

	/**
	 * Message handling related.
	 * Note: we need the message handler so we can asynchrnously do something in the context of a touch
	 * event callback without breaking the call hierarchy.  Refer class MessageHandler for more details
	 */
	private MessageHandler mMessageHandler; // this is required to asynchronously process events such as check for game completed
	private static final int MESSAGE_CHECK_GAME_COMPLETED = 100; //id used to identify the message between sender and receiver
	
	/**
	 * Game Constants
	 */
	private static final int NUM_WORDS = 4;
	
	/**
	 * Data Keys used for storing/restoring the game.
	 */
	private static final String KEY_DATA_WORDS = "WORDS";
	private static final String KEY_DATA_MEANINGS = "MEANINGS";
	private static final String KEY_DATA_FOUND_WORDS = "FOUNDWORDS";
}