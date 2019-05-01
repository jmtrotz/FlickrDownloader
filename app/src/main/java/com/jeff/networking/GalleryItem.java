package com.jeff.networking;

/**
 * Date: 4/20/18
 * @author Jeffrey Trotz
 * @version 1.0
 */
public class GalleryItem
{
    private String mCaption;
    private String mID;
    private String mURL;

    /**
     * Setter for mCaption
     * @param caption Image caption
     */
    public void setCaption(String caption)
    {
        this.mCaption = caption;
    }

    /**
     * Getter for mCaption
     * @return Returns mCaption as a String
     */
    public String getCaption()
    {
        return mCaption;
    }

    /**
     * Setter for mID
     * @param id Image ID
     */
    public void setID(String id)
    {
        this.mID = id;
    }

    /**
     * Getter for mID
     * @return Returns mID as a String
     */
    public String getID()
    {
        return mID;
    }

    /**
     * Setter for mURL
     * @param url Image URL
     */
    public void setURL(String url)
    {
        this.mURL = url;
    }

    /**
     * Getter for mURL
     * @return Returns the URL as a String
     */
    public String getURL()
    {
        return mURL;
    }
}
