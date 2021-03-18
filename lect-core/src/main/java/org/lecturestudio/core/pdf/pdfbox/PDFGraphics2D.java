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

package org.lecturestudio.core.pdf.pdfbox;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.Map;

import org.lecturestudio.core.pdf.PdfFontManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

/**
 * This class extends the fundamental Graphics2D to provide control over
 * geometry, coordinate transformations, color management, and text layout in
 * order to draw and create PDF content. You MUST call close() when you are
 * finished with drawing.
 * 
 * @author Alex Andres
 */
public class PDFGraphics2D extends Graphics2D {
	
	private static final Logger LOG = LogManager.getLogger(PDFGraphics2D.class);

	/** Convenience constant for full opacity. */
	protected static final int OPAQUE = 255;
	
	/** Default Transform to be used for creating FontRenderContext. */
    protected AffineTransform defaultTransform = new AffineTransform();

	/**
	 * Current AffineTransform. This is the concatenation of the original
	 * AffineTransform (i.e., last setTransform invocation) and the following
	 * transform invocations, as captured by originalTransform and the
	 * transformStack.
	 */
    protected AffineTransform transform = new AffineTransform();

    /** Current Paint. */
    protected Paint paint = Color.black;

    /** Current Stroke. */
    protected Stroke stroke = new BasicStroke();

    /** Current Composite. */
    protected Composite composite = AlphaComposite.SrcOver;

    /** Current clip. */
    protected Shape clip = null;

    /** Current set of RenderingHints. */
    protected RenderingHints hints = new RenderingHints(null);

    /** Current Font. */
    protected Font font = new Font("sanserif", Font.PLAIN, 12);

    /** Current background color. */
    protected Color background = new Color(0, 0, 0, 0);

    /** Current foreground color. */
    protected Color foreground = Color.black;
    
    /** Used to get font metrics. */
	protected Graphics2D fmg;
	
	/** The PDF document to which to draw. */
	protected PDDocument document;
	
	/** The PDF page to which to draw. */
    protected PDPage page;
    
    /** The PDF content stream of the page. */
    protected PDPageContentStream stream;
    
    /** Protect against infinite recursion. */
    protected boolean inPossibleRecursion = false;
    
    // Static.
    {
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        fmg = bi.createGraphics();
    }
	
	
    /**
	 * Create a new PdfGraphics2DStream for a PDF page to which the drawn content should
	 * be added. If the content should not be appended, then the origin of the page is
	 * moved to the top-left corner for further drawing operations.
	 * 
	 * @param document The PDF document to which to draw.
	 * @param page The PDF page to which to draw.
	 * @param streamName The PDF graphics stream name.
	 * @param appendContent true, if content should be appended to the existing one,
	 * 						false to overwrite.
	 */
	public PDFGraphics2D(PDDocument document, PDPage page, String streamName, boolean appendContent) {
		this(document, page, appendContent);
		
		if (stream != null && streamName != null) {
			// Get the newly created PDF graphics stream and tag it.
			Iterator<PDStream> streams = page.getContentStreams();
			PDStream pdStream = null;

			while (streams.hasNext()) {
				pdStream = streams.next();
			}

			pdStream.getCOSObject().setName(COSName.NAME, streamName);
		}
	}
    
	/**
	 * Create a new PdfGraphics2DStream for a PDF page to which the drawn content should
	 * be added. If the content should not be appended, then the origin of the page is
	 * moved to the top-left corner for further drawing operations.
	 * 
	 * @param document The PDF document to which to draw.
	 * @param page The PDF page to which to draw.
	 * @param appendContent true, if content should be appended to the existing one,
	 * 						false to overwrite.
	 */
	public PDFGraphics2D(PDDocument document, PDPage page, boolean appendContent) {
		this.document = document;
		this.page = page;
		
		try {
			stream = new PDPageContentStream(document, page, appendContent ? AppendMode.APPEND : AppendMode.OVERWRITE, true);

			if (!appendContent) {
				// Move to top-left corner.
				stream.transform(new Matrix(1, 0, 0, -1, 0, page.getMediaBox().getHeight()));
			}
		}
		catch (IOException e) {
			LOG.error("Create PDPageContentStream failed.", e);
		}
		
		// Workaround a JDK bug.
		this.hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
	}
	
