//based on a javaworld article

package net.atlanticbb.tantlinger.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * This class implements a standard data entry dialog with "OK" and
 * "Cancel" buttons. Subclasses can override the isDataValid(),
 * okButtonPressed(), and cancelButtonPressed() methods to perform
 * implementation specific processing.
 * <P>
 * By default, the dialog is modal, and has a JPanel with a
 * BorderLayout for its content pane.
 *
 * @author David Fraser
 * @author Michael Harris
 */
public class StandardDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    // Constants
    public static final int BUTTONS_CENTER = FlowLayout.CENTER;
    public static final int BUTTONS_LEFT = FlowLayout.LEFT;
    public static final int BUTTONS_RIGHT = FlowLayout.RIGHT;

    /** The spacing between components in pixels */
    private static final int COMPONENT_SPACING = 5;

    // Attributes

    /** Flag indicating if the "Cancel" button was pressed to close dialog */
    private boolean myIsDialogCancelled = true;

    /** The content pane for holding user components */
    private Container myUserContentPane;

    // Methods

    /**
     * This method is the default constructor.
     */
    public StandardDialog()
    {
        init(BUTTONS_CENTER, COMPONENT_SPACING);
    }

    /**
     * This method creates a StandardDialog with the given parent frame
     * and title.
     *
     * @param parent The parent frame for the dialog.
     * @param title The title to display in the dialog.
     */
    public StandardDialog(Frame parent, String title)
    {
        super(parent, title);
        init(BUTTONS_CENTER, COMPONENT_SPACING);
    }

    /**
     * This method creates a StandardDialog with the given parent dialog
     * and title.
     *
     * @param parent The parent dialog for the dialog.
     * @param title The title to display in the dialog.
     */
    public StandardDialog(Dialog parent, String title)
    {
        super(parent, title);
        init(BUTTONS_CENTER, COMPONENT_SPACING);
    }
    
    public StandardDialog(Dialog parent, String title, int orientation, int spacing)
    {
        super(parent, title);
        init(orientation, spacing);
    }
    
    public StandardDialog(Frame parent, String title, int orientation, int spacing)
    {
        super(parent, title);
        init(orientation, spacing);
    }
    
    public StandardDialog(Frame parent, String title, int orientation)
    {
        super(parent, title);
        init(orientation, COMPONENT_SPACING);
    }
    
    public StandardDialog(Dialog parent, String title, int orientation)
    {
        super(parent, title);
        init(orientation, COMPONENT_SPACING);
    }
    
    

    /**
     * This method sets up the default attributes of the dialog and
     * the content pane.
     */
    private void init(int orientation, int spacing)
    {
        setModal(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Setup the internal content pane to hold the user content pane
        // and the standard button panel

        JPanel internalContentPane = new JPanel();

        internalContentPane.setLayout(
            new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
                
        Action okAction = new AbstractAction("OK")
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent actionEvent)
            {
                if(isValidData())
                {
                    myIsDialogCancelled = false;
                    dispose();
                }
            }
        };
        

        Action cancelAction = new AbstractAction("Cancel") //$NON-NLS-1$
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent actionEvent)
            {
                myIsDialogCancelled = true;
                dispose();
            }
        };
        

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JButton okB = new JButton(okAction);        
        buttonPanel.add(okB);
        buttonPanel.add(new JButton(cancelAction));
        this.getRootPane().setDefaultButton(okB);
        
        JPanel spacer = new JPanel(new FlowLayout(orientation));
        spacer.add(buttonPanel);

        //internalContentPane.add(buttonPanel, BorderLayout.SOUTH);
		internalContentPane.add(spacer, BorderLayout.SOUTH);

        // Initialise the user content pane with a JPanel
        myUserContentPane = new JPanel(new BorderLayout());
        //setContentPane(new JPanel(new BorderLayout()));
        super.setContentPane(internalContentPane);

        // Finally, add a listener for the window close button.
        // Process this event the same as the "Cancel" button.

        WindowAdapter windowAdapter = new WindowAdapter()
        {
            public void windowClosing(WindowEvent windowEvent)
            {
                myIsDialogCancelled = true;
                dispose();
            }
        };

        addWindowListener(windowAdapter);
    }

    /**
     * This method gets the content pane for adding components.
     * Components should not be added directly to the dialog.
     *
     * @returns the content pane for the dialog.
     */
    public Container getContentPane()
    {
        return myUserContentPane;
    }

    /**
     * This method sets the content pane for adding components.
     * Components should not be added directly to the dialog.
     *
     * @param contentPane The content pane for the dialog.
     */
    public void setContentPane(Container contentPane)
    {
        myUserContentPane = contentPane;
        super.getContentPane().add(myUserContentPane, BorderLayout.CENTER);
    }

    /**
     * This method returns <code>true</code> if the User cancelled
     * the dialog otherwise <code>false</code>. The dialog is cancelled
     * if the "Cancel" button is pressed or the "Close" window button is
     * pressed, or the "Escape" key is pressed. In other words, if the
     * User has caused the dialog to close by any method other than by
     * pressing the "Ok" button, this method will return <code>true</code>.
     */
    public boolean hasUserCancelled()
    {
        return myIsDialogCancelled;
    }

    /**
     * This method is used to validate the current dialog box. This method
     * provides a default response of <code>true</code>. This method should be
     * implemented by each dialog that extends this class.
     *
     * @returns a boolean indicating if the data is valid.
     * <code>true</code> indicates that all of the fields were validated
     * correctly and <code>false</code> indicates the validation failed
     */
    protected boolean isValidData()
    {
        return true;
    }
}
