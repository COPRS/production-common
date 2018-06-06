<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xfdu="urn:ccsds:schema:xfdu:1" xmlns:s1="http://www.esa.int/safe/sentinel-1.0/sentinel-1" xmlns:s="http://www.esa.int/safe/sentinel-1.0" xmlns:s1sar="http://www.esa.int/safe/sentinel-1.0/sentinel-1/sar" xmlns:s1sarl1="http://www.esa.int/safe/sentinel-1.0/sentinel-1/sar/level-1" xmlns:gml="http://www.opengis.net/gml">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />
    
    <xsl:template match="/">
    	<instrumentConfigurationId>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='generalProductInformation']/metadataWrap/xmlData/s1sarl1:standAloneProductInformation/s1sarl1:instrumentConfigurationID/text()"/>
		</instrumentConfigurationId>
		<missionDataTakeId>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='generalProductInformation']/metadataWrap/xmlData/s1sarl1:standAloneProductInformation/s1sarl1:missionDataTakeID/text()"/>
		</missionDataTakeId>
		<pass>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='measurementOrbitReference']/metadataWrap/xmlData/s:orbitReference/s:extension/s1:orbitProperties/s1:pass/text()"/>
		</pass>
		<absoluteStartOrbit>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='measurementOrbitReference']/metadataWrap/xmlData/s:orbitReference/s:orbitNumber[@type='start']/text()"/>
		</absoluteStartOrbit>
		<absoluteStopOrbit>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='measurementOrbitReference']/metadataWrap/xmlData/s:orbitReference/s:orbitNumber[@type='stop']/text()"/>
		</absoluteStopOrbit>
		<relativeStartOrbit>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='measurementOrbitReference']/metadataWrap/xmlData/s:orbitReference/s:relativeOrbitNumber[@type='start']/text()"/>
		</relativeStartOrbit>
		<relativeStopOrbit>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='measurementOrbitReference']/metadataWrap/xmlData/s:orbitReference/s:relativeOrbitNumber[@type='stop']/text()"/>
		</relativeStopOrbit>
		<sliceNumber>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='generalProductInformation']/metadataWrap/xmlData/s1sarl1:standAloneProductInformation/s1sarl1:sliceNumber/text()"/>
		</sliceNumber>
		<startTime>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='acquisitionPeriod']/metadataWrap/xmlData/s:acquisitionPeriod/s:startTime/text()"/>
		</startTime>
		<stopTime>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='acquisitionPeriod']/metadataWrap/xmlData/s:acquisitionPeriod/s:stopTime/text()"/>
		</stopTime>
		<startTimeANX>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='acquisitionPeriod']/metadataWrap/xmlData/s:acquisitionPeriod/s:extension/s1:timeANX/s1:startTimeANX/text()"/>
		</startTimeANX>
		<stopTimeANX>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='acquisitionPeriod']/metadataWrap/xmlData/s:acquisitionPeriod/s:extension/s1:timeANX/s1:stopTimeANX/text()"/>
		</stopTimeANX>
		<!-- <totalNumberOfSlice>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject/metadataWrap/xmlData/CHANGEME"/>
		</totalNumberOfSlice>-->
		<sliceCoordinates>
			<xsl:copy-of select="//xfdu:XFDU/metadataSection/metadataObject[@ID='measurementFrameSet']/metadataWrap/xmlData/s:frameSet/s:frame/s:footPrint/gml:coordinates/text()"/>
		</sliceCoordinates>
	</xsl:template>

</xsl:stylesheet>