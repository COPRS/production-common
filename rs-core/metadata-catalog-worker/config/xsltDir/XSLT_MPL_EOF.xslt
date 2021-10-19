<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />

	<xsl:variable name="POLARISATION" select="//*[local-name() = 'Polarisation']/text()" />
	<xsl:variable name="ABSOLUT_ORBIT" select="//OSV[position() = 2]/*[local-name() = 'Absolute_Orbit']/text()" />
    <xsl:variable name="PRODUCT_TYPE" select= "//*[local-name() = 'File_Type']/text()" />

	<xsl:template match="/">
	
		<xsl:variable name="CREATION_TIME" select= "//*[local-name() = 'Creation_Date']/text()"/>
    	<xsl:if test="$CREATION_TIME != ''">
    		<creationTime>
				<xsl:value-of select="$CREATION_TIME"/>
			</creationTime>
		</xsl:if>
		
		<xsl:variable name="VALIDITY_START" select= "//*[local-name() = 'Validity_Start']/text()"/>
    	<xsl:if test="$VALIDITY_START != ''">
    		<validityStartTime>
				<xsl:value-of select="$VALIDITY_START"/>
			</validityStartTime>
		</xsl:if>
		
		<xsl:variable name="VALIDITY_STOP" select= "//*[local-name() = 'Validity_Stop']/text()"/>
    	<xsl:if test="$VALIDITY_STOP != ''">
    		<validityStopTime>
				<xsl:value-of select="$VALIDITY_STOP"/>
			</validityStopTime>
		</xsl:if>
		
		<version>
			<xsl:copy-of select="//*[local-name() = 'File_Version']/text()" />
		</version>
		<site>
			<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'System']/text()"/>
		</site>
		<processorVersion>
			<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'Creator_Version']/text()"/>
		</processorVersion>
		
		<xsl:choose>
			<xsl:when test="contains(//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'File_Type']/text(), 'ERRMAT')">
				<productType>AUX_ECE</productType>					
			</xsl:when>
			<xsl:otherwise>
				<productType>
					<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'File_Type']/text()"/>
				</productType>
			</xsl:otherwise>			
		</xsl:choose>
		
		<xsl:if test="$POLARISATION != ''">
			<polarisation>
				<xsl:copy-of select="//*[local-name() = 'Polarisation']/text()" />
			</polarisation>
		</xsl:if>

		<absolutOrbit>
			<xsl:choose>
				<xsl:when test="starts-with($ABSOLUT_ORBIT, '+')">
					<xsl:copy-of
						select="substring($ABSOLUT_ORBIT,2)" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$ABSOLUT_ORBIT" />
				</xsl:otherwise>
			</xsl:choose>
		</absolutOrbit>
		
	    <xsl:if test="$PRODUCT_TYPE = 'AUX_RESORB'">
			<selectedOrbitFirstAzimuthTimeUtc>
			<xsl:copy-of select="//OSV[position() = 2]/*[local-name() = 'UTC']/text()" />
		</selectedOrbitFirstAzimuthTimeUtc>
		</xsl:if>
		
		<xsl:variable name="MISSION" select= "//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Mission']/text()"/>
    	<xsl:if test="$MISSION != ''">
    		<xsl:variable name="SAT_NUMBER" select= "substring($MISSION, string-length($MISSION))"/>
    		<xsl:variable name="SAT_FAMILY" select= "substring-before($MISSION, $SAT_NUMBER)"/>
    		<platformShortName>
				<xsl:value-of select="$SAT_FAMILY"/>
			</platformShortName>
			<platformSerialIdentifier>
				<xsl:value-of select="$SAT_NUMBER"/>
			</platformSerialIdentifier>
		</xsl:if>
		
</xsl:template>
</xsl:stylesheet>
