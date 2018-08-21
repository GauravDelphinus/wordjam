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

import android.app.Activity;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

public class HelpDialog extends Activity implements OnClickListener
{

		
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
   
		setContentView(R.layout.helpdialog);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mPlayType = extras.getInt(Constants.KEY_PLAY_TYPE);
			mGameType = extras.getInt(Constants.KEY_GAME_TYPE);
			mIsPlay = extras.getBoolean(Constants.KEY_IS_PLAY);
			mGameLevel = extras.getInt(Constants.KEY_GAME_LEVEL);
			mPracticeType = extras.getInt(Constants.KEY_PRACTICE_TYPE);
		}
		
		//do global values first
		TextView stopText = (TextView) findViewById(R.id.stop_help_text);
		stopText.setText(getResources().getString(R.string.stop_help_text_all_tests));	
		
		TextView pauseText = (TextView) findViewById(R.id.pause_help_text);
		pauseText.setText(getResources().getString(R.string.pause_help_text_all_tests));
		
		TextView revealText = (TextView) findViewById(R.id.reveal_help_text);
		revealText.setText(getResources().getString(R.string.reveal_help_text_all_tests));
		
		TextView nextText = (TextView) findViewById(R.id.next_help_text);
		nextText.setText(getResources().getString(R.string.next_help_text_all_tests));
		
		TextView helpText = (TextView) findViewById(R.id.help_help_text);
		helpText.setText(getResources().getString(R.string.help_help_text_all_tests));
		
		TextView shareText = (TextView) findViewById(R.id.share_help_text);
		shareText.setText(getResources().getString(R.string.share_help_text_all_tests));
		
		int[][] pointsChart = GameUtils.getPointChart(mPlayType, mIsPlay, mGameLevel, mPracticeType);
		
		String hintsStr = String.format(getResources().getString(R.string.help_hints_general_text),
				pointsChart[GameUtils.GAME_HINTS_TOTAL][mGameType]);
		
		TextView pointsText = (TextView) findViewById(R.id.help_points);

		String pointsStr = String.format(getResources().getString(R.string.help_points_general_text), 
				pointsChart[GameUtils.GAME_POINTS_ON_COMPLETION][mGameType],
				pointsChart[GameUtils.GAME_BONUS_POINTS_ON_FAST_COMPLETION][mGameType],
				pointsChart[GameUtils.GAME_FAST_COMPLETION_TIME][mGameType],
				-1 * pointsChart[GameUtils.GAME_POINTS_ON_GIVING_UP][mGameType]);
		
		//now do the ones that have test specific values
		TextView hintText = (TextView) findViewById(R.id.hint_help_text);
		TextView goalText = (TextView) findViewById(R.id.help_goal);
		TextView tipsText = (TextView) findViewById(R.id.help_tips);
		TextView titleText = (TextView) findViewById(R.id.help_title_text_view);
		

		String titleTextStr = String.format(getResources().getString(R.string.help_title), GameUtils.getGameNameFromGameType(mGameType, this));
		titleText.setText(titleTextStr);
		
		//tipsText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		tipsText.setSingleLine(false);
		
		if (mGameType == Constants.GAME_TYPE_RECALL_WORDS)
		{
			hintText.setText(getResources().getString(R.string.hint_help_text_word_meaning_test) + "  " + hintsStr);
			goalText.setText(getResources().getString(R.string.help_goal_word_meaning_test));
			tipsText.setText(getResources().getString(R.string.help_tips_word_meaning_test));
			pointsText.setText(pointsStr);
		}
		else if (mGameType == Constants.GAME_TYPE_MATCH_WORDS)
		{
			hintText.setText(getResources().getString(R.string.hint_help_text_match_words_test) + "  " + hintsStr);
			goalText.setText(getResources().getString(R.string.help_goal_match_words_test));
			tipsText.setText(getResources().getString(R.string.help_tips_match_words_test));
			String pointsStr2 = String.format(getResources().getString(R.string.help_points_match_words_text), 
					pointsChart[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType],
					-1 * pointsChart[GameUtils.GAME_POINTS_ON_INCORRECT_WORD_OR_LETTER][mGameType]);
			pointsText.setText(pointsStr + pointsStr2);
		}
		else if (mGameType == Constants.GAME_TYPE_HANGMAN)
		{
			hintText.setText(getResources().getString(R.string.hint_help_text_hangman_test) + "  " + hintsStr);
			goalText.setText(getResources().getString(R.string.help_goal_hangman_test));
			tipsText.setText(getResources().getString(R.string.help_tips_hangman_test));
			String pointsStr2 = String.format(getResources().getString(R.string.help_points_hangman_text), 
					pointsChart[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType]);
			pointsText.setText(pointsStr + pointsStr2);
		}
		else if (mGameType == Constants.GAME_TYPE_CROSSWORD)
		{
			hintText.setText(getResources().getString(R.string.hint_help_text_crossword_test) + "  " + hintsStr);
			goalText.setText(getResources().getString(R.string.help_goal_crossword_test));
			tipsText.setText(getResources().getString(R.string.help_tips_crossword_test));
			String pointsStr2 = String.format(getResources().getString(R.string.help_points_crossword_text), 
					pointsChart[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType],
					-1 * pointsChart[GameUtils.GAME_POINTS_ON_INCORRECT_WORD_OR_LETTER][mGameType]);
			pointsText.setText(pointsStr + pointsStr2);
		}
		else if (mGameType == Constants.GAME_TYPE_WORD_JUMBLE)
		{
			hintText.setText(getResources().getString(R.string.hint_help_text_jumble_test) + "  " + hintsStr);
			goalText.setText(getResources().getString(R.string.help_goal_jumble_test));
			tipsText.setText(getResources().getString(R.string.help_tips_jumble_test));
			String pointsStr2 = String.format(getResources().getString(R.string.help_points_word_jumble_text), 
					pointsChart[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType]);
			pointsText.setText(pointsStr + pointsStr2);
		}
		else if (mGameType == Constants.GAME_TYPE_WORD_SEARCH)
		{
			hintText.setText(getResources().getString(R.string.hint_help_text_wordsearch_test) + "  " + hintsStr);
			goalText.setText(getResources().getString(R.string.help_goal_wordsearch_test));
			tipsText.setText(getResources().getString(R.string.help_tips_wordsearch_test));
			String pointsStr2 = String.format(getResources().getString(R.string.help_points_word_search_text), 
					pointsChart[GameUtils.GAME_POINTS_ON_CORRECT_WORD_OR_LETTER][mGameType]);
			pointsText.setText(pointsStr + pointsStr2);
		}
		
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);
    	
		honorTheme();
	}
	
	private void honorTheme()
    {
    	LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
    	ll.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    }
	
	public void onClick(View v)
    {
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }
	
	private int mPlayType, mGameType;
	private boolean mIsPlay;
	private int mGameLevel, mPracticeType;
}