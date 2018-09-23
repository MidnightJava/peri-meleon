<?xml version="1.0" encoding="ISO-8859-1"?>
<!--XSLT to create directory as PDF. Second version, benefiting from reading the XSLT book.  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output method="xml"/>
	
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			
			<fo:layout-master-set>
				<fo:simple-page-master master-name="first-page" page-width="8.5in"
					page-height="11in">
					<fo:region-body region-name="page-body" margin="0.5in"
						margin-top="2.5in" column-count="2" column-gap="0.5in"/>
					<fo:region-before region-name="header" extent="3.5in"/>
				</fo:simple-page-master>
				
				<fo:simple-page-master master-name="succeeding-page"
					page-width="8.5in" page-height="11in">
					<fo:region-body region-name="page-body" margin="0.5in"
						margin-top="1.25in" column-count="2" column-gap="0.5in"/>
					<fo:region-before region-name="succeeding-header" extent="1in"/>
				</fo:simple-page-master>
				
				<fo:page-sequence-master master-name="the-sequence">
					<fo:single-page-master-reference
						master-reference="first-page"/>
					<fo:repeatable-page-master-reference
						master-reference="succeeding-page"/>
				</fo:page-sequence-master>
			</fo:layout-master-set>
			
			<fo:page-sequence master-reference="the-sequence">
				
				<fo:static-content flow-name="header">
					<xsl:apply-templates select="/directory"/>
				</fo:static-content>
				
				<fo:static-content flow-name="succeeding-header">
					<fo:block font-family="Minion Regular" font-size="14pt" text-align-last="justify"
						margin-top="0.5in" margin-left="0.5in" margin-right="0.5in"
						padding-before="7pt" padding-after="7pt"
						border-start-style="none" border-end-style="none"
						border-before-style="solid" border-before-color="black" border-before-width=".2mm" 
						border-after-style="solid"  border-after-color="black"  border-after-width=".2mm">
						<fo:leader/>
						<fo:inline font-family="AGaramondTitling" font-style="normal" letter-spacing="2pt">
							<xsl:text>MEMBER DIRECTORY</xsl:text>
						</fo:inline>
						<fo:inline font-family="Minion Italic" font-style="normal">
							<xsl:text>   |   </xsl:text>
							<xsl:for-each select="/directory">
								<xsl:value-of select="@date"/>
							</xsl:for-each>
						</fo:inline>
						<fo:leader/>
						<fo:inline font-family="Minion Italic" font-style="normal">
							<xsl:text>page </xsl:text>
							<fo:page-number/>
						</fo:inline>
					</fo:block>
				</fo:static-content>
				
				<fo:flow flow-name="page-body">
					<xsl:apply-templates select="/directory/*"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	
	<xsl:template match="/directory">
		<fo:block start-indent="0.5in" padding-before="0.5in" text-align="center"
			padding-after="7pt">
			<fo:external-graphic src="url(/Library/Application Support/com.tamelea.perimeleon/resource/NHPCLogo2-C-mid.jpg)"/>
		</fo:block>
		<fo:block font-family="Minion Regular" font-size="14pt" text-align="center"
			margin-left="0.5in" margin-right="0.5in"
			padding-before="7pt" padding-after="7pt"
			border-start-style="none" border-end-style="none"
			border-before-style="solid" border-before-color="black" border-before-width=".2mm" 
			border-after-style="solid"  border-after-color="black"  border-after-width=".2mm">
			<fo:inline font-family="AGaramondTitling" font-style="normal" letter-spacing="2pt">
				<xsl:text>MEMBER DIRECTORY</xsl:text>
			</fo:inline>
			<fo:inline font-family="Minion Italic" font-style="normal">
				<xsl:text>   |   </xsl:text>
				<xsl:value-of select="@date"/>
			</fo:inline>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="household">
		<fo:block font-family="Minion Regular" font-size="12pt"
			font-weight="normal" space-before="18pt">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="family-name">
		<fo:inline font-family="Minion RegularSC" font-size="14pt">
			<xsl:value-of select="@text"/>
			<xsl:text>, </xsl:text>
		</fo:inline>
	</xsl:template>
	
	<xsl:template match="head-and-spouse">
		<xsl:value-of select="@text"/>
	</xsl:template>
	
	<xsl:template match="others">
		<fo:block font-family="Minion Italic" font-size="12pt" font-style="normal"
			keep-with-previous="always">
			<xsl:value-of select="@text"/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="address">
		<fo:block font-family="Minion Regular" font-size="12pt" font-style="normal"
			keep-with-previous="always">
			<xsl:value-of select="@text"/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="address2">
		<fo:block font-family="Minion Regular" font-size="12pt" font-style="normal"
			keep-with-previous="always">
			<xsl:value-of select="@text"/>
		</fo:block>
	</xsl:template>
	
	<!-- We have to do the 'for-each' thing here because state and postal-code aren't
		nested inside city -->
	<xsl:template match="city">
		<fo:block font-family="Minion Regular" font-size="12pt" font-style="normal"
			keep-with-previous="always">
			<xsl:value-of select="@text"/>
			<xsl:text>, </xsl:text>
			<xsl:for-each select="../state">
				<xsl:value-of select="@text"/>
				<xsl:text> </xsl:text>
			</xsl:for-each>
			<xsl:for-each select="../postal-code">
				<xsl:value-of select="@text"/>
			</xsl:for-each>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="country">
		<fo:block font-family="Minion Regular" font-size="12pt" font-style="normal"
			keep-with-previous="always">
			<xsl:value-of select="@text"/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="home-phone">
		<fo:block font-family="Minion Regular" font-size="12pt" font-style="normal"
			keep-with-previous="always">
			<xsl:value-of select="@text"/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="member">
		<fo:block font-family="Abadi MT Condensed Light" font-size="12pt"
			keep-with-previous="always" text-indent="-0.2in" start-indent="0.2in">
			<xsl:value-of select="@text"/>
			<xsl:text>: </xsl:text>
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="datum">
		<fo:inline wrap-option="no-wrap">
			<xsl:value-of select="@text"/>
		</fo:inline>
	</xsl:template>
	
</xsl:stylesheet>