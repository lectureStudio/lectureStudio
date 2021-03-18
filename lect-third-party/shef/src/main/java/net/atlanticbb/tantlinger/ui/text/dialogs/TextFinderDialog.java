/*
 *  =================================================== 
 *  This program contains code from the book "Swing" 
 *  2nd Edition by Matthew Robinson and Pavel Vorobiev 
 *  http://www.spindoczine.com/sbe 
 *  =================================================== 
 * 
 * This class (Mostly) comes from the manning "Swing" book
 * by Matthew Robinson and Pavel Vorobiev.  I'd come across it
 * and only after i started writing my own finder/replacer..
 * So, rather than waste time reinventing the wheel,
 * I modified it to fit my needs.
 * -BT
 * 
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;


/**
 * A find and replace dialog for JTextComponents 
 * 
 */
public class TextFinderDialog extends JDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    public static final char[] WORD_SEPARATORS = {' ', '\t', '\n',
		'\r', '\f', '.', ',', ':', '-', '(', ')', '[', ']', '{',
		'}', '<', '>', '/', '|', '\\', '\'', '\"'};
	
	public static final int FIND = 0;
	public static final int REPLACE = 1;
    
	protected Frame owner;
	protected JTextComponent monitor;
	protected JTabbedPane tb;
	protected JTextField txtFind1;
	protected JTextField txtFind2;
	protected Document docFind;
	protected Document docReplace;
	protected ButtonModel modelWord;
	protected ButtonModel modelCase;
	protected ButtonModel modelUp;
	protected ButtonModel modelDown;
	
	//private TextEditPopupManager popupManager = new TextEditPopupManager();
    private TextEditPopupManager popupManager = TextEditPopupManager.getInstance();

	//protected int searchIndex = -1;
	protected boolean searchUp = false;
	protected String  searchData;
    
    private static final String TITLE = i18n.str("find_and_replace"); //$NON-NLS-1$
    //private JTextComponent textComp;

    public TextFinderDialog(Frame owner, JTextComponent tc, int index)
    {
        super(owner, TITLE, false);
        init(tc, index);        
    }
    
	public TextFinderDialog(Dialog owner, JTextComponent tc, int index)
	{
		super(owner, TITLE, false);
		init(tc, index);        
	}
	
	private void init(JTextComponent tc, int index)
	{
		setJTextComponent(tc);
		
		tb = new JTabbedPane();

		// "Find" panel
		JPanel p1 = new JPanel(new BorderLayout());

		JPanel pc1 = new JPanel(new BorderLayout());

		JPanel pf = new JPanel();
		pf.setLayout(new DialogLayout(20, 5));
		pf.setBorder(new EmptyBorder(8, 5, 8, 0));
		pf.add(new JLabel(i18n.str("find_what")));
        
		txtFind1 = new JTextField();
		docFind = txtFind1.getDocument();
		pf.add(txtFind1);
		pc1.add(pf, BorderLayout.CENTER);
		popupManager.registerJTextComponent(txtFind1);

		JPanel po = new JPanel(new GridLayout(2, 2, 8, 2));
		po.setBorder(new TitledBorder(new EtchedBorder(), i18n.str("options")));

		JCheckBox chkWord = new JCheckBox(i18n.str("whole_words_only"));
		chkWord.setMnemonic('w');
		modelWord = chkWord.getModel();
		po.add(chkWord);

		ButtonGroup bg = new ButtonGroup();
		JRadioButton rdUp = new JRadioButton(i18n.str("search_up"));
		rdUp.setMnemonic('u');
		modelUp = rdUp.getModel();
		bg.add(rdUp);
		po.add(rdUp);

		JCheckBox chkCase = new JCheckBox(i18n.str("match_case"));
		chkCase.setMnemonic('c');
		modelCase = chkCase.getModel();
		po.add(chkCase);

		JRadioButton rdDown = new JRadioButton(i18n.str("search_down"), true);
		rdDown.setMnemonic('d');
		modelDown = rdDown.getModel();
		bg.add(rdDown);
		po.add(rdDown);
		pc1.add(po, BorderLayout.SOUTH);

		p1.add(pc1, BorderLayout.CENTER);

		JPanel p01 = new JPanel(new FlowLayout());
		JPanel p = new JPanel(new GridLayout(2, 1, 2, 8));

		ActionListener findAction = new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				findNext(false, true);
			}
		};
    	
		JButton btFind = new JButton(i18n.str("find_next"));
		btFind.addActionListener(findAction);
		btFind.setMnemonic('f');
		p.add(btFind);

		ActionListener closeAction = new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				setVisible(false);
                //txtFind1 = null;
                //txtFind2 = null;
			}
		};
    	
		JButton btClose = new JButton(i18n.str("close"));
		btClose.addActionListener(closeAction);
		btClose.setDefaultCapable(true);
		p.add(btClose);
		p01.add(p);
		p1.add(p01, BorderLayout.EAST);

		tb.addTab(i18n.str("find"), p1);

		// "Replace" panel
		JPanel p2 = new JPanel(new BorderLayout());

		JPanel pc2 = new JPanel(new BorderLayout());

		JPanel pc = new JPanel();
		pc.setLayout(new DialogLayout(20, 5));
		pc.setBorder(new EmptyBorder(8, 5, 8, 0));
        
		
		pc.add(new JLabel(i18n.str("find_what")));
		txtFind2 = new JTextField();
		txtFind2.setDocument(docFind);
		pc.add(txtFind2);
		popupManager.registerJTextComponent(txtFind2);

		pc.add(new JLabel(i18n.str("replace")));
		JTextField txtReplace = new JTextField();
		docReplace = txtReplace.getDocument();
		pc.add(txtReplace);
		pc2.add(pc, BorderLayout.CENTER);
		popupManager.registerJTextComponent(txtReplace);

		po = new JPanel(new GridLayout(2, 2, 8, 2));
		po.setBorder(new TitledBorder(new EtchedBorder(), i18n.str("options")));

		chkWord = new JCheckBox(i18n.str("whole_words_only"));
		chkWord.setMnemonic('w');
		chkWord.setModel(modelWord);
		po.add(chkWord);

		bg = new ButtonGroup();
		rdUp = new JRadioButton(i18n.str("search_up"));
		rdUp.setMnemonic('u');
		rdUp.setModel(modelUp);
		bg.add(rdUp);
		po.add(rdUp);

		chkCase = new JCheckBox(i18n.str("match_case"));
		chkCase.setMnemonic('c');
		chkCase.setModel(modelCase);
		po.add(chkCase);

		rdDown = new JRadioButton(i18n.str("search_down"), true);
		rdDown.setMnemonic('d');
		rdDown.setModel(modelDown);
		bg.add(rdDown);
		po.add(rdDown);
		pc2.add(po, BorderLayout.SOUTH);

		p2.add(pc2, BorderLayout.CENTER);

		JPanel p02 = new JPanel(new FlowLayout());
		p = new JPanel(new GridLayout(3, 1, 2, 8));

		ActionListener replaceAction = new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				findNext(true, true);
			}
		};
		JButton btReplace = new JButton(i18n.str("replace"));
		btReplace.addActionListener(replaceAction);
		btReplace.setMnemonic('r');
		p.add(btReplace);

		ActionListener replaceAllAction = new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				int counter = 0;
				while (true) 
				{
					int result = findNext(true, false);
					if (result < 0)    // error
						return;
					else if (result == 0)    // no more
						break;
					counter++;
				}
				JOptionPane.showMessageDialog(owner, 
					counter+ " " + i18n.str("replacements_prompt"), "Info",
					JOptionPane.INFORMATION_MESSAGE);
			}
		};
		JButton btReplaceAll = new JButton(i18n.str("replace_all"));
		btReplaceAll.addActionListener(replaceAllAction);
		btReplaceAll.setMnemonic('a');
		p.add(btReplaceAll);

		btClose = new JButton(i18n.str("close"));
		btClose.addActionListener(closeAction);
		btClose.setDefaultCapable(true);
		p.add(btClose);
		p02.add(p);
		p2.add(p02, BorderLayout.EAST);
        
		// Make button columns the same size
		p01.setPreferredSize(p02.getPreferredSize());

		tb.addTab(i18n.str("replace"), p2);

		tb.setSelectedIndex(index);

		getContentPane().add(tb, BorderLayout.CENTER);
	
		WindowListener flst = new WindowAdapter() 
		{ 
			public void windowActivated(WindowEvent e) 
			{
				//searchIndex = -1;
				if (tb.getSelectedIndex()==0)
                    if(!txtFind1.hasFocus())
                        txtFind1.requestFocusInWindow();
				else
                    if(!txtFind2.hasFocus())
                        txtFind2.requestFocusInWindow();
					
			}

			public void windowDeactivated(WindowEvent e) 
			{
				searchData = null;
			}
		};
		addWindowListener(flst);

		pack();
		setResizable(false);
	}    

    
	public void setJTextComponent(JTextComponent tc)
	{
		monitor = tc;
        if(!monitor.hasFocus())
            monitor.requestFocusInWindow();	
	}
  	
	public JTextComponent getJTextComponent()
	{
		return monitor;	
	}

	public void setSelectedIndex(int index) 
	{    	
		tb.setSelectedIndex(index);
		//setVisible(true);
		//searchIndex = -1;
	}
  	
	public int getSelectedIndex()
	{
		return tb.getSelectedIndex();	
	}
  	
	public void show(int index)
	{
		setSelectedIndex(index);
		setLocationRelativeTo(owner);
		setVisible(true);
        if(!monitor.hasFocus())
            monitor.requestFocusInWindow(); 	
	}  	
  	
	public int findNext(boolean doReplace, boolean showWarnings)
	{
		int pos = monitor.getCaretPosition();
  		
		String key = "";
		try { key = docFind.getText(0, docFind.getLength()); }
		catch(BadLocationException ex){}
    	
		if(key.length()==0) 
		{
			warning(i18n.str("no_target_prompt"));
			return -1;
		}
    	
		if(modelWord.isSelected()) 
		{
			for(int k=0; k<WORD_SEPARATORS.length; k++) 
			{
				if(key.indexOf(WORD_SEPARATORS[k]) >= 0) 
				{
					warning(i18n.str("illegal_character_prompt") +
						" \'"+WORD_SEPARATORS[k]+"\'");
					return -1;
				}
			}
		}
    	
		String replacement = "";
		if(doReplace) 
		{
			try 
			{
				replacement = docReplace.getText(0, docReplace.getLength());
			} 
			catch (BadLocationException ex) {}
		}
    	
		if(modelUp.isSelected() != searchUp)    	
			searchUp = modelUp.isSelected();
      	
      	String searchData = "";     	
		try{
		    searchData = monitor.getDocument().getText(
		        0, monitor.getDocument().getLength());		
		}
		catch(Exception ex)
		{
		    ex.printStackTrace();
		    return -1;
		}
		
		if(!modelCase.isSelected()) 
		{
			searchData = searchData.toLowerCase();
			key = key.toLowerCase();
		}
    	
		int index;
		while(true)
		{
			if(!searchUp)
				index = searchData.indexOf(key, pos);
			else
				index = searchData.lastIndexOf(key, pos - 1);
      	
			if(index < 0 || index >= searchData.length())
			{
				if(showWarnings)
					warning(i18n.str("text_not_found"));
				return 0;
			}
      	
			if(modelWord.isSelected())
			{
				boolean s1 = index > 0;
				boolean b1 = s1 && !isSeparator(searchData.charAt(index - 1));
				boolean s2 = (index + key.length()) < searchData.length();
				boolean b2 = s2 && !isSeparator(searchData.charAt(index + key.length()));
        		
				if(b1 || b2)
				{
					if(!searchUp && s2)
					{
						pos = index + key.length();
						continue;
					}
        			
					if(searchUp && s1)
					{
						pos = index;
						continue;
					}
        			
					if(showWarnings)
						warning(i18n.str("text_not_found"));
					return 0;
				}        		        		
			}
      		
			break;
		}    	
    	
		if(doReplace) 
		{
			setSelection(index, index + key.length(), searchUp);
			monitor.replaceSelection(replacement);
			setSelection(index, index+replacement.length(), searchUp);
      		
		}
		else
			setSelection(index, index + key.length(), searchUp);
      		
		return 1;      	   	    	
	}
  	
	public void setSelection(int xStart, int xFinish, boolean moveUp) 
	{
		if(moveUp) 
		{
			monitor.setCaretPosition(xFinish);
			monitor.moveCaretPosition(xStart);
		}
		else
		{
			monitor.setCaretPosition(xStart);
			monitor.moveCaretPosition(xFinish);
		}
	}
  	
	protected boolean isSeparator(char ch) 
	{
		for(int k = 0; k < WORD_SEPARATORS.length; k++)
			if(ch == WORD_SEPARATORS[k])
				return true;
		return false;
	}

	protected void warning(String message) 
	{
		JOptionPane.showMessageDialog(owner, 
			message, TITLE, JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	
	
    
	private class DialogLayout implements LayoutManager
	{
		protected static final int COMP_TWO_COL = 0;
		protected static final int COMP_BIG = 1;
		protected static final int COMP_BUTTON = 2;

		protected int m_divider = -1;
		protected int m_hGap = 10;
		protected int m_vGap = 5;
		protected Vector m_v = new Vector();

		public DialogLayout() {}

		public DialogLayout(int hGap, int vGap)
		{
			m_hGap = hGap;
			m_vGap = vGap;
		}

		public void addLayoutComponent(String name, Component comp) {}

		public void removeLayoutComponent(Component comp) {}

		public Dimension preferredLayoutSize(Container parent)
		{
			m_v.removeAllElements();
			int w = 0;
			int h = 0;
			int type = -1;

			for (int k=0 ; k<parent.getComponentCount(); k++)
			{
				Component comp = parent.getComponent(k);
				int newType = getLayoutType(comp);
				if (k == 0)
					type = newType;

				if (type != newType)
				{
					Dimension d = preferredLayoutSize(m_v, type);
					w = Math.max(w, d.width);
					h += d.height + m_vGap;
					m_v.removeAllElements();
					type = newType;
				}

				m_v.addElement(comp);
			}

			Dimension d = preferredLayoutSize(m_v, type);
			w = Math.max(w, d.width);
			h += d.height + m_vGap;

			h -= m_vGap;

			Insets insets = parent.getInsets();
			return new Dimension(w+insets.left+insets.right,
				h+insets.top+insets.bottom);
		}

		protected Dimension preferredLayoutSize(Vector v, int type)
		{
			int w = 0;
			int h = 0;
			switch (type)
			{
			case COMP_TWO_COL:
				int divider = getDivider(v);
				for (int k=1 ; k<v.size(); k+=2)
				{
					Component comp = (Component)v.elementAt(k);
					Dimension d = comp.getPreferredSize();
					w = Math.max(w, d.width);
					h += d.height + m_vGap;
				}
				h -= m_vGap;
				return new Dimension(divider+w, h);

			case COMP_BIG:
				for (int k=0 ; k<v.size(); k++)
				{
					Component comp = (Component)v.elementAt(k);
					Dimension d = comp.getPreferredSize();
					w = Math.max(w, d.width);
					h += d.height + m_vGap;
				}
				h -= m_vGap;
				return new Dimension(w, h);

			case COMP_BUTTON:
				Dimension d = getMaxDimension(v);
				w = d.width + m_hGap;
				h = d.height;
				return new Dimension(w*v.size()-m_hGap, h);
			}
			throw new IllegalArgumentException("Illegal type "+type); //$NON-NLS-1$
		}

		public Dimension minimumLayoutSize(Container parent)
		{
			return preferredLayoutSize(parent);
		}

		public void layoutContainer(Container parent)
		{
			m_v.removeAllElements();
			int type = -1;

			Insets insets = parent.getInsets();
			int w = parent.getWidth() - insets.left - insets.right;
			int x = insets.left;
			int y = insets.top;

			for (int k=0 ; k<parent.getComponentCount(); k++)
			{
				Component comp = parent.getComponent(k);
				int newType = getLayoutType(comp);
				if (k == 0)
					type = newType;

				if (type != newType)
				{
					y = layoutComponents(m_v, type, x, y, w);
					m_v.removeAllElements();
					type = newType;
				}

				m_v.addElement(comp);
			}

			y = layoutComponents(m_v, type, x, y, w);
			m_v.removeAllElements();
		}

		protected int layoutComponents(Vector v, int type, int x, int y, int w)
		{
			switch (type)
			{
			case COMP_TWO_COL:
				int divider = getDivider(v);
				for (int k=1 ; k<v.size(); k+=2)
				{
					Component comp1 = (Component)v.elementAt(k-1);
					Component comp2 = (Component)v.elementAt(k);
					Dimension d = comp2.getPreferredSize();

					comp1.setBounds(x, y, divider, d.height);
					comp2.setBounds(x+divider, y, w-divider, d.height);
					y += d.height + m_vGap;
				}
				//y -= m_vGap;
				return y;

			case COMP_BIG:
				for (int k=0 ; k<v.size(); k++)
				{
					Component comp = (Component)v.elementAt(k);
					Dimension d = comp.getPreferredSize();
					comp.setBounds(x, y, w, d.height);
					y += d.height + m_vGap;
				}
				//y -= m_vGap;
				return y;

			case COMP_BUTTON:
				Dimension d = getMaxDimension(v);
				int ww = d.width*v.size() + m_hGap*(v.size()-1);
				int xx = x + Math.max(0, (w - ww)/2);
				for (int k=0 ; k<v.size(); k++)
				{
					Component comp = (Component)v.elementAt(k);
					comp.setBounds(xx, y, d.width, d.height);
					xx += d.width + m_hGap;
				}
				return y + d.height;
			}
			throw new IllegalArgumentException("Illegal type "+type); //$NON-NLS-1$
		}

		public int getHGap()
		{
			return m_hGap;
		}

		public int getVGap()
		{
			return m_vGap;
		}

		public void setDivider(int divider)
		{
			if (divider > 0)
				m_divider = divider;
		}

		public int getDivider()
		{
			return m_divider;
		}

		protected int getDivider(Vector v)
		{
			if (m_divider > 0)
				return m_divider;

			int divider = 0;
			for (int k=0 ; k<v.size(); k+=2)
			{
				Component comp = (Component)v.elementAt(k);
				Dimension d = comp.getPreferredSize();
				divider = Math.max(divider, d.width);
			}
			divider += m_hGap;
			return divider;
		}

		protected Dimension getMaxDimension(Vector v)
		{
			int w = 0;
			int h = 0;
			for (int k=0 ; k<v.size(); k++)
			{
				Component comp = (Component)v.elementAt(k);
				Dimension d = comp.getPreferredSize();
				w = Math.max(w, d.width);
				h = Math.max(h, d.height);
			}
			return new Dimension(w, h);
		}

		protected int getLayoutType(Component comp)
		{
			if (comp instanceof AbstractButton)
				return COMP_BUTTON;
			else if (comp instanceof JPanel ||
				comp instanceof JScrollPane ||
				comp instanceof JTabbedPane)
				return COMP_BIG;
			else
				return COMP_TWO_COL;
		}

	}
}