	/**
	 * Create a new PdfGraphics2DStream as a copy of the provided one. This is
	 * usually used by {@link #create()}.
	 * 
	 * @param template
	 *            The PdfGraphics2DStream to copy.
	 */
	public PDFGraphics2D(PDFGraphics2D template) {
		super();
		
		this.document = template.document;
		this.page = template.page;
		this.stream = template.stream;
		this.hints = (RenderingHints) template.hints.clone();
		this.transform = new AffineTransform(template.transform);
		this.defaultTransform = new AffineTransform(template.defaultTransform);
		this.paint = template.paint;
		this.stroke = template.stroke;
		this.composite = template.composite;
		this.font = template.font;
		this.background = template.background;
		this.foreground = template.foreground;

		if (template.clip != null) {
			this.clip = new GeneralPath(template.clip);
		}
		else {
			this.clip = null;
		}
	}

	public void close() {
		try {
			stream.close();
		}
		catch (IOException e) {
			LOG.error("Close PDPageContentStream failed.", e);
		}

		dispose();
	}
	
	@Override
	public void draw(Shape s) {
		Color color = getColor();
		if (color == null || color.getAlpha() == 0) {
			return;
		}

		try {
			stream.saveGraphicsState();
			
			boolean opaque = color.getAlpha() == 255;
			if (!opaque) {
				applyAlpha(color.getAlpha(), OPAQUE);
			}
			
			stream.setStrokingColor(getColor());
			stream.setNonStrokingColor(getBackground());
			
			applyStroke(getStroke());
			
			PathIterator iter = s.getPathIterator(getTransform());
			processPathIterator(iter);
			
			stream.stroke();
			stream.restoreGraphicsState();
		}
		catch (IOException e) {
			LOG.error("Write PDPageContentStream failed.", e);
		}
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		boolean rendered = true;

		if (xform.getDeterminant() != 0) {
			AffineTransform inverse = null;
			try {
				inverse = xform.createInverse();
			}
			catch (NoninvertibleTransformException e) {
				throw new RuntimeException(e);
			}
			
			transform(xform);
			rendered = drawImage(img, 0, 0, null);
			transform(inverse);
		}
		else {
			AffineTransform transform = new AffineTransform(getTransform());
			transform(xform);
			rendered = drawImage(img, 0, 0, null);
			setTransform(transform);
		}

		return rendered;
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		img = op.filter(img, null);
		drawImage(img, x, y, null);
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		LOG.error("drawRenderedImage() not implemented.");
		throw new UnsupportedOperationException("drawRenderableImage() not implemented");
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		LOG.error("drawRenderableImage() not implemented.");
		throw new UnsupportedOperationException("drawRenderableImage() not implemented");
	}

	@Override
	public void drawString(String str, int x, int y) {
		drawString(str, (float) x, (float) y);
	}

	@Override
	public void drawString(String str, float x, float y) {
		AffineTransform localTransform = new AffineTransform();
		localTransform.concatenate(getTransform());
		localTransform.translate(x, y);
		localTransform.scale(1, -1);

		try {
			stream.saveGraphicsState();
			stream.setStrokingColor(getBackground());
			stream.setNonStrokingColor(getColor());

			boolean opaque = getColor().getAlpha() == 255;
			if (!opaque) {
				applyAlpha(getColor().getAlpha(), OPAQUE);
			}

			PdfFontManager fontManager = PdfFontManager.getInstance();
			PDFont pdFont = fontManager.getPdfFont(getFont(), document);

			if (pdFont == null) {
				pdFont = getPDFont();
			}

			// TODO: remove this fallback MacOS
			pdFont = PDType1Font.HELVETICA;

			// Replace &nbsp;
			str = str.replace("\u00a0", " ");

			stream.beginText();
			stream.setFont(pdFont, getFont().getSize2D());
			stream.setTextMatrix(new Matrix(localTransform));
			stream.showText(str);
			stream.endText();
			stream.restoreGraphicsState();
		}
		catch (IOException e) {
			LOG.error("Write PDPageContentStream failed.", e);
		}
	}
	
	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		drawString(iterator, (float) x, (float) y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		if (inPossibleRecursion) {
			throw new IllegalStateException("Called itself: drawString(AttributedCharacterIterator)");
		}
		else {
			inPossibleRecursion = true;
			TextLayout layout = new TextLayout(iterator, getFontRenderContext());
			layout.draw(this, x, y);
			inPossibleRecursion = false;
		}
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		Shape glyphOutline = g.getOutline(x, y);
		fill(glyphOutline);
	}

