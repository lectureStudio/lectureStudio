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

import com.openhtmltopdf.extend.FSDOMMutator;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
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
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.pdf.pdfbox.PDFGraphics2D;
import org.lecturestudio.presenter.api.util.NumericStringComparator;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * PDF-based quiz document that contains the quiz question and charts showing
 * the result of all received quiz answers.
 *
 * @author Alex Andres
 */
public class QuizDocument extends Document {

	private static final NumericStringComparator NS_COMPARATOR = new NumericStringComparator();

	private static final int PAGE_WIDTH = 640;

	private static final int PAGE_HEIGHT = 480;


	public QuizDocument(Dictionary dict, QuizResult result) throws IOException {
		init(createDocument(dict, result));
		setDocumentType(DocumentType.QUIZ);
		setTitle("Quiz");
	}

	private static PdfDocument createDocument(Dictionary dict, QuizResult result)
			throws IOException {
		Quiz quiz = result.getQuiz();
		QuizType type = quiz.getType();

		PDDocument doc = new PDDocument();

		// Create the first page with the question on it.
		renderQuestion(doc, quiz);

		if (!result.getResult().isEmpty()) {
			// Create a new page with the bar-chart.
			renderChartQuestions(doc, quiz);
			renderChart(doc, result, createBarChart(dict, result));

			// Create a new page with the pie-chart.
			renderChartQuestions(doc, quiz);
			renderChart(doc, result, createPieChart(dict, result));

			if (type == QuizType.MULTIPLE) {
				// Create a new page with the statistics bar-chart.
				renderChartQuestions(doc, quiz);
				renderChart(doc, result, createBarChartAnswerStats(dict, result));
			}
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		doc.save(stream);
		doc.close();

		stream.flush();
		stream.close();

		return new PdfDocument(stream.toByteArray());
	}

	private static void renderQuestion(PDDocument pdDocument, Quiz quiz)
			throws IOException {
		var jdoc = Jsoup.parseBodyFragment(quiz.getQuestion());
		jdoc.body().attr("style", "font-family: Helvetica, Sans-Serif;");
		jdoc.outputSettings().prettyPrint(true);

		List<String> options = quiz.getOptions();

		// Add options below question.
		if (options.size() > 0) {
			Element uList = jdoc.body().appendElement("ul");
			uList.attr("style", "padding-top: 10px;");
			String prefix = "";

			for (int i = 0; i < options.size(); i++) {
				if (quiz.getType() != QuizType.NUMERIC) {
					prefix = quiz.getOptionAlpha(i + "") + ")&nbsp;";
				}

				uList.append("<p>" + prefix + options.get(i) + "</p>");
			}
		}

		renderHtmlPage(jdoc, pdDocument);
	}

	private static void renderChartQuestions(PDDocument pdDocument, Quiz quiz)
			throws IOException {
		List<String> options = quiz.getOptions();

		if (options.isEmpty()) {
			return;
		}

		var jdoc = Jsoup.parseBodyFragment("");
		jdoc.body().attr("style", "font-family: Helvetica, Sans-Serif;");
		jdoc.outputSettings().prettyPrint(true);

		Element div = jdoc.body().appendElement("div");
		div.attr("style", "padding-top: 400px;");

		Element table = div.appendElement("table");
		table.attr("style", "width: 100%; border-collapse: collapse;");

		Element row = null;

		String prefix = "";
		String suffix = "";
		int maxLength = 40;	// Option text not longer than one line.

		if (options.size() < 7) {
			// Allow option text to be two lines long.
			maxLength = 95;
		}

		for (int i = 0; i < options.size(); i++) {
			if (i % 2 == 0) {
				row = table.appendElement("tr");
			}

			Element data = row.appendElement("td");
			data.attr("style", "padding: 0 0.7em 0.7em 0;");

			String text = options.get(i);
			int textLength = text.length();

			if (quiz.getType() != QuizType.NUMERIC) {
				prefix = quiz.getOptionAlpha(i + "") + ")";
			}
			if (textLength > maxLength) {
				text = text.substring(0, maxLength);
				suffix = "...";
			}

			data.text(String.format("%s %s%s", prefix, text, suffix));
		}

		renderHtmlPage(jdoc, pdDocument);
	}

	private static void renderChart(PDDocument pdDocument, QuizResult result, Chart<?, ?> chart) {
		if (result.getResult().isEmpty()) {
			return;
		}

		int chartHeight = (int) (PAGE_HEIGHT * 0.75);
		int pageIndex = pdDocument.getNumberOfPages() - 1;

		// Draw chart below last text line.
		int marginX = 50;
		int marginY = 35;

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

		PDPage pdPage = pdDocument.getPage(pageIndex);

		PDFGraphics2D g2dStream = new PDFGraphics2D(pdDocument, pdPage, true);
		// Move to top-left corner.
		g2dStream.transform(new AffineTransform(1, 0, 0, -1, 0, pdPage.getMediaBox().getHeight()));
		g2dStream.translate(marginX, marginY);
		chart.paint(g2dStream, PAGE_WIDTH - 2 * marginX, chartHeight - 2 * marginY);
		g2dStream.close();
	}

	private static void renderHtmlPage(org.jsoup.nodes.Document jdoc, PDDocument pdDoc)
			throws IOException {
		System.out.println(jdoc.html());

		FSDOMMutator domChanger = (doc) -> {
			NodeList fontTags = doc.getElementsByTagName("font");

			for (int i = 0; i < fontTags.getLength(); i++) {
				org.w3c.dom.Element fontTag = (org.w3c.dom.Element) fontTags.item(i);

				if (fontTag.hasAttribute("color")) {
					String color = fontTag.getAttribute("color");

					if (!fontTag.hasAttribute("style")) {
						fontTag.setAttribute("style", "color: " + color + ';');
					}
					else {
						String oldStyle = fontTag.getAttribute("style");
						String newStyle = oldStyle + "; color: " + color + ';';

						fontTag.setAttribute("style", newStyle);
					}
				}
			}
		};

		FSDOMMutator domChanger2 = (doc) -> {
			NodeList divs = doc.getElementsByTagName("div");

			for (int i = 0; i < divs.getLength(); i++) {
				org.w3c.dom.Element div = (org.w3c.dom.Element) divs.item(i);

				if (div.getChildNodes().getLength() == 1) {
					org.w3c.dom.Node node = div.getChildNodes().item(0);

					if (node.getNodeType() == Node.TEXT_NODE
							&& node.getTextContent().trim().isEmpty()) {
						div.setAttribute("style", "height: 20px;");
					}
				}
			}
		};

		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.withW3cDocument(new W3CDom().fromJsoup(jdoc), "/");
		builder.withProducer("lecturePresenter");
		builder.addDOMMutator(domChanger);
		builder.addDOMMutator(domChanger2);
		builder.usePDDocument(pdDoc);
		builder.useDefaultPageSize(PAGE_WIDTH / 72f, PAGE_HEIGHT / 72f,
				PageSizeUnits.INCHES);

		var buildPdfRenderer = builder.buildPdfRenderer();
		buildPdfRenderer.layout();
		buildPdfRenderer.createPDFWithoutClosing();
		buildPdfRenderer.close();
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
