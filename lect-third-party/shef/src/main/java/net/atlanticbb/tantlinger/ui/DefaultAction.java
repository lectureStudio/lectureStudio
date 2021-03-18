package net.atlanticbb.tantlinger.ui;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.bushe.swing.action.BasicAction;
import org.bushe.swing.action.EnabledUpdater;

public class DefaultAction extends BasicAction implements EnabledUpdater
{    
    private static final long serialVersionUID = 1L;

    public DefaultAction()
    {
        this(null);        
    }
    
    public DefaultAction(String id)
    {
        this(id, null);         
    }

    public DefaultAction(String id, Icon icon)
    {
        this(id, null, null, icon);        
    }

    public DefaultAction(String id, Integer mnemonic, KeyStroke accelerator, Icon icon)
    {
        this(id, id, id, mnemonic, accelerator, icon);        
    }

    public DefaultAction(String id, String shortDesc, String longDesc, Integer mnemonic, KeyStroke accelerator, Icon icon)
    {
        this(id, id, id, shortDesc, longDesc, mnemonic, accelerator, icon);        
    }
    
    public DefaultAction(String id, String actionName, String actionCommandName, String shortDesc, 
        String longDesc, Integer mnemonic, KeyStroke accelerator, Icon icon)
    {
        this(id, actionName, actionCommandName, shortDesc, longDesc, 
            mnemonic, accelerator, icon, false, true);        
    }

    public DefaultAction(String id, String actionName, String actionCommandName, 
        String shortDesc, String longDesc, Integer mnemonic, KeyStroke accelerator, 
        Icon icon, boolean toolbarShowsText, boolean menuShowsIcon)
    {
        super(id, actionName, actionCommandName, shortDesc, longDesc, mnemonic,
            accelerator, icon, toolbarShowsText, menuShowsIcon);        
    }

    public boolean updateEnabled()
    {        
        updateEnabledState();
        return isEnabled();
    }

    public boolean shouldBeEnabled(Action action)
    {        
        return shouldBeEnabled();
    }


//    public void updateEnabledState() {
//        boolean shouldBe = shouldBeEnabled();
//        if(shouldBe != isEnabled())
//            setEnabled(shouldBe);
//    }

//     public void putContextValue(Object key, Object contextValue) {
//         if(getContext() != null && getContext().containsKey(key) && getContext().get(key) == contextValue) {
//            return;
//        }
//
//        super.putContextValue(key, contextValue);
//    }
    
    /**
     * Catch all for anything thrown in the execute() method.
     * This implementation shows an ExceptionDialog.
     * (non-Javadoc)
     * @see org.bushe.swing.action.BasicAction#actionPerformedCatch(java.lang.Throwable)
     */
    protected void actionPerformedCatch(Throwable t)
    {
        //UIUtils.showError(/*DesktopApp.getMainFrame()*/, t);
        t.printStackTrace();
    }    
}
