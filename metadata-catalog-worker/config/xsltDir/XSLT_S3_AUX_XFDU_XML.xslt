<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />

	<!--======= Create a flat XML structure with the necessary information ======= -->
	<xsl:template match="/">
		<validityStartTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime">
					<xsl:value-of
						select="//*[local-name()='generalProductInformation']/*[local-name()='validityStartTime']" />
				</xsl:with-param>
			</xsl:call-template>
		</validityStartTime>
		<validityStopTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime">
					<xsl:value-of
						select="//*[local-name()='generalProductInformation']/*[local-name()='validityStopTime']" />
				</xsl:with-param>
			</xsl:call-template>
		</validityStopTime>
		<creationTime>
			<xsl:call-template name="formatDateTime">
				<xsl:with-param name="receivedDateTime">
					<xsl:value-of
						select="//*[local-name()='generalProductInformation']/*[local-name()='creationTime']" />
				</xsl:with-param>
			</xsl:call-template>
		</creationTime>
		<adfQualityCheck>
			<xsl:value-of
				select="//*[local-name()='adfQuality']/*[local-name()='adfQualityCheck']" />
		</adfQualityCheck>
		<baselineCollection>
			<xsl:value-of
				select="//*[local-name()='generalProductInformation']/*[local-name()='baselineCollection']" />
		</baselineCollection>
		<site>
			<xsl:value-of
				select="//*[local-name()='processing']/*[local-name()='facility']/@site" />
		</site>
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
		<timeliness>
			<NRT>
				<xsl:value-of select="$NRT" />
			</NRT>
			<STC>
				<xsl:value-of select="$STC" />
			</STC>
			<NTC>
				<xsl:value-of select="$NTC" />
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