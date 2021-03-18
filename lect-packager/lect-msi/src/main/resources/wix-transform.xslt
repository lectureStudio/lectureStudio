<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wix="http://schemas.microsoft.com/wix/2006/wi" xmlns="http://schemas.microsoft.com/wix/2006/wi" version="1.0" exclude-result-prefixes="xsl wix">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<!-- By default, copy all elements and nodes into the output -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- Remove lectureEditorFX.exe from harvested files, since we need recording file association -->
	<xsl:template match="wix:Component[ wix:File/@Source = 'SourceDir\lectureEditor.exe' ]" />

</xsl:stylesheet>