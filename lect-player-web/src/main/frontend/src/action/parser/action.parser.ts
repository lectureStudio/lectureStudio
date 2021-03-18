import { ProgressiveDataView } from "./progressive-data-view";
import { Action } from "../action";
import { ActionType } from "../action-type";
import { PenPoint } from "../../geometry/pen-point";
import { PenAction } from "../pen.action";
import { Brush } from "../../paint/brush";
import { Color } from "../../paint/color";
import { HighlighterAction } from "../highlighter.action";
import { PointerAction } from "../pointer.action";
import { ArrowAction } from "../arrow.action";
import { LineAction } from "../line.action";
import { RectangleAction } from "../rectangle.action";
import { EllipseAction } from "../ellipse.action";
import { ToolBeginAction } from "../tool-begin.action";
import { ToolExecuteAction } from "../tool-execute.action";
import { ToolEndAction } from "../tool-end.action";
import { UndoAction } from "../undo.action";
import { RedoAction } from "../redo.action";
import { ClearShapesAction } from "../clear-shapes.action";
import { KeyAction } from "../key.action";
import { CloneAction } from "../clone.action";
import { SelectAction } from "../select.action";
import { SelectGroupAction } from "../select-group.action";
import { PanAction } from "../pan.action";
import { ExtendViewAction } from "../extend-view.action";
import { Rectangle } from "../../geometry/rectangle";
import { ZoomAction } from "../zoom.action";
import { ZoomOutAction } from "../zoom-out.action";
import { RubberAction } from "../rubber.action";
import { TextAction } from "../text.action";
import { TextFontAction } from "../text-font.action";
import { TextChangeAction } from "../text-change.action";
import { TextMoveAction } from "../text-move.action";
import { Point } from "../../geometry/point";
import { TextRemoveAction } from "../text-remove.action";
import { Font } from "../../paint/font";
import { TextHighlightAction } from "../text-highlight.action";
import { LatexAction } from "../latex.action";
import { LatexFontAction } from "../latex-font.action";

class ActionParser {

	private static readonly KEY_EVENT_MASK = 1;


	static parse(dataView: ProgressiveDataView, type: ActionType): Action {
		let action: Action = null;
		const keyEvent: KeyboardEvent = this.parseActionHeader(dataView);

		switch (type) {
			case ActionType.CLEAR_SHAPES:
				action = this.atomicAction(dataView, ClearShapesAction);
				break;
			case ActionType.UNDO:
				action = this.atomicAction(dataView, UndoAction);
				break;
			case ActionType.REDO:
				action = this.atomicAction(dataView, RedoAction);
				break;
			case ActionType.KEY:
				action = this.atomicAction(dataView, KeyAction);
				break;
			case ActionType.PEN:
				action = this.toolBrushAction(dataView, PenAction);
				break;
			case ActionType.HIGHLIGHTER:
				action = this.toolBrushAction(dataView, HighlighterAction);
				break;
			case ActionType.POINTER:
				action = this.toolBrushAction(dataView, PointerAction);
				break;
			case ActionType.ARROW:
				action = this.toolBrushAction(dataView, ArrowAction);
				break;
			case ActionType.LINE:
				action = this.toolBrushAction(dataView, LineAction);
				break;
			case ActionType.RECTANGLE:
				action = this.toolBrushAction(dataView, RectangleAction);
				break;
			case ActionType.ELLIPSE:
				action = this.toolBrushAction(dataView, EllipseAction);
				break;
			case ActionType.CLONE:
				action = this.atomicAction(dataView, CloneAction);
				break;
			case ActionType.SELECT:
				action = this.atomicAction(dataView, SelectAction);
				break;
			case ActionType.SELECT_GROUP:
				action = this.atomicAction(dataView, SelectGroupAction);
				break;
			case ActionType.LATEX:
				action = this.textAction(dataView, LatexAction);
				break;
			case ActionType.LATEX_FONT_CHANGE:
				action = this.latexFontAction(dataView);
				break;
			case ActionType.TEXT:
				action = this.textAction(dataView, TextAction);
				break;
			case ActionType.TEXT_CHANGE:
				action = this.textChangeAction(dataView);
				break;
			case ActionType.TEXT_FONT_CHANGE:
				action = this.textFontAction(dataView);
				break;
			case ActionType.TEXT_LOCATION_CHANGE:
				action = this.textMoveAction(dataView);
				break;
			case ActionType.TEXT_REMOVE:
				action = this.textRemoveAction(dataView);
				break;
			case ActionType.TEXT_SELECTION:
				action = this.textHighlightAction(dataView);
				break;
			case ActionType.TOOL_BEGIN:
				action = this.toolDragAction(dataView, ToolBeginAction);
				break;
			case ActionType.TOOL_EXECUTE:
				action = this.toolDragAction(dataView, ToolExecuteAction);
				break;
			case ActionType.TOOL_END:
				action = this.toolDragAction(dataView, ToolEndAction);
				break;
			case ActionType.PANNING:
				action = this.atomicAction(dataView, PanAction);
				break;
			case ActionType.EXTEND_VIEW:
				action = this.extendViewAction(dataView);
				break;
			case ActionType.ZOOM:
				action = this.atomicAction(dataView, ZoomAction);
				break;
			case ActionType.ZOOM_OUT:
				action = this.atomicAction(dataView, ZoomOutAction);
				break;
			case ActionType.RUBBER:
				action = this.atomicAction(dataView, RubberAction);
				break;
		}

		if (action) {
			action.keyEvent = keyEvent;
		}

		return action;
	}

