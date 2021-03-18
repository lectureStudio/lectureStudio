/*
 * Created on Nov 6, 2007
 */
package net.atlanticbb.tantlinger.i18n;

import java.util.ResourceBundle;

/**
 * @author Bob Tantlinger
 *
 */
public class I18n
{
	private static ResourceBundle dict;
	
    private static I18n instance;
    
    
    private I18n()
    {
        
    }
    
    public String str(String key)
    {
    	return dict.getString("editor." + key);
    }
    
    public static I18n getInstance(String _package)
    {
        if(instance == null)
        {
        	instance = new I18n();
        }
        
        return instance;
    }
    
    public static void setDictionary(ResourceBundle dict)
    {
    	I18n.dict = dict;
    }
    
}
