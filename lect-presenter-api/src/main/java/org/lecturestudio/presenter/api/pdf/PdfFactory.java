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

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.colors.SeriesColors;

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
			String[] listing = FileUtils.getResourceListing(
					"org/scilab/forge/jlatexmath/fonts",
					(name) -> name.endsWith(".ttf"));

			for (String filePath : listing) {
				fontManager.addFontFile(filePath);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Load LaTeX fonts failed", e);
		}
	}

	public static void createMessagePage(PdfDocument doc, final String message) {
		Document jdoc = Document.createShell("");
		jdoc.outputSettings().prettyPrint(false);
		jdoc.body().addClass("chat-message");

		String[] parts = message.split("\n");

		for (String part : parts) {
			if (part.equals("\n")) {
				continue;
			}

			// Search for URLs in the text.
			UrlDetector parser = new UrlDetector(part, UrlDetectorOptions.Default);
			List<Url> found = parser.detect();

			// Each line is encapsulated in a <div>.
			Element div = jdoc.body().appendElement("div");

			if (found.isEmpty()) {
				div.text(part);
			}
			else {
				for (Url url : found) {
					String orig = url.getOriginalUrl();
					int origIndex = part.indexOf(orig);
					String s = part.substring(0, origIndex);
					part = part.substring(origIndex + orig.length());

					// Raw text belongs into a <span> element.
					div.appendElement("span").text(s);

					// Create the link.
					Element a = div.appendElement("a");
					a.attr("href", orig);
					a.attr("target", "_blank");
					a.text(orig);
				}

				// Add remaining raw text.
				if (!part.isEmpty() || !part.isBlank()) {
					div.appendElement("span").text(part);
				}
			}
		}

		PDFGraphics2D g2dStream = (PDFGraphics2D) doc.createPageGraphics2D(0);
		renderHtml(jdoc.html(), g2dStream, CONTENT_X, CONTENT_Y);
		g2dStream.close();
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

		int chartHeight = PAGE_HEIGHT - PAGE_HEIGHT / 4;
		int pageIndex = document.createPage();

		// Draw chart below last text line.
		int margin = 20;

		// Set (bar)chart y-axis tick spacing.
		if (chart instanceof CategoryChart) {
			CategoryChart catChart = (CategoryChart) chart;
			Map<String, CategorySeries> seriesMap = catChart.getSeriesMap();
			double yMax = 0;

			for (String key : seriesMap.keySet()) {
				CategorySeries series = seriesMap.get(key);

				yMax = Math.max(yMax, series.getYMax());
			}

			int ySpacing = (int) Math.max(chartHeight / 10.0, chartHeight / yMax);

			catChart.getStyler().setYAxisTickMarkSpacingHint(ySpacing);
		}

		PDFGraphics2D g2dStream = (PDFGraphics2D) document.createPageGraphics2D(pageIndex);
		g2dStream.translate(0, 0);
		chart.paint(g2dStream, PAGE_WIDTH, chartHeight - margin);
		g2dStream.close();

		g2dStream = (PDFGraphics2D) document.createAppendablePageGraphics2D(pageIndex);
		renderChartQuestions(g2dStream, result.getQuiz(), 0, chartHeight - margin);
		g2dStream.close();
	}

	private static void renderChartQuestions(Graphics2D context, Quiz quiz, int x, int y) {
		List<String> options = quiz.getOptions();

		if (options.size() < 1) {
			return;
		}

		// Add options below question.
		Document jdoc = Document.createShell("");
		jdoc.outputSettings().prettyPrint(false);

		Element table = jdoc.body().appendElement("table");
		Element row = null;
		String prefix = "";

		for (int i = 0; i < options.size(); i++) {
			if (i % 2 == 0) {
				row = table.appendElement("tr");
			}

			Element data = row.appendElement("td");

			if (quiz.getType() != QuizType.NUMERIC) {
				prefix = quiz.getOptionAlpha(i + "") + ") ";
			}

			data.text(prefix + options.get(i));
		}

		renderHtml(jdoc.html(), context, x, y);
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
		chart.getStyler().setPlotContentSize(0.75);
		chart.getStyler().setPlotBackgroundColor(Color.WHITE);
		chart.getStyler().setSeriesColors(new ChartColors().getSeriesColors());

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
		chart.getStyler().setSeriesColors(new ChartColors().getSeriesColors());

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
		chart.getStyler().setSeriesColors(new ChartColors().getSeriesColors());

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
			}
			else {
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
			chart.addSeries(" ", new int[] { 0 }, new int[] { 0 });
		}
		else {
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

		HTMLEditorKit kit = new MyHTMLEditorKit();

		JEditorPane editorPane = new JEditorPane();
		editorPane.setSize(PAGE_WIDTH - 2 * x, PAGE_HEIGHT);
		editorPane.setEditorKitForContentType("text/html", kit);
		editorPane.setContentType("text/html");

		// Back up old style sheet, since it is stored statically.
		StyleSheet defStyleSheet = kit.getStyleSheet();
		// Remove custom style sheet used by the HTMLEditor.
		defStyleSheet.removeStyleSheet(CUSTOM_STYLESHEET);

		StyleSheet styleSheet = new StyleSheet();
		styleSheet.addStyleSheet(defStyleSheet);
		styleSheet.addRule("body {background: #ffffff; color:#000; font-family:Arial; font-size:" + (FONT_SIZE * 6) + "px; margin: 0px; }");
		styleSheet.addRule("* {background: #ffffff; color:#000; }");
		styleSheet.addRule("a { margin: 0px; padding: 0px; }");
		styleSheet.addRule("ul { margin-left: 10px; padding: 0px; }");
		styleSheet.addRule("tt {font-size:" + (FONT_SIZE - 2) + "px; }");
		styleSheet.addRule("code {background: #DAE6E6; font-size:" + (FONT_SIZE - 2) + "px; font-family:Monospace; }");
		styleSheet.addRule("table {width: 100%; }");
		styleSheet.addRule("table td {background: #ffffff; }");
		styleSheet.addRule("tr {width: 50%; }");
		styleSheet.addRule("td {width: 50%; }");
		styleSheet.addRule(".chat-message { margin-top: 50px; text-align: center; }");

		kit.setStyleSheet(styleSheet);

		editorPane.setDocument(kit.createDefaultDocument());
		editorPane.setText(html);
		editorPane.paint(context);

		// Restore style sheet.
		defStyleSheet.addStyleSheet(CUSTOM_STYLESHEET);
		kit.setStyleSheet(defStyleSheet);

		context.translate((float) -x, (float) -y);
	}



	private static class ChartColors implements SeriesColors {

		public static final Color C1 = Color.decode("#003f5c");
		public static final Color C2 = Color.decode("#008861");
		public static final Color C3 = Color.decode("#77afaf");
		public static final Color C4 = Color.decode("#d45087");
		public static final Color C5 = Color.decode("#ff7c43");
		public static final Color C6 = Color.decode("#ffa600");
		public static final Color C7 = Color.decode("#aaa900");
		public static final Color C8 = Color.decode("#7e38ff");

		private final Color[] seriesColors;


		public ChartColors() {
			seriesColors = new Color[] { C1, C2, C3, C4, C5, C6, C7, C8 };
		}

		@Override
		public Color[] getSeriesColors() {
			return seriesColors;
		}
	}



	private static class MyViewFactory extends HTMLEditorKit.HTMLFactory {

		@Override
		public View create(javax.swing.text.Element e) {
			View view = super.create(e);

			if (view instanceof ImageView) {
				((ImageView) view).setLoadsSynchronously(true);
			}
			else if (view instanceof InlineView) {
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
								}
								catch (BadLocationException e) {
									e.printStackTrace();
									p1 = constBreak;
								}
							}

							return createFragment(p0, p1);
						}
						return this;
					}
				};
			}
			else if (view instanceof ParagraphView) {
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
