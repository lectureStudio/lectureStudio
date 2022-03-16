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

package org.lecturestudio.javafx.control;

import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class Sponsor extends Control {

	private String organization;

	private String image;

	private String linkName;

	private String linkUrl;


	public Sponsor() {
		super();

		initialize();
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getOrganizationLinkName() {
		return linkName;
	}

	public String getOrganizationLinkUrl() {
		return linkUrl;
	}

	public void setOrganizationLink(String name, String url) {
		this.linkName = name;
		this.linkUrl = url;
	}

	public String getOrganizationImage() {
		return image;
	}

	public void setOrganizationImage(String path) {
		this.image = path;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/sponsor.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new SponsorSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll("sponsor");

		setFocusTraversable(false);
	}
}
