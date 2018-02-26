<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ns1="http://www.esa.int/safe/sentinel-1.0" xmlns:ns2="http://www.esa.int/safe/sentinel-1.0/sentinel-1/auxiliary/sar">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />
    
    <xsl:template match="/">
    	<!--<missionid>
			<xsl:copy-of select="//ns1:platform/ns1:familyName/text()"/>
		</missionid>
		<satelliteid>
			<xsl:copy-of select="//ns1:number/text()"/>
		</satelliteid>
		<productType>
			<xsl:copy-of select="//ns2:auxProductType/text()"/>
		</productType>-->
		<instrumentConfigurationId>
			<xsl:copy-of select="//ns2:instrumentConfigurationId/text()"/>
		</instrumentConfigurationId>
		<creationTime>
			<xsl:copy-of select="//ns2:generation/text()"/>
		</creationTime>
		<validityStartTime>
			<xsl:copy-of select="//ns2:validity/text()"/>
		</validityStartTime>
		<site>
			<xsl:copy-of select="//ns1:facility/@site"/>
		</site>
	</xsl:template>

</xsl:stylesheet>