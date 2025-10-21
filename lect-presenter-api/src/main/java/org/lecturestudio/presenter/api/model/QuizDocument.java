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

import static java.util.Objects.isNull;
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
import java.util.Arrays;
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
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.IOUtils;
import org.lecturestudio.presenter.api.quiz.wordcloud.*;
import org.lecturestudio.presenter.api.util.NumericStringComparator;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizOption;
import org.lecturestudio.web.api.model.quiz.QuizResult;

/**
 * Represents a document designed for rendering quizzes and their associated results.
 * This class is responsible for generating PDF documents containing quiz questions,
 * answers, and result visualizations such as bar charts and pie charts.
 * It extends the HtmlToPdfDocument class to provide specific functionality for quiz-related content.
 * <p>
 * The QuizDocument class provides both constructors and helper methods for creating
 * and rendering different types of quiz documents. The class supports multiple-choice
 * questions, chart rendering, and text layout adjustments specific to quiz presentations.
 *
 * @author Alex Andres
 */
public class QuizDocument extends HtmlToPdfDocument {

	/**
	 * Used for sorting numeric quiz answers in {@link #getSortedAnswers(QuizResult)} method
	 * when dealing with numeric quiz types.
	 */
	private static final NumericStringComparator NS_COMPARATOR = new NumericStringComparator();

	/** The quiz result data containing answers and statistics for this document. */
	private final QuizResult result;


	/**
	 * Creates a new quiz document with the specified template and quiz result.
	 *
	 * @param templateFile  the file to use as a template for the document.
	 * @param contentBounds the dimensional bounds defining the content area of the document.
	 * @param dict          the dictionary used for localization of text elements.
	 * @param result        the quiz result data containing questions, answers, and statistics.
	 *
	 * @throws IOException if an I/O error occurs during document creation.
	 */
	public QuizDocument(File templateFile, Rectangle2D contentBounds,
						Dictionary dict, QuizResult result) throws Exception {
		this.result = result;

		init(createDocument(templateFile, contentBounds, dict, result));
		setDocumentType(DocumentType.QUIZ);
		setTitle(dict.get("quiz"));
		setComment(result.getQuiz().getComment());
	}

	/**
	 * Checks whether this quiz document contains any answers.
	 *
	 * @return {@code true} if the result exists and contains at least one answer,
	 * {@code false} otherwise.
	 */
	public boolean hasAnswers() {
		return nonNull(result) && !result.getResult().isEmpty();
	}

	/**
	 * Sets a comment on all pages of this quiz document.
	 * The comment is only added if it is not null and not blank.
	 *
	 * @param comment the text to add as a note to each page of the document
	 */
	private void setComment(String comment) {
		if (nonNull(comment) && !comment.isBlank()) {
			getPages().forEach(page -> page.addTextNote(comment));
		}
	}

	/**
	 * Creates a helper for generating a document representing a multiple-choice quiz,
	 * rendering questions and associated answer statistics in chart format.
	 * In other words, it creates a new page with the statistics bar-chart.
	 * This method serves as an auxiliary method for the method
	 * {@link #createDocument(File, Rectangle2D, Dictionary, QuizResult) createDocument}.
	 *
	 * @param contentBounds the dimensional bounds defining the content area of the document.
	 * @param dict          the dictionary used for localization or textual customization.
	 * @param result        the quiz result data used to populate the document.
	 * @param tplDoc        the template PDF document, which may be null.
	 * @param doc           the target PDF document where the quiz will be rendered.
	 * @param quiz          the quiz containing questions and answers to be included in the document.
	 *
	 * @throws IOException if an I/O error occurs during document creation or rendering.
	 */
	private static void createMultipleChoicePage(final Rectangle2D contentBounds, final Dictionary dict,
												 final QuizResult result, final PDDocument tplDoc,
												 final PDDocument doc, final Quiz quiz) throws IOException {
		// Create a new page with the statistics bar-chart.
		renderChartQuestions(tplDoc, doc, contentBounds, quiz);
		renderChart(tplDoc, doc, result, createBarChartAnswerStats(dict, result), contentBounds);
	}

