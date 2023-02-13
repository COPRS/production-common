<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />
		
	<xsl:template match="/">
		<validityStartTime>
			<xsl:copy-of select="//*[local-name() = 'acquisitionPeriod']/*[local-name() = 'startTime']/text()"/>
		</validityStartTime>
		<validityStopTime>
			<xsl:copy-of select="//*[local-name() = 'acquisitionPeriod']/*[local-name() = 'stopTime']/text()"/>
		</validityStopTime>
		<platfomShortName>
			<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'familyName']/text()"/>
		</platfomShortName>
		<platformSerialIdentifier>
			<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'number']/text()"/>
		</platformSerialIdentifier>
		<orbitNumber>
			<xsl:copy-of select="//*[local-name() = 'orbitNumber'][@*[local-name()='type' and .='start']]/text()"/>
		</orbitNumber>
		<lastOrbitNumber>
			<xsl:copy-of select="//*[local-name() = 'orbitNumber'][@*[local-name()='type' and .='stop']]/text()"/>
		</lastOrbitNumber>
		<relativeOrbitNumber>
			<xsl:copy-of select="//*[local-name() = 'relativeOrbitNumber'][@*[local-name()='type' and .='start']]/text()"/>
		</relativeOrbitNumber>
		<creationTime>
			<xsl:copy-of select="string(//*[local-name() = 'processing']/@start)"/>
		</creationTime>
		<site>
			<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/@site)"/>
		</site>

		<instrumentShortName>HKTM</instrumentShortName>

		<productType>PRD_HKTM__</productType>
	</xsl:template>
</xsl:stylesheet>