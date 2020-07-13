<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />

	<!--================================== local variables ========================================= -->
	<xsl:variable name="PRODUCT_NAME"
		select="//*[local-name()='keys']/feature[@key='originalName']" />
	<xsl:variable name="PRODUCT_TYPE"
		select="substring($PRODUCT_NAME, 5, 11)" />

	<!--======= Create a flat XML structure with the necessary information ======= -->
	<xsl:template match="/">
		<validityStartTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='temporalCoverage']/startTime" />
			</xsl:call-template>
		</validityStartTime>

		<validityStopTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='temporalCoverage']/stopTime" />
			</xsl:call-template>
		</validityStopTime>

		<creationTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="substring($PRODUCT_NAME, 49, 15)" />
			</xsl:call-template>
		</creationTime>

		<ISIPProvider>
			<xsl:value-of
				select="//*[local-name()='specificParameters']/feature[@key='ISIPProvider']" />
		</ISIPProvider>

		<dumpStart>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='specificParameters']/feature[@key='dumpStart']" />
			</xsl:call-template>
		</dumpStart>
		
		<receivingStartTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='specificParameters']/feature[@key='receivingStartTime']" />
			</xsl:call-template>
		</receivingStartTime>

		<receivingStopTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime"
					select="//*[local-name()='specificParameters']/feature[@key='receivingStopTime']" />
			</xsl:call-template>
		</receivingStopTime>

		<receivingGroundStation>
			<xsl:value-of
				select="//*[local-name()='specificParameters']/feature[@key='receivingGroundStation']" />
		</receivingGroundStation>
		
		<granuleNumber>
			<xsl:value-of
				select="//*[local-name()='specificParameters']/feature[@key='granuleNumber']" />
		</granuleNumber>

		<granulePosition>
			<xsl:value-of
				select="//*[local-name()='specificParameters']/feature[@key='granulePosition']" />
		</granulePosition>

		<qualityIndicator>
			<xsl:value-of
				select="//*[local-name()='quality']" />
		</qualityIndicator>

		<xsl:call-template name="setTimeliness" />
	</xsl:template>

	<!--=================== Getting Timeliness Fields Template =================== -->
	<!-- L0 products should always use NRT -->
	<xsl:template name="setTimeliness">
		<timeliness>
			<NRT>
				<xsl:value-of select="'true'" />
			</NRT>
			<STC>
				<xsl:value-of select="'false'" />
			</STC>
			<NTC>
				<xsl:value-of select="'false'" />
			</NTC>
		</timeliness>
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

</xsl:stylesheet>