	/**
	 * Creates a helper for generating a document representing a free-text quiz.
	 * This method assists in rendering free-text quiz responses and the relevant
	 * content within the specified target PDF document.
	 * It serves as a method for handling free-text input quizzes and is an auxiliary method for the method
	 * {@link #createDocument(File, Rectangle2D, Dictionary, QuizResult) createDocument}.
	 *
	 * @param contentBounds the dimensional bounds defining the content area of the document.
	 * @param result        the quiz result data used to populate the document.
	 * @param tplDoc        the template PDF document, which may be null.
	 * @param doc           the target PDF document where the quiz will be rendered.
	 *
	 * @throws IOException if an I/O error occurs during document creation or rendering.
	 */
	private static void createWordCloudPage(final Rectangle2D contentBounds,
											final QuizResult result,
											final PDDocument tplDoc,
											final PDDocument doc) throws IOException {
		// Create a new page with a word cloud.
		var jdoc = Jsoup.parseBodyFragment("");
		jdoc.head().append("<link rel=\"stylesheet\" href=\"html/quiz.css\">");
		jdoc.outputSettings().prettyPrint(true);

		renderHtmlPage(jdoc, tplDoc, doc, contentBounds, new HashMap<>());

		// Add the word cloud to the last page.
		List<WordItem> words = getWordFrequencies(result);

		int pageIndex = doc.getNumberOfPages() - 1;
		PDPage pdPage = doc.getPage(pageIndex);

		Rectangle2D pageBounds = getPageBounds(tplDoc);

		int width = (int) (contentBounds.getWidth() * pageBounds.getWidth());
		int height = (int) (contentBounds.getHeight() * pageBounds.getHeight());
		int marginX = (int) (contentBounds.getX() * pageBounds.getWidth());
		int marginY = (int) (contentBounds.getY() * pageBounds.getHeight());

		LayoutConfig customConfig = new LayoutConfig()
				.horizontalPadding(12)
				.verticalPadding(8)
				.maxVerticalVariance(20)
				.enableVerticalVariance(true)
				.minLineSpacing(15);

		FontCalculator fontCalculator = new TieredFontCalculator(4, 20, 48);

		HorizontalWordLayout layout = new HorizontalWordLayout(width, height, customConfig, fontCalculator);
		layout.layoutWords(words);

		PDFGraphics2D g2dStream = new PDFGraphics2D(doc, pdPage, true);
		// Move to the top-left corner.
		g2dStream.transform(new AffineTransform(1, 0, 0, -1, 0, pdPage.getMediaBox().getHeight()));
		g2dStream.translate(marginX, marginY);
		layout.renderWordCloud(g2dStream, words);
		g2dStream.close();
	}

	/**
	 * Analyzes a quiz result to extract word frequencies for visualization in a word cloud.
	 *
	 * @param result The quiz result containing answers to be analyzed for word frequency.
	 *
	 * @return a list of WordItem objects with words and their frequencies for word cloud rendering.
	 */
	private static List<WordItem> getWordFrequencies(QuizResult result) {
		List<WordItem> words = new ArrayList<>();
		Map<String, Integer> frequencyMap = new HashMap<>();
		Map<String, String> displayTextMap = new HashMap<>();

		// Process the quiz result to count word frequencies.
		for (var entry : result.getResult().entrySet()) {
			String[] options = entry.getKey().getOptions();
			Integer count = entry.getValue();

			// For each option in the current answer, increment its frequency count.
			for (String option : options) {
				if (isNull(option) || option.isBlank()) {
					continue;
				}

				// Shorten to a maximum length for consistent grouping and rendering.
				String original = IOUtils.shortenString(option.trim(), 32);
				String key = original.toLowerCase();

				Integer frequency = frequencyMap.get(key);
				frequency = isNull(frequency) ? count : frequency + count;

				// Update the frequency count for this option (case-insensitive).
				frequencyMap.put(key, frequency);

				// Choose a display string: prefer a variant starting with a capital letter if seen.
				String currentDisplay = displayTextMap.get(key);
				if (currentDisplay == null) {
					displayTextMap.put(key, original);
				}
				else {
					boolean currentStartsCapital = !currentDisplay.isEmpty() && Character.isUpperCase(currentDisplay.codePointAt(0));
					boolean originalStartsCapital = !original.isEmpty() && Character.isUpperCase(original.codePointAt(0));

					if (originalStartsCapital && !currentStartsCapital) {
						displayTextMap.put(key, original);
					}
				}
			}
		}

		// Convert the frequency map entries to WordItem objects for word cloud visualization.
		for (var entry : frequencyMap.entrySet()) {
			String display = displayTextMap.getOrDefault(entry.getKey(), entry.getKey());
			words.add(new WordItem(display, entry.getValue()));
		}

		return words;
	}

