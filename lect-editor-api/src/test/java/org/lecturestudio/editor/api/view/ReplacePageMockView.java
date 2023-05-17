package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;

public class ReplacePageMockView implements ReplacePageView {
	public ConsumerAction<String> setOnReplaceTypeChangeAction;
	public Page setCurrentPagePage;
	public Page setNewPagePage;
	public Action setOnPreviousPageNewDocAction;
	public Action setOnNextPageNewDocAction;
	public ConsumerAction<Integer> setOnPageNumberNewDocAction;
	public Action setOnPreviousPageCurrentDocAction;
	public Action setOnNextPageCurrentDocAction;
	public ConsumerAction<Integer> setOnPageNumberCurrentDocAction;
	public int setTotalPagesNewDocLabelInt;
	public int setTotalPagesCurrentDocLabelInt;
	public Action setOnAbortAction;
	public Action setOnReplaceAction;
	public Action setOnConfirmAction;
	public boolean disableInputBoolean;
	public boolean setDisableAllPagesTypeRadioBoolean;
	public boolean doneBoolean;

	@Override
	public void setPageCurrentDoc(Page page) {
		setCurrentPagePage = page;
	}

	@Override
	public void setPageNewDoc(Page page) {
		setNewPagePage = page;
	}

	@Override
	public void setOnPreviousPageNewDoc(Action action) {
		setOnPreviousPageNewDocAction = action;
	}

	@Override
	public void setOnNextPageNewDoc(Action action) {
		setOnNextPageNewDocAction = action;
	}

	@Override
	public void setOnPageNumberNewDoc(ConsumerAction<Integer> action) {
		setOnPageNumberNewDocAction = action;
	}

	@Override
	public void setOnPreviousPageCurrentDoc(Action action) {
		setOnPreviousPageCurrentDocAction = action;
	}

	@Override
	public void setOnNextPageCurrentDoc(Action action) {
		setOnNextPageCurrentDocAction = action;
	}

	@Override
	public void setOnPageNumberCurrentDoc(ConsumerAction<Integer> action) {
		setOnPageNumberCurrentDocAction = action;
	}

	@Override
	public void setTotalPagesNewDocLabel(int pages) {
		setTotalPagesNewDocLabelInt = pages;
	}

	@Override
	public void setTotalPagesCurrentDocLabel(int pages) {
		setTotalPagesCurrentDocLabelInt = pages;
	}

	@Override
	public void setOnAbort(Action action) {
		setOnAbortAction = action;
	}

	@Override
	public void setOnReplace(Action action) {
		setOnReplaceAction = action;
	}

	@Override
	public void setOnConfirm(Action action) {
		setOnConfirmAction = action;
	}

	@Override
	public void disableInput() {
		disableInputBoolean = true;
	}

	@Override
	public void enableInput() {
		doneBoolean = true;
	}

	@Override
	public void setDisableAllPagesTypeRadio(boolean disable) {
		setDisableAllPagesTypeRadioBoolean = disable;
	}

	@Override
	public void setOnReplaceTypeChange(ConsumerAction<String> action) {
		setOnReplaceTypeChangeAction = action;
	}
}
