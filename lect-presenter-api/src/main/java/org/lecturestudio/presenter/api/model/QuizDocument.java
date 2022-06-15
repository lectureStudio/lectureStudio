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

package org.lecturestudio.presenter.api.model;

import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.colors.SeriesColors;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.pdf.pdfbox.PDFGraphics2D;
import org.lecturestudio.presenter.api.util.NumericStringComparator;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;

/**
 * PDF-based quiz document that contains the quiz question and charts showing
 * the result of all received quiz answers.
 *
 * @author Alex Andres
 */
public class QuizDocument extends HtmlToPdfDocument {

	private static final NumericStringComparator NS_COMPARATOR = new NumericStringComparator();


	public QuizDocument(File templateFile, Rectangle2D contentBounds,
			Dictionary dict, QuizResult result) throws IOException {
		init(createDocument(templateFile, contentBounds, dict, result));
		setDocumentType(DocumentType.QUIZ);
		setTitle(dict.get("quiz"));
	}

	private static PdfDocument createDocument(File templateFile,
			Rectangle2D contentBounds, Dictionary dict, QuizResult result)
			throws IOException {
		PDDocument tplDoc = templateFile.exists() ?
				PDDocument.load(templateFile) :
				null;
		PDDocument doc = new PDDocument();

		Quiz quiz = result.getQuiz();
		QuizType type = quiz.getType();

		// Create the first page with the question on it.
		renderQuestion(tplDoc, doc, contentBounds, quiz);

		if (!result.getResult().isEmpty()) {
			if (type == QuizType.MULTIPLE) {
				// Create a new page with the statistics bar-chart.
				renderChartQuestions(tplDoc, doc, contentBounds, quiz);
				renderChart(tplDoc, doc, result,
						createBarChartAnswerStats(dict, result), contentBounds);
			}

			// Create a new page with the bar-chart.
			renderChartQuestions(tplDoc, doc, contentBounds, quiz);
			renderChart(tplDoc, doc, result, createBarChart(dict, result),
					contentBounds);

			// Create a new page with the pie-chart.
			renderChartQuestions(tplDoc, doc, contentBounds, quiz);
			renderChart(tplDoc, doc, result, createPieChart(dict, result),
					contentBounds);
		}

		PdfDocument pdfDocument = createPdfDocument(doc);

		if (nonNull(tplDoc)) {
			tplDoc.close();
		}

		doc.close();

		return pdfDocument;
	}

	private static void renderQuestion(PDDocument tplDoc, PDDocument doc,
			Rectangle2D contentBounds, Quiz quiz) throws IOException {
		String question = quiz.getQuestion().replaceAll("&nbsp;", " ");

		var jdoc = Jsoup.parseBodyFragment(question);
		jdoc.head().append("<link rel=\"stylesheet\" href=\"html/quiz.css\">");
		jdoc.outputSettings().prettyPrint(true);

		Map<String, String> resourceMap = new HashMap<>();

		Elements images = jdoc.getElementsByTag("img");
		for (Element e : images) {
			String src = e.absUrl("src");
			File imgFile = new File(URI.create(src).getPath());
			String newPath = Paths.get("quiz", imgFile.getName()).toString()
					.replaceAll("\\\\", "/");

			// Replace by new relative web-root path.
			e.attr("src", newPath);

			resourceMap.put(newPath, src);
		}

		List<String> options = quiz.getOptions();

		// Add options below question.
		if (options.size() > 0) {
			Element uList = jdoc.body().appendElement("ul");
			uList.addClass("options");

			String prefix = "";

			for (int i = 0; i < options.size(); i++) {
				if (quiz.getType() != QuizType.NUMERIC) {
					prefix = quiz.getOptionAlpha(i + "") + ") ";
				}

				Element item = uList.appendElement("p");
				item.text(prefix + options.get(i));
			}
		}

		renderHtmlPage(jdoc, tplDoc, doc, contentBounds, resourceMap);
	}

	private static boolean textFits(String text, Dimension areaToFit) {
		Font font = new Font("Helvetica", Font.PLAIN, 12);

		AttributedString as = new AttributedString(text);
		as.addAttribute(TextAttribute.FONT, font);

		AttributedCharacterIterator aci = as.getIterator();

		int start = aci.getBeginIndex();
		int end = aci.getEndIndex();

		LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(aci,
				new FontRenderContext(null, false, false));

		float width = (float) areaToFit.width;
		float height = 0;
		lineBreakMeasurer.setPosition(start);

		while (lineBreakMeasurer.getPosition() < end) {
			TextLayout textLayout = lineBreakMeasurer.nextLayout(width);
			height += textLayout.getAscent();
			height += textLayout.getDescent() + textLayout.getLeading();
		}

		return height <= areaToFit.getHeight();
	}

	private static String makeTextFit(String text, Dimension areaToFit) {
		if (!textFits(text, areaToFit)) {
			// Remove last word and try again.
			List<String> words = new ArrayList<>(List.of(text.split("\\s+")));
			words.remove(words.size() - 1);

			return makeTextFit(String.join(" ", words), areaToFit);
		}

		return text;
	}