	/**
	 * Creates a PDF document based on a quiz template and results. The method generates
	 * pages for different types of quiz content like questions, multiple-choice statistics,
	 * free-text responses, and associated charts (bar and pie charts). It uses a given
	 * template file, bounds, and data to format and render the document.
	 *
	 * @param templateFile  the file to use as a template for creating the document;
	 *                      if it doesn't exist, no template is used.
	 * @param contentBounds the dimensional bounds defining the content region for the document.
	 * @param dict          the dictionary used for localization or textual customization in the
	 *                      rendering process.
	 * @param result        the quiz result data containing questions, answers, and statistics
	 *                      to be displayed in the document.
	 *
	 * @return a new instance of {@link PdfDocument} representing the generated quiz document.
	 *
	 * @throws IOException if an I/O error occurs during the document creation or rendering process.
	 */
	private static PdfDocument createDocument(
			File templateFile,
			Rectangle2D contentBounds,
			Dictionary dict,
			QuizResult result) throws Exception {
		PDDocument tplDoc = templateFile.exists() ? PDDocument.load(templateFile) : null;
		PDDocument doc = new PDDocument();

		Quiz quiz = result.getQuiz();
		QuizType type = quiz.getType();
		boolean hasCorrectAnswers = quiz.getOptions().stream().anyMatch(QuizOption::isCorrect);

		// Create the first page with the question on it.
		renderQuestion(tplDoc, doc, contentBounds, quiz, false);

		if (type != QuizType.FREE_TEXT && hasCorrectAnswers) {
			renderQuestion(tplDoc, doc, contentBounds, quiz, true);
		}

		// Checks if the list of results is empty.
		if (!result.getResult().isEmpty()) {
			if (type == QuizType.FREE_TEXT) {
				// Create a new page with a word cloud.
				createWordCloudPage(contentBounds, result, tplDoc, doc);
			}
			else {
				if (type == QuizType.MULTIPLE) {
					// Create a new page with the statistics bar-chart.
					createMultipleChoicePage(contentBounds, dict, result, tplDoc, doc, quiz);
				}

				// Create a new page with the bar-chart.
				renderChartQuestions(tplDoc, doc, contentBounds, quiz);
				renderChart(tplDoc, doc, result, createBarChart(dict, result), contentBounds);

				// Create a new page with the pie-chart.
				renderChartQuestions(tplDoc, doc, contentBounds, quiz);
				renderChart(tplDoc, doc, result, createPieChart(dict, result), contentBounds);
			}
		}

		PdfDocument pdfDocument = createPdfDocument(doc);

		if (nonNull(tplDoc)) {
			tplDoc.close();
		}

		doc.close();

		return pdfDocument;
	}

