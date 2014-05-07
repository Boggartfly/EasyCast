/*
 * Copyright 2014 Parth Sane

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.laer.easycast;



import com.laer.easycast.R;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;







public class FirstPane extends Fragment {
	
    ViewGroup myViewGroup;
    
   

    

    @Override
	public void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
        
    }

    @Override
	public void onStop() {
    	
    	super.onStop();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = new View(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root= inflater.inflate(R.layout.firstlayout,container,false);
        root.setBackgroundColor(Color.GRAY);
        setHasOptionsMenu(true);
        myViewGroup=container;
        
        
        
        return root;
    }


    
   
}