	private static void renderChartQuestions(PDDocument tplDoc, PDDocument doc,
			Rectangle2D contentBounds, Quiz quiz) throws IOException {
		List<String> options = quiz.getOptions();

		if (options.isEmpty()) {
			return;
		}

		Rectangle2D pageBounds = getPageBounds(tplDoc);
		double width = contentBounds.getWidth() * pageBounds.getWidth();
		double height = contentBounds.getHeight() * pageBounds.getHeight();
		int descHeight = (int) (height / 1.9 * 1.5);

		var jdoc = Jsoup.parseBodyFragment("");
		jdoc.head().append("<link rel=\"stylesheet\" href=\"html/quiz.css\">");
		jdoc.outputSettings().prettyPrint(true);

		Element div = jdoc.body().appendElement("div");
		div.addClass("chart");
		div.attr("style", String.format("padding-top: %dpx;", descHeight));

		Element table = div.appendElement("table");
		Element row = null;

		int maxWidth = (int) (width / 2);
		int maxHeight = 16;    // Option text not longer than one line.

		if (options.size() < 7) {
			// Allow option text to be two lines long.
			maxHeight = 32;
		}

		for (int i = 0; i < options.size(); i++) {
			if (i % 2 == 0) {
				row = table.appendElement("tr");
			}

			Element data = row.appendElement("td");
			Element tdDiv = data.appendElement("div");

			String prefix = "";
			String text = options.get(i);

			if (quiz.getType() != QuizType.NUMERIC) {
				prefix = quiz.getOptionAlpha(i + "") + ")";
			}

			Dimension textSize = new Dimension(maxWidth, maxHeight);

			if (!textFits(text, textSize)) {
				text = makeTextFit(text, textSize);
				text = text.substring(0, text.length() - 3) + "...";
			}

			tdDiv.text(String.format("%s %s", prefix, text));
		}

		renderHtmlPage(jdoc, tplDoc, doc, contentBounds, Map.of());
	}

	private static void renderChart(PDDocument tplDoc, PDDocument doc,
			QuizResult result, Chart<?, ?> chart, Rectangle2D contentBounds) {
		if (result.getResult().isEmpty()) {
			return;
		}

		int pageIndex = doc.getNumberOfPages() - 1;

		Rectangle2D pageBounds = getPageBounds(tplDoc);

		int marginX = (int) (contentBounds.getX() * pageBounds.getWidth());
		int marginY = (int) (contentBounds.getY() * pageBounds.getHeight());
		int chartWidth = (int) (contentBounds.getWidth() * pageBounds.getWidth());
		int chartHeight = (int) (contentBounds.getHeight() * pageBounds.getHeight() / 1.75);

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

		PDPage pdPage = doc.getPage(pageIndex);

		PDFGraphics2D g2dStream = new PDFGraphics2D(doc, pdPage, true);
		// Move to top-left corner.
		g2dStream.transform(new AffineTransform(1, 0, 0, -1, 0, pdPage.getMediaBox().getHeight()));
		g2dStream.translate(marginX, marginY);
		chart.paint(g2dStream, chartWidth, chartHeight);
		g2dStream.close();
	}

	private static PieChart createPieChart(Dictionary dict, QuizResult result) {
		PieChart chart = new PieChartBuilder().theme(ChartTheme.GGPlot2).build();
		chart.getStyler().setPlotContentSize(0.75);
		chart.getStyler().setPlotBackgroundColor(Color.WHITE);
		chart.getStyler().setSeriesColors(new ChartColors().getSeriesColors());

		Map<QuizAnswer, Integer> resultMap = result.getResult();
		Collection<QuizAnswer> answers = getSortedAnswers(result);

		for (QuizAnswer answer : answers) {
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
		chart.getStyler().setAxisTicksLineVisible(false);
		chart.getStyler().setSeriesColors(new ChartColors().getSeriesColors());

		Map<QuizAnswer, Integer> resultMap = result.getResult();
		int[] xValues = new int[resultMap.size()];
		int index = 0;

		for (int i = 0; i < xValues.length; i++) {
			xValues[i] = i;
		}

		Collection<QuizAnswer> answers = getSortedAnswers(result);

		for (QuizAnswer answer : answers) {
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
		chart.getStyler().setAxisTicksLineVisible(false);
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

	private static Collection<QuizAnswer> getSortedAnswers(QuizResult result) {
		Map<QuizAnswer, Integer> resultMap = result.getResult();

		if (result.getQuiz().getType() == QuizType.NUMERIC) {
			// Sort options.
			Map<String, QuizAnswer> sortedMap = new TreeMap<>(NS_COMPARATOR);

			for (QuizAnswer answer : resultMap.keySet()) {
				sortedMap.put(result.getAnswerText(answer), answer);
			}

			return sortedMap.values();
		}

		return resultMap.keySet();
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
}
