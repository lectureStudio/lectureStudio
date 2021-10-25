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

package org.lecturestudio.presenter.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.tool.ColorPalette;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.presenter.api.view.ToolbarView;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.components.FontPickerButton;
import org.lecturestudio.swing.components.RecordButton;
import org.lecturestudio.swing.components.TeXFontPickerButton;
import org.lecturestudio.swing.components.ToolColorPickerButton;
import org.lecturestudio.swing.components.ToolGroupButton;
import org.lecturestudio.swing.components.toolbar.CustomizedToolbar;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.FontConverter;
import org.lecturestudio.swing.layout.WrapFlowLayout;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "main-toolbar", presenter = org.lecturestudio.presenter.api.presenter.ToolbarPresenter.class)
public class SwingToolbarView extends JPanel implements ToolbarView {

	private final ResourceBundle resourceBundle;

	private final ToolController toolController;

	private ConsumerAction<Color> paletteColorAction;

	private ConsumerAction<Font> textBoxFontAction;

	private ConsumerAction<TeXFont> texBoxFontAction;

	private ButtonGroup colorGroup;

	private ButtonGroup toolGroup;

	private JButton undoButton;

	private JButton redoButton;

	private ToolColorPickerButton customColorButton;

	private JToggleButton colorButton1;

	private JToggleButton colorButton2;

	private JToggleButton colorButton3;

	private JToggleButton colorButton4;

	private JToggleButton colorButton5;

	private JToggleButton colorButton6;

	private JToggleButton penButton;

	private JToggleButton highlighterButton;

	private JToggleButton pointerButton;

	private JToggleButton textSelectButton;

	private JToggleButton lineButton;

	private JToggleButton arrowButton;

	private JToggleButton rectangleButton;

	private JToggleButton ellipseButton;

	private ToolGroupButton selectButton;

	private JToggleButton eraseButton;

	private FontPickerButton textButton;

	private TeXFontPickerButton texButton;

	private JButton clearButton;

	private JToggleButton gridButton;

	private JToggleButton extendButton;

	private JToggleButton whiteboardButton;

	private JToggleButton displaysButton;

	private JToggleButton zoomInButton;

	private JToggleButton panButton;

	private JButton zoomOutButton;

	private RecordButton startRecordingButton;

	private JButton stopRecordingButton;

	private JToggleButton streamEnableButton;

	private JToggleButton streamMicButton;

	private JToggleButton streamCamButton;

	private CustomizedToolbar customizedToolbar;

	private List<String> defaultToolbarButtonNames;


	@Inject
	SwingToolbarView(ResourceBundle resourceBundle, ToolController toolController) {
		super();

		this.resourceBundle = resourceBundle;
		this.toolController = toolController;

		setLayout(new WrapFlowLayout(FlowLayout.LEFT, 0, 0));
	}

	@Override
	public void setDocument(Document doc) {
		boolean isWhiteboard = nonNull(doc) && doc.isWhiteboard();

		textSelectButton.setEnabled(!isWhiteboard);
		whiteboardButton.setSelected(isWhiteboard);
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		boolean hasUndo = false;
		boolean hasRedo = false;
		boolean extended = false;
		boolean hasGrid = false;
		boolean zoomedIn = false;

		if (nonNull(page)) {
			hasUndo = page.hasUndoActions();
			hasRedo = page.hasRedoActions();
		}
		if (nonNull(parameter)) {
			extended = parameter.isExtended();
			hasGrid = parameter.showGrid();
			zoomedIn = parameter.isZoomMode();
		}

		undoButton.setEnabled(hasUndo);
		redoButton.setEnabled(hasRedo);
		extendButton.setSelected(extended);
		gridButton.setSelected(hasGrid);
		selectButton.setEnabled(hasUndo);
		panButton.setEnabled(zoomedIn);
		zoomOutButton.setEnabled(zoomedIn);
	}

	@Override
	public void setScreensAvailable(boolean screensAvailable) {
		SwingUtils.invoke(() -> displaysButton.setEnabled(screensAvailable));
	}

	@Override
	public void setPresentationViewsVisible(boolean viewsVisible) {
		SwingUtils.invoke(() -> displaysButton.setSelected(viewsVisible));
	}

