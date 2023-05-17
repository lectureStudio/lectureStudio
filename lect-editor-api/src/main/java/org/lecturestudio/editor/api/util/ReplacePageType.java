package org.lecturestudio.editor.api.util;

public enum ReplacePageType {

	REPLACE_ALL_PAGES("allPagesTypeRadio"),

	REPLACE_SINGLE_PAGE("currentPageTypeRadio");


	private final String name;

	ReplacePageType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ReplacePageType parse(String name) {
		if (name.equals(REPLACE_ALL_PAGES.getName())) {
			return REPLACE_ALL_PAGES;
		}

		if (name.equals(REPLACE_SINGLE_PAGE.getName())) {
			return REPLACE_SINGLE_PAGE;
		}

		return null;
	}
}
