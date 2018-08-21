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

public class Constants
{
	public static final String APP_PNAME = "com.ezeeideas.wordjam";
	
	// Shared Prefs
	public static final String PREFS_NAME = "app_preference";
	public static final String PREFS_NEW_GAME_CHOICE = "new_game_choice";
	public static final int PREFS_NEW_GAME_SAME_TYPE = 1;
	public static final int PREFS_NEW_GAME_RANDOM_TYPE = 2;
	public static final int PREFS_NEW_GAME_ASK_ME = 3;
	
	public static final String PREFS_RESTORE_APP_CHOICE = "restore_app_choice";
	public static final int PREFS_RESTORE_APP_HOME_SCREEN = 1;
	public static final int PREFS_RESTORE_APP_RECENT_GAME = 2;
	
	public static final int TEST_TYPE_HANGMAN = 1;
	public static final int TEST_TYPE_CROSSWORD = 2;
	public static final int TEST_TYPE_WORDSEARCH = 3;
	public static final int TEST_TYPE_WORDJUMBLE = 4;
	
	// DB related

	public static final String HANGMAN_TABLE_NAME = "hangman";
	public static final String CROSSWORD_TABLE_NAME = "crossword";
	public static final String WORD_JUMBLE_TABLE_NAME = "wordjumble";
	public static final String WORD_SEARCH_TABLE_NAME = "wordsearch";
	
	// PLAY or PRACTICE?
	public static final String KEY_IS_PLAY = "is_play";
	
	// Game related
	
	// Challenge Type
	public static final String KEY_PLAY_TYPE = "play_type"; //the key to pass between activities
	public static final int PLAY_TYPE_ENGLISH_CLASSIC = 1;
	public static final int PLAY_TYPE_GRE = 2;
	public static final int PLAY_TYPE_SAT = 3;
	public static final int PLAY_TYPE_KIDS = 4;
	public static final int NUM_CHALLENGES = 4; //number of challanges / Plays
	
	// Level Types
	public static final int LEVEL_1 = 1;
	public static final int LEVEL_2 = 2;
	public static final int LEVEL_3 = 3;
	public static final int LEVEL_4 = 4;
	public static final int LEVEL_5 = 5;
	public static final int LEVEL_6 = 6;
	
	// Practice Type
	public static final String KEY_PRACTICE_TYPE = "pratice_type"; //the key to pass between activities
	public static final int PRACTICE_TYPE_EASY = 1;
	public static final int PRACTICE_TYPE_MEDIUM = 2;
	public static final int PRACTICE_TYPE_HARD = 3;
	public static final int PRACTICE_TYPE_VERY_HARD = 4;
	public static final int PRACTICE_TYPE_DEFAULT = PRACTICE_TYPE_EASY;
	
	//Game Types
	public static final String KEY_GAME_NUM = "game_num";
	public static final String KEY_GAME_TYPE = "game_type";
	public static final int GAME_TYPE_RECALL_WORDS = GameUtils.GAME_TYPE_RECALL_WORDS;
	public static final int GAME_TYPE_MATCH_WORDS = GameUtils.GAME_TYPE_MATCH_WORDS;
	public static final int GAME_TYPE_HANGMAN = GameUtils.GAME_TYPE_HANGMAN;
	public static final int GAME_TYPE_WORD_JUMBLE = GameUtils.GAME_TYPE_WORD_JUMBLE;
	public static final int GAME_TYPE_WORD_SEARCH = GameUtils.GAME_TYPE_WORD_SEARCH;
	public static final int GAME_TYPE_CROSSWORD = GameUtils.GAME_TYPE_CROSSWORD;
	
	//Game States
	public static final String KEY_GAME_STATE = "game_state";
	public static final int GAME_STATE_NOT_STARTED = 1;
	public static final int GAME_STATE_IN_PROGRESS = 2;
	public static final int GAME_STATE_COMPLETED = 3;
	public static final int GAME_STATE_GIVEN_UP = 4;
	public static final int GAME_STATE_PAUSED = 5;
	public static final int GAME_STATE_FAILED = 6;
	
	//Other Keys used for Storing/restoring games
	public static final String KEY_GAME_HINTS_REMAINING = "game_hints_remaining";
	public static final String KEY_GAME_HINTS_USED = "game_hints_used";
	public static final String KEY_GAME_POINTS = "game_points";
	public static final String KEY_GAME_DURATION = "game_duration";
	public static final String KEY_GAME_DATA = "game_data";
	public static final String KEY_IS_NEW_GAME = "is_new_game";
	public static final String KEY_GAME_LAST_PLAYED = "game_last_played";
	public static final String KEY_GAME_LEVEL = "game_level";

	
	public static final String KEY_LEVEL_POINTS = "level_points";
	public static final String KEY_LEVEL_GAMES_REMAINING = "level_games_remaining";
	public static final String KEY_LEVEL_HINTS_REMAINING = "level_hints_remaining";
	public static final String KEY_LEVEL_HINTS_EARNED = "level_hints_earned";
	public static final String KEY_LEVEL_NUM_GAMES = "level_num_games";
	
	// Level States
	public static final String KEY_LEVEL = "level";
	public static final String KEY_LEVEL_STATE = "level_state";
	public static final int LEVEL_STATE_NOT_STARTED = 1;
	public static final int LEVEL_STATE_IN_PROGRESS = 2;
	public static final int LEVEL_STATE_COMPLETED = 3;
	
	// Practice (non Play) related
	public static final String PRACTICE_GAMES_COMPLETED = "practice_games_completed"; //No. of practice games completed so far.  Passed along to next practice game and added up for the session.
	public static final String PRACTICE_TIME_SPENT = "practice_time_spent"; // Total practice time spent in this session.  Passed along to next practice game and added up for the session.
	public static final String PRACTICE_HINTS_USED = "practice_hints_used"; // Total practice hints used in this session.  Passed along to next practice game and added up for the session.
	public static final String PRACTICE_POINTS_EARNED = "practice_points_earned"; // Total practice points earned in this session.  Passed along to next practice game and added up for the session.
	
	
	// Activity identification when passing results between each other
	public static final int KEY_SOURCE_ACTIVITY = 100;
	public static final int ACTION_GO_TO_LEVEL_HOME = 1;
	public static final int ACTION_PUBLISH_STORY_ON_FACEBOOK = 200;
	public static final int KEY_WORD_DIFFICULTY = 200;
	public static final int KEY_THEME = 300;
	
	// Preferences
	public static final String PREF_WORD_DIFFICULTY = "word_difficulty";
	public static final String PREF_THEME = "theme";
	public static final String PREF_THEME_COLOR = "themeColor";
	
	// Feedback
	public static final String SUPPORT_EMAIL = "https://www.github.com/gauravdelphinus/wordjam";


	// Facebook
	public static final String FACEBOOK_APP_URL = "https://www.facebook.com/appcenter/wordjam";
	public static final String FACEBOOK_STORY_PICTURE_URL = "http://ezeeideas.com/wordjam/facebook_share_icon.png";
	public static final String KEY_FACEBOOK_STORY_TITLE = "facebook_story_title";
	public static final String KEY_FACEBOOK_STORY_CAPTION = "facebook_story_caption";
	public static final String KEY_FACEBOOK_STORY_DESCRIPTION = "facebook_story_description";
	public static final String FACEBOOK_PAGE_URL = "https://www.facebook.com/pages/Word-Jam/130607287109919";

	public static final String TWITTER_PAGE_URL = "https://twitter.com/gauravdelphinus";

}