/*
 * Created on Jan 10, 2006
 *
 */

package net.atlanticbb.tantlinger.ui;

import java.awt.*;
import javax.swing.*;

public class OptionDialog extends StandardDialog
{    
	private static final long serialVersionUID = -298096240256846303L;
	private JPanel internalContentPane;
    private Container contentPane;
    
    public OptionDialog(Frame parent, String headerTitle)
    {
        super(parent, headerTitle, BUTTONS_RIGHT);
        init();        
    }

    public OptionDialog(Dialog parent, String headerTitle)
    {
        super(parent, headerTitle, BUTTONS_RIGHT);
        init();
    }
    
    private void init()
    {
        internalContentPane = new JPanel(new BorderLayout());
        internalContentPane.setOpaque(false);
        
        super.setContentPane(internalContentPane);
    }
    
    public Container getContentPane()
    {
        return contentPane;
    }
    
    public void setContentPane(Container c)
    {
        //internalContentPane.remove(contentPane);
        contentPane = c;
        internalContentPane.add(c, BorderLayout.CENTER);
        
    }
}