	@Override
	public void fill(Shape shape) {
		AffineTransform trans = getTransform();
		Paint paint = getPaint();

		try {
			if (paint instanceof GradientPaint) {
				GradientPaint gp = (GradientPaint) paint;
				PDShadingPattern shading = createAxialShading(gp);
				
				// Add shading to page resources.
				COSName name = page.getResources().add(shading);
				
				PDPattern pattern = new PDPattern(new PDResources(shading.getCOSObject()));
				PDColor color = new PDColor(name, pattern);
				
				stream.setNonStrokingColor(color);
				
				if (shape instanceof Rectangle2D) {
		            Rectangle2D rect = (Rectangle2D) shape;
		            stream.addRect((float) rect.getMinX(), (float) rect.getMinY(), (float) rect.getWidth(), (float) rect.getHeight());
		        }
				else {
		            PathIterator iter = shape.getPathIterator(trans);
		            processPathIterator(iter);
		            doDrawing(true, false, iter.getWindingRule() == PathIterator.WIND_EVEN_ODD);
		        }
				
				stream.closePath();
				stream.fill();
			}
			else {
				stream.saveGraphicsState();
				
				if (paint instanceof Color) {
					Color color = (Color) paint;
					
					if (color.getAlpha() == 0)
						return;
					
					// Create alpha painting command.
					boolean opaque = color.getAlpha() == 255;
					if (!opaque) {
						applyAlpha(OPAQUE, color.getAlpha());
					}
				}
				
				stream.setStrokingColor(getBackground());
				stream.setNonStrokingColor(getColor());

				PathIterator iter = shape.getPathIterator(trans);
				processPathIterator(iter);
				doDrawing(true, false, iter.getWindingRule() == PathIterator.WIND_EVEN_ODD);

				stream.restoreGraphicsState();
			}
		}
		catch (IOException e) {
			LOG.error("Write PDPageContentStream failed.", e);
		}
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		if (onStroke) {
			s = getStroke().createStrokedShape(s);
		}

		s = getTransform().createTransformedShape(s);

		return s.intersects(rect);
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return new PDFGraphicsConfiguration();
	}

	@Override
	public void setComposite(Composite comp) {
		this.composite = comp;
	}

	@Override
	public void setPaint(Paint paint) {
		if (paint == null) {
			return;
		}

		this.paint = paint;
		
		if (paint instanceof Color) {
			foreground = (Color) paint;
		}
	}

