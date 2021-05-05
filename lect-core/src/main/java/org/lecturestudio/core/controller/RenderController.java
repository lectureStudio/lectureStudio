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

package org.lecturestudio.core.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.GridConfiguration;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.GraphicsContext;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.GridShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.RenderContext;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * The controller to handle page render requests.
 *
 * @author Alex Andres
 */
public class RenderController extends Controller {

	private static final Logger LOG = LogManager.getLogger(RenderController.class);

	/** The render context. */
	private final RenderContext renderContext;


	/**
	 * Create a {@code RenderController} with the specified contexts.
	 *
	 * @param context       The application context.
	 * @param renderContext The render context.
	 */
	public RenderController(ApplicationContext context, RenderContext renderContext) {
		super(context);

		this.renderContext = renderContext;
	}

	/**
	 * Copy constructor to create a {@code RenderController} with a different
	 * {@code ApplicationContext}.
	 *
	 * @param context    The new application context.
	 * @param controller The render controller to copy, except the application
	 *                   context.
	 */
	public RenderController(ApplicationContext context, RenderController controller) {
		super(context);

		this.renderContext = controller.renderContext;
	}

	/**
	 * Render the foreground, all annotations, of a page.
	 */
	public void renderShapes(GraphicsContext gc, ViewType viewType, Dimension2D imageSize, Page page, List<Shape> shapes) {
		try {
			final PresentationParameterProvider ppProvider = getContext().getPagePropertyProvider(viewType);
			final PresentationParameter parameter = ppProvider.getParameter(page);

			Rectangle2D pageRect = parameter.getViewRect();

			double sx = imageSize.getWidth() / pageRect.getWidth();

			gc.save();
			gc.scale(sx, sx);
			gc.translate(-pageRect.getX(), -pageRect.getY());

			renderContext.render(viewType, shapes, gc);

			gc.restore();
		}
		catch (Exception e) {
			LOG.error("Rendering failed", e);
		}
	}

	/**
	 * Render a page on a surface the specified render task contains.
	 */
	public void renderPage(BufferedImage image, Page page, ViewType viewType) {
		try {
			final PresentationParameterProvider ppProvider = getContext().getPagePropertyProvider(viewType);
			final PresentationParameter parameter = ppProvider.getParameter(page);

			page.getDocument().getDocumentRenderer().render(page, parameter, image);

			if (page.getDocument().isWhiteboard()) {
				renderGrid(image, parameter, viewType);
			}
		}
		catch (Exception e) {
			LOG.error("Rendering failed", e);
		}
	}

	private void renderGrid(BufferedImage image, PresentationParameter parameter, ViewType viewType) throws Exception {
		if (parameter.showGrid()) {
			Rectangle2D pageRect = parameter.getViewRect();
			GridConfiguration gridConfig = parameter.getGridConfiguration();

			GridShape gridShape = new GridShape();
			gridShape.setViewRatio(new Dimension2D(4.0, 3.0));
			gridShape.setColor(gridConfig.getColor());
			gridShape.setHorizontalLinesInterval(gridConfig.getHorizontalLinesInterval());
			gridShape.setHorizontalLinesVisible(gridConfig.getHorizontalLinesVisible());
			gridShape.setVerticalLinesInterval(gridConfig.getVerticalLinesInterval());
			gridShape.setVerticalLinesVisible(gridConfig.getVerticalLinesVisible());

			double sx = image.getWidth() / pageRect.getWidth();

			Graphics2D g = image.createGraphics();
			g.scale(sx, sx);
			g.translate(-pageRect.getX(), -pageRect.getY());

			renderContext.render(viewType, List.of(gridShape), new SwingGraphicsContext(g));

			g.dispose();
		}
	}
}
