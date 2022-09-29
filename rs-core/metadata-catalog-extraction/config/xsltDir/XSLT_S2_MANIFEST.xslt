<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />
		
	<xsl:template match="/">

		<platfomShortName>
			<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'familyName']/text()"/>
		</platfomShortName>

		<operationalMode>
			<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'mode']/text()"/>
		</operationalMode>

		<relativeOrbitNumber>
			<xsl:copy-of select="//*[local-name() = 'relativeOrbitNumber'][@*[local-name()='type' and .='start']]/text()"/>
		</relativeOrbitNumber>

	</xsl:template>

</xsl:stylesheet>