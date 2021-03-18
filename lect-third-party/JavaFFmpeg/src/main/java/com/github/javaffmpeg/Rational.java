package com.github.javaffmpeg;

/**
 * A rational number is a number that can be expressed as a fraction p/q where p and q are integers
 * and q!=0. A rational number p/q is said to have numerator p and denominator q.
 * 
 * @author Alex Andres
 */
public class Rational {

	private int numerator;
	
	private int denominator;
	
	
	/**
	 * @param numerator
	 * @param denominator
	 */
	public Rational(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * @return the numerator
	 */
	public int getNumerator() {
		return numerator;
	}

	/**
	 * @return the denominator
	 */
	public int getDenominator() {
		return denominator;
	}
	
}
