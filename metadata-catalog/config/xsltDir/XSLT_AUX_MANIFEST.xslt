<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

    <xsl:template match="/">
		<instrumentConfigurationId>
			<xsl:copy-of select="//*[local-name() = 'instrumentConfigurationId']/text()"/>
		</instrumentConfigurationId>
		<creationTime>
			<xsl:copy-of select="//*[local-name() = 'generation']/text()"/>
		</creationTime>
		<validityStartTime>
			<xsl:copy-of select="//*[local-name() = 'validity']/text()"/>
		</validityStartTime>
		<site>
			<xsl:value-of select="//*[local-name() = 'facility']/@site"/>
		</site>
	</xsl:template>

</xsl:stylesheet>