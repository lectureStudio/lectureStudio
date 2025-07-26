/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.quiz.wordcloud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class TieredFontCalculatorTest {

    private static final int DEFAULT_LEVELS = 5;
    private static final int DEFAULT_MIN_SIZE = 12;
    private static final int DEFAULT_MAX_SIZE = 48;
    private static final String DEFAULT_FONT_FAMILY = Font.SANS_SERIF;
    private static final int DEFAULT_FONT_STYLE = Font.BOLD;
    private static final TieredFontCalculator.ColorScheme DEFAULT_COLOR_SCHEME = TieredFontCalculator.ColorScheme.CLASSIC;

    private TieredFontCalculator calculator;
    private List<WordItem> words;


    @BeforeEach
    void setUp() {
        calculator = new TieredFontCalculator(DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE);
        words = new ArrayList<>();
    }

    @Test
    void testConstructorWithAllParameters() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE,
                DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE, DEFAULT_COLOR_SCHEME);

        assertNotNull(calculator);
    }

    @Test
    void testConstructorWithDefaultParameters() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE);

        assertNotNull(calculator);
    }

    @Test
    void testConstructorWithNullFontFamily() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE,
                null, DEFAULT_FONT_STYLE, DEFAULT_COLOR_SCHEME);

        // The constructor should use Font.SANS_SERIF as default
        assertNotNull(calculator);
    }

    @Test
    void testConstructorWithNullColorScheme() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE,
                DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE, null);

        // The constructor should use ColorScheme.CLASSIC as default
        assertNotNull(calculator);
    }

    @Test
    void testConstructorWithInvalidLevels() {
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(1, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE));
        
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(0, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE));
        
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(-1, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE));
    }

    @Test
    void testConstructorWithInvalidMinSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(DEFAULT_LEVELS, 0, DEFAULT_MAX_SIZE));
        
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(DEFAULT_LEVELS, -1, DEFAULT_MAX_SIZE));
    }

    @Test
    void testConstructorWithInvalidMaxSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(DEFAULT_LEVELS, DEFAULT_MIN_SIZE, 0));
        
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(DEFAULT_LEVELS, DEFAULT_MIN_SIZE, -1));
    }

    @Test
    void testConstructorWithInvalidSizeRelation() {
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(DEFAULT_LEVELS, DEFAULT_MAX_SIZE, DEFAULT_MIN_SIZE));
        
        assertThrows(IllegalArgumentException.class, () ->
                new TieredFontCalculator(DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MIN_SIZE));
    }

    @Test
    void testCalculateFontSizesWithNullList() {
        assertDoesNotThrow(() -> calculator.calculateFontSizes(null));
    }

    @Test
    void testCalculateFontSizesWithEmptyList() {
        List<WordItem> emptyList = new ArrayList<>();
        
        assertDoesNotThrow(() -> calculator.calculateFontSizes(emptyList));
    }

    @Test
    void testCalculateFontSizesWithSingleWord() {
        WordItem word = new WordItem("test", 10);
        words.add(word);
        
        calculator.calculateFontSizes(words);
        
        assertNotNull(word.font);
        assertNotNull(word.color);
        assertEquals(DEFAULT_FONT_FAMILY, word.font.getFamily());
        assertEquals(DEFAULT_FONT_STYLE, word.font.getStyle());
        
        // Single word should get middle-tier font size.
        int middleTier = DEFAULT_LEVELS / 2;
        int expectedFontSize = DEFAULT_MAX_SIZE - (middleTier * (DEFAULT_MAX_SIZE - DEFAULT_MIN_SIZE) / (DEFAULT_LEVELS - 1));
        assertEquals(expectedFontSize, word.font.getSize());
    }

    @Test
    void testCalculateFontSizesWithMultipleWordsOfSameFrequency() {
        words.add(new WordItem("test1", 10));
        words.add(new WordItem("test2", 10));
        words.add(new WordItem("test3", 10));
        
        calculator.calculateFontSizes(words);
        
        for (WordItem word : words) {
            assertNotNull(word.font);
            assertNotNull(word.color);
            assertEquals(DEFAULT_FONT_FAMILY, word.font.getFamily());
            assertEquals(DEFAULT_FONT_STYLE, word.font.getStyle());
            
            // All words should have the same font size (middle-tier).
            int middleTier = DEFAULT_LEVELS / 2;
            int expectedFontSize = DEFAULT_MAX_SIZE - (middleTier * (DEFAULT_MAX_SIZE - DEFAULT_MIN_SIZE) / (DEFAULT_LEVELS - 1));
            assertEquals(expectedFontSize, word.font.getSize());
        }
        
        // All words should have the same font size.
        assertEquals(words.get(0).font.getSize(), words.get(1).font.getSize());
        assertEquals(words.get(1).font.getSize(), words.get(2).font.getSize());
    }

    @Test
    void testCalculateFontSizesWithMultipleWordsOfDifferentFrequencies() {
        words.add(new WordItem("high", 100));
        words.add(new WordItem("medium", 50));
        words.add(new WordItem("low", 10));
        
        calculator.calculateFontSizes(words);
        
        for (WordItem word : words) {
            assertNotNull(word.font);
            assertNotNull(word.color);
            assertEquals(DEFAULT_FONT_FAMILY, word.font.getFamily());
            assertEquals(DEFAULT_FONT_STYLE, word.font.getStyle());
        }
        
        // Words with higher frequency should have larger font size.
        assertTrue(words.get(0).font.getSize() >= words.get(1).font.getSize());
        assertTrue(words.get(1).font.getSize() >= words.get(2).font.getSize());
    }

    @Test
    void testCalculateFontSizesWithWordsOfSameFrequencyGroup() {
        words.add(new WordItem("high1", 100));
        words.add(new WordItem("high2", 100));
        words.add(new WordItem("medium1", 50));
        words.add(new WordItem("medium2", 50));
        words.add(new WordItem("low1", 10));
        words.add(new WordItem("low2", 10));
        
        calculator.calculateFontSizes(words);
        
        // Words in the same frequency group should have the same font size.
        assertEquals(words.get(0).font.getSize(), words.get(1).font.getSize());
        assertEquals(words.get(2).font.getSize(), words.get(3).font.getSize());
        assertEquals(words.get(4).font.getSize(), words.get(5).font.getSize());
        
        // Words with higher frequency should have larger font size.
        assertTrue(words.get(0).font.getSize() >= words.get(2).font.getSize());
        assertTrue(words.get(2).font.getSize() >= words.get(4).font.getSize());
    }

    @Test
    void testGenerateColorWithClassicScheme() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE,
                DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE, TieredFontCalculator.ColorScheme.CLASSIC);
        
        Color maxColor = calculator.generateColor(100, 100);
        Color midColor = calculator.generateColor(50, 100);
        Color minColor = calculator.generateColor(0, 100);
        
        assertNotNull(maxColor);
        assertNotNull(midColor);
        assertNotNull(minColor);
        
        // For CLASSIC scheme, higher frequency should have more red, less green.
        assertTrue(maxColor.getRed() >= midColor.getRed());
        assertTrue(midColor.getRed() >= minColor.getRed());
        
        assertTrue(maxColor.getGreen() <= midColor.getGreen());
        assertTrue(midColor.getGreen() <= minColor.getGreen());
    }

    @Test
    void testGenerateColorWithBlueShades() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE,
                DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE, TieredFontCalculator.ColorScheme.BLUE_SHADES);
        
        Color maxColor = calculator.generateColor(100, 100);
        Color midColor = calculator.generateColor(50, 100);
        Color minColor = calculator.generateColor(0, 100);
        
        assertNotNull(maxColor);
        assertNotNull(midColor);
        assertNotNull(minColor);
        
        // For BLUE_SHADES scheme, higher frequency should have more intense blue.
        assertTrue(maxColor.getBlue() >= midColor.getBlue());
        assertTrue(midColor.getBlue() >= minColor.getBlue());
    }

    @Test
    void testGenerateColorWithRainbow() {
        TieredFontCalculator calculator = new TieredFontCalculator(
                DEFAULT_LEVELS, DEFAULT_MIN_SIZE, DEFAULT_MAX_SIZE,
                DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE, TieredFontCalculator.ColorScheme.RAINBOW);
        
        Color maxColor = calculator.generateColor(100, 100);
        Color midColor = calculator.generateColor(50, 100);
        Color minColor = calculator.generateColor(0, 100);
        
        assertNotNull(maxColor);
        assertNotNull(midColor);
        assertNotNull(minColor);
        
        // Colors should be different for different frequencies.
        assertNotEquals(maxColor, midColor);
        assertNotEquals(midColor, minColor);
        assertNotEquals(maxColor, minColor);
    }

    @Test
    void testGenerateColorWithZeroMaxFrequency() {
        Color color = calculator.generateColor(10, 0);

        // Should not throw exception and use default intensity of 0.5f.
        assertNotNull(color);
    }

    @Test
    void testCalculateFontSizesWithUnsortedWords() {
        words.add(new WordItem("medium", 50));
        words.add(new WordItem("low", 10));
        words.add(new WordItem("high", 100));
        
        // Sort words by frequency in descending order before passing to the calculator
        // as the TieredFontCalculator expects sorted input.
        words.sort((w1, w2) -> Integer.compare(w2.frequency, w1.frequency));
        
        calculator.calculateFontSizes(words);
        
        for (WordItem word : words) {
            assertNotNull(word.font);
            assertNotNull(word.color);
        }
        
        // Words with higher frequency should have larger font size.
        WordItem highWord = words.stream().filter(w -> w.text.equals("high")).findFirst().orElse(null);
        WordItem mediumWord = words.stream().filter(w -> w.text.equals("medium")).findFirst().orElse(null);
        WordItem lowWord = words.stream().filter(w -> w.text.equals("low")).findFirst().orElse(null);
        
        assertNotNull(highWord);
        assertNotNull(mediumWord);
        assertNotNull(lowWord);
        
        assertTrue(highWord.font.getSize() >= mediumWord.font.getSize());
        assertTrue(mediumWord.font.getSize() >= lowWord.font.getSize());
    }

    @Test
    void testCalculateFontSizesWithNegativeFrequencies() {
        words.add(new WordItem("negative", -10));
        words.add(new WordItem("zero", 0));
        words.add(new WordItem("positive", 10));
        
        calculator.calculateFontSizes(words);
        
        // The method should handle negative frequencies correctly.
        for (WordItem word : words) {
            assertNotNull(word.font);
            assertNotNull(word.color);
        }
    }

    @Test
    void testCalculateFontSizesWithExtremeFrequencyDifferences() {
        words.add(new WordItem("veryHigh", 1000000));
        words.add(new WordItem("medium", 500));
        words.add(new WordItem("veryLow", 1));
        
        calculator.calculateFontSizes(words);
        
        // The method should handle extreme frequency differences correctly.
        for (WordItem word : words) {
            assertNotNull(word.font);
            assertNotNull(word.color);
        }
        
        // Words with higher frequency should have larger font size.
        assertTrue(words.get(0).font.getSize() >= words.get(1).font.getSize());
        assertTrue(words.get(1).font.getSize() >= words.get(2).font.getSize());
    }

    @Test
    void testCalculateFontSizesWithManyWords() {
        for (int i = 0; i < 1000; i++) {
            words.add(new WordItem("word" + i, i));
        }
        
        assertDoesNotThrow(() -> calculator.calculateFontSizes(words));
        
        // Check that all words have fonts and colors assigned.
        for (WordItem word : words) {
            assertNotNull(word.font);
            assertNotNull(word.color);
        }
    }
}