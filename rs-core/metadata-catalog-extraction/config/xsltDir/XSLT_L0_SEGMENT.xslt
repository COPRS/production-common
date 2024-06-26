<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

   <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
   </xsl:template>
   
   <xsl:template match="/">
    	<productConsolidation>
			<xsl:copy-of select="//*[local-name() = 'productConsolidation']/text()"/>
		</productConsolidation>
		<productSensingConsolidation>
			<xsl:copy-of select="//*[local-name() = 'productSensingConsolidation']/text()" />
		</productSensingConsolidation>
		<instrumentConfigurationId>
			<xsl:copy-of select="//*[local-name() = 'instrumentConfigurationID']/text()"/>
		</instrumentConfigurationId>
		<missionDataTakeId>
			<xsl:copy-of select="//*[local-name() = 'missionDataTakeID']/text()"/>
		</missionDataTakeId>
		<circulationFlag>
			<xsl:copy-of select="//*[local-name() = 'circulationFlag']/text()"/>
		</circulationFlag>
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
		<segmentCoordinates>
			<xsl:copy-of select="//*[local-name() = 'coordinates']/text()"/>
		</segmentCoordinates>
		<xsl:if test="string(//*[local-name() = 'processing']/@start) != ''">
			<creationTime>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/@start)"/>
			</creationTime>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[name() = 's1:qualityProperties']/*[name() = 's1:numOfMissingElements']/text() != ''">
			<qualityNumOfMissingElements>
				<xsl:copy-of select="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[name() = 's1:qualityProperties']/*[name() = 's1:numOfMissingElements']/text()"/>
			</qualityNumOfMissingElements>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[name() = 's1:qualityProperties']/*[name() = 's1:numOfCorruptedElements']/text() != ''">
			<qualityNumOfCorruptedElements>
				<xsl:copy-of select="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[name() = 's1:qualityProperties']/*[name() = 's1:numOfCorruptedElements']/text()"/>
			</qualityNumOfCorruptedElements>
		</xsl:if>
		<xsl:apply-templates select="node()"/>
	</xsl:template>

   	<xsl:template match="//*[local-name() = 'packetStoreID']">
		<packetStoreID><xsl:value-of select="text()"/></packetStoreID>
	</xsl:template>

</xsl:stylesheet>