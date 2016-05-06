package howhighami.com.howhighami;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;


/**
 * Created by gciluffo on 4/9/16.
 */
public class PhotoEditorFragment extends Fragment {

    private Uri mUri;
    private int mAltitude;
    private Boolean mAddAltitudeText = false;
    private static final String TAG = "PhotoEditorFragment";
    public static final String PICTURE_ID = "howhighami.picture.id";
    public static final String PICTURE_ALT = "howhighami.picture.alt";
    public static final String PICTURE_ADD = "howhighami.picture.add";
    public static final int REQUEST_EDIT = 5;
    private ImageView mImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_edit, container, false);
        mImageView = (ImageView) view.findViewById(R.id.editedImageView);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        AdobeCSDKFoundation.initializeCSDKFoundation(getActivity().getApplicationContext());
        Log.d(TAG, "PHOTO EDITOR FRAGMENT ONCREATE ");

        // Get file path and start Adobe editor
        Bundle args = getArguments();
        String uri = args.getString(PICTURE_ID);
        mAltitude = args.getInt(PICTURE_ALT);
        mUri = Uri.parse(uri);
        mAddAltitudeText = args.getBoolean(PICTURE_ADD);
    }

    /**
     * Gets path to image with uri
     * @return
     */
    public String getRealPathFromURI(Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(
                    contentUri,
                    proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }

    // Initialize the menu from the res/menu directory
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.image_menu, menu);
    }

    /**
     * Handle options being selected in title bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.share:
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "Check out how high I am right now!");
                i.putExtra(Intent.EXTRA_STREAM, mUri);
                startActivity(Intent.createChooser(i, "Share this image via"));
                return true;
            case R.id.delete:
                // find the parent and notify it of the deletion
                GalleryActivity parent = (GalleryActivity) getActivity();
                parent.notifyDelete(mUri);

                // return to parent activity
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
                return true;
            case R.id.edit:
                Intent i2 = new Intent(getActivity(), AdobeEditor.class);
                i2.putExtra("uri", mUri.toString());
                startActivity(i2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "PHOTO EDITOR FRAGMENT onSTART ");
    }

    /**
    * For some reason this fragment is called before the picture is saved to file
    * To compensate we read from file on resume after we do the callback in gallery fragment
    * */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "PHOTO EDITOR FRAGMENT onRESUME ");
        // If text needs to be added
        if(mAddAltitudeText){
            Bitmap oldBitmap = PictureUtils.getScaledBitmap(getRealPathFromURI(mUri), getActivity());
            String text = "Elevation : " + mAltitude + " ft";
            /**
             * Edit the bitmap to add text
             */
            Bitmap newBitmap = PictureUtils.drawTextToBitmap(oldBitmap, text);
            mImageView.setImageBitmap(newBitmap);
            /**
             * Writes bitmap to original file path so the edited image will appear in the grid
             * in the main activity
             */
            PictureUtils.writeBitmapToFile(newBitmap, getRealPathFromURI(mUri));
            mAddAltitudeText = false;
        }
        // If user just want to look at image/edit/share
        else {
            Bitmap oldBitmap = PictureUtils.getScaledBitmap(getRealPathFromURI(mUri), getActivity());
            mImageView.setImageBitmap(oldBitmap);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQUEST_EDIT  && resultCode == getActivity().RESULT_OK) {
//            // get the URI of the edited image
//            Log.d(TAG, "got result. Current uri " + mUri.toString() + " new uri " + data.getSerializableExtra("IMG_URI"));
//            mUri = Uri.parse(data.getStringExtra("IMG_URI"));
//        }
    }
}
