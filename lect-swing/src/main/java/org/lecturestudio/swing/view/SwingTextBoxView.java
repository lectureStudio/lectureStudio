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

package org.lecturestudio.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicTextAreaUI;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.swing.beans.Binding;
import org.lecturestudio.swing.components.TextInputPageObject;
import org.lecturestudio.swing.converter.FontConverter;
import org.lecturestudio.swing.util.SwingUtils;

public class SwingTextBoxView extends TextInputPageObject<TextShape> implements TextBoxView {

	private static final java.awt.Color THEME_COLOR = new java.awt.Color(40, 190, 140);

	private Binding textBinding;

	private JTextArea textArea;

	private double fontSize;


	public SwingTextBoxView() {
		super();
	}

	@Override
	public void setFocus(boolean focus) {
		super.setFocus(focus);

		if (focus) {
			textArea.requestFocus();
		}
	}

	@Override
	public String getText() {
		return nonNull(getPageShape()) ? getPageShape().getText() : null;
	}

	@Override
	public void setText(String text) {
		if (nonNull(getPageShape())) {
			getPageShape().setText(text);
		}
	}

	@Override
	public TextAttributes getTextAttributes() {
		return nonNull(getPageShape()) ? getPageShape().getTextAttributes() : null;
	}

	@Override
	public void setTextAttributes(TextAttributes attributes) {
		if (nonNull(getPageShape())) {
			getPageShape().setTextAttributes(attributes);
		}
	}

	@Override
	public Color getTextColor() {
		return nonNull(getPageShape()) ? getPageShape().getTextColor() : null;
	}

	@Override
	public void setTextColor(Color color) {
		if (nonNull(getPageShape())) {
			getPageShape().setTextColor(color);
		}
	}

	@Override
	public Font getTextFont() {
		return nonNull(getPageShape()) ? getPageShape().getFont() : null;
	}

	@Override
	public void setTextFont(Font font) {
		if (nonNull(getPageShape())) {
			getPageShape().setFont(font);
		}
	}

	@Override
	protected void dispose() {
		if (nonNull(textBinding)) {
			textBinding.unbind();
		}

		super.dispose();
	}

	@Override
	protected void updateToShapeContent(TextShape shape) {
		if (isNull(shape)) {
			return;
		}

		fontSize = shape.getFont().getSize();

		textArea.setFont(FontConverter.INSTANCE.to(shape.getFont()));
		textArea.setText(shape.getText());

		textBinding = SwingUtils.bindBidirectional(textArea, shape.textProperty());
	}

	@Override
	protected void updateToTransform(AffineTransform transform) {
		TextShape shape = getPageShape();
		Insets padding = getInsets();
		Rectangle textBounds = textArea.getBounds();
		Rectangle2D shapeRect = shape.getBounds();

		double xOffset = textBounds.getMinX() + padding.left + 1;
		double yOffset = textBounds.getMinY() + padding.top + 1;

		if (!getFocus()) {
//			yOffset = padding.top + 1;
		}

		double s = transform.getScaleX();
		double tx = transform.getTranslateX();
		double ty = transform.getTranslateY();
		double x = Math.ceil((shapeRect.getX() + tx) * s) - xOffset;
		double y = Math.ceil((shapeRect.getY() + ty) * s) - yOffset;

		Font font = shape.getFont().clone();
		font.setSize(fontSize * s);

		textArea.setFont(FontConverter.INSTANCE.to(font));

		setLocation((int) x, (int) y);
		updateContentSize();
	}

	@Override
	protected java.awt.Color getThemeColor() {
		return THEME_COLOR;
	}

