package net.atlanticbb.tantlinger.ui;

import javax.swing.JPanel;

import java.awt.Frame;

import javax.swing.JDialog;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;



import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;

import net.atlanticbb.tantlinger.i18n.I18n;


public class ExceptionDialog extends JDialog
{
	
	private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui");
	
    private static final long serialVersionUID = 1L;
    private static final int PREFERRED_WIDTH = 450;
    private static final String DEFAULT_TITLE = "Error";  //  @jve:decl-index=0: //$NON-NLS-1$

    private JPanel jContentPane = null;
    private JLabel iconLabel = null;
    private JLabel titleLabel = null;
    private JLabel msgLabel = null;
    private JPanel buttonPanel = null;
    private JButton okButton = null;
    private JButton detailsButton = null;
    private JScrollPane scrollPane = null;
    private JTextArea textArea = null;
    private JSeparator separator = null;
    
    
    public ExceptionDialog()
    {
        super();
        init(new Exception());
        
    }
    
    /**
     * @param owner
     */
    public ExceptionDialog(Frame owner, Throwable th)
    {
        super(owner, DEFAULT_TITLE);
        init(th);
        
    }
    
    public ExceptionDialog(Dialog owner, Throwable th)
    {
        super(owner, DEFAULT_TITLE);
        init(th);
    }
    
    private void init(Throwable th)
    {        
        setModal(true);
        initialize();        
        setThrowable(th);
        showDetails(false);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {            
        this.setContentPane(getJContentPane());
        getRootPane().setDefaultButton(getOkButton());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if(jContentPane == null)
        {
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridwidth = 3;
            gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.anchor = GridBagConstraints.WEST;
            gridBagConstraints4.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints4.gridy = 3;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = GridBagConstraints.BOTH;
            gridBagConstraints11.gridy = 4;
            gridBagConstraints11.weightx = 1.0;
            gridBagConstraints11.weighty = 1.0;
            gridBagConstraints11.gridwidth = 3;
            gridBagConstraints11.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints11.gridx = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 2;
            gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints3.gridheight = 3;
            gridBagConstraints3.insets = new Insets(0, 0, 10, 0);
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            gridBagConstraints3.gridy = 0;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 1;
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.insets = new Insets(0, 20, 5, 5);
            gridBagConstraints2.gridy = 1;
            msgLabel = new JLabel();
            msgLabel.setText(""); //$NON-NLS-1$
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            gridBagConstraints1.insets = new Insets(0, 10, 5, 5);
            gridBagConstraints1.fill = GridBagConstraints.NONE;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.gridy = 0;
            titleLabel = new JLabel();
            titleLabel.setText(i18n.str("error_prompt")); //$NON-NLS-1$
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridheight = 3;
            gridBagConstraints.anchor = GridBagConstraints.NORTH;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints.gridy = 0;
            iconLabel = new JLabel();
            iconLabel.setText(""); //$NON-NLS-1$
            iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon")); //$NON-NLS-1$
            //iconLabel.setIcon(new ImageIcon(getClass().getResource("/com/neatomatic/ui/images/x32/cancel.png")));
            jContentPane = new JPanel();            
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.setBorder(BorderFactory.createEmptyBorder(12, 5, 10, 5));
            jContentPane.add(iconLabel, gridBagConstraints);
            jContentPane.add(titleLabel, gridBagConstraints1);
            jContentPane.add(msgLabel, gridBagConstraints2);
            jContentPane.add(getButtonPanel(), gridBagConstraints3);
            jContentPane.add(getScrollPane(), gridBagConstraints11);
            jContentPane.add(getSeparator(), gridBagConstraints4);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel()
    {
        if(buttonPanel == null)
        {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(2);
            gridLayout.setHgap(0);
            gridLayout.setVgap(5);
            gridLayout.setColumns(1);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(gridLayout);
            buttonPanel.add(getOkButton(), null);
            buttonPanel.add(getDetailsButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes okButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOkButton()
    {
        if(okButton == null)
        {
            okButton = new JButton();
            okButton.setText(i18n.str("ok")); //$NON-NLS-1$
            okButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    dispose();
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes detailsButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getDetailsButton()
    {
        if(detailsButton == null)
        {
            detailsButton = new JButton();
            detailsButton.setText(i18n.str("_details")); //$NON-NLS-1$
            detailsButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    toggleDetails();
                }
            });
        }
        return detailsButton;
    }
    
    private void toggleDetails()
    {
        showDetails(!isDetailsVisible());
    }
    
    public void showDetails(boolean b)
    {
        if(b)
        {
            detailsButton.setText(i18n.str("details_")); //$NON-NLS-1$
            scrollPane.setVisible(true);
            separator.setVisible(false);
        }
        else
        {
            detailsButton.setText(i18n.str("_details")); //$NON-NLS-1$
            scrollPane.setVisible(false);
            separator.setVisible(true);
        }
        
        setResizable(true);        
        pack();
        setResizable(false);
    }
    
    public boolean isDetailsVisible()
    {
        return scrollPane.isVisible();
    }
    
    public void setThrowable(Throwable th)
    {
        String msg = i18n.str("no_message_given"); //$NON-NLS-1$
        if(th.getLocalizedMessage() != null && !th.getLocalizedMessage().equals("")) //$NON-NLS-1$
            msg = th.getLocalizedMessage();
        msgLabel.setText(msg);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        th.printStackTrace(new PrintStream(bout));
        
        String stackTrace = new String(bout.toByteArray());        
        textArea.setText(stackTrace);
        textArea.setCaretPosition(0);
    }
    
    /**
     * @return a JSeparator that gets swapped out with the detail pane.
     */
    private JSeparator getSeparator()
    {
        if(separator == null)
        {
            separator = new JSeparator();
            separator.setPreferredSize(new Dimension(PREFERRED_WIDTH, 3));
        }

        return separator;
    }

    /**
     * This method initializes scrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getScrollPane()
    {
        if(scrollPane == null)
        {
            scrollPane = new JScrollPane();
            scrollPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, 200));
            scrollPane.setViewportView(getTextArea());
        }
        return scrollPane;
    }

    /**
     * This method initializes textArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTextArea()
    {
        if(textArea == null)
        {
            textArea = new JTextArea();
            textArea.setTabSize(2);
            textArea.setEditable(false);
            textArea.setOpaque(false);
        }
        return textArea;
    }
}
