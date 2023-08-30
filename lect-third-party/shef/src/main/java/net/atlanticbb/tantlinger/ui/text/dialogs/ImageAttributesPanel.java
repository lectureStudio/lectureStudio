/*
 * Created on Jan 14, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;

import org.bushe.swing.ImageUtils;

public class ImageAttributesPanel extends HTMLAttributeEditorPanel
{
	private static final long serialVersionUID = -1889961658768622283L;

//    private static final String ALIGNMENTS[] =
//    {
//        "top", "middle", "bottom", "left", "right" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//    };

    private static final String LAST_USED_FOLDER = "last.used.folder";

    private static final String[] FILE_EXT =
    {
        "jpg", "jpeg", "png", "gif"
    };
    
    private JLabel imgUrlLabel = null;
    private JCheckBox altTextCB = null;
    private JCheckBox widthCB = null;
    private JCheckBox heightCB = null;
    private JCheckBox borderCB = null;
    private JSpinner widthField = null;
    private JSpinner heightField = null;
    private JSpinner borderField = null;
    private JCheckBox vSpaceCB = null;
    private JCheckBox hSpaceCB = null;
//    private JCheckBox alignCB = null;
    private JSpinner vSpaceField = null;
    private JSpinner hSpaceField = null;
//    private JComboBox alignCombo = null;
    private JTextField imgUrlField = null;
    private JTextField altTextField = null;
    private JPanel attribPanel = null;

    private JPanel spacerPanel = null;

    /**
     * This is the default constructor
     */
    public ImageAttributesPanel()
    {
        super();
        initialize();
        updateComponentsFromAttribs();
    }    
    
    public void updateComponentsFromAttribs()
    {
    	Dimension size = null;
    	
        if(attribs.containsKey("src")) {
        	String src = attribs.get("src").toString();
        	
        	try {
				size = ImageUtils.getImageDimension(new File(new URL(src).getFile()));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
        	
            imgUrlField.setText(src);
        }
        
        if(attribs.containsKey("alt"))
        {
            altTextCB.setSelected(true);
            altTextField.setEditable(true);
            altTextField.setText(attribs.get("alt").toString());
        }
        else
        {
            altTextCB.setSelected(false);
            altTextField.setEditable(false);
        }
        
        if(attribs.containsKey("width"))
        {
            widthCB.setSelected(true);
            widthField.setEnabled(true);
            try
            {
            	if (size != null) {
            		double value = Double.valueOf(attribs.get("width").toString());
            		value /= size.width;
            		
            		widthField.getModel().setValue(value);
            	}
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            widthCB.setSelected(false);
            widthField.setEnabled(false);
        }
        
        if(attribs.containsKey("height"))
        {
            heightCB.setSelected(true);
            heightField.setEnabled(true);
            try
            {
            	if (size != null) {
            		double value = Double.valueOf(attribs.get("height").toString());
            		value /= size.height;
            		
            		heightField.getModel().setValue(value);
            	}
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            heightCB.setSelected(false);
            heightField.setEnabled(false);
        }
        
        if(attribs.containsKey("hspace"))
        {
            hSpaceCB.setSelected(true);
            hSpaceField.setEnabled(true);
            try
            {
                hSpaceField.getModel().setValue(
                    Integer.valueOf(attribs.get("hspace").toString()));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            hSpaceCB.setSelected(false);
            hSpaceField.setEnabled(false);
        }
        
        if(attribs.containsKey("vspace"))
        {
            vSpaceCB.setSelected(true);
            vSpaceField.setEnabled(true);
            try
            {
                vSpaceField.getModel().setValue(
                    Integer.valueOf(attribs.get("vspace").toString()));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            vSpaceCB.setSelected(false);
            vSpaceField.setEnabled(false);
        }
        
        if(attribs.containsKey("border"))
        {
            borderCB.setSelected(true);
            borderField.setEnabled(true);
            try
            {
                borderField.getModel().setValue(
                    Integer.valueOf(attribs.get("border").toString()));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            borderCB.setSelected(false);
            borderField.setEnabled(false);
        }
    }
    
    
    public void updateAttribsFromComponents()
    {
    	String src = imgUrlField.getText();
    	
		Dimension size = null;

		try {
			size = ImageUtils.getImageDimension(new File(new URL(src).getFile()));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
        	
        attribs.put("src", src);
        
        if(altTextCB.isSelected())
            attribs.put("alt", altTextField.getText());
        else
            attribs.remove("alt");
        
        if(widthCB.isSelected()) {
        	int width = (int)((double)widthField.getModel().getValue() * size.width);
        	attribs.put("width", width);
        }
        else {
            attribs.remove("width");
        }
        
        if(heightCB.isSelected()) {
        	int height = (int)((double)heightField.getModel().getValue() * size.height);
        	attribs.put("height", height);
        }
        else {
            attribs.remove("height");
        }
        
        if(vSpaceCB.isSelected())
            attribs.put("vspace", vSpaceField.getModel().getValue().toString());
        else
            attribs.remove("vspace");
        
        if(hSpaceCB.isSelected())
            attribs.put("hspace", hSpaceField.getModel().getValue().toString());
        else
            attribs.remove("hspace");
        
        if(borderCB.isSelected())
            attribs.put("border", borderField.getModel().getValue().toString());
        else
            attribs.remove("border");
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.gridx = 0;
        gridBagConstraints21.gridwidth = 3;
        gridBagConstraints21.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints21.weighty = 1.0;
        gridBagConstraints21.gridy = 3;
        GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
        gridBagConstraints16.gridx = 0;
        gridBagConstraints16.gridwidth = 3;
        gridBagConstraints16.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints16.gridy = 2;
        GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
        gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints15.gridy = 1;
        gridBagConstraints15.weightx = 1.0;
        gridBagConstraints15.insets = new java.awt.Insets(0,0,10,0);
        gridBagConstraints15.gridwidth = 2;
        gridBagConstraints15.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints15.gridx = 1;
        GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
        gridBagConstraints14.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints14.gridy = 0;
        gridBagConstraints14.weightx = 1.0;
        gridBagConstraints14.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints14.gridwidth = 1;
        gridBagConstraints14.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints14.gridx = 1;
        GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
        gridBagConstraints18.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints18.gridy = 0;
        gridBagConstraints18.weightx = 0.2;
        gridBagConstraints18.insets = new java.awt.Insets(0,5,5,0);
        gridBagConstraints18.gridwidth = 1;
        gridBagConstraints18.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints18.gridx = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,10,5);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints.gridy = 0;
        imgUrlLabel = new JLabel();
        imgUrlLabel.setText(i18n.str("image_url")); //$NON-NLS-1$
        
        JButton browse = new JButton(i18n.str("browse"));
        browse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = showOpenFileDialog(FILE_EXT, null, null);
				if (file != null)
					imgUrlField.setText("file:///" + file.getAbsolutePath());
			}
		});
        
        this.setLayout(new GridBagLayout());
        this.setSize(365, 188);
        this.setOpaque(false);
        this.add(imgUrlLabel, gridBagConstraints);
        this.add(getAltTextCB(), gridBagConstraints1);
        this.add(getImgUrlField(), gridBagConstraints14);
        this.add(browse, gridBagConstraints18);
        this.add(getAltTextField(), gridBagConstraints15);
        this.add(getAttribPanel(), gridBagConstraints16);
        this.add(getSpacerPanel(), gridBagConstraints21);
        
        TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
        popupMan.registerJTextComponent(imgUrlField);
        popupMan.registerJTextComponent(altTextField);
    }

    private File showOpenFileDialog(String[] fileFilters, String currentDir, String selectedFile) {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
    	JFileChooser fc = createFileChooser(fileFilters, currentDir, selectedFile);

		if (currentDir == null) {
            String lastFolderPath = prefs.get(LAST_USED_FOLDER, System.getProperty("user.home"));

			fc.setCurrentDirectory(new File(lastFolderPath));
		}

		int result = fc.showOpenDialog(this);
		File file = fc.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION && isValidFile(file)) {
            prefs.put(LAST_USED_FOLDER, file.getParent());
			return file;
		}

		return null;
	}
    
    private JFileChooser createFileChooser(String[] fileExt, String currentDir, String selectedFile) {
    	JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);

		StringBuffer desc = new StringBuffer();
		desc.append(" ");
		desc.append(i18n.str("image"));
		desc.append(" ( ");
		
		for (int i = 0; i < fileExt.length; i++) {
			desc.append(fileExt[i]);
			if (i < fileExt.length - 1)
				desc.append(", ");
		}
		
		desc.append(" )");
		
		fc.addChoosableFileFilter(new FileNameExtensionFilter(desc.toString(), fileExt));

		if (currentDir != null) {
			fc.setCurrentDirectory(new File(currentDir));
		}

		if (selectedFile != null) {
			fc.setSelectedFile(new File(selectedFile));
		}

		return fc;
	}
    
    private boolean isValidFile(File file) {
		return file.exists() && file.isFile() && file.canRead();
	}
    
    /**
     * This method initializes altTextCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getAltTextCB()
    {
        if(altTextCB == null)
        {
            altTextCB = new JCheckBox();
            altTextCB.setOpaque(false);
            altTextCB.setText(i18n.str("alt_text")); //$NON-NLS-1$
            altTextCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    altTextField.setEditable(altTextCB.isSelected());
                }
            });
        }
        return altTextCB;
    }

    /**
     * This method initializes widthCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getWidthCB()
    {
        if(widthCB == null)
        {
            widthCB = new JCheckBox();
            widthCB.setOpaque(false);
            widthCB.setText(i18n.str("width.factor")); //$NON-NLS-1$
            widthCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    widthField.setEnabled(widthCB.isSelected());
                }
            });
        }
        return widthCB;
    }

    /**
     * This method initializes heightCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getHeightCB()
    {
        if(heightCB == null)
        {
            heightCB = new JCheckBox();
            heightCB.setOpaque(false);
            heightCB.setText(i18n.str("height.factor")); //$NON-NLS-1$
            heightCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    heightField.setEnabled(heightCB.isSelected());
                }
            });
        }
        return heightCB;
    }

    /**
     * This method initializes borderCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getBorderCB()
    {
        if(borderCB == null)
        {
            borderCB = new JCheckBox();
            borderCB.setOpaque(false);
            borderCB.setText(i18n.str("border")); //$NON-NLS-1$
            borderCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    borderField.setEnabled(borderCB.isSelected());
                }
            });
        }
        return borderCB;
    }

    /**
     * This method initializes widthField	
     * 	
     * @return javax.swing.JSpinner
     */
    private JSpinner getWidthField()
    {
        if(widthField == null)
        {
            widthField = new JSpinner(new SpinnerNumberModel(1, 0.1, 999, 0.1));
            widthField.setOpaque(false);
            //widthField.setColumns(4);
        }
        return widthField;
    }

    /**
     * This method initializes heightField	
     * 	
     * @return javax.swing.JSpinner
     */
    private JSpinner getHeightField()
    {
        if(heightField == null)
        {
            heightField = new JSpinner(new SpinnerNumberModel(1, 0.1, 999, 0.1));
            heightField.setOpaque(false);
            //heightField.setColumns(4);
        }
        return heightField;
    }

    /**
     * This method initializes borderField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getBorderField()
    {
        if(borderField == null)
        {
            borderField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
            borderField.setOpaque(false);
            //borderField.setColumns(4);
        }
        return borderField;
    }

    /**
     * This method initializes vSpaceCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getVSpaceCB()
    {
        if(vSpaceCB == null)
        {
            vSpaceCB = new JCheckBox();
            vSpaceCB.setOpaque(false);
            vSpaceCB.setText(i18n.str("vspace")); //$NON-NLS-1$
            vSpaceCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    vSpaceField.setEnabled(vSpaceCB.isSelected());
                }
            });
        }
        return vSpaceCB;
    }

    /**
     * This method initializes hSpaceCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getHSpaceCB()
    {
        if(hSpaceCB == null)
        {
            hSpaceCB = new JCheckBox();
            hSpaceCB.setOpaque(false);
            hSpaceCB.setText(i18n.str("hspace")); //$NON-NLS-1$
            hSpaceCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    hSpaceField.setEnabled(hSpaceCB.isSelected());
                }
            });
        }
        return hSpaceCB;
    }

    /**
     * This method initializes alignCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
//    private JCheckBox getAlignCB()
//    {
//        if(alignCB == null)
//        {
//            alignCB = new JCheckBox();
//            alignCB.setOpaque(false);
//            alignCB.setText(i18n.str("align")); //$NON-NLS-1$
//            alignCB.addItemListener(new java.awt.event.ItemListener()
//            {
//                public void itemStateChanged(java.awt.event.ItemEvent e)
//                {
//                    alignCombo.setEnabled(alignCB.isSelected());
//                }
//            });
//        }
//        return alignCB;
//    }

    /**
     * This method initializes vSpaceField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getVSpaceField()
    {
        if(vSpaceField == null)
        {
            vSpaceField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            vSpaceField.setOpaque(false);
            //vSpaceField.setColumns(4);
        }
        return vSpaceField;
    }

    /**
     * This method initializes hSpaceField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getHSpaceField()
    {
        if(hSpaceField == null)
        {
            hSpaceField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            hSpaceField.setOpaque(false);
            //hSpaceField.setColumns(4);
        }
        return hSpaceField;
    }

    /**
     * This method initializes alignCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
//    private JComboBox getAlignCombo()
//    {
//        if(alignCombo == null)
//        {
//            alignCombo = new JComboBox(ALIGNMENTS);
//            
//        }
//        return alignCombo;
//    }

    /**
     * This method initializes imgUrlField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getImgUrlField()
    {
        if(imgUrlField == null)
        {
            imgUrlField = new JTextField();
        }
        return imgUrlField;
    }

    /**
     * This method initializes altTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getAltTextField()
    {
        if(altTextField == null)
        {
            altTextField = new JTextField();
        }
        return altTextField;
    }

    /**
     * This method initializes attribPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getAttribPanel()
    {
        if(attribPanel == null)
        {
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints13.insets = new Insets(0, 0, 10, 0);
            gridBagConstraints13.gridx = 3;
            gridBagConstraints13.gridy = 2;
            gridBagConstraints13.weightx = 1.0;
            gridBagConstraints13.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints12.insets = new java.awt.Insets(0,0,10,0);
            gridBagConstraints12.gridx = 3;
            gridBagConstraints12.gridy = 1;
            gridBagConstraints12.weightx = 0.0;
            gridBagConstraints12.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints11.insets = new java.awt.Insets(0,0,5,0);
            gridBagConstraints11.gridx = 3;
            gridBagConstraints11.gridy = 0;
            gridBagConstraints11.weightx = 0.0;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints10.gridx = 2;
            gridBagConstraints10.gridy = 2;
            gridBagConstraints10.insets = new java.awt.Insets(0,0,10,5);
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints9.gridx = 2;
            gridBagConstraints9.gridy = 1;
            gridBagConstraints9.insets = new java.awt.Insets(0,0,10,5);
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints8.gridx = 2;
            gridBagConstraints8.gridy = 0;
            gridBagConstraints8.insets = new java.awt.Insets(0,0,5,5);
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints7.insets = new java.awt.Insets(0,0,10,10);
            gridBagConstraints7.gridx = 1;
            gridBagConstraints7.gridy = 2;
            gridBagConstraints7.weightx = 0.0;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints6.insets = new java.awt.Insets(0,0,10,10);
            gridBagConstraints6.gridx = 1;
            gridBagConstraints6.gridy = 1;
            gridBagConstraints6.weightx = 0.0;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.anchor = GridBagConstraints.WEST;
            gridBagConstraints5.insets = new Insets(0, 0, 5, 10);
            gridBagConstraints5.gridx = 1;
            gridBagConstraints5.gridy = 0;
            gridBagConstraints5.weightx = 0.0;
            gridBagConstraints5.fill = GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridy = 2;
            gridBagConstraints4.insets = new java.awt.Insets(0,0,10,5);
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 1;
            gridBagConstraints3.insets = new java.awt.Insets(0,0,10,5);
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 0;
            gridBagConstraints2.insets = new java.awt.Insets(0,0,5,5);
            attribPanel = new JPanel();
            attribPanel.setOpaque(false);
            attribPanel.setLayout(new GridBagLayout());
            attribPanel.add(getWidthCB(), gridBagConstraints2);
            attribPanel.add(getHeightCB(), gridBagConstraints3);
            attribPanel.add(getBorderCB(), gridBagConstraints4);
            attribPanel.add(getWidthField(), gridBagConstraints5);
            attribPanel.add(getHeightField(), gridBagConstraints6);
            attribPanel.add(getBorderField(), gridBagConstraints7);
            attribPanel.add(getVSpaceCB(), gridBagConstraints8);
            attribPanel.add(getHSpaceCB(), gridBagConstraints9);
//            attribPanel.add(getAlignCB(), gridBagConstraints10);
            attribPanel.add(getVSpaceField(), gridBagConstraints11);
            attribPanel.add(getHSpaceField(), gridBagConstraints12);
//            attribPanel.add(getAlignCombo(), gridBagConstraints13);
        }
        return attribPanel;
    }

    /**
     * This method initializes spacerPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getSpacerPanel()
    {
        if(spacerPanel == null)
        {
            spacerPanel = new JPanel();
            spacerPanel.setOpaque(false);
        }
        return spacerPanel;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
