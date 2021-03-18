/*
 * Created on Jan 20, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.ImageView;

/**
 * 
 * An ImageView which caches images to a local directory after they're
 * initially loaded. After the images are cached, they're subsequently
 * loaded from the local directory, thereby reducing the overhead of
 * refetching them from their remote location.
 * 
 * @author Bob Tantlinger
 *
 */
public class CachedImageView extends ImageView 
{
    public static final String USER_HOME = System.getProperty("user.home");
    /** Directory where app properties are stored */
    public static final String PROP_DIR = USER_HOME + File.separator + ".thinga";
    public static final String IMG_CACHE_DIR = PROP_DIR + File.separator + "img_cache";
    
    //flag which keeps track of if the image has been writen to disk
    private boolean hasCached;    
    
    public CachedImageView(Element elem)
    {
        super(elem);       
    }
    
    public void paint(Graphics g, Shape a) 
    {
        super.paint(g, a);
        
        Image img = getImage();
        
        //test if the image is completely loaded. 
        int flags = Toolkit.getDefaultToolkit().checkImage(
            img, -1, -1, getContainer());        
        boolean done = ((flags & (ImageObserver.ALLBITS)) != 0);
        if(done && !hasCached) //If it is, write it out to the cache       
            cacheImage(img);        
    }
    
    /**
     * Writes an image out to the cache
     * @param image
     */
    private void cacheImage(final Image image)
    {
        final File cImg = getCachedImg();        
        if(image == null || cImg == null || cImg.exists())
            return;
        
        hasCached = true;//set the hasCached flag so we dont write it twice
        
        Runnable runner = new Runnable()
        {
            public void run()
            {                
                String type = "png";//assume png for non-jpegs
                if(cImg.getAbsolutePath().endsWith("jpg"))
                    type = "jpg";
        
                BufferedImage bufImg = makeBufferedImage(image);
                try
                {
                    ImageIO.write(bufImg, type, cImg); 
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }                
            }
        };
        
        //System.err.println("CACHE " + cImg.getName());
        Thread th = new Thread(runner, "Image Cacher");
        th.setPriority(Thread.NORM_PRIORITY);
        th.start();
    }
    
    
    /**
     * Gets the real image URL as specified in the src attribute of the image tag
     * @return
     */
    private String srcAttribute()
    {
        return (String)getElement().getAttributes().getAttribute(HTML.Attribute.SRC);
    }
    
    /**
     * Return a URL for the image source, 
     * or null if it could not be determined.
     * 
     * If the image is cached, the local file URL is returned.
     * The superclass treats this the same as the actual url, but
     * the image is loaded from the local cache rather than redownloading it
     */
    public URL getImageURL()
    {
        String src = srcAttribute();
        if(src == null)        
            return null;        
        
        URL imgURL = null;        
        File diskCachedImg = getCachedImg();
        
        try
        {
            if(diskCachedImg.exists())
            {
                imgURL = diskCachedImg.toURI().toURL();
            }
            else
            {
                URL reference = ((HTMLDocument)getDocument()).getBase();
                imgURL = new URL(reference, src);
            }          
        }
        catch(MalformedURLException e)
        {
            
        }
        
        return imgURL;
    }
    
    private BufferedImage makeBufferedImage(Image image)
    {
        BufferedImage buffered;
        Graphics2D g2;

        buffered = new BufferedImage(image.getWidth(null),
                                      image.getHeight(null),
                                      BufferedImage.TYPE_INT_RGB);

        g2 = buffered.createGraphics();
        g2.drawImage(image, null, null);
        g2.dispose();

        return buffered;
    }
    
    /**
     * Returns the local file of the cached version of the image.
     * The local file is named with the absolute value of the hashcode for the
     * actual image url and a "png" or "jpg" file extension
     * @return
     */
    private File getCachedImg()
    {        
        String iUrl = srcAttribute();
        if(iUrl == null)
            return null;
        
        String imgType = ".png";
        if(iUrl.toLowerCase().endsWith(".jpg") || iUrl.toLowerCase().endsWith(".jpeg"))
            imgType = ".jpg";
        
        int hc = Math.abs(iUrl.hashCode());
        File cacheImg = new File(IMG_CACHE_DIR, hc + imgType);
        return cacheImg;        
    }
}
