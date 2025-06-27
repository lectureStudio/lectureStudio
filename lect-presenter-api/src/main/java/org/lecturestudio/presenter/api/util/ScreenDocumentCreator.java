/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.ScreenCapturer;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.ScreenDocument;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.web.api.model.ScreenSource;

public class ScreenDocumentCreator {

	public static void create(DocumentService documentService,
			ScreenSource screenSource) throws IOException {
		ScreenDocument screenDoc = null;
		boolean found = false;

		// Search for a possibly existing screen-document.
		for (Document doc : documentService.getDocuments().asList()) {
			if (doc.getTitle().equals(screenSource.getTitle())) {
				screenDoc = (ScreenDocument) doc;
				found = true;
				break;
			}
		}

		if (isNull(screenDoc)) {
			// No document found, create a new one.
			screenDoc = new ScreenDocument();
			screenDoc.setTitle(screenSource.getTitle());

			Document prevDoc = documentService.getDocuments()
					.getSelectedDocument();

			if (nonNull(prevDoc)) {
				Rectangle2D pageRect = prevDoc.getPage(0).getPageRect();
				screenDoc.setPageSize(new Dimension2D(pageRect.getWidth(),
						pageRect.getHeight()));
			}
		}

		ScreenDocument doc = screenDoc;
		boolean exists = found;

		DesktopCapturer desktopCapturer = screenSource.isWindow()
				? new WindowCapturer()
				: new ScreenCapturer();
		DesktopSource desktopSource = new DesktopSource(screenSource.getTitle(),
				screenSource.getId());

		desktopCapturer.selectSource(desktopSource);
		desktopCapturer.start((result, videoFrame) -> {
			// NOTE: Avoid asynchronous access to the VideoFrame, otherwise the app will crash.
			//       For asynchronous access, the VideoFrame must be copied and released after processing.
			try {
				doc.createPage(VideoFrameConverter.convertVideoFrame(videoFrame,
						null,
						(int) (doc.getPageSize().getWidth() * 3),
						(int) (doc.getPageSize().getHeight() * 3)));

				// Release the VideoFrame to avoid memory leaks.
				videoFrame.release();

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				doc.toOutputStream(stream);

				stream.flush();
				stream.close();

				Document newDoc = new ScreenDocument(stream.toByteArray());
				// Set the newly created page as the selected one.
				newDoc.selectPage(newDoc.getPageCount() - 1);

				if (exists) {
					documentService.replaceDocument(doc, newDoc, true);

					// Do not close a replaced document, since its pages cannot be saved later.
				}
				else {
					documentService.addDocument(newDoc);

					// Close copied document.
					doc.close();
				}

				documentService.selectDocument(newDoc);
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
		desktopCapturer.captureFrame();
		desktopCapturer.dispose();
	}
}
