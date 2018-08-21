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

import com.ezeeideas.wordjam.ColorPickerDialog.OnColorChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

public class ThemesActivity extends Activity implements OnClickListener
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
			updateThemeAndExit(Themes.THEME_ID_SUNSET_ORANGE);
		}
		else if (v.getId() == R.id.theme_2_button)
		{
			updateThemeAndExit(Themes.THEME_ID_SKY_BLUE);
		}
		else if (v.getId() == R.id.theme_3_button)
		{
			updateThemeAndExit(Themes.THEME_ID_FOREST_GREEN);
		}
		else if (v.getId() == R.id.theme_4_button)
		{
			updateThemeAndExit(Themes.THEME_ID_ELECTRIC_PURPLE);
		}
		else if (v.getId() == R.id.theme_5_button)
		{
			updateThemeAndExit(Themes.THEME_ID_COFFEE_TIME);
		}
		else if (v.getId() == R.id.theme_6_button)
		{
			//updateThemeAndExit(Themes.THEME_ID_CUSTOM_COLOR);
			
        	//Intent intent = new Intent(ThemesDialog.this, CustomThemesDialog.class);
        	//startActivity(intent);
		
			Dialog d = new ColorPickerDialog(this, 
						new ColorPickerDialog.OnColorChangedListener() {
							
							public void colorChanged(int color) {
								// TODO Auto-generated method stub
								Themes.setCustomThemeColor(color);
								updateThemeAndExit(Themes.THEME_ID_CUSTOM_COLOR);
							}
						}, 0);
					 
	        d.show();
	        
		}
	    	

	}
	
	// Set the current theme to what user has chosen
	public void updateThemeAndExit(int theme)
	{	      
		Themes.setTheme(theme);
		
		// return the value to the calling activity
		setResult(theme);
		this.finish();
	}
	
	private void updateTheme()
	{
		Intent i = new Intent(this, ThemesActivity.class);
    	
    	this.finish();
    	startActivity(i);
	}
}