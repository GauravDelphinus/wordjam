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

public class PracticePointSummaryDialog extends GameDialogType1
{

	public PracticePointSummaryDialog(Context context, GameUtils.PracticeInfo practiceInfo) 
	{
		super(context);
		mPracticeInfo = practiceInfo;
		
		setupViews();
		
		postSetupViews();
	}

	protected void setupViews() 
	{
		Utils.Log("setupViews in child");
		setContentView(R.layout.dialog_practice_point_summary);
		
		TextView titleTextView = (TextView) findViewById(R.id.dialog_practice_point_summary_title_text_view);
		String titleText = String.format(getContext().getResources().getString(R.string.dialog_practice_point_summary_title_text), mPracticeInfo.practiceType);
		titleTextView.setText(titleText);
		
		TextView pointsRemainingValueTextView = (TextView) findViewById(R.id.points_earned_value_text_view);
		pointsRemainingValueTextView.setText(String.valueOf(mPracticeInfo.practicePointsEarned));
		
		TextView gamesRemainingValueTextView = (TextView) findViewById(R.id.games_completed_value_text_view);
		gamesRemainingValueTextView.setText(String.valueOf(mPracticeInfo.practiceGamesCompleted));
		
		TextView hintsRemainingValueTextView = (TextView) findViewById(R.id.hints_used_value_text_view);
		hintsRemainingValueTextView.setText(String.valueOf(mPracticeInfo.practiceHintsUsed));
		
		TextView timeSpentValueTextView = (TextView) findViewById(R.id.time_spent_value_text_view);
		String timeValue = Utils.convertTime(mPracticeInfo.practiceTimeSpent);
		timeSpentValueTextView.setText(timeValue);
	}

	@Override
	protected void setupRootView() 
	{
		mRootView = findViewById(R.id.game_dialog_root_layout);
	}
	
	private GameUtils.PracticeInfo mPracticeInfo; //the level information required to display the dialog data

	@Override
	protected void setupPositiveAction1View() 
	{
		mPositiveAction1View = findViewById(R.id.share_button);
	}
}