	@Override
	public void setRecordingState(ExecutableState state) {
		SwingUtils.invoke(() -> {
			boolean started = state == ExecutableState.Started ||
					state == ExecutableState.Suspended;

			startRecordingButton.setState(state);
			stopRecordingButton.setEnabled(started);
		});
	}

	@Override
	public void setStreamingState(ExecutableState state) {
		SwingUtils.invoke(() -> {
			boolean started = state == ExecutableState.Started ||
					state == ExecutableState.Suspended;

			streamMicButton.setEnabled(started);
			streamCamButton.setEnabled(started);
		});
	}

	@Override
	public void showRecordNotification(boolean show) {
		SwingUtils.invoke(() -> startRecordingButton.setBlink(show));
	}

	@Override
	public void setOnUndo(Action action) {
		SwingUtils.bindAction(undoButton, action);
	}

	@Override
	public void setOnRedo(Action action) {
		SwingUtils.bindAction(redoButton, action);
	}

	@Override
	public void setOnCustomPaletteColor(ConsumerAction<Color> action) {
		this.paletteColorAction = action;
	}

	@Override
	public void setOnCustomColor(Action action) {
		SwingUtils.bindAction(customColorButton, action);
	}

	@Override
	public void setOnColor1(Action action) {
		SwingUtils.bindAction(colorButton1, action);
	}

	@Override
	public void setOnColor2(Action action) {
		SwingUtils.bindAction(colorButton2, action);
	}

	@Override
	public void setOnColor3(Action action) {
		SwingUtils.bindAction(colorButton3, action);
	}

	@Override
	public void setOnColor4(Action action) {
		SwingUtils.bindAction(colorButton4, action);
	}

	@Override
	public void setOnColor5(Action action) {
		SwingUtils.bindAction(colorButton5, action);
	}

	@Override
	public void setOnColor6(Action action) {
//		SwingUtils.bindAction(colorButton6, action);
	}

