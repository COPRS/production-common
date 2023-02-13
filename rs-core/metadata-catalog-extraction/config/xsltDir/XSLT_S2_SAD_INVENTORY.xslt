<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />
		
	<xsl:template match="/">
		<validityStartTime>
			<xsl:copy-of select="//*[local-name() = 'Validity_Start']/text()"/>
		</validityStartTime>
		<validityStopTime>
			<xsl:copy-of select="//*[local-name() = 'Validity_Stop']/text()"/>
		</validityStopTime>
		<creationTime>
			<xsl:copy-of select="//*[local-name() = 'Generation_Time']/text()"/>
		</creationTime>
		<orbitNumber>
			<xsl:copy-of select="//*[local-name() = 'Start_Orbit_Number']/text()"/>
		</orbitNumber>
		<lastOrbitNumber>
			<xsl:copy-of select="//*[local-name() = 'Stop_Orbit_Number']/text()"/>
		</lastOrbitNumber>

		<xsl:variable name="IMqualityInfo" select="//*[local-name() = 'Quality_Info']"/>
		<qualityStatus>
			<xsl:choose>
				<xsl:when test="$IMqualityInfo = 100.0">NOMINAL</xsl:when>
				<xsl:otherwise>DEGRADED</xsl:otherwise>
			</xsl:choose>
		</qualityStatus>
		<qualityInfo>
			<xsl:value-of
				select="//*[local-name() = 'Quality_Info']" />
		</qualityInfo>

		<productType>
			<xsl:copy-of select="//*[local-name() = 'File_Type']/text()"/>
		</productType>
		<instrumentShortName>SAD</instrumentShortName>
		<site>
			<xsl:copy-of select="//*[local-name() = 'Processing_Station']/text()"/>
		</site>

		<xsl:variable name="IMsatelliteCode" select="//*[local-name() = 'Satellite_Code']" />
		<platformSerialIdentifier>
			<xsl:value-of select="substring($IMsatelliteCode, 3, 1)" />
		</platformSerialIdentifier>

		<platfomShortName>SENTINEL-2</platfomShortName>
	</xsl:template>
</xsl:stylesheet>