	private static parseActionHeader(dataView: ProgressiveDataView): KeyboardEvent {
		const header = dataView.getInt32();
		let keyEvent: KeyboardEvent = null;

		if ((header & this.KEY_EVENT_MASK) == this.KEY_EVENT_MASK) {
			keyEvent = this.parseKeyEvent(dataView);
		}

		return keyEvent;
	}

	private static parseKeyEvent(dataView: ProgressiveDataView): KeyboardEvent {
		const keyCode = dataView.getInt32();
		const modifiers = dataView.getInt32();
		const actionType = dataView.getInt8();
		let typeArg: string;

		switch (actionType) {
			case 0:
				typeArg = "keydown";
				break;
			case 1:
				typeArg = "keyup";
				break;
			case 2:
				typeArg = "keypress";
				break;
			default:
				throw new Error("Unknown key event type");
		}

		/** The Shift key modifier constant. */
		const SHIFT_MASK = 1 << 1;
		/** The Control key modifier constant. */
		const CTRL_MASK = 1 << 2;
		/** The Alt key modifier constant. */
		const ALT_MASK = 1 << 3;

		return new KeyboardEvent(typeArg, {
			//code: keyCode,
			shiftKey: (modifiers & SHIFT_MASK) != 0,
			ctrlKey: (modifiers & CTRL_MASK) != 0,
			altKey: (modifiers & ALT_MASK) != 0
		});
	}

	private static atomicAction<T>(dataView: ProgressiveDataView, type: { new(): T }): T {
		return new type();
	}

	private static toolBrushAction<T>(dataView: ProgressiveDataView, type: { new(brush: Brush): T }): T {
		const rgba = dataView.getInt32();
		const lineCap = dataView.getInt8();
		const brushWidth = dataView.getFloat64();

		const color = Color.fromRGBNumber(rgba);
		const brush = new Brush(color, brushWidth);
		const action = new type(brush);

		return action;
	}

	private static toolDragAction<T>(dataView: ProgressiveDataView, type: { new(point: PenPoint): T }): T {
		const point = new PenPoint(dataView.getFloat32(), dataView.getFloat32(), dataView.getFloat32());
		const action = new type(point);

		return action;
	}

	private static extendViewAction(dataView: ProgressiveDataView): ExtendViewAction {
		const x = dataView.getFloat64();
		const y = dataView.getFloat64();
		const w = dataView.getFloat64();
		const h = dataView.getFloat64();

		return new ExtendViewAction(new Rectangle(x, y, w, h));
	}

	private static textAction<T>(dataView: ProgressiveDataView, type: { new(handle: number): T }): T {
		const handle = dataView.getInt32();
		const action = new type(handle);

		return action;
	}

	private static textChangeAction(dataView: ProgressiveDataView): TextChangeAction {
		const handle = dataView.getInt32();
		const textLength = dataView.getInt32();
		const text = dataView.getString(textLength);

		return new TextChangeAction(handle, text);
	}

	private static textMoveAction(dataView: ProgressiveDataView): TextMoveAction {
		const handle = dataView.getInt32();
		const point = new Point(dataView.getFloat64(), dataView.getFloat64());

		return new TextMoveAction(handle, point);
	}

	private static textRemoveAction(dataView: ProgressiveDataView): TextRemoveAction {
		const handle = dataView.getInt32();

		return new TextRemoveAction(handle);
	}

	private static textHighlightAction(dataView: ProgressiveDataView): TextHighlightAction {
		const rgba = dataView.getInt32();
		const count = dataView.getInt32();

		const color = Color.fromRGBNumber(rgba);
		const textBounds = new Array<Rectangle>();

		for (let i = 0; i < count; i++) {
			const x = dataView.getFloat64();
			const y = dataView.getFloat64();
			const w = dataView.getFloat64();
			const h = dataView.getFloat64();

			textBounds.push(new Rectangle(x, y, w, h));
		}

		return new TextHighlightAction(color, textBounds);
	}

	private static textFontAction(dataView: ProgressiveDataView): TextFontAction {
		const handle = dataView.getInt32();
		const rgba = dataView.getInt32();
		const fontFamilyLength = dataView.getInt32();
		const fontFamily = dataView.getString(fontFamilyLength);
		const fontSize = dataView.getFloat64();
		const posture = dataView.getInt8();
		const weight = dataView.getInt8();
		const strikethrough = dataView.getInt8() > 0;
		const underline = dataView.getInt8() > 0;

		let fontWeight = (weight + 1) * 100;
		let fontStyle;

		switch (posture) {
			case 0:
				fontStyle = "normal";
				break;
			case 1:
				fontStyle = "italic";
				break;
			default:
				console.error("Unsupported font style");
				break;
		}

		const color = Color.fromRGBNumber(rgba);
		const font = new Font(fontFamily, fontSize, fontStyle, fontWeight.toString());

		const attributes = new Map<string, boolean>();
		attributes.set("strikethrough", strikethrough);
		attributes.set("underline", underline);

		return new TextFontAction(handle, font, color, attributes);
	}

	private static latexFontAction(dataView: ProgressiveDataView): LatexFontAction {
		const handle = dataView.getInt32();
		const fontType = dataView.getInt32();
		const fontSize = dataView.getFloat32();
		const rgba = dataView.getInt32();

		const color = Color.fromRGBNumber(rgba);
		const font = new Font("Arial", fontSize);

		const attributes = new Map<string, boolean>();

		return new LatexFontAction(handle, font, color, attributes);
	}
}

export { ActionParser };