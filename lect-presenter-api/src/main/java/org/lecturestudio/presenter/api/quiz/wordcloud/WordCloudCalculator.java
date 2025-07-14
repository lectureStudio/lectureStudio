package org.lecturestudio.presenter.api.quiz.wordcloud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Calculates and manages word placement for a word cloud visualization.
 * The WordcloudCalculator class is responsible for determining whether
 * word items in a word cloud layout overlap and manages configurations
 * such as the threshold frequency for displaying words.
 */
@Getter
@Setter
@AllArgsConstructor
public class WordCloudCalculator {

    /**
     * The default threshold frequency used for filtering words in a word cloud.
     * Words with a frequency less than this value are excluded from the visualization.
     */
    public static final int DEFAULT_THRESHOLD_FREQUENCY = 1;

    /**
     * Specifies the default maximum font size used when rendering words in a word cloud.
     * This value represents the upper limit of the font size range and ensures that words
     * with the highest frequency are displayed prominently.
     */
    public static final int DEFAULT_MAX_FONT_SIZE = 48;

    /**
     * Represents the default minimum font size used by the WordcloudCalculator.
     * <p>
     * This value is used as the lower bound for font size calculations for words in a word cloud.
     * Words with lower frequencies will not be rendered smaller than this font size.
     */
    public static final int DEFAULT_MIN_FONT_SIZE = 12;

    // ============================================================
    //                        ATTRIBUTES
    // ============================================================

    /**
     * Threshold frequency for displaying the word.
     * Words with a frequency below this value will not be displayed.
     */
    private int thresholdFrequency; // Minimum frequency for the word to be displayed#

    /**
     * The maximum font size for words in the word cloud.
     * This property defines the largest font size that can be applied
     * to any word in the visualization. Words with the highest frequencies
     * will typically use this font size.
     */
    private int maxFontSize;

    /**
     * The minimum font size for words in the word cloud.
     * Determines the smallest allowable font size for rendering words
     * in the visualization. Words with lower prominence or frequency
     * may be rendered using this size to maintain readability.
     */
    private int minFontSize;


    // ============================================================
    //                        CONSTRUCTORS
    // ============================================================

    public WordCloudCalculator() {
        this(DEFAULT_THRESHOLD_FREQUENCY, DEFAULT_MAX_FONT_SIZE, DEFAULT_MIN_FONT_SIZE);
    }

    // ============================================================
    //                          METHODS
    // ============================================================

    /**
     * Determines if two word items in a word cloud will collide based on their bounding rectangles.
     *
     * @param word1 the first word item, including its bounding rectangle.
     * @param word2 the second word item, including its bounding rectangle.
     * @return true if the bounding rectangles of the two word items intersect, false otherwise.
     */
    public static boolean willCollide(final WordItem word1, final WordItem word2) {
        // Check if the bounding rectangles of two words overlap
        return word1.bounds.intersects(word2.bounds);
    }

    /**
     * Calculates the font size for a word based on its frequency, given the range of font sizes
     * and the maximum frequency in the dataset.
     *
     * @param frequency the frequency of the word to calculate the font size for
     * @param maxFrequency the maximum frequency in the dataset, used for normalization
     * @param minFontSize the minimum font size in the font size range
     * @param maxFontSize the maximum font size in the font size range
     * @return the calculated font size for the given word's frequency
     */
    public int calculateFontSize(final int frequency, final int maxFrequency, final int minFrequency, final int maxFontSize,
                                        final int minFontSize) {
        // Calculate the font size based on the frequency of the word
        if (maxFrequency == 0) {
            return minFontSize; // Avoid division by zero
        }

        if(maxFrequency == minFrequency) {
            return maxFontSize; // If all frequencies are the same, return the minimum font size
        }

        return (int) Math.ceil ( (double) ((maxFontSize - minFontSize) * (frequency - minFrequency)
                /(maxFrequency-minFrequency)) + minFontSize );
    }

}
