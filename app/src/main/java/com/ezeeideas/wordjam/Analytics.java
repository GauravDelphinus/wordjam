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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class Analytics
{
	public static void trackEvent(Context context, String category, String action, String label, Integer value)
	{
		EasyTracker easyTracker = EasyTracker.getInstance(context);
		
		Long longValue = null;
		if (value != null)
		{
			longValue = value.longValue();
		}
		
		if (easyTracker != null)
		{
			Utils.Log("analytics: calling easyTracker.send");
			// MapBuilder.createEvent().build() returns a Map of event fields and values
			// that are set and sent with the hit.
			easyTracker.send(MapBuilder
					.createEvent(category,     // Event category (required)
							action,  // Event action (required)
							label,   // Event label
							longValue)            // Event value
							.build()
					);
		}
	}
	
	public static String getAnalyticsCategoryFromGame(int gameType)
	{
		switch (gameType)
		{
		case Constants.GAME_TYPE_RECALL_WORDS:
			return ANALYTICS_CATEGORY_GAME_RECALL_WORDS;
		case Constants.GAME_TYPE_MATCH_WORDS:
			return ANALYTICS_CATEGORY_GAME_MATCH_WORDS;
		case Constants.GAME_TYPE_HANGMAN:
			return ANALYTICS_CATEGORY_GAME_HANGMAN;
		case Constants.GAME_TYPE_CROSSWORD:
			return ANALYTICS_CATEGORY_GAME_CROSSWORD;
		case Constants.GAME_TYPE_WORD_JUMBLE:
			return ANALYTICS_CATEGORY_GAME_WORD_JUMBLE;
		case Constants.GAME_TYPE_WORD_SEARCH:
			return ANALYTICS_CATEGORY_GAME_WORD_SEARCH;
	
		}
		
		return null;
	}
	
	// Categories
	public static final String ANALYTICS_CATEGORY_UI_ACTION = "ui_action";
	public static final String ANALYTICS_CATEGORY_IMPLICIT_ACTION = "implicit_action";
	public static final String ANALYTICS_CATEGORY_GAME_RECALL_WORDS = "game_recall_words";
	public static final String ANALYTICS_CATEGORY_GAME_MATCH_WORDS = "game_match_words";
	public static final String ANALYTICS_CATEGORY_GAME_HANGMAN = "game_hangman";
	public static final String ANALYTICS_CATEGORY_GAME_CROSSWORD = "game_crossword";
	public static final String ANALYTICS_CATEGORY_GAME_WORD_JUMBLE = "game_word_jumble";
	public static final String ANALYTICS_CATEGORY_GAME_WORD_SEARCH = "game_word_search";
	
	// Actions
	public static final String ANALYTICS_ACTION_BUTTON_PRESS = "button_press";
	public static final String ANALYTICS_ACTION_SELECTION_MADE = "selection_made";
	
	// Home Screen Labels
	public static final String ANALYTICS_LABEL_OPTIONS_BUTTON = "options_button";
	public static final String ANALYTICS_LABEL_BROWSE_BUTTON = "browse_button";
	public static final String ANALYTICS_LABEL_ABOUT_BUTTON = "about_button";
	public static final String ANALYTICS_LABEL_EXIT_BUTTON = "exit_button";
	public static final String ANALYTICS_LABEL_PLAY_BUTTON = "play_button";
	
	/**
	 * Theme Selection
	 */
	public static final String ANALYTICS_LABEL_THEMES_SELECTION = "themes_selection";
	
	public static final int ANALYTICS_VALUE_THEME_SUNSET_ORANGE = Themes.THEME_ID_SUNSET_ORANGE;
	public static final int ANALYTICS_VALUE_THEME_SKY_BLUE = Themes.THEME_ID_SKY_BLUE;
	public static final int ANALYTICS_VALUE_THEME_FOREST_GREEN = Themes.THEME_ID_FOREST_GREEN;
	public static final int ANALYTICS_VALUE_THEME_ELECTRIC_PURPLE = Themes.THEME_ID_ELECTRIC_PURPLE;
	public static final int ANALYTICS_VALUE_THEME_COFFEE_TIME = Themes.THEME_ID_COFFEE_TIME;
	public static final int ANALYTICS_VALUE_THEME_CUSTOM_COLOR = Themes.THEME_ID_CUSTOM_COLOR;
	
	/**
	 * Word Difficulty Selection
	 */
	public static final String ANALYTICS_LABEL_WORD_DIFFICULTY_SELECTION = "word_difficulty_selection";
	
	public static final int ANALYTICS_VALUE_WORD_DIFFICULTY_VERY_EASY = Constants.PRACTICE_TYPE_EASY;
	public static final int ANALYTICS_VALUE_WORD_DIFFICULTY_EASY = Constants.PRACTICE_TYPE_MEDIUM;
	public static final int ANALYTICS_VALUE_WORD_DIFFICULTY_HARD = Constants.PRACTICE_TYPE_HARD;
	public static final int ANALYTICS_VALUE_WORD_DIFFICULTY_VERY_HARD = Constants.PRACTICE_TYPE_VERY_HARD;
	
	/**
	 * Browse Screen Action Labels
	 */
	public static final String ANALYTICS_LABEL_SEARCH_WORDS_BUTTON = "search_words_button";
	public static final String ANALYTICS_LABEL_BROWSE_WORDS_BUTTON = "browse_words_button";
	
	/**
	 * About Screen Action Labels
	 */
	public static final String ANALYTICS_LABEL_RATE_BUTTON = "rate_button";
	public static final String ANALYTICS_LABEL_FEEDBACK_BUTTON = "feedback_button";
	public static final String ANALYTICS_LABEL_FACEBOOK_BUTTON = "facebook_button";
	public static final String ANALYTICS_LABEL_TWITTER_BUTTON = "twitter_button";
	
	/**
	 * Game Selection Labels
	 */
	public static final String ANALYTICS_LABEL_RECALL_WORDS_BUTTON = "recall_words_button";
	public static final String ANALYTICS_LABEL_MATCH_WORDS_BUTTON = "match_words_button";
	public static final String ANALYTICS_LABEL_HANGMAN_BUTTON = "hangman_button";
	public static final String ANALYTICS_LABEL_CROSSWORD_BUTTON = "crossword_button";
	public static final String ANALYTICS_LABEL_WORD_JUMBLE_BUTTON = "word_jumble_button";
	public static final String ANALYTICS_LABEL_WORD_SEARCH_BUTTON = "word_search_button";
	
	/**
	 * Game Action Labels
	 */
	public static final String ANALYTICS_LABEL_STOP_BUTTON = "stop_button";
	public static final String ANALYTICS_LABEL_PAUSE_BUTTON = "pause_button";
	public static final String ANALYTICS_LABEL_REVEAL_BUTTON = "reveal_button";
	public static final String ANALYTICS_LABEL_HINT_BUTTON = "hint_button";
	public static final String ANALYTICS_LABEL_HELP_BUTTON = "help_button";
	public static final String ANALYTICS_LABEL_NEXT_GAME_BUTTON = "next_game_button";
	public static final String ANALYTICS_LABEL_BACK_BUTTON = "back_button";
	public static final String ANALYTICS_LABEL_GAME_SUMMARY_SHARE_BUTTON = "share_game_summary";
	public static final String ANALYTICS_LABEL_REVEAL_GAME_BUTTON = "reveal_game_button";
	public static final String ANALYTICS_LABEL_REVEAL_SHARE_BUTTON = "reveal_share_button";
	public static final String ANALYTICS_LABEL_REVEAL_CANCELLED = "reveal_cancelled";
	public static final String ANALYTICS_LABEL_HINT_USED = "hint_used";
	public static final String ANALYTICS_LABEL_HINT_SHARE_BUTTON = "hint_share_button";
	public static final String ANALYTICS_LABEL_SHARE_BUTTON = "share_button";
	public static final String ANALYTICS_LABEL_SHARE_GAME_BUTTON = "share_game_button";
	public static final String ANALYTICS_LABEL_INVITE_FRIENDS_BUTTON = "invite_friends_button";
	
}