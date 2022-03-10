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

package org.lecturestudio.presenter.api.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.SizeRequirements;
import javax.swing.text.BadLocationException;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;
import javax.swing.text.html.StyleSheet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.PieStyler.AnnotationType;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.theme.GGPlot2Theme;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.pdf.PdfFontManager;
import org.lecturestudio.core.pdf.pdfbox.PDFGraphics2D;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;

/**
 * PDF document factory.
 *
 * @author Alex Andres
 */
public class PdfFactory {

	public static StyleSheet CUSTOM_STYLESHEET = new StyleSheet();

	private static final int PAGE_WIDTH = 640;

	private static final int PAGE_HEIGHT = 480;

	private static final int CONTENT_X = 30;

	private static final int CONTENT_Y = 10;

	private static final int FONT_SIZE = 16;


	static {
		// Load LaTeX fonts.
		PdfFontManager fontManager = PdfFontManager.getInstance();

		try {
			String[] listing = FileUtils
					.getResourceListing("org/scilab/forge/jlatexmath/fonts",
							(name) -> name.endsWith(".ttf"));

			for (String filePath : listing) {
				fontManager.addFontFile(filePath);
			}
		} catch (Exception e) {
			throw new RuntimeException("Load LaTeX fonts failed", e);
		}
	}

	public static PdfDocument createMessageDocument(final String message) throws Exception {
		final PdfDocument pdfDocument = new PdfDocument();

		createTextPage(pdfDocument, message);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		pdfDocument.toOutputStream(stream);
		stream.flush();
		stream.close();

		pdfDocument.close();

		return new PdfDocument(stream.toByteArray());
	}

	public static PdfDocument createQuizDocument(Dictionary dict, QuizResult result) throws Exception {
		PdfDocument pdfDocument = new PdfDocument();
		Quiz quiz = result.getQuiz();
		QuizType type = quiz.getType();

		createQuestionPage(pdfDocument, quiz);
		createBarChartResultPage(dict, pdfDocument, result);
		createPieChartResultPage(dict, pdfDocument, result);

		if (type == QuizType.MULTIPLE) {
			createBarChartAnswerStatsPage(dict, pdfDocument, result);
		}

		// Flush the PDF to a stream and recreate it.
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		pdfDocument.toOutputStream(stream);
		stream.flush();
		stream.close();

		// DEBUG
//		FileOutputStream fstream = new FileOutputStream("quiz.pdf");
//		pdfDocument.toOutputStream(fstream);
//		fstream.flush();
//		fstream.close();

		pdfDocument.close();

		return new PdfDocument(stream.toByteArray());
	}

	private static void createChartPage(PdfDocument document, QuizResult result, Chart<?, ?> chart) {
		if (result.getResult().isEmpty()) {
			return;
		}

		int pageIndex = document.createPage();

		// Draw chart below last text line.
		double textEndY = 0;
		double margin = 20;

		// Set (bar)chart y-axis tick spacing.
		if (chart instanceof CategoryChart) {
			CategoryChart catChart = (CategoryChart) chart;
			Map<String, CategorySeries> seriesMap = catChart.getSeriesMap();
			double yMax = 0;

			for (String key : seriesMap.keySet()) {
				CategorySeries series = seriesMap.get(key);

				yMax = Math.max(yMax, series.getYMax());
			}

			int ySpacing = (int) Math.max(PAGE_HEIGHT / 10.0, PAGE_HEIGHT / yMax);

			catChart.getStyler().setYAxisTickMarkSpacingHint(ySpacing);
		}

		PDFGraphics2D g2dStream = (PDFGraphics2D) document.createPageGraphics2D(pageIndex);
		g2dStream.translate(0, textEndY);
		chart.paint(g2dStream, PAGE_WIDTH, (int) (PAGE_HEIGHT - margin - textEndY));
		g2dStream.close();
	}

	private static void createTextPage(final PdfDocument document, final String text) {
		int pageIndex = document.createPage();

		PDFGraphics2D g2dStream = (PDFGraphics2D) document.createPageGraphics2D(pageIndex);
		renderTextPage(g2dStream, text);
		g2dStream.close();
	}

	private static void createQuestionPage(PdfDocument document, Quiz quiz) {
		int pageIndex = document.createPage();

		PDFGraphics2D g2dStream = (PDFGraphics2D) document.createPageGraphics2D(pageIndex);
		renderQuestionPage(g2dStream, quiz, FONT_SIZE, CONTENT_X, CONTENT_Y);
		g2dStream.close();
	}

	private static void createBarChartResultPage(Dictionary dict, PdfDocument document, QuizResult result) {
		CategoryChart chart = createBarChart(dict, result);
		createChartPage(document, result, chart);
	}