	@Override
	public void setStroke(Stroke s) {
		this.stroke = s;
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		if (hintValue != null) {
			hints.put(hintKey, hintValue);
		}
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return hints.get(hintKey);
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		this.hints.clear();
		this.hints.putAll(hints);
	}

	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		this.hints.putAll(hints);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return hints;
	}

	@Override
	public void translate(int x, int y) {
		if (x != 0 || y != 0) {
			transform.translate(x, y);
		}
	}

	@Override
	public void translate(double tx, double ty) {
		transform.translate(tx, ty);
	}

	@Override
	public void rotate(double theta) {
		transform.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		transform.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		transform.scale(sx, sy);
	}

	@Override
	public void shear(double shx, double shy) {
		transform.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform Tx) {
		transform.concatenate(Tx);
	}

	@Override
	public void setTransform(AffineTransform Tx) {
		transform = new AffineTransform(Tx);
	}

	@Override
	public AffineTransform getTransform() {
		return new AffineTransform(transform);
	}

	@Override
	public Paint getPaint() {
		return paint;
	}

	@Override
	public Composite getComposite() {
		return composite;
	}

	@Override
	public void setBackground(Color color) {
		if (color == null) {
			return;
		}

		background = color;
	}

	@Override
	public Color getBackground() {
		return background;
	}

	@Override
	public Stroke getStroke() {
		return stroke;
	}

	@Override
	public void clip(Shape s) {
		if (s != null) {
			s = transform.createTransformedShape(s);
		}

		if (clip != null) {
			Area newClip = new Area(clip);
			newClip.intersect(new Area(s));
			clip = new GeneralPath(newClip);
		}
		else {
			clip = s;
		}
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		Object antialiasingHint = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
		boolean isAntialiased = true;
		
		if (antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_ON &&
			antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {

			// If ANTIALIAS was not turned off, then use the general rendering hint.
			if (antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) {
				antialiasingHint = hints.get(RenderingHints.KEY_ANTIALIASING);

				// Test general hint.
				if (antialiasingHint != RenderingHints.VALUE_ANTIALIAS_ON &&
					antialiasingHint != RenderingHints.VALUE_ANTIALIAS_DEFAULT) {
					
					// ANTIALIAS was not requested. However, if it was not turned off explicitly, use it.
					if (antialiasingHint == RenderingHints.VALUE_ANTIALIAS_OFF) {
						isAntialiased = false;
					}
				}
			}
			else {
				isAntialiased = false;
			}
		}

		// Find out whether fractional metrics should be used.
		boolean useFractionalMetrics = true;
		
		if (hints.get(RenderingHints.KEY_FRACTIONALMETRICS) == RenderingHints.VALUE_FRACTIONALMETRICS_OFF) {
			useFractionalMetrics = false;
		}

		FontRenderContext frc = new FontRenderContext(defaultTransform, isAntialiased, useFractionalMetrics);
		return frc;
	}

	@Override
	public Graphics create() {
		return new PDFGraphics2D(this);
	}

	@Override
	public Color getColor() {
		return foreground;
	}

	@Override
	public void setColor(Color c) {
		if (c == null) {
			return;
		}

		if (paint != c) {
			setPaint(c);
		}
	}

	@Override
	public void setPaintMode() {
		if (composite != null) {
			setComposite(composite);
		}
		else {
			setComposite(AlphaComposite.SrcOver);
		}
	}

	@Override
	public void setXORMode(Color c) {
		setComposite(AlphaComposite.Xor);
	}

	@Override
	public Font getFont() {
		return font;
	}

	@Override
	public void setFont(Font font) {
		if (font != null) {
			this.font = font;
		}
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return fmg.getFontMetrics(f);
	}

	@Override
	public Rectangle getClipBounds() {
		Shape clip = getClip();
		
		if (clip == null) {
			// Return dummy rectangle, otherwise elements will not be rendered.
			clip = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}
        
		return clip.getBounds();
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		clip(new Rectangle(x, y, width, height));
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		setClip(new Rectangle(x, y, width, height));
	}

	@Override
	public Shape getClip() {
		try {
			return transform.createInverse().createTransformedShape(clip);
		}
		catch (NoninvertibleTransformException e) {
			LOG.error("Get clipping failed.", e);
			return null;
		}
	}

	@Override
	public void setClip(Shape clip) {
		if (clip != null) {
			this.clip = transform.createTransformedShape(clip);
		}
		else {
			this.clip = null;
		}
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		LOG.error("copyArea() not implemented.");
		throw new UnsupportedOperationException("copyArea() not implemented");
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		draw(line);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		Rectangle rect = new Rectangle(x, y, width, height);
        fill(rect);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		Paint paint = getPaint();
		setColor(getBackground());
		fillRect(x, y, width, height);
		setPaint(paint);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight);
        draw(rect);
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight);
        fill(rect);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
        draw(oval);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
        fill(oval);
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		Arc2D arc = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        draw(arc);
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		Arc2D arc = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        fill(arc);
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		if (nPoints > 0) {
			GeneralPath path = new GeneralPath();
			path.moveTo(xPoints[0], yPoints[0]);
			
			for (int i = 1; i < nPoints; i++)
				path.lineTo(xPoints[i], yPoints[i]);

			draw(path);
		}
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Polygon polygon = new Polygon(xPoints, yPoints, nPoints);
        draw(polygon);
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Polygon polygon = new Polygon(xPoints, yPoints, nPoints);
        fill(polygon);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		int width = img.getWidth(observer);
		int height = img.getHeight(observer);

		if (width == -1 || height == -1) {
			return false;
		}

		return drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		String key = "Im" + img.hashCode();
		
		PDXObject xobject = null;
		try {
			xobject = page.getResources().getXObject(COSName.getPDFName(key));
		}
		catch (IOException e) {
			LOG.error("Get XObject failed.", e);
		}
		
		if (xobject == null) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = image.createGraphics();
			g.setComposite(AlphaComposite.SrcOver);
			g.setBackground(new Color(1, 1, 1, 0));
			g.setPaint(new Color(1, 1, 1, 0));
			g.fillRect(0, 0, width, height);
			g.setComposite(getComposite());
			
			if (!g.drawImage(img, 0, 0, width, height, observer)) {
				return false;
			}
			g.dispose();

			xobject = createImageXObject(key, image);
		}
		
		if (xobject == null)
			return false;
		
		try {
			PDImageXObject ximage = (PDImageXObject) xobject;
			
			int w = ximage.getWidth();
			int h = ximage.getHeight();
			
			float tx = (float) getTransform().getTranslateX();
			float ty = (float) getTransform().getTranslateY();
			
			// Flip image around y-axis.
			stream.drawImage(ximage, x + tx, y + ty + h, w, -h);
		}
		catch (IOException e) {
			LOG.error("Draw PDImageXObject failed.", e);
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		LOG.error("drawImage() not implemented.");
		throw new UnsupportedOperationException("drawImage() not implemented");
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		LOG.error("drawImage() not implemented.");
		throw new UnsupportedOperationException("drawImage() not implemented");
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		LOG.error("drawImage() not implemented.");
		throw new UnsupportedOperationException("drawImage() not implemented");
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		LOG.error("drawImage() not implemented.");
		throw new UnsupportedOperationException("drawImage() not implemented");
	}

	@Override
	public void dispose() {
		page = null;
		stream = null;
		hints = null;
		transform = null;
		defaultTransform = null;
		paint = null;
		stroke = null;
		composite = null;
		font = null;
		background = null;
		foreground = null;
		clip = null;
	}
	
	protected PDFont getPDFont() {
        Font font = getFont();
        
        if (font.getFamily().equals(Font.MONOSPACED)) {
        	if (font.isBold() && font.isItalic())
        		return PDType1Font.COURIER_BOLD_OBLIQUE;
            else if (font.isBold())
            	return PDType1Font.COURIER_BOLD;
            else if (font.isItalic())
            	return PDType1Font.COURIER_OBLIQUE;
            else
            	return PDType1Font.COURIER;
        }
        
        if (font.isBold() && font.isItalic())
        	return PDType1Font.HELVETICA_BOLD_OBLIQUE;
        else if (font.isBold())
        	return PDType1Font.HELVETICA_BOLD;
        else if (font.isItalic())
        	return PDType1Font.HELVETICA_OBLIQUE;
        else
        	return PDType1Font.HELVETICA;
	}
	
	/**
     * Applies the given alpha values for filling and stroking.
     * 
     * @param nonStrokeAlpha A value between 0 and 255 for filling.
     * @param strokeAlpha A value between 0 and 255 for stroking.
     */
	protected void applyAlpha(int nonStrokeAlpha, int strokeAlpha) {
		if (nonStrokeAlpha != OPAQUE || strokeAlpha != OPAQUE) {
			checkTransparencyAllowed();
			
			// Create blending graphics state.
			PDExtendedGraphicsState extGState = new PDExtendedGraphicsState();
			extGState.getCOSObject().setName(COSName.BM, "Multiply");
			
			if (strokeAlpha != OPAQUE) {
				extGState.setStrokingAlphaConstant(strokeAlpha / 255f);
			}
			if (nonStrokeAlpha != OPAQUE) {
				extGState.setNonStrokingAlphaConstant(nonStrokeAlpha / 255f);
			}
			
			try {
				stream.setGraphicsStateParameters(extGState);
			}
			catch (IOException e) {
				LOG.error("Set graphics state parameters failed.", e);
			}
		}
	}
	
	/**
     * Apply the stroke to the PDF.
     * This takes the java stroke and outputs the appropriate settings
     * to the PDF so that the stroke attributes are handled.
     *
     * @param stroke The java stroke.
     * 
	 * @throws IOException 
     */
    protected void applyStroke(Stroke stroke) throws IOException {
    	if (stroke == null)
    		return;
    	
        if (stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke) stroke;
            AffineTransform tx = getTransform();

            float[] da = bs.getDashArray();
            if (da != null) {
            	stream.setLineDashPattern(da, bs.getDashPhase());
            }
            
            stream.setLineCapStyle(bs.getEndCap());
            stream.setLineJoinStyle(bs.getLineJoin());
            stream.setLineWidth((float) (bs.getLineWidth() * tx.getScaleX()));
            
            float ml = bs.getMiterLimit();
            if (ml > 0) {
            	stream.setMiterLimit(ml);
            }
        }
    }
	
	/**
	 * Checks if the use of transparency is allowed. PDF/A and PDF/X have no transparency.
	 */
	protected void checkTransparencyAllowed() {
		// TODO
    }
	
	/**
	 * Do the PDF drawing command. This does the PDF drawing command according to
	 * fill stroke and winding rule.
	 *
	 * @param fill true if filling the path
	 * @param stroke true if stroking the path
	 * @param nonzero true if using the non-zero winding rule
	 * 
	 * @throws IOException
	 */
    protected void doDrawing(boolean fill, boolean stroke, boolean nonzero) throws IOException {
        if (fill) {
            if (stroke) {
                if (nonzero) {
                	stream.fillAndStrokeEvenOdd();
                }
                else {
                	stream.fillAndStroke();
                }
            }
            else {
                if (nonzero) {
                	stream.fillEvenOdd();
                }
                else {
                	stream.fill();
                }
            }
        }
        else {
        	stream.stroke();
        }
    }
	
	/**
     * Processes a path iterator generating the necessary painting operations.
     * 
     * @param iter PathIterator to process.
     * 
	 * @throws IOException 
     */
	protected void processPathIterator(PathIterator iter) throws IOException {
		while (!iter.isDone()) {
			float[] vals = new float[6];
			int type = iter.currentSegment(vals);
			
			switch (type) {
				case PathIterator.SEG_CUBICTO:
					stream.curveTo(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
					break;
				case PathIterator.SEG_LINETO:
					stream.lineTo(vals[0], vals[1]);
					break;
				case PathIterator.SEG_MOVETO:
					stream.moveTo(vals[0], vals[1]);
					break;
				case PathIterator.SEG_QUADTO:
					stream.curveTo1(vals[0], vals[1], vals[2], vals[3]);
					break;
				case PathIterator.SEG_CLOSE:
					stream.closePath();
					break;
				default:
					break;
			}
			iter.next();
		}
	}
	
	protected PDShadingPattern createAxialShading(GradientPaint paint) throws IOException {
		COSArray C0 = new COSArray();
		C0.setFloatArray(paint.getColor1().getColorComponents(null));
		
		COSArray C1 = new COSArray();
		C1.setFloatArray(paint.getColor2().getColorComponents(null));
		
		COSArray domain = new COSArray();
		domain.setFloatArray(new float[] { 0, 1 });
		
		COSDictionary functionDictionary = new COSDictionary();
		functionDictionary.setInt(COSName.FUNCTION_TYPE, 2);
		functionDictionary.setInt(COSName.N, 1);
		functionDictionary.setItem(COSName.C0, C0);
		functionDictionary.setItem(COSName.C1, C1);
		functionDictionary.setItem(COSName.DOMAIN, domain);
		
		PDFunction function = new PDFunctionType2(functionDictionary);
		
		float x0 = (float) paint.getPoint1().getX();
		float y0 = (float) paint.getPoint1().getY();
		float x1 = (float) paint.getPoint2().getX();
		float y1 = (float) paint.getPoint2().getY();
		
		COSArray coords = new COSArray();
		coords.setFloatArray(new float[] { x0, y0, x1, y1 });
		
		COSArray extend = new COSArray();
		extend.add(COSBoolean.TRUE);
		extend.add(COSBoolean.TRUE);
		
		PDShadingType2 shading = new PDShadingType2(new COSDictionary());
		shading.setShadingType(PDShading.SHADING_TYPE2);
		shading.setColorSpace(PDDeviceRGB.INSTANCE);
		shading.setFunction(function);
		shading.setCoords(coords);
		shading.setExtend(extend);
		
		PDShadingPattern pattern = new PDShadingPattern();
		pattern.setMatrix(new AffineTransform());
		pattern.setShading(shading);
		
		return pattern;
	}
	
	protected PDImageXObject createImageXObject(String key, BufferedImage image) {
		PDImageXObject ximage = null;
		
		try {
			ximage = LosslessFactory.createFromImage(document, image);
		}
		catch (IOException e) {
			LOG.error("Create PDImageXObject failed.", e);
		}
		
		return ximage;
	}
	
}
