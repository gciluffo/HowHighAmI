package howhighami.com.howhighami;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gciluffo on 3/30/16.
 */


public class GalleryFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GalleryFragment";
    private static final String uri = "image-uri";
    // Request codes for implicit intents
    private static final int REQUEST_PHOTO= 1;

    /**
     * Our RecyclerView object to display the photos
     */
    private RecyclerView mPhotoRecyclerView;
    /**
    * The Adapter for our RecyclerView
     */
    private PhotoAdapter mPhotoAdapter;
    /**
     * The current list of photos to be displaying
     */
    private List<GalleryItem> mGalleryItems;

    /**
     * The google services API client which allows us to get a Location
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The current altitude. This is set in the async task
     */
    private double mCurrentAltitude;


    private Callbacks mCallbacks;


    public interface Callbacks {
        void sendPicture(String path);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d(TAG, "created mGoogleApiClient to be used for GPS");
        }

        mGalleryItems = new ArrayList<>();
        new GoogleAltitude().execute();
    }

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
        Log.d(TAG, "onCreateView() called");

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mPhotoAdapter = null;
        setupAdapter();

        // TODO: Reload images when closing/starting the app in onPause/onResume

        return view;
    }

    // Initialize the menu from the res/menu directory
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.gallery_menu, menu);
    }

    /**
     * Handle options being selected in title bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.take_picture:

                /**
                 * Take the picture, save it to disk, record URI
                 */
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Image File name");
                Uri mCapturedImageURI = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                /**
                 * Create a galleryItem object and add to list of galleryItem to be displayed
                 */
                startActivityForResult(intentPicture, REQUEST_PHOTO);
                GalleryItem newItem = new GalleryItem();
                newItem.setFilePath(getRealPathFromURI(mCapturedImageURI));
                newItem.setUri(mCapturedImageURI);
                Log.d(TAG, "The Uri from fragment is " + newItem.getUri().toString());

                // Run async task to get current altitude
                new GoogleAltitude().execute();
                Log.d(TAG, "THE ALTITUDE IS " + mCurrentAltitude + " feet");
                newItem.setElevation(mCurrentAltitude);

                // TODO: Open different fragment for editing before adding to grid(pass the bitmap to editing fragment)?
                // Send path to activity
                mCallbacks.sendPicture(newItem.getUri().toString());


                mGalleryItems.add(newItem);

                return true;
            case R.id.share:
                // TODO: Set up sharing photo and elevation data to facebook, twitter etc. This could probably be implemented last
                return true;
            case R.id.delete:
                makeToast(Toast.LENGTH_SHORT, "Select the image you want to delete");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PHOTO && resultCode == getActivity().RESULT_OK) {
            Log.d(TAG, "Successfully got image from camera activity");
            mPhotoAdapter.notifyDataSetChanged();
        }

    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Called when a connection to the google play services API has been established
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "On Connected Called");
    }

    /**
     * Called when the connection to the google play services API has been lost
     * @param arg0
     */
    @Override
    public void onConnectionSuspended(int arg0) {
        // purposely left blank
        // GoogleAPIClient will attempt to reconnect automatically
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFalied() called");
        makeToast(Toast.LENGTH_LONG, "Lost connection to location services.");
    }

    private void makeToast(int duration, String message) {
        Context context = getContext();
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
//    @Override
//    public void onDisconnected() {
//
//    }

    class GoogleAltitude extends AsyncTask<Void, Void, Double> {

        // Do things to connect to Google database and query for altitude with current lat/long
        protected Double doInBackground(Void... params) {
            LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            // TODO: Add check to see if user accepts permission
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.d(TAG, "LONGITUDE IS " + longitude);
            Log.d(TAG, "LATITUDE IS " + latitude);

            double result = Double.NaN;
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            String url = "http://maps.googleapis.com/maps/api/elevation/"
                    + "xml?locations=" + String.valueOf(latitude)
                    + "," + String.valueOf(longitude)
                    + "&sensor=true";
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    int r = -1;
                    StringBuffer respStr = new StringBuffer();
                    while ((r = instream.read()) != -1)
                        respStr.append((char) r);
                    String tagOpen = "<elevation>";
                    String tagClose = "</elevation>";
                    if (respStr.indexOf(tagOpen) != -1) {
                        int start = respStr.indexOf(tagOpen) + tagOpen.length();
                        int end = respStr.indexOf(tagClose);
                        String value = respStr.substring(start, end);
                        result = (double)(Double.parseDouble(value)*3.2808399); // convert from meters to feet
                    }
                    instream.close();
                }
            } catch (ClientProtocolException e) {}
            catch (IOException e) {}

            return result;
        }

        @Override
        protected void onPostExecute(Double alt) {
            Log.d(TAG, "onPostExecute value is " + alt);
            // Set the value
            mCurrentAltitude = alt;
        }
    }

    /*****************************************************************************
     * Everything under this comment is related to displaying the photos on the grid
     *
     *****************************************************************************/
    private void setupAdapter() {
        Log.d(TAG, "setupAdapter() called");

        // check if the fragment is added to an activity
        if (isAdded()) {
            // set the adapter
            Log.d(TAG, "Created a new adapter");
            mPhotoAdapter = new PhotoAdapter(mGalleryItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        } else if (!isAdded()) {
            Log.d(TAG, "fragment not added to activity yet");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated( Bundle ) called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    /**
     * The ViewHolder for our RecyclerView to display Photos
     */
    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /**
         * A textview to display the photo caption
         */
        private ImageView mItemImageView;


        /**
         * Creates a new ViewHolder
         *
         * @param itemView currently must be a text view
         */
        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
            mItemImageView.setOnClickListener(this);
        }

        /**
         * listen for clicks on an individual image
         */
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "Image: " + pos);
            GalleryItem item = mGalleryItems.get(pos);
        }

        /**
         * Binds a gallery item to this ViewHolder
         *
         * @see GalleryItem
         */
        public void bindDrawable(Bitmap image) {
            // Resize the image for the grid layout
            Bitmap resized = Bitmap.createScaledBitmap(image, 120, 240, true);
            mItemImageView.setImageBitmap(resized);
        }
    }

    /**
     * The Adapter for our RecyclerView
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        /**
         * The list of photos to display
         */
        private List<GalleryItem> mGalleryItems;

        /**
         * Create a new adapter with a given set of photos
         *
         * @param galleryItems the photos to display
         */
        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        // Display the picture in the grid
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Bitmap bitmap = PictureUtils.getScaledBitmap(galleryItem.getFilePath(), getActivity());

            photoHolder.bindDrawable(bitmap);
            //Drawable placeholder = getResources().getDrawable(R.drawable.shaq);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

}
