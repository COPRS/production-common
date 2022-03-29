<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />
    <xsl:template match="/">
    	<instrumentConfigurationId>
			<xsl:copy-of select="//*[local-name() = 'instrumentConfigurationID']/text()"/>
		</instrumentConfigurationId>
		<missionDataTakeId>
			<xsl:copy-of select="//*[local-name() = 'missionDataTakeID']/text()"/>
		</missionDataTakeId>
		<pass>
			<xsl:copy-of select="//*[local-name() = 'pass']/text()"/>
		</pass>
		<absoluteStartOrbit>
			<xsl:copy-of select="//*[local-name() = 'orbitNumber'][@*[local-name()='type' and .='start']]/text()"/>
		</absoluteStartOrbit>
		<absoluteStopOrbit>
			<xsl:copy-of select="//*[local-name() = 'orbitNumber'][@*[local-name()='type' and .='stop']]/text()"/>
		</absoluteStopOrbit>
		<relativeStartOrbit>
			<xsl:copy-of select="//*[local-name() = 'relativeOrbitNumber'][@*[local-name()='type' and .='start']]/text()"/>
		</relativeStartOrbit>
		<relativeStopOrbit>
			<xsl:copy-of select="//*[local-name() = 'relativeOrbitNumber'][@*[local-name()='type' and .='stop']]/text()"/>
		</relativeStopOrbit>
		<sliceNumber>
			<xsl:copy-of select="//*[local-name() = 'sliceNumber']/text()"/>
		</sliceNumber>
		<startTime>
			<xsl:copy-of select="//*[local-name() = 'startTime']/text()"/>
		</startTime>
		<stopTime>
			<xsl:copy-of select="//*[local-name() = 'stopTime']/text()"/>
		</stopTime>
		<startTimeANX>
			<xsl:copy-of select="//*[local-name() = 'startTimeANX']/text()"/>
		</startTimeANX>
		<stopTimeANX>
			<xsl:copy-of select="//*[local-name() = 'stopTimeANX']/text()"/>
		</stopTimeANX>
		<sliceCoordinates>
			<xsl:copy-of select="//*[local-name() = 'coordinates']/text()"/>
		</sliceCoordinates>
	</xsl:template>
</xsl:stylesheet>