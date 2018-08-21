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

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.Window;

public class TestDialog extends Dialog implements OnTouchListener, OnDismissListener
{
	public interface TestDialogListener
	{
		public void onDialogOptionSelected(TestDialog dialog, int option);
	}
	
	private TestDialogListener mListener;
	
	public static final int TEST_RESULT_PASS = 1;
	public static final int TEST_RESULT_FAIL = 2;
	public static final int TEST_RESULT_SUMMARY = 3;
	
	private int mType; // type of test (pass, fail, summary)
	private Context mContext;
	private String mAnswer; // in case we want to display the correct answer
	
	private int mNumTests, mNumTestsPassed, mNumHintsUsed; // used for test summary
	
	public TestDialog(Context context, int type, String answer)
	{
		super(context);
		mContext = context;
		
		mType = type;
		mAnswer = answer;
		
		mListener = (TestDialogListener)mContext;
		
		//don't show a title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//initialize the view based on the type
		refreshData();
	}

	public void setTestData(int numTests, int numTestsPassed, int numHintsUsed)
	{
		//Log.d(null, "################## setting test data, total tests = " + numTests);
		mNumTests = numTests;
		mNumTestsPassed = numTestsPassed;
		mNumHintsUsed = numHintsUsed;
		
		refreshData();
	}
	
	private void refreshData()
	{
		if (mType == TEST_RESULT_PASS)
		{
			setContentView(R.layout.test_pass);
		}
		else if (mType == TEST_RESULT_FAIL)
		{
			setContentView(R.layout.test_fail);
			
			TextView title = (TextView) findViewById(R.id.test_fail_title);
			TextView text = (TextView) findViewById(R.id.test_fail_text);
			
			if (mAnswer.length() > 0)
			{
				title.setText(mContext.getResources().getString(R.string.test_fail_title_with_answer));
				
				String text_str = String.format(mContext.getResources().getString(R.string.test_fail_text_with_answer), mAnswer);
				text.setText(text_str);
			}
			else
			{
				title.setText(mContext.getResources().getString(R.string.test_fail_title_without_answer));
				text.setText(mContext.getResources().getString(R.string.test_fail_text_without_answer));
			}
			
			
			LinearLayout layout = (LinearLayout) findViewById(R.id.layout_root);
			layout.setOnTouchListener(this);
			
			ImageView imageView = (ImageView)findViewById(R.id.test_fail_image);
			imageView.setOnTouchListener(new View.OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					Utils.Log("onTouch called for layout");
					return true;
				}
			});
			
			/*
			imageView.setOnTouchListener(new View.OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					Utils.Log("onTouch2 called!");
					return true;
				}
			});
			*/
			
		}
		else if (mType == TEST_RESULT_SUMMARY)
		{
			setContentView(R.layout.test_result);
			
			TextView total_tests = (TextView) findViewById(R.id.test_result_total_tests);
			total_tests.setText(Integer.toString(mNumTests));
			
			TextView passed_tests = (TextView) findViewById(R.id.test_result_passed_tests);
			passed_tests.setText(Integer.toString(mNumTestsPassed));
			
			TextView hints_used = (TextView) findViewById(R.id.test_result_hints_used);
			hints_used.setText(Integer.toString(mNumHintsUsed));
		}
	}
	
	public boolean onTouch(View v, MotionEvent event) 
	{		
		Utils.Log("onTouch1 called!, event =" + event.getAction());
		mListener.onDialogOptionSelected(this, Constants.ACTION_GO_TO_LEVEL_HOME);
		return false;
	}
	
	/*
	@Override
	public boolean dispatchTouchEvent(MotionEvent me)
	{
		Utils.Log("dispatchTouchEvent called!"); 
		//this.dismiss();
		return false;
	}
	*/

	public void onDismiss(DialogInterface dialog) {
		Utils.Log("onDismiss called!");
		
	}
}