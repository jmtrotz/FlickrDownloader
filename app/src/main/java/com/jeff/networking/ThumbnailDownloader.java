package com.jeff.networking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class to manage the thumbnails downloaded from Flickr
 * Date: 4/22/18
 * @author Jeffrey Trotz
 * @version 1.0
 */
public class ThumbnailDownloader<T> extends HandlerThread
{
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private boolean mHasQuit = false;
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    /**
     * Constructor
     * @param responseHandler Response handler
     */
    public ThumbnailDownloader(Handler responseHandler)
    {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    /**
     * Calls onThumbnailDownloaded()
     * @param <T> Generic identifier
     */
    public interface ThumbnailDownloadListener<T>
    {
        void onThumbnailDownloaded(T target, Bitmap bitmap);
    }

    /**
     * Sets thumbnail download listener
     * @param listener Thumbnail download listener interface
     */
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener)
    {
        mThumbnailDownloadListener = listener;
    }

    /**
     * Removes messages stored in the queue
     */
    public void clearQueue()
    {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    /**
     * Queues up the image thumbnails
     * @param target Generic object
     * @param url Image URL
     */
    public void queueThumbnail(T target, String url)
    {
        if (url == null)
        {
            mRequestMap.remove(target);
        }

        else
        {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    /**
     * Handles background thread messages
     * @param target Generic object
     */
    private void handleRequest(final T target)
    {
        try
        {
            final String url = mRequestMap.get(target);

            if (url == null)
            {
                return;
            }

            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            mResponseHandler.post(new Runnable()
            {
                public void run()
                {
                    if (mRequestMap.get(target) != url || mHasQuit)
                    {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        }

        catch (IOException ioException)
        {
            Log.e(TAG, "Error downloading image", ioException);
        }
    }

    /**
     * Called when the application is closed
     * @return Returns a call to the super class quit() method
     */
    @Override
    public boolean quit()
    {
        mHasQuit = true;
        return super.quit();
    }

    /**
     * Calls handleRequest() to handle messages for the background thread
     */
    @Override
    protected void onLooperPrepared()
    {
        mRequestHandler = new Handler()
        {
            @Override
            public void handleMessage(Message message)
            {
                if (message.what == MESSAGE_DOWNLOAD)
                {
                    T target = (T) message.obj;
                    handleRequest(target);
                }
            }
        };
    }
}