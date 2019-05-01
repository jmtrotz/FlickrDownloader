package com.jeff.networking;

import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to download JSON data from Flickr
 * Date: 4/20/18
 * @author Jeffrey Trotz
 * @version 1.0
 */
public class FlickrFetcher
{
    private static final String TAG = "FlickrFetchr";

    /**
     * Converts the image URL into a byte array and returns it as a String
     * @param urlSpec Image URL
     * @return Returns the byte array it converted the URL to as a String
     * @throws IOException Throws an exception if there's an IO error
     */
    public String getUrlString(String urlSpec) throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * Opens input and output streams and stores the collected data in a byte array
     * @param urlSpec Flickr URL
     * @return Returns a byte array containing the downloaded data
     * @throws IOException Throws an exception if there's an IO error
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = inputStream.read(buffer)) > 0)
            {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            return outputStream.toByteArray();
        }

        finally
        {
            connection.disconnect();
        }
    }

    /**
     * Builds the URL, downloads the JSON data, and places the downloaded image in an array list
     * @return Returns an array list of downloaded images
     */
    public List<GalleryItem> fetchItems()
    {
        List<GalleryItem> galleryItems = new ArrayList<>();

        try
        {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", "81081bb696b492004599691158dc188e")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();

            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(galleryItems, jsonBody);
        }

        catch (IOException ioException)
        {
            Log.e(TAG, "Failed to fetch items", ioException);
        }

        catch (JSONException jsonException)
        {
            Log.e(TAG, "Failed to parse JSON", jsonException);
        }

        return galleryItems;
    }

    /**
     * Sifts through the JSON data and pulls out the information that we're interested in
     * @param galleryItems Array list of images to show in the gallery
     * @param jsonBody Raw JSON data to be parsed
     * @throws JSONException Throws a JSON exception if there's an issue with the JSON data
     */
    private void parseItems(List<GalleryItem> galleryItems, JSONObject jsonBody) throws JSONException
    {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++)
        {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setID(photoJsonObject.getString("id"));
            galleryItem.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s"))
            {
                continue;
            }

            galleryItem.setURL(photoJsonObject.getString("url_s"));
            galleryItems.add(galleryItem);
        }
    }
}