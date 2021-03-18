/*
 * Created on Dec 24, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.OptionDialog;

public class NewTableDialog extends OptionDialog
{
	private static final long serialVersionUID = 4149091901094515326L;

	private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    private LayoutPanel layoutPanel = new LayoutPanel();
    private TableAttributesPanel propsPanel;
    
    public NewTableDialog(Frame parent)
    {
        super(parent, i18n.str("new_table"));         //$NON-NLS-1$ //$NON-NLS-2$
        init();
    }
    
    public NewTableDialog(Dialog parent)
    {
        super(parent, i18n.str("new_table"));         //$NON-NLS-1$ //$NON-NLS-2$
        init();
    }
    
    private void init()
    {
        //default attribs
        Hashtable ht = new Hashtable();
        ht.put("border", "1"); //$NON-NLS-1$ //$NON-NLS-2$
        ht.put("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
        propsPanel = new TableAttributesPanel();
        propsPanel.setAttributes(ht);
        
        propsPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(i18n.str("properties")),  //$NON-NLS-1$
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(layoutPanel, BorderLayout.NORTH);
        mainPanel.add(propsPanel, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(mainPanel);
        setSize(new Dimension(330, 380));
        setResizable(false);        
    }
    
    public String getHTML()
    {
        String html = "<table"; //$NON-NLS-1$
        Map attribs = propsPanel.getAttributes();
        
        for(Iterator e = attribs.keySet().iterator(); e.hasNext();)
        {
            String key = e.next().toString();
            String val = attribs.get(key).toString();
            html += ' ' + key + "=\"" + val + "\"";             //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        html += ">\n"; //$NON-NLS-1$
        
        int numRows = layoutPanel.getRows();
        int numCols = layoutPanel.getColumns();
        for(int row = 1; row <= numRows; row++)
        {
            html += "<tr>\n"; //$NON-NLS-1$
            for(int col = 1; col <= numCols; col++)
            {
                html += "\t<td>\n</td>\n"; //$NON-NLS-1$
            }
            html += "</tr>\n"; //$NON-NLS-1$
        }
        
        return html + "</table>"; //$NON-NLS-1$
    }
    
    private class LayoutPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private JLabel rowsLabel = null;
        private JLabel colsLabel = null;
        private int iRows, iCols;
        private JSpinner rowsField = null;
        private JSpinner colsField = null;
        
        /**
         * This is the default constructor
         */
        public LayoutPanel()
        {
            this(1, 1);
        }
        
        public LayoutPanel(int r, int c)
        {
            super();
            iRows = (r > 0) ? r : 1;
            iCols = (c > 0) ? c : 1;
            initialize();
        }
        
        public int getRows()
        {
            return Integer.parseInt(rowsField.getModel().getValue().toString());
        }
        
        public int getColumns()
        {
            return Integer.parseInt(colsField.getModel().getValue().toString());
        }

        /**
         * This method initializes this
         * 
         * @return void
         */
        private void initialize()
        {
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints7.gridy = 0;
            gridBagConstraints7.weightx = 1.0;
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints7.gridx = 3;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints6.gridy = 0;
            gridBagConstraints6.weightx = 0.0;
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints6.insets = new java.awt.Insets(0,0,0,15);
            gridBagConstraints6.gridx = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 2;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.insets = new java.awt.Insets(0,0,0,5);
            gridBagConstraints1.gridy = 0;
            colsLabel = new JLabel();
            colsLabel.setText(i18n.str("columns")); //$NON-NLS-1$
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0,0,0,5);
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.gridy = 0;
            rowsLabel = new JLabel();
            rowsLabel.setText(i18n.str("rows")); //$NON-NLS-1$
            this.setLayout(new GridBagLayout());
            this.setSize(330, 60);
            this.setPreferredSize(new java.awt.Dimension(330,60));
            //this.setMaximumSize(this.getPreferredSize());
            this.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(null, i18n.str("layout"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null), javax.swing.BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
            this.add(rowsLabel, gridBagConstraints);
            this.add(colsLabel, gridBagConstraints1);
            this.add(getRowsField(), gridBagConstraints6);
            this.add(getColsField(), gridBagConstraints7);
        }

        /**
         * This method initializes rowsField    
         *  
         * @return javax.swing.JSpinner 
         */
        private JSpinner getRowsField()
        {
            if(rowsField == null)
            {
                rowsField = new JSpinner(new SpinnerNumberModel(iRows, 1, 999, 1));            
            }
            return rowsField;
        }

        /**
         * This method initializes colsField    
         *  
         * @return javax.swing.JSpinner
         */
        private JSpinner getColsField()
        {
            if(colsField == null)
            {
                colsField = new JSpinner(new SpinnerNumberModel(iCols, 1, 999, 1));     
            }
            return colsField;
        }
    }
 
}