	@Override
	protected JComponent createContent() {
		textArea = new JTextArea() {

			List<TextLayout> layouts = new ArrayList<>();

			float wrapWidth;

			Shape caret;

			int hit1, hit2;


			{
//				addMouseListener(new MouseHandler());
//				addMouseMotionListener(new MouseMotionHandler());
			}

//			@Override
//			protected void paintComponent(Graphics g) {
//				Graphics2D g2d = (Graphics2D) g;
//				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
//						RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//
//				layouts = getTextLayouts();
//
//				float x = 3, y = 3;
//
//				for (TextLayout layout : layouts) {
//					float dx = layout.isLeftToRight() ? 0 : wrapWidth - layout.getAdvance();
//
//					y += layout.getAscent();
//
//					layout.draw(g2d, x + dx, y);
//
//					y += layout.getDescent() + layout.getLeading();
//				}
//
//				if (nonNull(caret)) {
//					g2d.setColor(java.awt.Color.BLUE);
//					g2d.draw(caret);
//				}
//			}

			private int getHitLocation(int mouseX, int mouseY) {
				layouts = getTextLayouts();

				FontRenderContext frc = new FontRenderContext(null, true, true);
				float x = 3, y = 3;
				int hit = -1;

				for (TextLayout layout : layouts) {
					float dx = layout.isLeftToRight() ? 0 : wrapWidth - layout.getAdvance();

					y += layout.getAscent();

					Rectangle bounds = layout.getPixelBounds(frc, x + dx, y);

					if (bounds.y <= mouseY && mouseY <= bounds.y + bounds.height) {
						TextHitInfo hitInfo = layout.hitTestChar(mouseX, mouseY);
						hit = hitInfo.getInsertionIndex();

						AffineTransform at = AffineTransform.getTranslateInstance(x + dx, y);
						Shape[] caretShapes = layout.getCaretShapes(hit);

						caret = at.createTransformedShape(caretShapes[0]);
						break;
					}

					y += layout.getDescent() + layout.getLeading();
				}

				return hit;
			}

			class MouseHandler extends MouseAdapter {

				@Override
				public void mousePressed(MouseEvent e) {
					hit1 = getHitLocation(e.getX(), e.getY());
					hit2 = hit1;
					repaint();
				}

			}

			class MouseMotionHandler extends MouseMotionAdapter {

				public void mouseDragged(MouseEvent e) {
					hit2 = getHitLocation(e.getX(), e.getY());
					repaint();
				}
			}
		};
		textArea.setUI(new BasicTextAreaUI());
		textArea.setOpaque(false);
		textArea.setForeground(new java.awt.Color(0, 0, 0, 0));
		textArea.setFocusable(true);
		textArea.requestFocus();
		textArea.addCaretListener(e -> updateContentSize());
		textArea.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				setFocus(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				setFocus(false);
			}
		});

		return textArea;
	}

	@Override
	protected void onRelocateShape(Point2D location) {
		getPageShape().setLocation(location);
	}

	float wrapWidth;

	private List<TextLayout> getTextLayouts() {
		List<TextLayout> layouts = new ArrayList<>();
		TextShape shape = getPageShape();
		String text = shape.getText();

		if (text.isEmpty()) {
			return layouts;
		}

		// Scale font.
		Font font = shape.getFont().clone();
		font.setSize(font.getSize() * getPageTransform().getScaleY());

		java.awt.Font textFont = FontConverter.INSTANCE.to(font);

		AttributedString styledText = new AttributedString(text);
		styledText.addAttribute(TextAttribute.FONT, textFont);
		styledText.addAttribute(TextAttribute.UNDERLINE, shape.isUnderline() ? TextAttribute.UNDERLINE_ON : -1);
		styledText.addAttribute(TextAttribute.STRIKETHROUGH, shape.isStrikethrough());
		styledText.addAttribute(TextAttribute.FOREGROUND, java.awt.Color.red);

		FontRenderContext frc = new FontRenderContext(null, true, true);

		AttributedCharacterIterator iterator = styledText.getIterator();
		int start = iterator.getBeginIndex();
		int end = iterator.getEndIndex();

		LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
		measurer.setPosition(start);

		wrapWidth = getWrappingWidth(text, frc, textFont);
		int limit;
		int newLineIndex;

		while (measurer.getPosition() < end) {
			limit = measurer.nextOffset(wrapWidth);

			newLineIndex = text.indexOf('\n', measurer.getPosition());
			if (newLineIndex != -1) {
				limit = newLineIndex;
			}

			layouts.add(measurer.nextLayout(wrapWidth, limit + 1, false));
		}

		return layouts;
	}

	private float getWrappingWidth(String text, FontRenderContext frc, java.awt.Font textFont) {
		String[] lines = text.split("\\n");
		double wrapWidth = 0;

		for (String line : lines) {
			wrapWidth = Math.max(wrapWidth, textFont.getStringBounds(line, frc).getWidth());
		}

		return (float) wrapWidth;
	}
}
