<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

    <xsl:template match="/">
    	<creationTime>
			<xsl:copy-of select="//*[local-name() = 'Creation_Time']/text()"/>
		</creationTime>
		<validityStartTime>
			<xsl:copy-of select="//*[local-name() = 'Validity_Start']/text()"/>
		</validityStartTime>
		<validityStopTime>
			<xsl:copy-of select="//*[local-name() = 'Validity_Stop']/text()"/>
		</validityStopTime>
	</xsl:template>

</xsl:stylesheet>