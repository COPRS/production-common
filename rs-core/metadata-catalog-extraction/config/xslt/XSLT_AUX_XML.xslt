<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

    <xsl:template match="/">
	    <xsl:choose>
	    	<xsl:when test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'Creation_Date']/text() != ''">
	    		<creationTime>
					<xsl:value-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'Creation_Date']/text()"/>
				</creationTime>
			</xsl:when>
			<xsl:otherwise>
				<creationTime>
		    	    <xsl:copy-of select="//*[local-name() = 'Creation_Time']/text()"/>
			    </creationTime>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
	    	<xsl:when test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Validity_Period']/*[local-name() = 'Validity_Start']/text() != ''">
	    		<validityStartTime>
					<xsl:value-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Validity_Period']/*[local-name() = 'Validity_Start']/text()"/>
				</validityStartTime>
			</xsl:when>
			<xsl:otherwise>
				<validityStartTime>
	        		<xsl:copy-of select="//*[local-name() = 'Validity_Start']/text()"/>
				</validityStartTime>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
	    	<xsl:when test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Validity_Period']/*[local-name() = 'Validity_Stop']/text() != ''">
	    		<validityStopTime>
					<xsl:value-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Validity_Period']/*[local-name() = 'Validity_Stop']/text()"/>
				</validityStopTime>
			</xsl:when>
			<xsl:otherwise>
			    <validityStopTime>
	        		<xsl:copy-of select="//*[local-name() = 'Validity_Stop']/text()"/>
	    		</validityStopTime>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'System']/text() != ''">
			<site>
				<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'System']/text()"/>
			</site>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'Creator_Version']/text() != ''">
			<processorVersion>
				<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Source']/*[local-name() = 'Creator_Version']/text()"/>
			</processorVersion>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'File_Type']/text() != ''">
			<productType>
				<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'File_Type']/text()"/>
			</productType>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Mission']/text() != ''">
			<platformShortName>
				<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Mission']/text()"/>
			</platformShortName>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Mission']/text() != ''">
			<platformSerialIdentifier>
				<xsl:copy-of select="//*[local-name() = 'Earth_Explorer_Header']/*[local-name() = 'Fixed_Header']/*[local-name() = 'Mission']/text()"/>
			</platformSerialIdentifier>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>