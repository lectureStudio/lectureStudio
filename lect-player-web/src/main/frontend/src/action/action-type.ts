enum ActionType {

	/*
	 * Tool actions
	 */
	TOOL_BEGIN,
	TOOL_EXECUTE,
	TOOL_END,

	/*
	 * Stroke actions
	 */
	PEN, HIGHLIGHTER, POINTER,

	/*
	 * Form actions
	 */
	ARROW, LINE, RECTANGLE, ELLIPSE,

	/*
	 * Text actions
	 */
	TEXT, TEXT_CHANGE, TEXT_FONT_CHANGE, TEXT_LOCATION_CHANGE, TEXT_REMOVE, TEXT_SELECTION,

	LATEX, LATEX_FONT_CHANGE,

	/*
	 * Rearrangement actions
	 */
	UNDO, REDO, CLONE, SELECT, SELECT_GROUP, RUBBER, CLEAR_SHAPES,

	/*
	 * Zoom actions
	 */
	PANNING, EXTEND_VIEW, ZOOM, ZOOM_OUT,

	/*
	 * Atomic actions
	 */
	NEXT_PAGE, KEY,

	STATIC

}

export { ActionType };