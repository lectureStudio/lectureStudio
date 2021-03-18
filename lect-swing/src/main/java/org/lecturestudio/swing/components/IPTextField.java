/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.swing.components;

import static java.util.Objects.isNull;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;

import org.lecturestudio.swing.filter.IntFilter;

public class IPTextField extends JTextField implements Serializable {

	private static final long serialVersionUID = -2092720946775232323L;



	public enum IPVersion {
		IPV4,
		IPV6
	}



	private static final char IPV4_SEPARATOR = '.';

	private static final char IPV6_SEPARATOR = ':';

	private static final int IPV4_FIELDS = 4;

	private static final int IPV6_FIELDS = 8;

	private int maxFieldLength;

	private JTextField[] components;

	private JComponent finalFocusComponent;

	private char separator;

	private IPVersion version;


	/**
	 * Constructs the IPV4 Field.
	 */
	public IPTextField() {
		this(IPVersion.IPV4, null);
	}

	/**
	 * Constructs the IPTextField for the specified version.
	 *
	 * @param version the IP version.
	 */
	public IPTextField(IPVersion version) {
		this(version, null);
	}

	/**
	 * Constructs the IPTextField for the specified version and IP address.
	 *
	 * @param version the IP version.
	 * @param address the IP address.
	 */
	public IPTextField(IPVersion version, InetAddress address) {
		super(new PlainDocument() {

			@Override
			public String getText(int offset, int length) {
				return "";
			}

			@Override
			public void getText(int offset, int length, Segment txt) {

			}
		}, null, 0);

		this.version = version;

		setFocusable(false);
		initComponent();

		if (address != null) {
			setInetAddress(address);
		}
	}

	@Override
	public String getText() {
		return getIPAddress();
	}

	@Override
	public void setText(String text) {
		text = text.strip();

		if (isNull(text) || text.isEmpty()) {
			return;
		}

		try {
			InetAddress ip = (version == IPVersion.IPV4) ?
					Inet4Address.getByName(text) :
					Inet6Address.getByName(text);
			setInetAddress(ip);

			updateModel();
		}
		catch (UnknownHostException e) {
			throw new RuntimeException("IP: " + text, e);
		}
	}

	public void setFinalFocusComponent(JComponent comp) {
		this.finalFocusComponent = comp;
		this.finalFocusComponent.requestFocus();
	}

	/**
	 * Returns the IP version.
	 *
	 * @return the IP version.
	 */
	public IPVersion getVersion() {
		return version;
	}

	/**
	 * Sets the IP version.
	 *
	 * @param version the new IP version.
	 */
	public void setVersion(IPVersion version) {
		this.version = version;

		removeAll();
		initComponent();
	}

	/**
	 * Gets the IP address as string.
	 *
	 * @return the IP address string.
	 */
	public String getIPAddress() {
		StringBuilder sb = new StringBuilder();

		for (JTextField component : components) {
			sb.append((component).getText());
			sb.append(separator);
		}
		// Remove the last separator.
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	/**
	 * Sets the new IP address.
	 *
	 * @param inetAddress the new @{code InetAddress}.
	 */
	public void setInetAddress(InetAddress inetAddress) {
		if (inetAddress == null) {
			return;
		}
		if (inetAddress instanceof Inet4Address && version != IPVersion.IPV4) {
			throw new IllegalArgumentException("The IP version of this component is IPV6");
		}
		if (inetAddress instanceof Inet6Address && version != IPVersion.IPV6) {
			throw new IllegalArgumentException("The IP version of this component is IPV4");
		}

		String ip = inetAddress.getHostAddress();
		String regex = separator == IPV4_SEPARATOR ? "\\" : "";
		regex += String.valueOf(separator);
		String[] octets = ip.split(regex);

		for (int i = 0; i < octets.length; i++) {
			components[i].setText(octets[i]);
		}
	}

	public InetAddress getInetAddress() {
		InetAddress address;

		try {
			address = Inet4Address.getByName(getIPAddress());
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		return address;
	}

	/**
	 * Initializes the component.
	 */
	private void initComponent() {
		setPreferredSize(new Dimension(150, this.getPreferredSize().height));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		if (version == IPVersion.IPV4) {
			separator = IPV4_SEPARATOR;
			maxFieldLength = 3;
		}
		else {
			separator = IPV6_SEPARATOR;
			maxFieldLength = 4;
		}

		components = createInputFields();
		JTextField[] separatorComponents = createFieldSeparators(components.length - 1);

		for (int i = 0, j = 0; i < components.length; i++, j++) {
			add(components[i]);

			if (i != components.length - 1) {
				add(separatorComponents[j]);
			}
		}
	}

	private void updateModel() {
		try {
			getDocument().insertString(0, getIPAddress(), null);
		}
		catch (BadLocationException e) {
			throw new RuntimeException("Update IP model failed", e);
		}
	}

	private JTextField[] createInputFields() {
		DocumentListener listener = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateModel();
			}
		};

		int count = (version == IPVersion.IPV4) ? IPV4_FIELDS : IPV6_FIELDS;

		JTextField[] fields = new JTextField[count];

		for (int i = 0; i < fields.length; i++) {
			fields[i] = new TextComponent(this);
			fields[i].getDocument().addDocumentListener(listener);
		}

		return fields;
	}