	private static void renderQuestion(PDDocument tplDoc, PDDocument doc,
									   Rectangle2D contentBounds, Quiz quiz, boolean markCorrect) throws Exception {
		String question = quiz.getQuestion().replaceAll("&nbsp;", " ");

		var jdoc = Jsoup.parseBodyFragment(question);
		jdoc.head().append("<link rel=\"stylesheet\" href=\"html/quiz.css\">");
		jdoc.outputSettings().prettyPrint(true);

		Map<String, String> resourceMap = new HashMap<>();

		Elements images = jdoc.getElementsByTag("img");
		for (Element e : images) {
			String src = e.absUrl("src");
			File imgFile = new File(FileUtils.decodePath(src));
			String newPath = Paths.get("quiz", imgFile.getName()).toString()
					.replaceAll("\\\\", "/");
			newPath = new URI(null, null, newPath, null).toASCIIString();

			// Replace src by new relative web-root path.
			e.attr("src", newPath);

			resourceMap.put(newPath, src);
		}

		List<QuizOption> options = quiz.getOptions();

		// Add options below question.
		if (!options.isEmpty()) {
			Element uList = jdoc.body().appendElement("ul");
			uList.addClass("options");

			String prefix = "";

			for (int i = 0; i < options.size(); i++) {
				if (quiz.getType() == QuizType.SINGLE || quiz.getType() == QuizType.MULTIPLE) {
					prefix = quiz.getOptionAlpha(i + "") + ") ";
				}

				QuizOption option = options.get(i);
				String optionText = option.getOptionText();
				String itemText = prefix + (nonNull(optionText) ? optionText : "");

				if (markCorrect && option.isCorrect()) {
					itemText += " <span class=\"icon-check\"></span>";
				}

				Element item = uList.appendElement("p");
				item.html(itemText);

				if (markCorrect && option.isCorrect()) {
					item.addClass("correct");
				}
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
			// Remove the last word and try again.
			List<String> words = new ArrayList<>(List.of(text.split("\\s+")));
			words.remove(words.size() - 1);

			return makeTextFit(String.join(" ", words), areaToFit);
		}

		return text;
	}

	private static void renderChartQuestions(PDDocument tplDoc, PDDocument doc,
											 Rectangle2D contentBounds, Quiz quiz) throws IOException {
		List<QuizOption> options = quiz.getOptions();

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

		int maxWidth = (int) (width / 2) - 70;
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
			String text = options.get(i).getOptionText();

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
		if (chart instanceof CategoryChart catChart) {
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
		// Move to the top-left corner.
		g2dStream.transform(new AffineTransform(1, 0, 0, -1, 0, pdPage.getMediaBox().getHeight()));
		g2dStream.translate(marginX, marginY);
		chart.paint(g2dStream, chartWidth, chartHeight);
		g2dStream.close();
	}

	private static PieChart createPieChart(Dictionary dict, QuizResult result) {
		PieChart chart = new PieChartBuilder().theme(ChartTheme.GGPlot2).build();
		chart.getStyler().setPlotContentSize(0.7);
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setForceAllLabelsVisible(true);
		chart.getStyler().setLabelsDistance(1.25);
		chart.getStyler().setLabelsVisible(true);
		chart.getStyler().setAntiAlias(true);
		chart.getStyler().setLabelsFontColorAutomaticEnabled(true);
		chart.getStyler().setStartAngleInDegrees(90);
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
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setXAxisTicksVisible(true);
		chart.getStyler().setAxisTicksLineVisible(false);
		chart.getStyler().setSeriesColors(new ChartColors().getSeriesColors());

		Map<QuizAnswer, Integer> resultMap = result.getResult();
		String[] xValues = new String[resultMap.size()];
		int index = 0;

		Collection<QuizAnswer> answers = getSortedAnswers(result);

		for (QuizAnswer answer : answers) {
			xValues[index] = result.getAnswerText(answer);

			index++;
		}

		index = 0;

		for (QuizAnswer answer : answers) {
			Integer[] yValues = new Integer[resultMap.size()];
			Arrays.fill(yValues, 0);

			yValues[index] = resultMap.get(answer);

			chart.addSeries(result.getAnswerText(answer), List.of(xValues), List.of(yValues));

			index++;
		}

		if (answers.size() > 9) {
			chart.getStyler().setXAxisLabelRotation(45);
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
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setXAxisTicksVisible(true);
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
			String[] xValues = new String[chartMap.size()];
			int index = 0;

			for (String answer : chartMap.keySet()) {
				xValues[index] = answer;

				index++;
			}

			index = 0;

			for (String answer : chartMap.keySet()) {
				Integer[] yValues = new Integer[chartMap.size()];
				Arrays.fill(yValues, 0);

				yValues[index] = chartMap.get(answer);

				chart.addSeries(answer, List.of(xValues), List.of(yValues));

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
