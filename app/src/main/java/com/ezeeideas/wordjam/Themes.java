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

import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

public class Themes
{
	// ############# list of theme IDs.  Add to this list if you want to add new themes
	public static final int THEME_ID_SUNSET_ORANGE = 1;
	public static final int THEME_ID_SKY_BLUE = 2;
	public static final int THEME_ID_FOREST_GREEN = 3;
	public static final int THEME_ID_ELECTRIC_PURPLE = 4;
	public static final int THEME_ID_COFFEE_TIME = 5;
	public static final int THEME_ID_CUSTOM_COLOR = 6;
	
	private static Vector<Theme> mThemes;
	private static int mCurrentThemeID = THEME_ID_SUNSET_ORANGE; // default theme that we ship with
	private static Theme mCurrentTheme;
	private static Context mContext;
	
	private static int mScreenSize; // one of small, medium, large and x-large
	private static TextSizes mTextSizes;
	private static final float MIN_TEXT_SIZE = 15f; // minimum text size of any control, in scaled pixels
	// Initialize the theme static object
	public static void initialize(Context context)
	{	
		Log.d("gaurav", "Themes.initialize");
		updateContext(context);
		
		// ########### initialize themes.  Add to this list for new themes
		mThemes = new Vector<Theme>();
		mThemes.add(new Theme(THEME_ID_SUNSET_ORANGE, "Sunset Orange"));
		mThemes.add(new Theme(THEME_ID_SKY_BLUE, "Sky Blue"));
		mThemes.add(new Theme(THEME_ID_FOREST_GREEN, "Forest Green"));
		mThemes.add(new Theme(THEME_ID_ELECTRIC_PURPLE, "Electric Purple"));
		mThemes.add(new Theme(THEME_ID_COFFEE_TIME, "Coffee Time"));
		mThemes.add(new Theme(THEME_ID_CUSTOM_COLOR, "Custom Color"));
			
		mThemes.size();
		
		mCurrentThemeID = getTheme();
		updateCurrentTheme();
		
		// now check out the text sizes
		mTextSizes = new TextSizes(getScreenSize());
	}
	
	public static String getThemeNameFromID(int themeid)
	{
		switch (themeid)
		{
		case THEME_ID_SUNSET_ORANGE: return mContext.getResources().getString(R.string.theme_1_label);
		case THEME_ID_SKY_BLUE: return mContext.getResources().getString(R.string.theme_2_label);
		case THEME_ID_FOREST_GREEN: return mContext.getResources().getString(R.string.theme_3_label);
		case THEME_ID_ELECTRIC_PURPLE: return mContext.getResources().getString(R.string.theme_4_label);
		case THEME_ID_COFFEE_TIME: return mContext.getResources().getString(R.string.theme_5_label);
		case THEME_ID_CUSTOM_COLOR: return mContext.getResources().getString(R.string.theme_6_label);
		default:break;
		}
		
		return null;
	}
	
	public static void updateContext(Context context)
	{
		mContext = context;
	}
	
