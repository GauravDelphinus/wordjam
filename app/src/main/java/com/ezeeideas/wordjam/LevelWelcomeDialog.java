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

public class LevelWelcomeDialog extends GameDialogType0 
{
	public LevelWelcomeDialog(Context context, int level, int levelTotalGames, int levelTotalHints, int levelTotalPoints)
	{
		super(context);
		
		mLevel = level;
		mTotalGames = levelTotalGames;
		mTotalHints = levelTotalHints;
		mTotalPoints = levelTotalPoints;
		
		setupViews();
		
		postSetupViews();
	}

	protected void setupViews() 
	{
		Utils.Log("setupViews in child");
		setContentView(R.layout.dialog_level_welcome);
		
		TextView titleTextView = (TextView) findViewById(R.id.dialog_title);
		String titleText = String.format(getContext().getResources().getString(R.string.dialog_level_welcome_title), mLevel);
		titleTextView.setText(titleText);
		
		TextView pointsRemainingValueTextView = (TextView) findViewById(R.id.points_to_be_earned_text_view);
		pointsRemainingValueTextView.setText(String.valueOf(mTotalPoints));
		
		TextView gamesRemainingValueTextView = (TextView) findViewById(R.id.games_to_be_completed_text_view);
		gamesRemainingValueTextView.setText(String.valueOf(mTotalGames));
		
		TextView hintsRemainingValueTextView = (TextView) findViewById(R.id.hints_available_text_view);
		hintsRemainingValueTextView.setText(String.valueOf(mTotalHints));
	}

	@Override
	protected void setupRootView() 
	{
		mRootView = findViewById(R.id.game_dialog_root_layout);
	}
	
	private int mLevel;
	private int mTotalGames, mTotalPoints, mTotalHints; //total values for this level at start
}
