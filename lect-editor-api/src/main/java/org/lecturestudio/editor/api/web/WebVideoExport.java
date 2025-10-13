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

package org.lecturestudio.editor.api.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.DocumentEventExecutor;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.swing.DefaultRenderContext;

public class WebVideoExport extends RecordingExport {

	private static final String TEMPLATE_FILE = "resources/export/web/video/index.html";

	private static final int PREVIEW_WIDTH = 250;

	private final Map<String, String> data = new HashMap<>();

	private final ApplicationContext context;

	private final Recording recording;

	private final RenderConfiguration config;

	private BufferedImage pageImage;


	public WebVideoExport(ApplicationContext context, Recording recording,
			RenderConfiguration config) {
		this.context = context;
		this.recording = recording;
		this.config = config;
	}

	public void setTitle(String title) {
		data.put("title", title);
	}

	public void setVideoSource(String source) {
		data.put("videoSource", source);
	}

	@Override
	protected void initInternal() {
		File targetFile = config.getOutputFile();

		setVideoSource(targetFile.getName());

		setTitle(FileUtils.stripExtension(targetFile.getName()));
	}

	@Override
	protected void startInternal() {
		CompletableFuture.runAsync(() -> {
			try {
				String pageModel = createPageModel(recording);

				data.put("pageModelData", pageModel);

				File targetFile = config.getOutputFile();
				String webExportPath = FileUtils.stripExtension(targetFile.getPath());
				File outputFile = new File(webExportPath + ".html");
				String indexContent = loadTemplateFile();
				indexContent = processTemplateFile(indexContent, data);

				writeTemplateFile(indexContent, outputFile);

				stop();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(throwable -> {
			LOG.error("HTML video export failed", throwable);
			return null;
		});
	}

	@Override
	protected void stopInternal() {

	}

	@Override
	protected void destroyInternal() {

	}

	private String loadTemplateFile() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE);

		if (isNull(is)) {
			throw new NullPointerException("Missing web index.html file.");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		return reader.lines().collect(Collectors.joining(System.lineSeparator()));
	}

	private String processTemplateFile(String fileContent, Map<String, String> data) {
		Pattern pattern = Pattern.compile("\"#\\{(.+?)}\"");
		Matcher matcher = pattern.matcher(fileContent);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String match = matcher.group(1);
			String replacement = data.get(match);

			if (nonNull(replacement)) {
				matcher.appendReplacement(sb, "'" + replacement + "'");
			}
			else {
				LOG.warn("Found match '{}' with no replacement.", match);
			}
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private void writeTemplateFile(String fileContent, File outputFile) throws IOException {
		Path path = Paths.get(outputFile.getPath());
		Files.writeString(path, fileContent);
	}

	private String createPageModel(Recording recording) throws Exception {
		// Create an execution context with own event buses to not interfere
		// with the main application context.
		ApplicationContext execContext = new ApplicationContext(null,
				context.getConfiguration(), context.getDictionary(),
				new EventBus(), new EventBus()) {

			@Override
			public void saveConfiguration() {
			}
		};

		RenderController renderController = new RenderController(execContext,
				new DefaultRenderContext());

		RecordedEvents events = recording.getRecordedEvents();
		Document document = createRenderedDocument(execContext);
		List<Page> pages = document.getPages();

		StringBuilder builder = new StringBuilder();
		builder.append("[");

		for (int i = 0; i < pages.size(); i++) {
			Page page = pages.get(i);

			builder.append("{");
			encodePageTime(builder, events.getRecordedPage(i).getTimestamp());
			builder.append(", ");
			encodePageText(builder, page.getPageText());
			builder.append(", ");
			encodePagePreview(builder, renderController, page, PREVIEW_WIDTH);
			builder.append("}");

			if (i + 1 < pages.size()) {
				builder.append(",");
			}
		}

		builder.append("]");

		return builder.toString();
	}

	private void encodePageTime(StringBuilder builder, int timestamp) {
		builder.append("\"time\": ");
		builder.append(timestamp + 150);
	}

	private void encodePageText(StringBuilder builder, String text) {
		builder.append("\"text\": ");
		builder.append("\"");
		builder.append(Base64.getEncoder().encodeToString(text.getBytes()));
		builder.append("\"");
	}

	private void encodePagePreview(StringBuilder builder,
			RenderController renderController, Page page, int width)
			throws IOException {
		ViewType viewType = ViewType.User;

		// Calculate the height for the desired image width.
		Rectangle2D pageRect = page.getDocument()
				.getPageRect(page.getPageNumber());
		int height = (int) (pageRect.getHeight() / pageRect.getWidth() * width);

		pageImage = getPageImage(pageImage, width, height);

		renderController.renderPage(pageImage, page, viewType);

		if (page.hasShapes()) {
			List<Shape> shapes = page.getShapes();
			Dimension2D imageSize = new Dimension2D(width, height);
			Graphics2D g = pageImage.createGraphics();

			SwingGraphicsContext gc = new SwingGraphicsContext(g);
			renderController.renderShapes(gc, viewType, imageSize, page, shapes);
			g.dispose();
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(pageImage, "png", os);

		builder.append("\"thumb\": ");
		builder.append("\"");
		builder.append("data:image/png;base64,");
		builder.append(Base64.getEncoder().encodeToString(os.toByteArray()));
		builder.append("\"");
	}

	private BufferedImage getPageImage(BufferedImage image, int width, int height) {
		if (isNull(image) || image.getWidth() != width || image.getHeight() != height) {
			if (nonNull(image)) {
				image.flush();
			}

			GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gConf = gEnv.getDefaultScreenDevice().getDefaultConfiguration();

			image = gConf.createCompatibleImage(width, height);
			image.setAccelerationPriority(1.0f);
		}

		return image;
	}

	private Document createRenderedDocument(ApplicationContext context) throws Exception {
		DocumentEventExecutor docEventExecutor = new DocumentEventExecutor(context, recording);
		docEventExecutor.executeEvents();

		return docEventExecutor.getDocument();
	}
}
