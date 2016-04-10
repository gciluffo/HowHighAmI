package howhighami.com.howhighami;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * Created by gciluffo on 4/9/16.
 */
public class PhotoEditorActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoEditorActivity";
    private String mUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "The URI in PhotoEditorActivity is: " + mUri);
        /*
        Bundle bundle=new Bundle();
        bundle.putString("message", mUri);
        //set Fragmentclass Arguments
        PhotoEditorFragment fragobj = new PhotoEditorFragment();
        fragobj.setArguments(bundle);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragmentContainer, fragobj);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        */
        
    }

    @Override
    protected Fragment createFragment() {
        Log.d(TAG, "creatingFragment() called");
        return new PhotoEditorFragment();
    }

}
