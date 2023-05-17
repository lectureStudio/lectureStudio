/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface ReplacePageView extends View {

	void setPageCurrentDoc(Page page);

	void setPageNewDoc(Page page);

	void setOnPreviousPageNewDoc(Action action);

	void setOnNextPageNewDoc(Action action);

	void setOnPageNumberNewDoc(ConsumerAction<Integer> action);

	void setOnPreviousPageCurrentDoc(Action action);

	void setOnNextPageCurrentDoc(Action action);

	void setOnPageNumberCurrentDoc(ConsumerAction<Integer> action);

	void setTotalPagesNewDocLabel(int pages);

	void setTotalPagesCurrentDocLabel(int pages);

	void setOnAbort(Action action);

	void setOnReplace(Action action);

	void setOnConfirm(Action action);

	void enableInput();

	void disableInput();

	void setDisableAllPagesTypeRadio(boolean disable);

	void setOnReplaceTypeChange(ConsumerAction<String> action);
}