	@Override
	public void setOnPenTool(Action action) {
		SwingUtils.bindAction(penButton, action);
		penButton.addChangeListener(e -> {
			if (penButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnHighlighterTool(Action action) {
		SwingUtils.bindAction(highlighterButton, action);
		highlighterButton.addChangeListener(e -> {
			if (highlighterButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnPointerTool(Action action) {
		SwingUtils.bindAction(pointerButton, action);
		pointerButton.addChangeListener(e -> {
			if (pointerButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnTextSelectTool(Action action) {
		SwingUtils.bindAction(textSelectButton, action);
	}

	@Override
	public void setOnLineTool(Action action) {
		SwingUtils.bindAction(lineButton, action);
		lineButton.addChangeListener(e -> {
			if (lineButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnArrowTool(Action action) {
		SwingUtils.bindAction(arrowButton, action);
		arrowButton.addChangeListener(e -> {
			if (arrowButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnRectangleTool(Action action) {
		SwingUtils.bindAction(rectangleButton, action);
		rectangleButton.addChangeListener(e -> {
			if (rectangleButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnEllipseTool(Action action) {
		SwingUtils.bindAction(ellipseButton, action);
		ellipseButton.addChangeListener(e -> {
			if (ellipseButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnSelectTool(Action action) {
		SwingUtils.bindAction(selectButton, action);
	}

	@Override
	public void setOnEraseTool(Action action) {
		SwingUtils.bindAction(eraseButton, action);
		eraseButton.addChangeListener(e -> {
			if (eraseButton.isSelected()) {
				setColorButtonsEnabled(false);
			}
		});
	}

	@Override
	public void setOnTextTool(Action action) {
		SwingUtils.bindAction(textButton, action);
		textButton.addChangeListener(e -> {
			if (textButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnTextBoxFont(ConsumerAction<Font> action) {
		this.textBoxFontAction = action;
	}

	@Override
	public void setOnTeXTool(Action action) {
		SwingUtils.bindAction(texButton, action);
		texButton.addChangeListener(e -> {
			if (texButton.isSelected()) {
				setColorButtonsEnabled(true);
			}
		});
	}

	@Override
	public void setOnClearTool(Action action) {
		SwingUtils.bindAction(clearButton, action);
	}

	@Override
	public void setOnShowGrid(Action action) {
		SwingUtils.bindAction(gridButton, action);
	}

	@Override
	public void setOnExtend(Action action) {
		SwingUtils.bindAction(extendButton, action);
	}

	@Override
	public void setOnWhiteboard(Action action) {
		SwingUtils.bindAction(whiteboardButton, action);
	}

	@Override
	public void setOnEnableDisplays(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(displaysButton, action);
	}

	@Override
	public void setOnZoomInTool(Action action) {
		SwingUtils.bindAction(zoomInButton, action);
		zoomInButton.addChangeListener(e -> {
			if (zoomInButton.isSelected()) {
				setColorButtonsEnabled(false);
			}
		});
	}

	@Override
	public void setOnZoomOutTool(Action action) {
		SwingUtils.bindAction(zoomOutButton, action);
	}

	@Override
	public void setOnPanTool(Action action) {
		SwingUtils.bindAction(panButton, action);
		panButton.addChangeListener(e -> {
			if (panButton.isSelected()) {
				setColorButtonsEnabled(false);
			}
		});
	}

	@Override
	public void setOnStartRecording(Action action) {
		SwingUtils.bindAction(startRecordingButton, action);
	}

	@Override
	public void setOnStopRecording(Action action) {
		SwingUtils.bindAction(stopRecordingButton, action);
	}

	@Override
	public void selectColorButton(ToolType toolType, PaintSettings settings) {
		SwingUtils.invoke(() -> {
			Enumeration<AbstractButton> colorIter = colorGroup.getElements();
			int index = 0;

			while (colorIter.hasMoreElements()) {
				AbstractButton button = colorIter.nextElement();
				Color color = ColorPalette.getColor(toolType, index++);

				if (isNull(color)) {
					throw new IllegalArgumentException("No color assigned to the color-button");
				}

				// Select button with assigned brush color.
				if (nonNull(settings) && color.equals(settings.getColor())) {
					colorGroup.setSelected(button.getModel(), true);
				}

				setButtonColor(button, ColorConverter.INSTANCE.to(color));
			}
		});
	}

	@Override
	public void selectToolButton(ToolType toolType) {
		SwingUtils.invoke(() -> {
			Enumeration<AbstractButton> toolIter = toolGroup.getElements();

			while (toolIter.hasMoreElements()) {
				AbstractButton button = toolIter.nextElement();
				Object userData = button.getClientProperty("tool");

				if (isNull(userData)) {
					continue;
				}

				// Mapping may contain multiple type entries.
				String[] types = userData.toString().split(",");

				if (types.length == 1) {
					ToolType type = ToolType.valueOf(types[0].trim());

					if (toolType == type) {
						toolGroup.setSelected(button.getModel(), true);
						break;
					}
				} else {
					// Handle multiple type entries.
					for (String type : types) {
						ToolType buttonType = ToolType.valueOf(type.trim());

						if (toolType == buttonType) {
							toolGroup.setSelected(button.getModel(), true);

							if (button instanceof ToolGroupButton) {
								ToolGroupButton groupButton = (ToolGroupButton) button;
								groupButton.selectToolType(toolType);
							}
							break;
						}
					}
				}
			}
		});

		customColorButton.getChooser().setToolType(toolType);
	}

	@Override
	public void openCustomizeToolbarDialog() {
		ButtonModel selectedTool = toolGroup.getSelection();
		ButtonModel selectedColor = colorGroup.getSelection();
		toolGroup.clearSelection();
		colorGroup.clearSelection();

		customizedToolbar.displayDialog((int) (getWidth() / 3.5));

		toolGroup.setSelected(selectedTool, true);
		colorGroup.setSelected(selectedColor, true);
	}

	@Override
	public void bindEnableStream(BooleanProperty enable) {
		SwingUtils.bindBidirectional(streamEnableButton, enable);
	}

	@Override
	public void bindEnableStreamMicrophone(BooleanProperty enable) {
		SwingUtils.bindBidirectional(streamMicButton, enable);
	}

	@Override
	public void bindEnableStreamCamera(BooleanProperty enable) {
		SwingUtils.bindBidirectional(streamCamButton, enable);
	}

	private void setColorButtonsEnabled(boolean enabled) {
		customColorButton.setEnabled(enabled);
		colorButton1.setEnabled(enabled);
		colorButton2.setEnabled(enabled);
		colorButton3.setEnabled(enabled);
		colorButton4.setEnabled(enabled);
		colorButton5.setEnabled(enabled);
	}

	private void setButtonColor(AbstractButton button, Paint paint) {
		int size = undoButton.getIcon().getIconHeight();
		int paintSize = size / 2 + 1;
		int paintOffset = paintSize / 2 - 1;

		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setPaint(paint);
		g2d.fillRect(paintOffset, paintOffset, paintSize, paintSize);
		g2d.setPaint(java.awt.Color.LIGHT_GRAY);
		g2d.drawRect(paintOffset - 2, paintOffset - 2, paintSize + 3, paintSize + 3);
		g2d.dispose();

		button.setIcon(new ImageIcon(image));
	}

	private AbstractButton initializeButton(AbstractButton button, String iconPath, String[] additionalIconPaths,
											String accelerator, String toolTipText, String group, String name,
											boolean defaultToolbarButton, ToolType... tools) {
		if (nonNull(iconPath)) {
			button.setIcon(AwtResourceLoader.getIcon(iconPath, 30));
		}

		if (nonNull(accelerator)) {
			KeyStroke keyStroke = KeyStroke.getKeyStroke(accelerator);
			button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStroke.toString());
			button.getActionMap().put(keyStroke.toString(), new AbstractAction() {

				private static final long serialVersionUID = 1063494257153775447L;

				@Override
				public void actionPerformed(ActionEvent e) {
					AbstractButton b = (AbstractButton) e.getSource();
					b.doClick();
				}
			});
		}

		if (nonNull(toolTipText)) {
			button.setToolTipText(resourceBundle.getString(toolTipText));
		}

		if (nonNull(group)) {
			if (group.equals("colorGroup")) {
				colorGroup.add(button);
			} else if (group.equals("toolGroup")) {
				toolGroup.add(button);
			}
		}

		if (nonNull(name)) {
			button.setName(name);
		}

		if (defaultToolbarButton) {
			defaultToolbarButtonNames.add(button.getName());
		}

		if (nonNull(tools) && tools.length > 0) {
			for (ToolType tool : tools) {
				button.putClientProperty("tool", tool);
			}
		}

		if (nonNull(additionalIconPaths)) {
			if (button instanceof ToolGroupButton) {
				((ToolGroupButton) button).setCopyIcon(AwtResourceLoader.getIcon(additionalIconPaths[0], 30));
				((ToolGroupButton) button).setSelectIcon(AwtResourceLoader.getIcon(additionalIconPaths[1], 30));
				((ToolGroupButton) button).setSelectGroupIcon(AwtResourceLoader.getIcon(additionalIconPaths[2], 30));
			} else if (button instanceof RecordButton) {
				((RecordButton) button).setBlinkIcon(AwtResourceLoader.getIcon(additionalIconPaths[0], 30));
				((RecordButton) button).setPauseIcon(AwtResourceLoader.getIcon(additionalIconPaths[1], 30));
				((RecordButton) button).setPausedIcon(AwtResourceLoader.getIcon(additionalIconPaths[2], 30));
			} else if (button instanceof JToggleButton) {
				button.setSelectedIcon(AwtResourceLoader.getIcon(additionalIconPaths[0], 30));
			}
		}

		button.setBackground(null);
		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent evt) {
				button.setBackground(java.awt.Color.decode("#D6D6D6"));
			}

			public void mouseExited(MouseEvent evt) {
				button.setBackground(null);
			}
		});
		button.setBorderPainted(false);

		return button;
	}

	private AbstractButton initializeButton(AbstractButton button, String iconPath, String accelerator,
											String toolTipText, String group, String name,
											boolean defaultToolbarButton, ToolType... tools) {
		return initializeButton(button, iconPath, null, accelerator, toolTipText, group, name, defaultToolbarButton, tools);
	}

	@ViewPostConstruct
	private void initialize() {
		colorGroup = new ButtonGroup();
		toolGroup = new ButtonGroup();

		defaultToolbarButtonNames = new ArrayList<>();

		List<JComponent> toolbarComponents = initializeToolbarComponents();

		customizedToolbar = new CustomizedToolbar(toolbarComponents.toArray(new JComponent[0]),
				defaultToolbarButtonNames.toArray(new String[0]), "first test",
				resourceBundle, toolController, colorGroup, toolGroup);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = -1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(customizedToolbar, c);

		customColorButton.addItemChangeListener(stroke -> {
			Color color = stroke.getColor();

			setButtonColor(customColorButton, ColorConverter.INSTANCE.to(color));
			executeAction(paletteColorAction, color);
		});
		textButton.addItemChangeListener(font -> executeAction(textBoxFontAction, FontConverter.INSTANCE.from(font)));
		texButton.addItemChangeListener(font -> executeAction(texBoxFontAction, font));
	}

	private List<JComponent> initializeToolbarComponents() {
		List<JComponent> toolbarComponents =  new ArrayList<>();

		toolbarComponents.add(undoButton = (JButton) initializeButton(new JButton(), "undo-tool.svg", "ctrl Z", "toolbar.undo.tooltip", null, "undoButton", true));
		toolbarComponents.add(redoButton = (JButton) initializeButton(new JButton(), "redo-tool.svg", "ctrl Y", "toolbar.redo.tooltip", null, "redoButton", true));
		toolbarComponents.add(customColorButton = (ToolColorPickerButton) initializeButton(new ToolColorPickerButton(resourceBundle), null, null, "F1", null, "colorGroup", "customColorButton", true));
		toolbarComponents.add(colorButton1 = (JToggleButton) initializeButton(new JToggleButton(), null, "F2", null, "colorGroup", "colorButton1", true));
		toolbarComponents.add(colorButton2 = (JToggleButton) initializeButton(new JToggleButton(), null, "F3", null, "colorGroup", "colorButton2", true));
		toolbarComponents.add(colorButton3 = (JToggleButton) initializeButton(new JToggleButton(), null, "F4", null, "colorGroup", "colorButton3", true));
		toolbarComponents.add(colorButton4 = (JToggleButton) initializeButton(new JToggleButton(), null, "F5", null, "colorGroup", "colorButton4", true));
		toolbarComponents.add(colorButton5 = (JToggleButton) initializeButton(new JToggleButton(), null, "F6", null, "colorGroup", "colorButton5", true));
		toolbarComponents.add(penButton = (JToggleButton) initializeButton(new JToggleButton(), "pen-tool.svg", "P", "toolbar.pen.tooltip", "toolGroup", "penButton", true, ToolType.PEN));
		toolbarComponents.add(highlighterButton = (JToggleButton) initializeButton(new JToggleButton(), "highlighter-tool.svg", "H", "toolbar.highlighter.tooltip", "toolGroup", "highlighterButton", true, ToolType.HIGHLIGHTER));
		toolbarComponents.add(pointerButton = (JToggleButton) initializeButton(new JToggleButton(), "pointer-tool.svg", "A", "toolbar.pointer.tooltip", "toolGroup", "pointerButton", true, ToolType.POINTER));
		toolbarComponents.add(textSelectButton = (JToggleButton) initializeButton(new JToggleButton(), "text-select-tool.svg", "S", "toolbar.text.select.tooltip", "toolGroup", "textSelectButton", true, ToolType.TEXT_SELECTION));
		toolbarComponents.add(lineButton = (JToggleButton) initializeButton(new JToggleButton(), "line-tool.svg", "I", "toolbar.line.tooltip", "toolGroup", "lineButton", false, ToolType.LINE));
		toolbarComponents.add(arrowButton = (JToggleButton) initializeButton(new JToggleButton(), "arrow-tool.svg", "W", "toolbar.arrow.tooltip", "toolGroup", "arrowButton", false, ToolType.ARROW));
		toolbarComponents.add(rectangleButton = (JToggleButton) initializeButton(new JToggleButton(), "rectangle-tool.svg", "R", "toolbar.rectangle.tooltip", "toolGroup", "rectangleButton", false, ToolType.RECTANGLE));
		toolbarComponents.add(ellipseButton = (JToggleButton) initializeButton(new JToggleButton(), "ellipse-tool.svg", "C", "toolbar.ellipse.tooltip", "toolGroup", "ellipseButton", false, ToolType.ELLIPSE));
		toolbarComponents.add(selectButton = (ToolGroupButton) initializeButton(new ToolGroupButton(), null, new String[]{"clone-tool.svg", "select-tool.svg", "select-group-tool.svg"}, "O", "toolbar.select.tooltip", "toolGroup", "selectButton", false, ToolType.SELECT, ToolType.SELECT_GROUP, ToolType.CLONE));
		toolbarComponents.add(eraseButton = (JToggleButton) initializeButton(new JToggleButton(), "rubber-tool.svg", "E", "toolbar.erase.tooltip", "toolGroup", "eraseButton", true, ToolType.RUBBER));
		toolbarComponents.add(textButton = (FontPickerButton) initializeButton(new FontPickerButton(resourceBundle), "text-tool.svg", "T", "toolbar.text.tooltip", "toolGroup", "textButton", false, ToolType.TEXT));
		toolbarComponents.add(texButton = (TeXFontPickerButton) initializeButton(new TeXFontPickerButton(resourceBundle), "latex-tool.svg", "X", "toolbar.latex.tooltip", "toolGroup", "texButton", false, ToolType.LATEX));
		toolbarComponents.add(clearButton = (JButton) initializeButton(new JButton(), "clear-tool.svg", "ESCAPE", "toolbar.clear.tooltip", null, "clearButton", true));
		toolbarComponents.add(gridButton = (JToggleButton) initializeButton(new JToggleButton(), "grid-tool.svg", "Q", "toolbar.grid.tooltip", null, "gridButton", false));
		toolbarComponents.add(extendButton = (JToggleButton) initializeButton(new JToggleButton(), "extend-tool.svg", "F7", "toolbar.extend.tooltip", null, "extendButton", true));
		toolbarComponents.add(whiteboardButton = (JToggleButton) initializeButton(new JToggleButton(), "whiteboard-tool.svg", "F8", "toolbar.whiteboard.tooltip", null, "whiteboardButton", false));
		toolbarComponents.add(displaysButton = (JToggleButton) initializeButton(new JToggleButton(), "display-tool.svg", null, "toolbar.displays.tooltip", null, "displaysButton", false));
		toolbarComponents.add(zoomInButton = (JToggleButton) initializeButton(new JToggleButton(), "zoom-in-tool.svg", "F10", "toolbar.zoom.in.tooltip", "toolGroup", "zoomInButton", false, ToolType.ZOOM));
		toolbarComponents.add(panButton = (JToggleButton) initializeButton(new JToggleButton(), "pan-tool.svg", "F11", "toolbar.zoom.pan.tooltip", "toolGroup", "panButton", false, ToolType.PANNING));
		toolbarComponents.add(zoomOutButton = (JButton) initializeButton(new JButton(), "zoom-out-tool.svg", "F12", "toolbar.zoom.out.tooltip", null, "zoomOutButton", false));
		toolbarComponents.add(startRecordingButton = (RecordButton) initializeButton(new RecordButton(), "record-tool.svg", new String[]{"record-blink-tool.svg", "record-pause-tool.svg", "record-resume-tool.svg"}, null, "toolbar.recording.start.tooltip", null, "startRecordingButton", true));
		toolbarComponents.add(stopRecordingButton = (JButton) initializeButton(new JButton(), "record-stop-tool.svg", null, "toolbar.recording.stop.tooltip", null, "stopRecordingButton", true));
		toolbarComponents.add(streamEnableButton = (JToggleButton) initializeButton(new JToggleButton(), "stream-indicator.svg", null, "toolbar.stream.start.tooltip", null, "streamEnableButton", false));
		toolbarComponents.add(streamMicButton = (JToggleButton) initializeButton(new JToggleButton(), "microphone-off.svg", new String[]{"microphone.svg"}, null, "toolbar.stream.microphone.tooltip", null, "streamMicButton", false));
		toolbarComponents.add(streamCamButton = (JToggleButton) initializeButton(new JToggleButton(), "camera-off.svg", new String[]{"camera.svg"}, null, "toolbar.stream.camera.tooltip", null, "streamCamButton", false));

		return toolbarComponents;
	}
}