	private static void createPieChartResultPage(Dictionary dict, PdfDocument document, QuizResult result) {
		PieChart chart = createPieChart(dict, result);
		createChartPage(document, result, chart);
	}

	private static void createBarChartAnswerStatsPage(Dictionary dict, PdfDocument document, QuizResult result) {
		CategoryChart chart = createBarChartAnswerStats(dict, result);
		createChartPage(document, result, chart);
	}

	private static PieChart createPieChart(Dictionary dict, QuizResult result) {
		PieChart chart = new PieChartBuilder().theme(ChartTheme.GGPlot2).build();
		chart.getStyler().setAnnotationType(AnnotationType.LabelAndPercentage);
		chart.getStyler().setAnnotationDistance(1.1);
		chart.getStyler().setPlotContentSize(0.75);
		chart.getStyler().setPlotBackgroundColor(Color.WHITE);

		Map<QuizAnswer, Integer> resultMap = result.getResult();

		for (QuizAnswer answer : resultMap.keySet()) {
			chart.addSeries(result.getAnswerText(answer), resultMap.get(answer));
		}

		return chart;
	}

	private static CategoryChart createBarChart(Dictionary dict, QuizResult result) {
		CategoryChart chart = new CategoryChartBuilder().theme(ChartTheme.XChart).build();
		chart.setXAxisTitle(dict.get("quiz.options"));
		chart.setYAxisTitle(dict.get("quiz.answers"));
		chart.getStyler().setOverlapped(true);
		chart.getStyler().setChartBackgroundColor(Color.WHITE);
		chart.getStyler().setLegendBorderColor(Color.WHITE);
		chart.getStyler().setXAxisTicksVisible(false);
		chart.getStyler().setSeriesColors(new GGPlot2Theme().getSeriesColors());

		Map<QuizAnswer, Integer> resultMap = result.getResult();
		int[] xValues = new int[resultMap.size()];
		int index = 0;

		for (int i = 0; i < xValues.length; i++) {
			xValues[i] = i;
		}

		for (QuizAnswer answer : resultMap.keySet()) {
			int[] yValues = new int[resultMap.size()];
			yValues[index] = resultMap.get(answer);

			chart.addSeries(result.getAnswerText(answer), xValues, yValues);

			index++;
		}

		return chart;
	}

	private static CategoryChart createBarChartAnswerStats(Dictionary dict, QuizResult result) {
		Quiz quiz = result.getQuiz();

		CategoryChart chart = new CategoryChartBuilder().theme(ChartTheme.XChart).build();
		chart.setXAxisTitle(dict.get("quiz.options"));
		chart.setYAxisTitle(dict.get("quiz.answers"));
		chart.getStyler().setOverlapped(true);
		chart.getStyler().setChartBackgroundColor(Color.WHITE);
		chart.getStyler().setLegendBorderColor(Color.WHITE);
		chart.getStyler().setXAxisTicksVisible(false);
		chart.getStyler().setSeriesColors(new GGPlot2Theme().getSeriesColors());

		Map<QuizAnswer, Integer> resultMap = result.getResult();
		Map<String, Integer> chartMap = new HashMap<>();

		for (QuizAnswer answer : resultMap.keySet()) {
			String[] options = answer.getOptions();

			if (options.length > 0) {
				for (String o : answer.getOptions()) {
					String key = quiz.getOptionAlpha(o);
					Integer count = resultMap.get(answer);

					Integer value = chartMap.get(key);
					if (value != null) {
						count += value;
					}

					chartMap.put(key, count);
				}
			} else {
				String key = "{ }";
				Integer count = resultMap.get(answer);
				Integer value = chartMap.get(key);

				if (value != null) {
					count += value;
				}

				chartMap.put(key, count);
			}
		}

		if (chartMap.isEmpty()) {
			// Create an empty chart.
			chart.addSeries(" ", new int[]{0}, new int[]{0});
		} else {
			int[] xValues = new int[chartMap.size()];
			int index = 0;

			for (int i = 0; i < xValues.length; i++) {
				xValues[i] = i;
			}

			for (String answer : chartMap.keySet()) {
				int[] yValues = new int[chartMap.size()];
				yValues[index] = chartMap.get(answer);

				chart.addSeries(answer, xValues, yValues);

				index++;
			}
		}

		return chart;
	}

	private static void renderTextPage(final Graphics2D context, final String text) {
		org.jsoup.nodes.Document jdoc = Jsoup.parseBodyFragment(String.format("<div style=\"text-align: center; margin-right: 50px;\">%s</div>", text));
		jdoc.outputSettings().prettyPrint(false);

		renderHtml(jdoc.html(), context, CONTENT_X, CONTENT_Y);
	}

