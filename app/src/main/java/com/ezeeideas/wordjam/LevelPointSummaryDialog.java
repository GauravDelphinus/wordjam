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

import java.text.DecimalFormat;

import android.content.Context;
import android.widget.TextView;

public class LevelPointSummaryDialog extends GameDialogType0 
{

	public LevelPointSummaryDialog(Context context, GameUtils.LevelInfo levelInfo) 
	{
		super(context);
		Utils.Log("in constructor, levelInfo = " + levelInfo);
		mLevelInfo = levelInfo;
		
		setupViews();
		
		postSetupViews();
	}

	protected void setupViews() 
	{
		Utils.Log("setupViews in child");
		setContentView(R.layout.dialog_level_point_summary);
		
		TextView titleTextView = (TextView) findViewById(R.id.dialog_title);
		String titleText = String.format(getContext().getResources().getString(R.string.dialog_leveL_point_summary_title_text), mLevelInfo.levelNum);
		titleTextView.setText(titleText);
		
		TextView pointsRemainingValueTextView = (TextView) findViewById(R.id.points_remaining_value_text_view);
		pointsRemainingValueTextView.setText(String.valueOf(mLevelInfo.levelPointsRemaining));
		
		TextView pointsEarnedTextView = (TextView) findViewById(R.id.points_earned_text_view);
		String pointsEarnedText = String.format(getContext().getResources().getString(R.string.points_remaining_text), mLevelInfo.levelTotalPoints - mLevelInfo.levelPointsRemaining);
		pointsEarnedTextView.setText(pointsEarnedText);
		
		TextView gamesRemainingValueTextView = (TextView) findViewById(R.id.games_remaining_value_text_view);
		gamesRemainingValueTextView.setText(String.valueOf(mLevelInfo.levelGamesRemaining));
		
		TextView gamesCompletedTextView = (TextView) findViewById(R.id.games_earned_text_view);
		String gamesCompletedText = String.format(getContext().getResources().getString(R.string.games_remaining_text), mLevelInfo.levelTotalGames - mLevelInfo.levelGamesRemaining);
		gamesCompletedTextView.setText(gamesCompletedText);
		
		TextView hintsRemainingValueTextView = (TextView) findViewById(R.id.hints_remaining_value_text_view);
		hintsRemainingValueTextView.setText(String.valueOf(mLevelInfo.levelHintsRemaining));
		
		TextView hintsConsumedTextView = (TextView) findViewById(R.id.hints_used_text_view);
		String hintsConsumedText = String.format(getContext().getResources().getString(R.string.hints_remaining_text), mLevelInfo.levelTotalHints + mLevelInfo.levelHintsEarned - mLevelInfo.levelHintsRemaining, mLevelInfo.levelHintsEarned);
		hintsConsumedTextView.setText(hintsConsumedText);
		
		double progress = GameUtils.getLevelProgress(mLevelInfo);
		DecimalFormat format = new DecimalFormat("#.00");
		String progressStr = format.format(progress);
		TextView progressValueTextView = (TextView) findViewById(R.id.progress_value_text_view);
		String progressValueText = String.format(getContext().getResources().getString(R.string.progress_value_text), progressStr);
		progressValueTextView.setText(progressValueText);
		
		TextView progressTextView = (TextView) findViewById(R.id.progress_text_view);
		if (progress < 50)
		{
			progressTextView.setText(R.string.progress_text_bad);
		}
		else if (progress < 75)
		{
			progressTextView.setText(R.string.progress_text_medium);
		}
		else
		{
			progressTextView.setText(R.string.progress_text_good);
		}
	}

	@Override
	protected void setupRootView() 
	{
		mRootView = findViewById(R.id.game_dialog_root_layout);
	}
	
	private GameUtils.LevelInfo mLevelInfo; //the level information required to display the dialog data
}
