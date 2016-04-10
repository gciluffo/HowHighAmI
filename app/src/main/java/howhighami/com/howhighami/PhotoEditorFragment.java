package howhighami.com.howhighami;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;


/**
 * Created by gciluffo on 4/9/16.
 */
public class PhotoEditorFragment extends Fragment implements IAdobeAuthClientCredentials, IAviaryClientCredentials {

    private Uri mUri;
    private double mAltitude;
    private Bitmap mOldBitmap, mNewBitmap;
    private static final String TAG = "PhotoEditorFragment";
    public static final String PICTURE_ID = "howhighami.picture.id";
    public static final String PICTURE_ALT = "howhighami.picture.alt";
    /* Be sure to fill in the two strings below. */
    private static final String CREATIVE_SDK_CLIENT_ID = "e264b6e98fa4459fbdf91e556b45a9e5";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "ae2eb369-6677-4dab-bba3-36d365bf252d";

    private ImageView mImageView;
    /**
     * Called when the view is created.  Gets a reference to our recycler view
     *
     * @param inflater           our layout inflater
     * @param container          the parent container
     * @param savedInstanceState a state from a saved previous run
     * @return the created view
     */
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
        mAltitude = args.getDouble(PICTURE_ALT);
        mUri = Uri.parse(uri);
    }

    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID; }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }

    /* 2) Add the getBillingKey() method */
    @Override
    public String getBillingKey() {
        return ""; // Leave this blank
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
        mOldBitmap = PictureUtils.getScaledBitmap(getRealPathFromURI(mUri), getActivity());
        String text = "Elevation : " + (int)mAltitude + " ft";
        /**
         * Edit the bitmap to add text
         */
        mNewBitmap = PictureUtils.drawTextToBitmap(mOldBitmap, text);
        mImageView.setImageBitmap(mNewBitmap);
        /**
         * Writes bitmap to original file path so the edited image will appear in the grid
         * in the main activity
         */
        PictureUtils.writeBitmapToFile(mNewBitmap, getRealPathFromURI(mUri));

        // TODO: Get Adobe Aviary working

        if(mUri != null) {

            //Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            //((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            Intent imageEditorIntent = new AdobeImageIntent.Builder(getContext())
                    .setData(mUri)
                    .build();

            startActivityForResult(imageEditorIntent, 1);
        }

    }
}
