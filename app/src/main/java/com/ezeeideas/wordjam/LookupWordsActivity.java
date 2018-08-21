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

import java.util.Vector;

import android.app.Activity;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;

public class LookupWordsActivity extends Activity implements OnClickListener, PieButton.PieButtonListener
{	
	private PieButton mThreeWayButton;
	private LinearLayout mSearchLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		setContentView(R.layout.words);
		
        // Set up click listeners for all the buttons
        mThreeWayButton = (PieButton) findViewById(R.id.lookup_choices_view);
        Vector<Integer> buttonPressedResources = new Vector<Integer>();
        buttonPressedResources.add(R.drawable.lookup_options_button_review);
        buttonPressedResources.add(R.drawable.lookup_options_button_browse);
        buttonPressedResources.add(R.drawable.lookup_options_button_search);
        mThreeWayButton.setAttributes(30, R.drawable.lookup_options_button_normal, buttonPressedResources);
  
        /*
        mSearchWordsTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchWordsTextView.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
            
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        handleSearch();
                        return true;
                    }
                    return false;
                }

                handleSearch();
                return true;
            }
        });
        */
          
        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(this);
        
        honorTheme();
	}
	
	private void honorTheme()
    {
    	RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
    	mainLayout.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    	
    	TextView headingText = (TextView) findViewById(R.id.words_title);
    	headingText.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_HEADINGS));
    }
	
    public void onClick(View v) {
        
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }
    
	public void onPieButtonSelected(PieButton button, int index) 
	{
		if (button == mThreeWayButton)
		{
			switch (index)
			{
			case 0:
				//Review
		        {

		        }
		        break;
			case 1:
				//Browse
		        {
		        	Intent intent = new Intent(this, WordTypeList.class);
		        	startActivity(intent);
		        }
		        break;
			case 2:
				//Search
			    {
			    	Intent intent = new Intent(this, SearchWords.class);
			    	startActivity(intent);
		        }
		        break;
			}
		}
	} 
    
	/*
    private void handleSearch()
    {
		String keyword = mSearchWordsTextView.getText().toString();
    	Intent intent = new Intent(this, SearchWords.class);
    	intent.putExtra(SearchWords.KEY_WORD_KEYWORD, keyword);
    	
    	startActivity(intent);
    }
    */
}