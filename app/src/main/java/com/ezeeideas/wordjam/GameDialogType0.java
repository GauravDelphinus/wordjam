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
import android.view.View;

/**
 * GameDialog Type 0
 * 
 * - No explicit buttons.
 * - Tap anywhere dismisses the dialog and executes positive action 1
 * - Hardware back dismisses the dialog and executes positive action 1
 * - Examples: Pause/Resume Game Dialog
 */
public abstract class GameDialogType0 extends GameDialog
{
	public GameDialogType0(Context context)
	{
		super(context);
	}

	protected void setupRequiredViews()
	{
		Utils.Log("setupRequiredViews");
		setupRootView(); //no buttons in this view
	}

	protected void handleTouch(View v)
	{		
		if (v == mRootView)
		{
			mListener.onDialogOptionSelected(this, GAME_DIALOG_ACTION_POSITIVE_1);
			this.dismiss();
		}
	}

	protected void handleDismiss()
	{
		mListener.onDialogOptionSelected(this, GAME_DIALOG_ACTION_POSITIVE_1);	
		this.dismiss();
	}
	
	/**
	 * Clients must implement these functions
	 */
	protected abstract void setupRootView();
}
