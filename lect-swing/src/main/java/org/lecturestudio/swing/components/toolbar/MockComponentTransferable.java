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
/* based on the code from the Pumpernickel project under the following licence: */
/*
 * This software is released as part of the Pumpernickel project.
 *
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 *
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package org.lecturestudio.swing.components.toolbar;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * A Transferable the encapsulates a MockComponent.
 * <P>
 * This really isn't specifically necessary, but SOME transferable is. And that transferable needs to <i>not</i> be text,
 * or an image, because then other eager apps would try to work with it.
 */
public class MockComponentTransferable implements Transferable {
	private final DataFlavor myDataFlavor;
	private final MockComponent mockComponent;

	public MockComponentTransferable(MockComponent mc) {
		try {
			myDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.mockComponent = mc;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(myDataFlavor)) {
			return mockComponent;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { myDataFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for (DataFlavor dataFlavor : flavors) {
			if (dataFlavor.equals(flavor))
				return true;
		}
		return false;
	}
}