	public static int getScreenSize()
	{
		Utils.Log("getScreenSize");
		mScreenSize = SCREEN_SIZE_MEDIUM;
		
		 if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) 
		 {
			 mScreenSize = SCREEN_SIZE_LARGE;
			 Utils.Log("############ Screen is SCREEN_SIZE_LARGE");
		 }
		 else if ((mContext.getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL)
		 {
			 mScreenSize = SCREEN_SIZE_MEDIUM;
			 Utils.Log("############ Screen is SCREEN_SIZE_MEDIUM");
		 } 
		 else if ((mContext.getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
		 {
			 mScreenSize = SCREEN_SIZE_SMALL;
			 Utils.Log("############ Screen is SCREEN_SIZE_SMALL");
		 }
		 else
		 {
		     mScreenSize = SCREEN_SIZE_XLARGE;
		     Utils.Log("############ Screen is SCREEN_SIZE_XLARGE");
		 }
		 
		 return mScreenSize;
	}
	
	private static void updateCurrentTheme()
	{
		int i;
	    for (i = 0; i < mThemes.size(); i++)
	    {
		    if (mThemes.elementAt(i).mThemeID == mCurrentThemeID)
		    {
		    	mCurrentTheme = mThemes.elementAt(i);
		    	if (mCurrentThemeID == THEME_ID_CUSTOM_COLOR)
		    	{
		    		mCurrentTheme.setCustomThemeColor(getCustomThemeColor());
		    	}
		    	break;
		    }
	    }
	    
	  //workaround to a bug where getColorFor crashes, probably because mCurrentTheme wasn't yet initialized
	  //we should probably hold the Themes out of the Context of any activity, and do it an App level.
	  if (mCurrentTheme == null)
	  {
	  	mCurrentTheme = new Theme(THEME_ID_SUNSET_ORANGE, "Sunset Orange");
	  }
	}
	
	// Set the current theme to what user has chosen
	public static void setTheme(int themeID)
	{
	      // We need an Editor object to make preference changes.
	      // All objects are from android.context.Context
	      SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
	      
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putInt(Constants.PREF_THEME, themeID);

	      // Commit the edits!
	      editor.commit();	
	      
	      // set the current theme
	      mCurrentThemeID = themeID;
	      updateCurrentTheme();
	}
	
	public static void setCustomThemeColor(int color)
	{
		SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
	      
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(Constants.PREF_THEME_COLOR, color);

	    // Commit the edits!
	    editor.commit();
	    
	    setTheme(THEME_ID_CUSTOM_COLOR);
	}
	
	public static int getTheme()
	{ 
		
		// Restore preferences
	    SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
	    int themeID = settings.getInt(Constants.PREF_THEME, THEME_ID_COFFEE_TIME);
	     
	    return themeID;
	}
	
	public static int getCustomThemeColor()
	{ 
		
		// Restore preferences
	    SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
	    int color = settings.getInt(Constants.PREF_THEME_COLOR, 0);
	     
	    return color;
	}
	
	public static int getColorFor(int item)
	{
		//workaround to a bug where getColorFor crashes, probably because mCurrentTheme wasn't yet initialized
		//we should probably hold the Themes out of the Context of any activity, and do it an App level.
		if (mCurrentTheme == null)
		{
			mCurrentTheme = new Theme(THEME_ID_SUNSET_ORANGE, "Sunset Orange");
		}
		  
		return mCurrentTheme.getColor(item);
	}
	
	// Various colors.  MUST KEEP THEM IN INCREASING ORDER, starting from 0.  COLOR_COUNT must be the last entry.
	public static final int COLOR_BACKGROUND = 0;
	public static final int COLOR_TEXT = 1;
	public static final int COLOR_LINE_HEADING = 2;
	public static final int COLOR_MATCH_WORDS_BUTTON = 3;
	public static final int COLOR_MATCH_WORDS_LINE = 4;
	public static final int COLOR_HANGMAN_DASHES = 5;
	public static final int COLOR_HANGMAN_LETTERS = 6;
	public static final int COLOR_HANGMAN_POD = 7;
	public static final int COLOR_HANGMAN_MAN = 8;
	public static final int COLOR_CROSSWORD_HIT_CELL = 9;
	public static final int COLOR_CROSSWORD_LETTER = 10;
	public static final int COLOR_WORDSEARCH_LETTER = 11;
	public static final int COLOR_WORDSEARCH_CELL_BACKGROUND = 12;
	public static final int COLOR_WORDSEARCH_BOARD_BACKGROUND = 13;
	public static final int COLOR_WORDSEARCH_LOOP = 14;
	public static final int COLOR_WORDSEARCH_HOVER_LETTER = 15;
	public static final int COLOR_WORDSEARCH_LOOP_FILL = 16;
	public static final int COLOR_WORDSEARCH_OVERLAY_BACKGROUND = 17;
	public static final int COLOR_WORDSEARCH_OVERLAY_TEXT = 18;
	public static final int COLOR_MATCH_WORDS_FOUND_BUTTON = 19;
	public static final int COLOR_COUNT = 20;
	
	public static class Theme
	{
		public int mThemeID;
		public String mThemeName;
		
		private int[] mColors;
		
		public Theme(int themeid, String themename)
		{
			mThemeID = themeid;
			mThemeName = themename;
			
			mColors = new int[COLOR_COUNT];
			switch (mThemeID)
			{
				// set the various theme specific properties here
				case THEME_ID_SUNSET_ORANGE:
				{
					// Set colors for this theme.  Must be equal to COLOR_COUNT
					mColors[COLOR_BACKGROUND] = lookupColor(R.color.background_1);
					mColors[COLOR_TEXT] = lookupColor(R.color.text_1);
					mColors[COLOR_LINE_HEADING] = lookupColor(R.color.line_heading_1);
					mColors[COLOR_MATCH_WORDS_BUTTON] = lookupColor(R.color.match_words_button_1);
					mColors[COLOR_MATCH_WORDS_FOUND_BUTTON] = lookupColor(R.color.match_words_found_button_1);
					mColors[COLOR_MATCH_WORDS_LINE] = lookupColor(R.color.match_words_line_1);
					mColors[COLOR_HANGMAN_DASHES] = lookupColor(R.color.hangman_dashes_1);
					mColors[COLOR_HANGMAN_LETTERS] = lookupColor(R.color.hangman_letters_1);
					mColors[COLOR_HANGMAN_POD] = lookupColor(R.color.hangman_pod_1);
					mColors[COLOR_HANGMAN_MAN] = lookupColor(R.color.hangman_1);
					mColors[COLOR_CROSSWORD_HIT_CELL] = lookupColor(R.color.crossword_hit_cell_1);
					mColors[COLOR_CROSSWORD_LETTER] = lookupColor(R.color.crossword_letter_1);
					mColors[COLOR_WORDSEARCH_LETTER] = Color.GRAY;
					mColors[COLOR_WORDSEARCH_CELL_BACKGROUND] = Color.WHITE;
					mColors[COLOR_WORDSEARCH_BOARD_BACKGROUND] = lookupColor(R.color.gray_translucent1);
					mColors[COLOR_WORDSEARCH_LOOP] = lookupColor(R.color.wordsearch_loop_border_1);
					mColors[COLOR_WORDSEARCH_HOVER_LETTER] = Color.BLACK;
					mColors[COLOR_WORDSEARCH_LOOP_FILL] = lookupColor(R.color.wordsearch_loop_fill_1);
					mColors[COLOR_WORDSEARCH_OVERLAY_BACKGROUND] = lookupColor(R.color.wordsearch_overlay_bg_1);
					mColors[COLOR_WORDSEARCH_OVERLAY_TEXT] = lookupColor(R.color.wordsearch_overlay_text_1);
				}
				break;
				
				case THEME_ID_SKY_BLUE:
				{
					// Set colors for this theme.  Must be equal to COLOR_COUNT
					mColors[COLOR_BACKGROUND] = lookupColor(R.color.background_2);
					mColors[COLOR_TEXT] = lookupColor(R.color.text_2);
					mColors[COLOR_LINE_HEADING] = lookupColor(R.color.line_heading_2);
					mColors[COLOR_MATCH_WORDS_BUTTON] = lookupColor(R.color.match_words_button_2);
					mColors[COLOR_MATCH_WORDS_FOUND_BUTTON] = lookupColor(R.color.match_words_found_button_2);
					mColors[COLOR_MATCH_WORDS_LINE] = lookupColor(R.color.match_words_line_2);
					
					mColors[COLOR_HANGMAN_DASHES] = lookupColor(R.color.hangman_dashes_2);
					mColors[COLOR_HANGMAN_LETTERS] = lookupColor(R.color.hangman_letters_2);
					mColors[COLOR_HANGMAN_POD] = lookupColor(R.color.hangman_pod_2);
					mColors[COLOR_HANGMAN_MAN] = lookupColor(R.color.hangman_2);
					mColors[COLOR_CROSSWORD_HIT_CELL] = lookupColor(R.color.crossword_hit_cell_2);
					mColors[COLOR_CROSSWORD_LETTER] = lookupColor(R.color.crossword_letter_2);
					mColors[COLOR_WORDSEARCH_LETTER] = Color.GRAY;
					mColors[COLOR_WORDSEARCH_CELL_BACKGROUND] = Color.WHITE;
					mColors[COLOR_WORDSEARCH_BOARD_BACKGROUND] = lookupColor(R.color.gray_translucent1);
					mColors[COLOR_WORDSEARCH_LOOP] = lookupColor(R.color.wordsearch_loop_border_2);
					mColors[COLOR_WORDSEARCH_HOVER_LETTER] = Color.BLACK;
					mColors[COLOR_WORDSEARCH_LOOP_FILL] = lookupColor(R.color.wordsearch_loop_fill_2);
					mColors[COLOR_WORDSEARCH_OVERLAY_BACKGROUND] = lookupColor(R.color.wordsearch_overlay_bg_2);
					mColors[COLOR_WORDSEARCH_OVERLAY_TEXT] = lookupColor(R.color.wordsearch_overlay_text_2);
				}
				break;
				
				case THEME_ID_FOREST_GREEN:
				{
					// Set colors for this theme.  Must be equal to COLOR_COUNT
					mColors[COLOR_BACKGROUND] = lookupColor(R.color.background_3);
					mColors[COLOR_TEXT] = lookupColor(R.color.text_3);
					mColors[COLOR_LINE_HEADING] = lookupColor(R.color.line_heading_3);
					mColors[COLOR_MATCH_WORDS_BUTTON] = lookupColor(R.color.match_words_button_3);
					mColors[COLOR_MATCH_WORDS_FOUND_BUTTON] = lookupColor(R.color.match_words_found_button_3);
					mColors[COLOR_MATCH_WORDS_LINE] = lookupColor(R.color.match_words_line_3);
					
					mColors[COLOR_HANGMAN_DASHES] = lookupColor(R.color.hangman_dashes_3);
					mColors[COLOR_HANGMAN_LETTERS] = lookupColor(R.color.hangman_letters_3);
					mColors[COLOR_HANGMAN_POD] = lookupColor(R.color.hangman_pod_3);
					mColors[COLOR_HANGMAN_MAN] = lookupColor(R.color.hangman_3);
					mColors[COLOR_CROSSWORD_HIT_CELL] = lookupColor(R.color.crossword_hit_cell_3);
					mColors[COLOR_CROSSWORD_LETTER] = lookupColor(R.color.crossword_letter_3);
					mColors[COLOR_WORDSEARCH_LETTER] = Color.GRAY;
					mColors[COLOR_WORDSEARCH_CELL_BACKGROUND] = Color.WHITE;
					mColors[COLOR_WORDSEARCH_BOARD_BACKGROUND] = lookupColor(R.color.gray_translucent1);
					mColors[COLOR_WORDSEARCH_LOOP] = lookupColor(R.color.wordsearch_loop_border_3);
					mColors[COLOR_WORDSEARCH_HOVER_LETTER] = Color.BLACK;
					mColors[COLOR_WORDSEARCH_LOOP_FILL] = lookupColor(R.color.wordsearch_loop_fill_3);
					mColors[COLOR_WORDSEARCH_OVERLAY_BACKGROUND] = lookupColor(R.color.wordsearch_overlay_bg_3);
					mColors[COLOR_WORDSEARCH_OVERLAY_TEXT] = lookupColor(R.color.wordsearch_overlay_text_3);
				}
				break;
				
				case THEME_ID_ELECTRIC_PURPLE:
				{
					// Set colors for this theme.  Must be equal to COLOR_COUNT
					mColors[COLOR_BACKGROUND] = lookupColor(R.color.background_4);
					mColors[COLOR_TEXT] = lookupColor(R.color.text_4);
					mColors[COLOR_LINE_HEADING] = lookupColor(R.color.line_heading_4);
					mColors[COLOR_MATCH_WORDS_BUTTON] = lookupColor(R.color.match_words_button_4);
					mColors[COLOR_MATCH_WORDS_FOUND_BUTTON] = lookupColor(R.color.match_words_found_button_4);
					mColors[COLOR_MATCH_WORDS_LINE] = lookupColor(R.color.match_words_line_4);
					
					mColors[COLOR_HANGMAN_DASHES] = lookupColor(R.color.hangman_dashes_4);
					mColors[COLOR_HANGMAN_LETTERS] = lookupColor(R.color.hangman_letters_4);
					mColors[COLOR_HANGMAN_POD] = lookupColor(R.color.hangman_pod_4);
					mColors[COLOR_HANGMAN_MAN] = lookupColor(R.color.hangman_4);
					mColors[COLOR_CROSSWORD_HIT_CELL] = lookupColor(R.color.crossword_hit_cell_4);
					mColors[COLOR_CROSSWORD_LETTER] = lookupColor(R.color.crossword_letter_4);
					mColors[COLOR_WORDSEARCH_LETTER] = Color.GRAY;
					mColors[COLOR_WORDSEARCH_CELL_BACKGROUND] = Color.WHITE;
					mColors[COLOR_WORDSEARCH_BOARD_BACKGROUND] = lookupColor(R.color.gray_translucent1);
					mColors[COLOR_WORDSEARCH_LOOP] = lookupColor(R.color.wordsearch_loop_border_4);
					mColors[COLOR_WORDSEARCH_HOVER_LETTER] = Color.BLACK;
					mColors[COLOR_WORDSEARCH_LOOP_FILL] = lookupColor(R.color.wordsearch_loop_fill_4);
					mColors[COLOR_WORDSEARCH_OVERLAY_BACKGROUND] = lookupColor(R.color.wordsearch_overlay_bg_4);
					mColors[COLOR_WORDSEARCH_OVERLAY_TEXT] = lookupColor(R.color.wordsearch_overlay_text_4);
				}
				break;
				
				case THEME_ID_COFFEE_TIME:
				{
					// Set colors for this theme.  Must be equal to COLOR_COUNT
					mColors[COLOR_BACKGROUND] = lookupColor(R.color.background_5);
					mColors[COLOR_TEXT] = lookupColor(R.color.text_5);
					mColors[COLOR_LINE_HEADING] = lookupColor(R.color.line_heading_5);
					mColors[COLOR_MATCH_WORDS_BUTTON] = lookupColor(R.color.match_words_button_5);
					mColors[COLOR_MATCH_WORDS_FOUND_BUTTON] = lookupColor(R.color.match_words_found_button_5);
					mColors[COLOR_MATCH_WORDS_LINE] = lookupColor(R.color.match_words_line_5);
					
					mColors[COLOR_HANGMAN_DASHES] = lookupColor(R.color.hangman_dashes_5);
					mColors[COLOR_HANGMAN_LETTERS] = lookupColor(R.color.hangman_letters_5);
					mColors[COLOR_HANGMAN_POD] = lookupColor(R.color.hangman_pod_5);
					mColors[COLOR_HANGMAN_MAN] = lookupColor(R.color.hangman_5);
					mColors[COLOR_CROSSWORD_HIT_CELL] = lookupColor(R.color.crossword_hit_cell_5);
					mColors[COLOR_CROSSWORD_LETTER] = lookupColor(R.color.crossword_letter_5);
					mColors[COLOR_WORDSEARCH_LETTER] = Color.GRAY;
					mColors[COLOR_WORDSEARCH_CELL_BACKGROUND] = Color.WHITE;
					mColors[COLOR_WORDSEARCH_BOARD_BACKGROUND] = lookupColor(R.color.gray_translucent1);
					mColors[COLOR_WORDSEARCH_LOOP] = lookupColor(R.color.wordsearch_loop_border_5);
					mColors[COLOR_WORDSEARCH_HOVER_LETTER] = Color.BLACK;
					mColors[COLOR_WORDSEARCH_LOOP_FILL] = lookupColor(R.color.wordsearch_loop_fill_5);
					mColors[COLOR_WORDSEARCH_OVERLAY_BACKGROUND] = lookupColor(R.color.wordsearch_overlay_bg_5);
					mColors[COLOR_WORDSEARCH_OVERLAY_TEXT] = lookupColor(R.color.wordsearch_overlay_text_5);
				}
				break;
				
			
			}
		}
		
		private int getColor(int id)
		{
			return mColors[id];
		}
		
		public void setCustomThemeColor(int color)
		{
			float f1 = 0.7F;
			float f2 = 1.2F;
			int darkShadeR = Math.max(0, (int)(Color.red(color) * f1));
			int darkShadeG = Math.max(0, (int)(Color.green(color) * f1));
			int darkShadeB = Math.max(0, (int)(Color.blue(color) * f1));
			int lightShadeR = Math.min(255, (int)(Color.red(color) * f2));
			int lightShadeG = Math.min(255, (int)(Color.green(color) * f2));
			int lightShadeB = Math.min(255, (int)(Color.blue(color) * f2));
			int lightShade = Color.rgb(lightShadeR, lightShadeG, lightShadeB);
			int darkShade = Color.rgb(darkShadeR, darkShadeG, darkShadeB);
			
			// Set colors for this theme.  Must be equal to COLOR_COUNT
			mColors[COLOR_BACKGROUND] = color;
			mColors[COLOR_TEXT] = color;
			mColors[COLOR_LINE_HEADING] = darkShade;
			mColors[COLOR_MATCH_WORDS_BUTTON] = lightShade;
			mColors[COLOR_MATCH_WORDS_FOUND_BUTTON] = darkShade;
			mColors[COLOR_MATCH_WORDS_LINE] = darkShade;
			
			mColors[COLOR_HANGMAN_DASHES] = Color.BLACK;
			mColors[COLOR_HANGMAN_LETTERS] = darkShade;
			mColors[COLOR_HANGMAN_POD] = darkShade;
			mColors[COLOR_HANGMAN_MAN] = darkShade;
			mColors[COLOR_CROSSWORD_HIT_CELL] = darkShade;
			mColors[COLOR_CROSSWORD_LETTER] = darkShade;
			mColors[COLOR_WORDSEARCH_LETTER] = Color.GRAY;
			mColors[COLOR_WORDSEARCH_CELL_BACKGROUND] = Color.WHITE;
			mColors[COLOR_WORDSEARCH_BOARD_BACKGROUND] = color;
			mColors[COLOR_WORDSEARCH_LOOP] = Color.argb(22, Color.red(darkShade), Color.green(darkShade), Color.blue(darkShade));
			mColors[COLOR_WORDSEARCH_HOVER_LETTER] = Color.BLACK;
			mColors[COLOR_WORDSEARCH_LOOP_FILL] = Color.argb(55, Color.red(darkShade), Color.green(darkShade), Color.blue(darkShade));
			mColors[COLOR_WORDSEARCH_OVERLAY_BACKGROUND] = Color.GRAY;
			mColors[COLOR_WORDSEARCH_OVERLAY_TEXT] = Color.WHITE;
		}
	}
	
	private static int lookupColor(int resourceID)
	{
		return mContext.getResources().getColor(resourceID);
	}
	
	// ######## text sizes based on screen sizes
	public static final int SCREEN_SIZE_SMALL = 1;
	public static final int SCREEN_SIZE_MEDIUM = 2;
	public static final int SCREEN_SIZE_LARGE = 3;
	public static final int SCREEN_SIZE_XLARGE = 4;
	
	public static final int TEXT_SIZE_UI_BUTTONS = 0;
	public static final int TEXT_SIZE_CHOICE_BUTTONS = 1;
	public static final int TEXT_SIZE_HEADINGS = 2;
	public static final int TEXT_SIZE_GIVEN_TEXT = 3;
	public static final int TEXT_SIZE_CROSSWORD_HINTS = 4;
	public static final int TEXT_SIZE_CROSSWORD_HINT_HEADING = 5;
	public static final int TEXT_SIZE_DEFAULT_TEXT = 6;
	public static final int TEXT_SIZE_WORDSEARCH_LIST_ITEM = 7;
	public static final int TEXT_SIZE_WORDSEARCH_LIST_HEADING = 8;
	public static final int TEXT_SIZE_COUNT = 9;
	
	public static float getTextSizeFor(int item)
	{
		return Math.max(mTextSizes.getTextSize(item), MIN_TEXT_SIZE);
	}
	
	//returns in scaled pixels
	public static float getMinTextSize()
	{
		return MIN_TEXT_SIZE;
	}
	
	public static class TextSizes
	{	
		private int[] mTextSizes;
		
		public TextSizes(int screenSize)
		{
			mTextSizes = new int[TEXT_SIZE_COUNT];
			switch (screenSize)
			{
				// set the various theme specific properties here.  All font size units are in 'sp' units (scaled pixels)
				case SCREEN_SIZE_SMALL:
				{
					mTextSizes[TEXT_SIZE_UI_BUTTONS] = 10;
					mTextSizes[TEXT_SIZE_CHOICE_BUTTONS] = 8; // 0.8 times UI button text size
					mTextSizes[TEXT_SIZE_HEADINGS] = 15; // 1.5 times UI button text size
					mTextSizes[TEXT_SIZE_GIVEN_TEXT] = 12; // 1.2 times UI button text size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINTS] = 7; // 2/3rd of ui button size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINT_HEADING] = 10; // same as ui button size
					mTextSizes[TEXT_SIZE_DEFAULT_TEXT] = 10; // same as ui button text size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_HEADING] = 10; // same as ui button size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_ITEM] = 7;
				}
				break;
				
				case SCREEN_SIZE_MEDIUM:
				{
					/*
					mTextSizes[TEXT_SIZE_UI_BUTTONS] = 15;
					mTextSizes[TEXT_SIZE_CHOICE_BUTTONS] = 12;
					mTextSizes[TEXT_SIZE_HEADINGS] = 24;
					mTextSizes[TEXT_SIZE_GIVEN_TEXT] = 18; // 1.2 times UI button text size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINTS] = 10; // 2/3rd of ui button size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINT_HEADING] = 15; // same as ui button size
					mTextSizes[TEXT_SIZE_DEFAULT_TEXT] = 15; // same as ui button text size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_HEADING] = 15; // same as ui button size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_ITEM] = 10;
					
					*/
					mTextSizes[TEXT_SIZE_UI_BUTTONS] = 18;
					mTextSizes[TEXT_SIZE_CHOICE_BUTTONS] = (int)(0.8 * mTextSizes[TEXT_SIZE_UI_BUTTONS]);
					mTextSizes[TEXT_SIZE_HEADINGS] = (int)(1.5 * mTextSizes[TEXT_SIZE_UI_BUTTONS]);
					mTextSizes[TEXT_SIZE_GIVEN_TEXT] = (int)(1.2 * mTextSizes[TEXT_SIZE_UI_BUTTONS]);
					mTextSizes[TEXT_SIZE_CROSSWORD_HINTS] = (int)(0.67 * mTextSizes[TEXT_SIZE_UI_BUTTONS]);
					mTextSizes[TEXT_SIZE_CROSSWORD_HINT_HEADING] = mTextSizes[TEXT_SIZE_UI_BUTTONS];
					mTextSizes[TEXT_SIZE_DEFAULT_TEXT] = mTextSizes[TEXT_SIZE_UI_BUTTONS];
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_HEADING] = mTextSizes[TEXT_SIZE_UI_BUTTONS];
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_ITEM] = (int)(0.67 * mTextSizes[TEXT_SIZE_UI_BUTTONS]);
				}
				break;	
				
				case SCREEN_SIZE_LARGE:
				{
					mTextSizes[TEXT_SIZE_UI_BUTTONS] = 20;
					mTextSizes[TEXT_SIZE_CHOICE_BUTTONS] = 16;
					mTextSizes[TEXT_SIZE_HEADINGS] = 30;
					mTextSizes[TEXT_SIZE_GIVEN_TEXT] = 24; // 1.2 times UI button text size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINTS] = 13; // 2/3rd of ui button size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINT_HEADING] = 20; // same as ui button size
					mTextSizes[TEXT_SIZE_DEFAULT_TEXT] = 20; // same as ui button text size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_HEADING] = 20; // same as ui button size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_ITEM] = 13;
				}
				break;	
				
				case SCREEN_SIZE_XLARGE:
				{
					mTextSizes[TEXT_SIZE_UI_BUTTONS] = 30;
					mTextSizes[TEXT_SIZE_CHOICE_BUTTONS] = 24;
					mTextSizes[TEXT_SIZE_HEADINGS] = 45;
					mTextSizes[TEXT_SIZE_GIVEN_TEXT] = 36; // 1.2 times UI button text size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINTS] = 20; // 2/3rd of ui button size
					mTextSizes[TEXT_SIZE_CROSSWORD_HINT_HEADING] = 30; // same as ui button size
					mTextSizes[TEXT_SIZE_DEFAULT_TEXT] = 30; // same as ui button text size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_HEADING] = 30; // same as ui button size
					mTextSizes[TEXT_SIZE_WORDSEARCH_LIST_ITEM] = 20;
				}
				break;	
			}
		}
		
		private int getTextSize(int textType)
		{
			return mTextSizes[textType];
		}
	}
}