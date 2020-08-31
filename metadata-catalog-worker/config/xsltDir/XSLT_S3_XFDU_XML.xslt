<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />

	<!--================================== local variables ========================================= -->
	<xsl:variable name="PRODUCT_NAME"
		select="//*[local-name()='generalProductInformation']//*[local-name()='productName']/text()" />
	<xsl:variable name="PRODUCT_TYPE"
		select="substring($PRODUCT_NAME, 5, 11)" />

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

		<xsl:call-template name="getBoundingPolygon" />

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

		<xsl:call-template name="getOrbits">
			<xsl:with-param name="xfduOrbitName"
				select="'relativeOrbitNumber'" />
			<xsl:with-param name="startOrbitName"
				select="'relativeStartOrbit'" />
			<xsl:with-param name="stopOrbitName"
				select="'relativeStopOrbit'" />
		</xsl:call-template>

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

	<!--============================== Bounding Polygon Template =================================== -->

	<xsl:template name="getBoundingPolygon">
		<xsl:variable name="coord">
			<xsl:call-template name="checkCoordinatesString">
				<xsl:with-param name="listOfCoordinates"
					select="concat(normalize-space(//*[local-name()='frameSet']/*[local-name()='footPrint']/*[local-name()='posList']/text()),' ')" />
				<xsl:with-param name="first" select="''" />
				<xsl:with-param name="coords" select="''" />
				<xsl:with-param name="pointsnum" select="'0'" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="string-length($coord) != 0">
			<boundingPolygon>
				<xsl:call-template name="convertToPolygon">
					<xsl:with-param name="listOfCoordinates"
						select="concat($coord,',')" />
				</xsl:call-template>
			</boundingPolygon>
		</xsl:if>
	</xsl:template>

	<!--============================== Check Coordinates Template =================================== -->
	<!-- Checks whether or not the provided coordinates string is at least 4 
		positions long and whether the first and last point are the same -->
	<xsl:template name="checkCoordinatesString">
		<xsl:param name="listOfCoordinates" />
		<xsl:param name="first" />
		<xsl:param name="coords" />
		<xsl:param name="pointsnum" />

		<xsl:choose>
			<!-- list of coordinates is exhausted, but the first and last point aren't 
				the same -->
			<xsl:when
				test="$listOfCoordinates = '' or $listOfCoordinates = ' ' ">
				<xsl:message terminate="yes">
					Error: first and last points of a footprint must be equal.
				</xsl:message>
			</xsl:when>
			<!-- first and last point are the same. Checks if the list of coordinates 
				(without the last point) is at least 3 entries long (the minimum polygon 
				is a triangle). If there are at least 3 entries, return the (formatted) list 
				of coordinates -->
			<xsl:when test="$first = $listOfCoordinates">
				<xsl:choose>
					<xsl:when test="$pointsnum &lt; 3">
						<xsl:message terminate="yes">
							Error: product provides only 3 points as a footprint! At least 4
							points are expected and the first and the last points must be
							equal.
						</xsl:message>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$coords" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- There are still some coordinates left to process. Call this template 
				recursively, until there are no points to process -->
			<xsl:when test="$first != $listOfCoordinates">
				<xsl:variable name="newpointsnum"
					select="$pointsnum + 1" />
				<xsl:variable name="newcoords">
					<xsl:call-template name="getNextCoordinate">
						<xsl:with-param name="listOfCoordinates"
							select="normalize-space($listOfCoordinates)" />
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="remainingListOfCoordinates"
					select="normalize-space(substring-after($listOfCoordinates,$newcoords))" />
				<xsl:variable name="newfirst">
					<xsl:choose>
						<xsl:when test="$first=''">
							<xsl:value-of select="$newcoords" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$first" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:call-template name="checkCoordinatesString">
					<xsl:with-param name="listOfCoordinates"
						select="$remainingListOfCoordinates" />
					<xsl:with-param name="first" select="$newfirst" />
					<xsl:with-param name="coords">
						<xsl:choose>
							<xsl:when test="$coords=''">
								<xsl:value-of select="$newcoords" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of
									select="concat($coords,',',$newcoords)" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:with-param>
					<xsl:with-param name="pointsnum"
						select="$newpointsnum" />
				</xsl:call-template>
			</xsl:when>

		</xsl:choose>
	</xsl:template>

	<!--============================== Get Next Coordinate Template =================================== -->
	<!-- extracts the next coordinate (two floating point numbers separated 
		by a space) from the list of coordinates -->
	<xsl:template name="getNextCoordinate">
		<xsl:param name="listOfCoordinates" />
		<xsl:variable name="longitude"
			select="substring-before($listOfCoordinates,' ')" />
		<xsl:variable name="tmp"
			select="substring-after($listOfCoordinates,' ')" />
		<xsl:variable name="latitude"
			select="substring-before($tmp,' ')" />
		<xsl:value-of select="concat($longitude,' ',$latitude)" />
	</xsl:template>


	<!--============================== Convert to Polygon Template ================================= -->
	<!-- Coordinates format: lat1,lon1 lat2,lon2 lat3,lon3 ... -->
	<xsl:template name="convertToPolygon">
		<xsl:param name="listOfCoordinates" />
		<xsl:variable name="separator" select="','" />
		<xsl:choose>
			<xsl:when test="contains($listOfCoordinates,' ')">
				<xsl:variable name="coords"
					select="substring-before($listOfCoordinates, $separator)" />
				<xsl:call-template name="getPoints">
					<xsl:with-param name="coords" select="$coords" />
				</xsl:call-template>
				<xsl:variable name="newListOfCoordinates"
					select="substring-after($listOfCoordinates,$separator)" />
				<xsl:call-template name="convertToPolygon">
					<xsl:with-param name="listOfCoordinates"
						select="normalize-space($newListOfCoordinates)" />
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="getPoints">
		<xsl:param name="coords" />
		<points>
			<xsl:value-of select="normalize-space($coords)" />
		</points>
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