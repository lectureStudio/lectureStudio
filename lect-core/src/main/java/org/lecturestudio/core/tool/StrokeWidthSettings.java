package org.lecturestudio.core.tool;

/**
 * Enum Settings for selecting the multiplier by how much the stroke of the tool should be multiplied.
 * This enables a preselection for the thickness of the strokes
 */
public enum StrokeWidthSettings {
	EXTRA_SMALL("EXTRA_SMALL", 0.33),
	SMALL("SMALL", 0.66),
	NORMAL("NORMAL", 1),
	BIG("BIG", 2),
	EXTRA_BIG("EXTRA_BIG", 3);

	private final String name;
	private final double multiplier;

	StrokeWidthSettings(String name, double multiplier) {
		this.name = name;
		this.multiplier = multiplier;
	}

	public String getName() {
		return name;
	}

	public double getMultiplier() {
		return multiplier;
	}

	@Override
	public String toString() {
		return getName();
	}
}