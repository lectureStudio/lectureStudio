<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:template match="/">
		<html>
			<head>
				<title>Messenger Log</title>
			</head>
			<body>
				<h2>
					<xsl:value-of select="log/title" />
				</h2>
				<table cellpadding="8" cellspacing="1" width="100%">
					<tr>
						<th>Host</th>
						<th>Time</th>
						<th>Message</th>
					</tr>
					<xsl:for-each select="log/entry">
						<xsl:choose>
							<xsl:when test="@abuse">
								<tr bgcolor="#FFEEEE">
									<td width="110">
										<xsl:value-of select="host" />
									</td>
									<td width="150">
										<xsl:value-of select="date" />
									</td>
									<td>
										<xsl:value-of select="message" />
									</td>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<tr bgcolor="#EEEEEE">
									<td width="110">
										<xsl:value-of select="host" />
									</td>
									<td width="150">
										<xsl:value-of select="date" />
									</td>
									<td>
										<xsl:value-of select="message" />
									</td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>