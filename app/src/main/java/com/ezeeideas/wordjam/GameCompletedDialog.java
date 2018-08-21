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

import android.content.Context;
import android.widget.TextView;

public class GameCompletedDialog extends GameDialogType1 {

	public GameCompletedDialog(Context context, int gamePoints)
	{
		super(context);
		
		mGamePoints = gamePoints;
		
		setupViews();
		
		postSetupViews();
	}

	protected void setupViews() 
	{
		setContentView(R.layout.dialog_game_completed);
		
		TextView gameCompletedTextView = (TextView) findViewById(R.id.dialog_game_completed_text);
		String gameCompletedText = String.format(getContext().getResources().getString(R.string.dialog_game_completed_text), mGamePoints);
		gameCompletedTextView.setText(gameCompletedText);
	}

	@Override
	protected void setupRootView() 
	{
		mRootView = findViewById(R.id.game_dialog_root_layout);
	}
	
	int mGamePoints; //total points earned in this game

	@Override
	protected void setupPositiveAction1View() 
	{
		mPositiveAction1View = findViewById(R.id.share_button);
	}
}
