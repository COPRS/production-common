<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />
    
    <xsl:template match="/">
    	<!--<productClass>
			<xsl:copy-of select="//File_Class/text()"/>
		</productClass>
		<productType>
			<xsl:copy-of select="//File_Type/text()"/>
		</productType>-->
		<creationTime>
			<xsl:copy-of select="//Creation_Date/text()"/>
		</creationTime>
		<validityStartTime>
			<xsl:copy-of select="//Validity_Start/text()"/>
		</validityStartTime>
		<validityStopTime>
			<xsl:copy-of select="//Validity_Stop/text()"/>
		</validityStopTime>
		<version>
			<xsl:copy-of select="//File_Version/text()"/>
		</version>
	</xsl:template>

</xsl:stylesheet>