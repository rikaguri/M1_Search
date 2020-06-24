package com.asreader.component;

import java.text.Collator;
import java.util.Comparator;

import android.graphics.drawable.Drawable;

public class ListData {
   
    public Drawable mIcon; 
    public String mAddr;
    public String mName;
    
    public static final Comparator<ListData> ALPHA_COMPARATOR = new Comparator<ListData>() 
    {
        private final Collator sCollator = Collator.getInstance();    
        @Override
        public int compare(ListData mListDate_1, ListData mListDate_2) 
        {
            return sCollator.compare(mListDate_1.mAddr, mListDate_2.mAddr);
        }
    };
}

