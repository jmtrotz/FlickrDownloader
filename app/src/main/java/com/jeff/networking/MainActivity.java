package com.jeff.networking;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for the application
 * Date: 4/19/18
 * @author Jeffrey Trotz
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity
{
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mGalleryItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    /**
     * Sets up the adapter for the Recycler View
     */
    private void setupAdapter()
    {
        mPhotoRecyclerView.setAdapter(new PhotoAdapter(mGalleryItems));
    }

    /**
     * Clears queued items when the app is closed
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mThumbnailDownloader.clearQueue();
        mThumbnailDownloader.quit();
    }

    /**
     * Sets up the Recycler View and displays photos when the app is launched
     * @param savedInstanceState Saved instance of the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhotoRecyclerView = findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>()
                {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap)
                    {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    /**
     * Background thread to download images from Flickr
     */
    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>>
    {
        /**
         * Downloads images from Flickr
         * @param params
         * @return
         */
        @Override
        protected List<GalleryItem> doInBackground(Void... params)
        {
            return new FlickrFetcher().fetchItems();
        }

        /**
         * Sets up view adapter after the data has been downloaded
         * @param galleryItems Array list of images
         */
        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems)
        {
            mGalleryItems = galleryItems;
            setupAdapter();
        }
    }

    /**
     * Holder class for the Recycler View
     */
    private class PhotoHolder extends RecyclerView.ViewHolder
    {
        private ImageView mItemImageView;

        /**
         * Constructor
         * @param itemView View to hold the images
         */
        public PhotoHolder(View itemView)
        {
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
        }

        /**
         * Binds the image from Flickr to the view
         * @param drawable
         */
        public void bindDrawable(Drawable drawable)
        {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    /**
     * Adapter class for the Recycler View
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>
    {
        private List<GalleryItem> mGalleryItems;

        /**
         * Constructor
         * @param galleryItems Array list of images
         */
        public PhotoAdapter(List<GalleryItem> galleryItems)
        {
            mGalleryItems = galleryItems;
        }

        /**
         * Creates the view
         * @param viewGroup Group of views
         * @param viewType Type of view
         * @return Returns the view
         */
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
        {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.list_item_gallery, viewGroup, false);
            return new PhotoHolder(view);
        }

        /**
         * Queues thumbnails to be displayed in the app
         * @param photoHolder Holder for the Recycler View
         * @param position Position of the photo in the array list of photos
         */
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position)
        {
            GalleryItem galleryItem = mGalleryItems.get(position);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getURL());
        }

        /**
         * Method to get the size of the array list of photos
         * @return Returns the size of the array list as an integer
         */
        @Override
        public int getItemCount()
        {
            return mGalleryItems.size();
        }
    }
}