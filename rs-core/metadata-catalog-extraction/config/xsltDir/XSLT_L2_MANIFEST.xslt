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
		<coordinates>
			<xsl:copy-of select="//*[local-name() = 'coordinates']/text()"/>
		</coordinates>
		<xsl:if test="string(//*[local-name() = 'processing']/@start) != ''">
			<creationTime>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/@start)"/>
			</creationTime>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/@site) != ''">
			<site>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/@site)"/>
			</site>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/*[local-name() = 'software']/@name) != ''">
			<processorName>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/*[local-name() = 'software']/@name)"/>
			</processorName>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/*[local-name() = 'software']/@version) != ''">
			<processorVersion>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/*[local-name() = 'software']/@version)"/>
			</processorVersion>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'platform']/*[local-name() = 'familyName']/text() != ''">
			<platformShortName>
				<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'familyName']/text()"/>
			</platformShortName>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'platform']/*[local-name() = 'number']/text() != ''">
			<platformSerialIdentifier>
				<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'number']/text()"/>
			</platformSerialIdentifier>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'familyName']/@abbreviation) != ''">
			<instrumentShortName>
				<xsl:copy-of select="string(//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'familyName']/@abbreviation)"/>
			</instrumentShortName>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'extension']/*[local-name() = 'instrumentMode']/*[local-name() = 'swath']/text() != ''">
			<swathIdentifier>
				<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'extension']/*[local-name() = 'instrumentMode']/*[local-name() = 'swath']/text()"/>
			</swathIdentifier>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'orbitReference']/*[local-name() = 'cycleNumber']/text() != ''">
			<cycleNumber>
				<xsl:copy-of select="//*[local-name() = 'orbitReference']/*[local-name() = 'cycleNumber']/text()"/>
			</cycleNumber>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'productComposition']/text() != ''">
			<productComposition>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'productComposition']/text()"/>		
			</productComposition>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'productTimelinessCategory']/text() != ''">
			<safeTimeliness>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'productTimelinessCategory']/text()"/>
			</safeTimeliness>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'sliceProductFlag']/text() != ''">
			<sliceProductFlag>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'sliceProductFlag']/text()"/>
			</sliceProductFlag>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'segmentStartTime']/text() != ''">
			<segmentStartTime>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'segmentStartTime']/text()"/>
			</segmentStartTime>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'totalSlices']/text() != ''">
			<totalNumberOfSlice>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'totalSlices']/text()"/>
			</totalNumberOfSlice>
		</xsl:if>
		<polarisationChannels>
			<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'transmitterReceiverPolarisation'][1]/text()"/>
		</polarisationChannels>		
		<polarisationChannels>
			<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'transmitterReceiverPolarisation'][2]/text()"/>
		</polarisationChannels>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'productClass']/text() != ''">
			<productClass>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'productClass']/text()"/>
			</productClass>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'extension']/*[local-name() = 'instrumentMode']/*[local-name() = 'mode']/text() != ''">
			<operationalMode>
				<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'extension']/*[local-name() = 'instrumentMode']/*[local-name() = 'mode']/text()"/>
			</operationalMode>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[local-name() = 'qualityProperties']/*[local-name() = 'numOfMissingElements']/text() != ''">
			<qualityNumOfMissingElements>
				<xsl:copy-of select="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[local-name() = 'qualityProperties']/*[local-name() = 'numOfMissingElements']/text()"/>
			</qualityNumOfMissingElements>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[local-name() = 'qualityProperties']/*[local-name() = 'numOfCorruptedElements']/text() != ''">
			<qualityNumOfCorruptedElements>
				<xsl:copy-of select="//*[local-name() = 'qualityInformation'][1]/*[local-name() = 'extension']/*[local-name() = 'qualityProperties']/*[local-name() = 'numOfCorruptedElements']/text()"/>
			</qualityNumOfCorruptedElements>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>