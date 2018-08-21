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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

public class CustomThemesDialog extends Activity implements OnClickListener
{
	private Button mTheme1Button, mTheme2Button, mTheme3Button, mTheme4Button, mTheme5Button, mTheme6Button;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
   
		setContentView(R.layout.activity_themes);
	
		mTheme1Button = (Button) findViewById(R.id.theme_1_button);
		mTheme1Button.setOnClickListener(this);
		mTheme2Button = (Button) findViewById(R.id.theme_2_button);
		mTheme2Button.setOnClickListener(this);
		mTheme3Button = (Button) findViewById(R.id.theme_3_button);
		mTheme3Button.setOnClickListener(this);
		mTheme4Button = (Button) findViewById(R.id.theme_4_button);
		mTheme4Button.setOnClickListener(this);
		mTheme5Button = (Button) findViewById(R.id.theme_5_button);
		mTheme5Button.setOnClickListener(this);
		mTheme6Button = (Button) findViewById(R.id.theme_6_button);
		mTheme6Button.setOnClickListener(this);
		
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);
		
		honorTheme();
	}
	
	private void honorTheme()
	{
		RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
    	mainLayout.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));	
    	
    	mTheme1Button.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_UI_BUTTONS));
    	mTheme2Button.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_UI_BUTTONS));
    	mTheme3Button.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_UI_BUTTONS));
    	mTheme4Button.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_UI_BUTTONS));
    	mTheme5Button.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_UI_BUTTONS));
    	mTheme6Button.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_UI_BUTTONS));
    	
    	TextView headingText = (TextView) findViewById(R.id.themes_title);
    	headingText.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_HEADINGS));
	}
	
	public void onClick(View v)
	{	
		if (v.getId() == R.id.back_button)
	    {
	    	this.finish();
	    	return;
	    }
		else if (v.getId() == R.id.theme_1_button)
		{  
			Themes.setTheme(Themes.THEME_ID_SUNSET_ORANGE);
		}
		else if (v.getId() == R.id.theme_2_button)
		{
			Themes.setTheme(Themes.THEME_ID_SKY_BLUE);
		}
		else if (v.getId() == R.id.theme_3_button)
		{
			Themes.setTheme(Themes.THEME_ID_FOREST_GREEN);
		}
		else if (v.getId() == R.id.theme_4_button)
		{
			Themes.setTheme(Themes.THEME_ID_ELECTRIC_PURPLE);
		}
		else if (v.getId() == R.id.theme_5_button)
		{
			Themes.setTheme(Themes.THEME_ID_COFFEE_TIME);
		}
		else if (v.getId() == R.id.theme_6_button)
		{
        	//Intent intent = new Intent(ThemesDialog.this, CustomThemesDialog.class);
        	//startActivity(intent);
		}
	    	
		Intent i = new Intent(this, ThemesActivity.class);
    	
    	this.finish();
    	startActivity(i);
	}
}