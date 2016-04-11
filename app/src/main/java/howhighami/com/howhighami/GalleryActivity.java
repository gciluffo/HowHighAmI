package howhighami.com.howhighami;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class GalleryActivity extends SingleFragmentActivity implements GalleryFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new GalleryFragment();
    }

    @Override
    public void sendPicture(String path, double alt, boolean addText) {

        // Start the photo editing fragment
        // Create fragment and give it an argument for the selected article
        PhotoEditorFragment newFragment = new PhotoEditorFragment();
        Bundle args = new Bundle();
        args.putString(PhotoEditorFragment.PICTURE_ID, path);
        args.putDouble(PhotoEditorFragment.PICTURE_ALT, alt);
        args.putBoolean(PhotoEditorFragment.PICTURE_ADD, addText);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

}
