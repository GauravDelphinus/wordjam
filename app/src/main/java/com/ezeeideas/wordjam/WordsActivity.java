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
import android.widget.TextView;

public class WordsActivity extends Activity implements OnClickListener
{		
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		setContentView(R.layout.activity_words);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(this);
        
        mSearchButton = (Button) findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(this);
        
        mBrowseButton = (Button) findViewById(R.id.browse_button);
        mBrowseButton.setOnClickListener(this);
        
        honorTheme();
	}
	
	private void honorTheme()
    {
    	RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
    	mainLayout.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    }
	
    public void onClick(View v) {
        
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else if (v.getId() == R.id.search_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_SEARCH_WORDS_BUTTON, null);
    		
    		Intent intent = new Intent(this, SearchWords.class);
	    	startActivity(intent);
    	}
    	else if (v.getId() == R.id.browse_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_BROWSE_WORDS_BUTTON, null);
    		
    		Intent intent = new Intent(this, WordTypeList.class);
        	startActivity(intent);
    	}
    }
    
    private Button mSearchButton, mBrowseButton;
}