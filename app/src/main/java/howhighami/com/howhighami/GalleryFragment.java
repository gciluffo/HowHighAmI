package howhighami.com.howhighami;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gciluffo on 3/30/16.
 */

public class GalleryFragment extends Fragment {

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
     * The current altitude. This is set in the async task
     */
    private double mCurrentAltitude;

    /**
     * Used to communitate with editor fragment
     */
    private Callbacks mCallbacks;


    public interface Callbacks {
        void sendPicture(String path, double alt, boolean addText);
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
                Log.d(TAG, "The path from fragment is " + newItem.getFilePath());

                /**
                 * Run the async task to get elevation
                 */
                new GoogleAltitude().execute();
                newItem.setElevation(mCurrentAltitude);

                /**
                 * Send file path and alt to edit fragment
                 */
                mCallbacks.sendPicture(newItem.getUri().toString(), mCurrentAltitude, true);
                mGalleryItems.add(newItem);

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

    private void makeToast(int duration, String message) {
        Context context = getContext();
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    class GoogleAltitude extends AsyncTask<Void, Void, Double> {

        /**
         * Do things to connect to google maps and get elevation from lat/long
         * @param params
         * @return
         */
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

        /**
         * Return the value from doInBackground to fragment
         * @param alt
         */
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
         * Pull up the fragment to edit it or share when clicking it
         */
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "Image: " + pos);
            GalleryItem item = mGalleryItems.get(pos);
            mCallbacks.sendPicture(item.getUri().toString(), item.getElevation(), false);
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
