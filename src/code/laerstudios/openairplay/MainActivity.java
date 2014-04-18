package code.laerstudios.openairplay;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Menu;
import android.view.View;






public class MainActivity extends FragmentActivity {

	
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View root = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(root);
        final SlidingPaneLayout slidingPaneLayout = SlidingPaneLayout.class.cast(root.findViewById(R.id.slidingpanelayout));
        slidingPaneLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
        	
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelOpened(View view) {

                switch (view.getId()) {
                    case R.id.fragment_secondpane:
                        getSupportFragmentManager().findFragmentById(R.id.fragment_firstpane).setHasOptionsMenu(true);
                        getSupportFragmentManager().findFragmentById(R.id.fragment_secondpane).setHasOptionsMenu(false);
                        System.out.println("Panel Opened");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPanelClosed(View view) {

                switch (view.getId()) {
                    case R.id.fragment_secondpane:
                        getSupportFragmentManager().findFragmentById(R.id.fragment_firstpane).setHasOptionsMenu(false);
                        getSupportFragmentManager().findFragmentById(R.id.fragment_secondpane).setHasOptionsMenu(true);
                        System.out.println("Panel Closed");
                        break;
                    default:
                        break;
                }
            }
        });


       

    
        


    }
  //This method will override by child class. Then base class can get the fragment
  		protected Fragment getSampleFragment() {
  	        return null;
  	    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
       
}
}
