<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />

	<!--================================== local variables ========================================= -->
	<xsl:variable name="PRODUCT_NAME"
		select="//*[local-name()='generalProductInformation']//*[local-name()='productName']/text()" />
	<xsl:variable name="PRODUCT_TYPE"
		select="substring($PRODUCT_NAME, 5, 11)" />
	<xsl:variable name="PRODUCT_GROUP"
	    select="substring($PRODUCT_TYPE, 1, 2)" />

	<!--======= Create a flat XML structure with the necessary information ======= -->
	<xsl:template match="/">
		<startTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='acquisitionPeriod']/*[local-name()='startTime']" />
			</xsl:call-template>
		</startTime>

		<stopTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='acquisitionPeriod']/*[local-name()='stopTime']" />
			</xsl:call-template>
		</stopTime>

		<!-- currently the aux search is used to search for level products, and the aux 
			search needs the field validityStartTime -->
		<validityStartTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='acquisitionPeriod']/*[local-name()='startTime']" />
			</xsl:call-template>
		</validityStartTime>

		<!-- currently the aux search is used to search for level products, and the aux 
			search needs the field validityStopTime -->
		<validityStopTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='acquisitionPeriod']/*[local-name()='stopTime']" />
			</xsl:call-template>
		</validityStopTime>

		<creationTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='creationTime']/text()" />
			</xsl:call-template>
		</creationTime>

		<baselineCollection>
			<xsl:value-of
				select="//*[local-name()='generalProductInformation']/*[local-name()='baselineCollection']" />
		</baselineCollection>

		<sliceCoordinates>
			<xsl:value-of select="normalize-space(//*[local-name()='frameSet']/*[local-name()='footPrint']/*[local-name()='posList']/text())"/>
		</sliceCoordinates>

		<site>
			<xsl:value-of
				select="//*[local-name()='processing']/*[local-name()='facility']/@site" />
		</site>

		<xsl:call-template name="getOrbits">
			<xsl:with-param name="xfduOrbitName"
				select="'orbitNumber'" />
			<xsl:with-param name="startOrbitName"
				select="'absoluteStartOrbit'" />
			<xsl:with-param name="stopOrbitName"
				select="'absoluteStopOrbit'" />
		</xsl:call-template>

		<orbitDirection>
			<xsl:value-of
				select="//*[local-name()='orbitNumber'][@type='start']/@groundTrackDirection" />
		</orbitDirection>

		<stopOrbitDirection>
			<xsl:value-of
				select="//*[local-name()='orbitNumber'][@type='stop']/@groundTrackDirection" />
		</stopOrbitDirection>

		<xsl:call-template name="getOrbits">
			<xsl:with-param name="xfduOrbitName"
				select="'relativeOrbitNumber'" />
			<xsl:with-param name="startOrbitName"
				select="'relativeStartOrbit'" />
			<xsl:with-param name="stopOrbitName"
				select="'relativeStopOrbit'" />
		</xsl:call-template>
		
		<cycleNumber>
			<xsl:value-of
				select="//*[local-name()='orbitReference']/*[local-name()='cycleNumber']/text()" />
		</cycleNumber>

		<receivingGroundStation>
			<xsl:value-of
				select="//*[local-name()='receivingGroundStation']" />
		</receivingGroundStation>

		<!-- for OLCI calibration products we need the L1Triggering value -->
		<xsl:if
			test="$PRODUCT_TYPE = 'OL_0_CR0___' or  $PRODUCT_TYPE = 'OL_0_CR1___' ">
			<L1Triggering>
				<xsl:value-of
					select="//*[local-name()='L1Triggering']/@triggers" />
			</L1Triggering>
		</xsl:if>
		
		<platformShortName>
			<xsl:value-of
				select="//*[local-name()='platform']/*[local-name()='familyName']/text()" />
		</platformShortName>
		
		<platformSerialIdentifier>
			<xsl:value-of
				select="//*[local-name()='platform']/*[local-name()='number']/text()" />
		</platformSerialIdentifier>

		<instrumentName>
			<xsl:value-of
				select="//*[local-name()='platform']//*[local-name()='familyName']/@abbreviation" />
		</instrumentName>

		<procTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='processing']/@start" />
			</xsl:call-template>
		</procTime>

		<granuleNumber>
			<xsl:call-template name="setValueOrDefault">
				<xsl:with-param name="nodeName"
					select="'granuleNumber'" />
				<xsl:with-param name="defaultValue" select="'1'" />
			</xsl:call-template>
		</granuleNumber>

		<granulePosition>
			<xsl:call-template name="setValueOrDefault">
				<xsl:with-param name="nodeName"
					select="'granulePosition'" />
				<xsl:with-param name="defaultValue" select="'NONE'" />
			</xsl:call-template>
		</granulePosition>

		<dumpStart>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='dumpInformation']/*[local-name()='dumpStart']" />
			</xsl:call-template>
		</dumpStart>

		<utcTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime">
					<xsl:call-template name="extractUTCTime">
						<xsl:with-param name="elementSet"
							select="//*[local-name()='metadataObject'][@ID='measurementOrbitReference']" />
						<xsl:with-param name="position" select="'1'" />
					</xsl:call-template>
				</xsl:with-param>
			</xsl:call-template>
		</utcTime>

		<utc1Time>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime">
					<xsl:call-template name="extractUTCTime">
						<xsl:with-param name="elementSet"
							select="//*[local-name()='metadataObject'][@ID='measurementOrbitReference']" />
						<xsl:with-param name="position" select="'2'" />
					</xsl:call-template>
				</xsl:with-param>
			</xsl:call-template>
		</utc1Time>

		<processingLevel>
			<xsl:value-of select="substring($PRODUCT_NAME, 8, 1)" />
		</processingLevel>

		<procVersion>
			<xsl:value-of
				select="//*[local-name()='processing']//*[local-name()='software']/@version" />
		</procVersion>

		<procName>
			<xsl:value-of
				select="//*[local-name()='processing']//*[local-name()='software']/@name" />
		</procName>

		<qualityIndicator>
			<xsl:value-of
				select="//*[local-name()='onlineQualityCheck']" />
		</qualityIndicator>

		<xsl:call-template name="setTimeliness">
			<xsl:with-param name="timeliness">
				<xsl:value-of
					select="//*[local-name()='generalProductInformation']/*[local-name()='timeliness']" />
			</xsl:with-param>
		</xsl:call-template>

		<operationalMode>
			<xsl:value-of
				select="//*[local-name()='instrument']//*[local-name()='mode']" />
		</operationalMode>
			
		<brightPercentage>
			<xsl:value-of
				select="//*[local-name()='classificationSummary']//*[local-name()='brightPixels']/@percentage" />
		</brightPercentage>

		<snowOrIcePercentage>
			<xsl:value-of
				select="//*[local-name()='classificationSummary']//*[local-name()='snowOrIcePixels']/@percentage" />
		</snowOrIcePercentage>

		<salineWaterPercentage>
			<xsl:choose>
				<xsl:when test="$PRODUCT_GROUP = 'SL'">
					<xsl:value-of
						select="//*[local-name()='classificationSummary'][@grid='1 km']//*[local-name()='salineWaterPixels']/@percentage" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="//*[local-name()='classificationSummary']//*[local-name()='salineWaterPixels']/@percentage" />
				</xsl:otherwise>
			</xsl:choose>
		</salineWaterPercentage>

		<coastalPercentage>
			<xsl:choose>
				<xsl:when test="$PRODUCT_GROUP = 'SL'">
					<xsl:value-of
						select="//*[local-name()='classificationSummary'][@grid='1 km']//*[local-name()='coastalPixels']/@percentage" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="//*[local-name()='classificationSummary']//*[local-name()='coastalPixels']/@percentage" />
				</xsl:otherwise>
			</xsl:choose>
		</coastalPercentage>

		<freshInlandWaterPercentage>
			<xsl:choose>
				<xsl:when test="$PRODUCT_GROUP = 'SL'">
					<xsl:value-of
						select="//*[local-name()='classificationSummary'][@grid='1 km']//*[local-name()='freshInlandWaterPixels']/@percentage" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="//*[local-name()='classificationSummary']//*[local-name()='freshInlandWaterPixels']/@percentage" />
				</xsl:otherwise>
			</xsl:choose>
		</freshInlandWaterPercentage>

		<tidalRegionPercentage>
			<xsl:choose>
				<xsl:when test="$PRODUCT_GROUP = 'SL'">
					<xsl:value-of
						select="//*[local-name()='classificationSummary'][@grid='1 km']//*[local-name()='tidalRegionPixels']/@percentage" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="//*[local-name()='classificationSummary']//*[local-name()='tidalRegionPixels']/@percentage" />
				</xsl:otherwise>
			</xsl:choose>
		</tidalRegionPercentage>

		<landPercentage>
			<xsl:choose>
				<xsl:when test="$PRODUCT_GROUP = 'SL'">
					<xsl:value-of
						select="//*[local-name()='classificationSummary'][@grid='1 km']//*[local-name()='landPixels']/@percentage" />
				</xsl:when>
				<xsl:when test="$PRODUCT_GROUP = 'SR'">
					<xsl:value-of
						select="//*[local-name()='sralProductInformation']//*[local-name()='landPercentage']" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="//*[local-name()='classificationSummary']//*[local-name()='landPixels']/@percentage" />
				</xsl:otherwise>
			</xsl:choose>
		</landPercentage>

		<closedSeaPercentage>
			<xsl:value-of
				select="//*[local-name()='sralProductInformation']//*[local-name()='closedSeaPercentage']" />
		</closedSeaPercentage>

		<continentalIcePercentage>
			<xsl:value-of
				select="//*[local-name()='sralProductInformation']//*[local-name()='continentalIcePercentage']" />
		</continentalIcePercentage>

		<openOceanPercentage>
			<xsl:value-of
				select="//*[local-name()='sralProductInformation']//*[local-name()='openOceanPercentage']" />
		</openOceanPercentage>

		<cloudPercentage>
			<xsl:choose>
				<xsl:when test="$PRODUCT_GROUP = 'SL'">
					<xsl:value-of
						select="//*[local-name()='classificationSummary'][@grid='1 km']//*[local-name()='cloudyPixels']/@percentage" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="//*[local-name()='classificationSummary']//*[local-name()='cloudyPixels']/@percentage" />
				</xsl:otherwise>
			</xsl:choose>
		</cloudPercentage>
		
		<s3timeliness>
			<xsl:value-of
				select="//*[local-name()='generalProductInformation']//*[local-name()='timeliness']" />
		</s3timeliness>

	</xsl:template>

	<!--=================== Getting Timeliness Fields Template =================== -->
	<xsl:template name="setTimeliness">
		<xsl:param name="timeliness" />
		<xsl:variable name="NRT">
			<xsl:choose>
				<xsl:when
					test="$timeliness = 'NR' or $timeliness = 'NN' or $timeliness = 'NS' or $timeliness = 'AL' or $timeliness = '__'">
					<xsl:value-of select="'true'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'false'" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="NTC">
			<xsl:choose>
				<xsl:when
					test="$timeliness = 'NT' or $timeliness = 'SN' or $timeliness = 'NN' or $timeliness = 'AL' or $timeliness = '__'">
					<xsl:value-of select="'true'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'false'" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="STC">
			<xsl:choose>
				<xsl:when
					test="$timeliness = 'ST' or $timeliness = 'SN' or $timeliness = 'NS' or $timeliness = 'AL' or $timeliness = '__'">
					<xsl:value-of select="'true'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'false'" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<NRT>
			<xsl:value-of select="$NRT" />
		</NRT>
		<STC>
			<xsl:value-of select="$STC" />
		</STC>
		<NTC>
			<xsl:value-of select="$NTC" />
		</NTC>
		
	</xsl:template>

	<!--==================== Format Date Time Template ========================= -->
	<!-- Desired format: yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z' -->
	<xsl:template name="formatDateTime">
		<xsl:param name="receivedDateTime" />
		<xsl:variable name="dateLength"
			select="string-length($receivedDateTime)" />
		<xsl:choose>
			<xsl:when test="$dateLength='15'">
				<xsl:call-template name="normalizeDateTime">
					<xsl:with-param name="receivedDateTime"
						select="$receivedDateTime" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$receivedDateTime" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="normalizeDateTime">
		<xsl:param name="receivedDateTime" />
		<xsl:variable name="year"
			select="substring($receivedDateTime,1,4)" />
		<xsl:variable name="month"
			select="substring($receivedDateTime,5,2)" />
		<xsl:variable name="day"
			select="substring($receivedDateTime,7,2)" />
		<xsl:variable name="hour"
			select="substring($receivedDateTime,10,2)" />
		<xsl:variable name="minute"
			select="substring($receivedDateTime,12,2)" />
		<xsl:variable name="second"
			select="substring($receivedDateTime,14,2)" />
		<xsl:value-of
			select="concat($year,'-',$month,'-',$day,'T',$hour,':',$minute,':',$second,'.','000000Z')" />
	</xsl:template>

	<!--============================== Getting Orbits Template =================================== -->
	<!-- template to extract absolute and relative orbitNumbers (start and stop) -->
	<xsl:template name="getOrbits">
		<xsl:param name="xfduOrbitName" />
		<xsl:param name="startOrbitName" select="absoluteStartOrbit" />
		<xsl:param name="stopOrbitName" select="absoluteStopOrbit" />
		<xsl:variable name="startOrbitNumber"
			select="//metadataObject//*[local-name()=$xfduOrbitName][@type='start']" />
		<xsl:element name="{$startOrbitName}">
			<xsl:value-of select="$startOrbitNumber" />
		</xsl:element>
		<xsl:choose>
			<xsl:when
				test="//metadataObject//*[local-name()=$xfduOrbitName]/@type='stop'">
				<xsl:element name="{$stopOrbitName}">
					<xsl:value-of
						select="//metadataObject//*[local-name()=$xfduOrbitName][@type='stop']" />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="{$stopOrbitName}">
					<xsl:value-of select="$startOrbitNumber" />
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--================================ Extract UTC Time Template ================================= -->
	<!-- extracts the utc time from the given element set and position -->
	<xsl:template name="extractUTCTime">
		<xsl:param name="elementSet" />
		<xsl:param name="position" />
		<xsl:value-of
			select="$elementSet/metadataWrap/xmlData//*[local-name()='ephemeris'][position()=$position]//*[local-name()='epoch'][@type='UTC']" />
	</xsl:template>

	<!-- ================================ Set Value or default Template ==================================== -->
	<!-- takes a name of a node, checks whether it exists, and if not sets the 
		default -->
	<xsl:template name="setValueOrDefault">
		<xsl:param name="nodeName" />
		<xsl:param name="defaultValue" />

		<xsl:choose>
			<xsl:when test="count(//*[local-name()=$nodeName])=0">
				<xsl:value-of select="$defaultValue" />
			</xsl:when>
			<xsl:when test="//*[local-name()=$nodeName]= ''">
				<xsl:value-of select="$defaultValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="//*[local-name()=$nodeName]" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>