	private JTextField[] createFieldSeparators(int length) {
		JTextField[] c = new JTextField[length];

		for (int i = 0; i < c.length; i++) {
			c[i] = createFieldSeparator();
		}

		return c;
	}

	private JTextField createFieldSeparator() {
		Dimension size = new Dimension(5, getPreferredSize().height);

		JTextField sepField = new JTextField();
		sepField.setUI(new BasicTextFieldUI());
		sepField.setBorder(BorderFactory.createEmptyBorder());
		sepField.setOpaque(false);
		sepField.setFocusable(false);
		sepField.setEditable(false);
		sepField.setBackground(getBackground());
		sepField.setForeground(getForeground());
		sepField.setText(String.valueOf(separator));
		sepField.setPreferredSize(size);
		sepField.setMaximumSize(size);
		sepField.setMinimumSize(size);
		sepField.setSize(size);

		return sepField;
	}



	private class TextComponent extends JFormattedTextField implements FocusListener, KeyListener {

		private static final long serialVersionUID = 1270246585019164909L;

		private final IPTextField ipTextField;


		private TextComponent(IPTextField ipField) {
			setUI(new BasicTextFieldUI());
			setBorder(BorderFactory.createEmptyBorder());
			setColumns(maxFieldLength);
			setOpaque(false);
			setHorizontalAlignment(CENTER);
			setPreferredSize(new Dimension(18, IPTextField.this.getPreferredSize().height));
			addFocusListener(this);
			addKeyListener(this);
			setInputVerifier(version == IPVersion.IPV4 ? new IPV4Verifier() :
					new IPV6Verifier());

			PlainDocument doc = (PlainDocument) getDocument();
			doc.setDocumentFilter(new IntFilter(maxFieldLength));

			this.ipTextField = ipField;
		}

		@Override
		public void transferFocus() {
			if (components[components.length - 1].equals(this) && finalFocusComponent != null) {
				finalFocusComponent.requestFocus();
			}
			else {
				super.transferFocus();
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
			selectText(((JTextField) e.getComponent()));
			ipTextField.repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
			getInputVerifier().verify(this);
			ipTextField.repaint();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			JTextField source = (JTextField) e.getSource();
			String text = source.getText();

			if (e.getKeyChar() == separator) {
				e.setKeyChar('\0');
				if (!components[components.length - 1].equals(this) && !text.isEmpty()) {
					transferFocus();
					return;
				}
			}
			if (text.isEmpty() && e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
				if (!components[0].equals(this)) {
					transferFocusBackward();
					return;
				}
			}

			switch (version) {
				case IPV4:
					if (!Character.isDigit(e.getKeyChar())) {
						e.setKeyChar('\0');
					}
					break;

				case IPV6:
					try {
						Integer.parseInt(String.valueOf(e.getKeyChar()), 16);
					}
					catch (Exception ex) {
						e.setKeyChar('\0');
					}
					break;
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
		}

		private void selectText(JTextField jTextField) {
			String text = jTextField.getText();

			if (text.isEmpty()) {
				return;
			}

			jTextField.setSelectionStart(0);
			jTextField.setSelectionEnd(text.length());
		}
	}



	private static class IPV4Verifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
			JTextField inputTxt = (JTextField) input;
			String text = inputTxt.getText();

			// Allow empty octets.
			if (text.isEmpty()) {
				return true;
			}

			try {
				if (Integer.parseInt(text) > 255) {
					inputTxt.setText(String.valueOf(255));
				}
				return true;
			}
			catch (Exception ex) {
				return false;
			}
		}
	}



	private static class IPV6Verifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
			JTextField inputTxt = (JTextField) input;

			// Allow empty octets.
			if (inputTxt.getText().isEmpty()) {
				return true;
			}

			try {
				inputTxt.setText(inputTxt.getText().toUpperCase());
				return true;
			}
			catch (Exception ex) {
				return false;
			}
		}
	}
}
