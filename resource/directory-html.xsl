<?xml version="1.0" encoding="ISO-8859-1"?>
<!--This is an experiment in applying XSLT. As far as it goes it's useful.
	To make this really useful, (1) exploit the CSS, and (2) put in the rest of the data. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="/">
		<html>
			<body>
				<h2>The Directory</h2>
				<xsl:for-each select="directory/household">
					<p><b>
						<xsl:for-each select="family-name">
							<xsl:value-of select="@text"/>
						</xsl:for-each> </b>
						<xsl:text>, </xsl:text>
						<xsl:for-each select="head-and-spouse">
							<xsl:value-of select="@text"/>
						</xsl:for-each> </p>
					
						<xsl:for-each select="others">
							<p><i><xsl:value-of select="@text"/></i></p>
						</xsl:for-each> 
					<p>
						<xsl:for-each select="home-phone">
							<xsl:value-of select="@text"/>
						</xsl:for-each> </p>
					<xsl:for-each select="member">
						<p><font size="-1">
							<xsl:value-of select="@text"/>
							<xsl:text>: </xsl:text>
							<xsl:for-each select="datum">
								<xsl:value-of select="@text"/>
							</xsl:for-each> </font> </p>
					</xsl:for-each>
				</xsl:for-each>
			</body>
		</html>
		
	</xsl:template>
	
</xsl:stylesheet>