	private static void renderQuestionPage(Graphics2D context, Quiz quiz, int fontSize, int x, int y) {
		String question = quiz.getQuestion();

		org.jsoup.nodes.Document jdoc = Jsoup.parseBodyFragment(question);
		jdoc.outputSettings().prettyPrint(false);

		List<String> options = quiz.getOptions();

		// Add options below question.
		if (options.size() > 0) {
			Element dl = jdoc.body().appendElement("ul");
			String prefix = "";

			for (int i = 0; i < options.size(); i++) {
				if (quiz.getType() != QuizType.NUMERIC) {
					prefix = quiz.getOptionAlpha(i + "") + ")&nbsp;";
				}

				dl.append("<p>" + prefix + options.get(i) + "</p>");
			}
		}

		renderHtml(jdoc.html(), context, x, y);
	}

	private static void renderHtml(String html, Graphics2D context, int x, int y) {
		context.translate((float) x, (float) y);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setSize(PAGE_WIDTH - 2 * x, PAGE_HEIGHT);

		HTMLEditorKit kit = new MyHTMLEditorKit();
		editorPane.setEditorKitForContentType("text/html", kit);
		editorPane.setContentType("text/html");

		// Back up old style sheet, since it is stored statically.
		StyleSheet defStyleSheet = kit.getStyleSheet();
		// Remove custom style sheet used by the HTMLEditor.
		defStyleSheet.removeStyleSheet(CUSTOM_STYLESHEET);

		StyleSheet styleSheet = new StyleSheet();
		styleSheet.addStyleSheet(defStyleSheet);
		styleSheet.addRule(
				String.format("body { color:#000; font-family:Arial; font-size: %dpx; margin: 0px; }", FONT_SIZE));
		styleSheet.addRule("ol { padding:5px; }");
		styleSheet.addRule("ul p { margin-left: 50px; }");
		styleSheet.addRule(String.format("tt {font-size: %dpx; }", FONT_SIZE - 2));
		styleSheet.addRule(
				String.format("code {background: #DAE6E6; font-size: %dpx; font-family:Monospace; }", FONT_SIZE - 2));

		kit.setStyleSheet(styleSheet);

		editorPane.setDocument(kit.createDefaultDocument());
		editorPane.setText(html);
		editorPane.paint(context);

		// Restore style sheet.
		defStyleSheet.addStyleSheet(CUSTOM_STYLESHEET);
		kit.setStyleSheet(defStyleSheet);

		context.translate((float) -x, (float) -y);
	}


	private static class MyViewFactory extends HTMLEditorKit.HTMLFactory {
		@Override
		public View create(javax.swing.text.Element e) {
			View view = super.create(e);

			if (view instanceof ImageView) {
				((ImageView) view).setLoadsSynchronously(true);
			} else if (view instanceof InlineView) {
				// Letter wrap for HTML in JEditorPane.
				return new InlineView(e) {
					@Override
					public int getBreakWeight(int axis, float pos, float len) {
						return GoodBreakWeight;
					}

					@Override
					public View breakView(int axis, int p0, float pos, float len) {
						if (axis == View.X_AXIS) {
							checkPainter();

							int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
							final int constBreak = p1 - 6;
							if (p0 == getStartOffset() && p1 == getEndOffset()) {
								return this;
							}

							// Implement word-wrap.
							if (p1 != 0) {
								try {
									String strChar = getDocument().getText(p1 + 1, 1);

									if (strChar != "\u00a0" || strChar != " ") {
										// Find previous whitespace.
										boolean foundWhitespace = false;
										while (!foundWhitespace) {
											p1--;
											strChar = getDocument().getText(p1, 1);

											foundWhitespace = strChar.equals("\u00a0") || strChar.equals(" ");
										}
									}
								} catch (BadLocationException e) {
									e.printStackTrace();
									p1 = constBreak;
								}
							}

							return createFragment(p0, p1);
						}
						return this;
					}
				};
			} else if (view instanceof ParagraphView) {
				// Letter wrap for HTML in JEditorPane.
				return new ParagraphView(e) {
					@Override
					protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
						if (r == null) {
							r = new SizeRequirements();
						}

						float pref = layoutPool.getPreferredSpan(axis);
						float min = layoutPool.getMinimumSpan(axis);

						// Don't include insets, Box.getXXXSpan will include them.
						r.minimum = (int) min;
						r.preferred = Math.max(r.minimum, (int) pref);
						r.maximum = Integer.MAX_VALUE;
						r.alignment = 0.5f;
						return r;
					}
				};
			}

			return view;
		}
	}


	private static class MyHTMLEditorKit extends HTMLEditorKit {
		private static final long serialVersionUID = -3654502936136295085L;

		@Override
		public ViewFactory getViewFactory() {
			return new MyViewFactory();
		}
	}

}
