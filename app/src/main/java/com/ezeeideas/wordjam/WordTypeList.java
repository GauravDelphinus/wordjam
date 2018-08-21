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

import java.util.ArrayList;
import android.view.View.OnClickListener;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

public class WordTypeList extends Activity implements OnClickListener
{
	private ArrayList<WordTypeItem> mWordTypes;
	
	public static final String KEY_WORD_TYPE = "word_type";
	public static final String KEY_WORD_TYPE_STR = "word_type_str";
	
	private WordJamApp mApp;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mApp = (WordJamApp) getApplication();

		setContentView(R.layout.wordtypelist);
		
		// initialize words
		mWordTypes = new ArrayList<WordTypeItem>();
		for (int i = 1; i <= 6; i++)
		{
			mWordTypes.add(new WordTypeItem(i, mApp.getShortNamesForType(i)));
		}
		
		//set up list view
		ListView wordTypeLV = (ListView) findViewById(R.id.wordtypelist);

		wordTypeLV.setAdapter(new WordTypeAdapter(this, R.layout.wordtypelistitem, mWordTypes));
		
		wordTypeLV.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				int wordType = mWordTypes.get(position).wordType;
				String wordTypeStr = mWordTypes.get(position).wordTypeStr;
				
		    	Intent i = new Intent(getBaseContext(), WordList.class);
		    	i.putExtra(WordTypeList.KEY_WORD_TYPE, wordType);
		    	i.putExtra(WordTypeList.KEY_WORD_TYPE_STR, wordTypeStr);
		 
		    	startActivity(i);
			}
			
		}
		);
		
		//button click listeners
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);
		
		honorTheme();
	}
	
	private void honorTheme()
    {
    	LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
    	ll.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    }
	
    public void onClick(View v) {
        
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }
    
	private class WordTypeItem
	{
		int wordType;
		String wordTypeStr;
		
		public WordTypeItem(int type, String name)
		{
			wordTypeStr = name;
			wordType = type;
		}
		
		@Override
		public String toString()
		{
			return wordTypeStr;
		}
	}
	
	public class WordTypeAdapter extends ArrayAdapter<WordTypeItem>
	{
		private List<WordTypeItem> mWordTypes;
		
		public WordTypeAdapter(Context context, int textViewResourceId, List<WordTypeItem> wordTypes)
		{
			super(context, textViewResourceId, wordTypes);
			mWordTypes = wordTypes;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Log.d(null, "################ getView called with position " + position);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.wordtypelistitem, null);
            }
            
            TextView wordTypeTextView = (TextView) v.findViewById(R.id.wordtypetextrow);
                    
                    if(wordTypeTextView != null){
                    	//Log.d(null, "setting the view text to [" + mWordTypes.get(position).wordTypeStr + "]");
                          wordTypeTextView.setText(mWordTypes.get(position).wordTypeStr);
                    }
            
            return v;
		}
	}
}