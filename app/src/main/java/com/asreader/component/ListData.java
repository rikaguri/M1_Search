package com.asreader.component;

import java.text.Collator;
import java.util.Comparator;

import android.graphics.drawable.Drawable;

public class ListData {
   
    public Drawable mIcon; 
    public String mAddr;
    public String mName;
    String tag1;
    String tag2;
    String theta;
    String dist;
    
    public static final Comparator<ListData> ALPHA_COMPARATOR = new Comparator<ListData>() 
    {
        private final Collator sCollator = Collator.getInstance();    
        @Override
        public int compare(ListData mListDate_1, ListData mListDate_2) 
        {
            return sCollator.compare(mListDate_1.mAddr, mListDate_2.mAddr);
        }
    };
    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public String getTag1(){
        return tag1;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public String getTag2(){
        return tag2;
    }

    public void setTheta(String theta) { this.theta = theta; }

    public String getTheta(){
        return theta;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public String getDist(){
        return dist;
    }
}




