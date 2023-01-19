<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />

	<xsl:template match="/">

		<startTime>
			<xsl:value-of
				select="//*[local-name() = 'Validity_Start']" />
		</startTime>

		<stopTime>
			<xsl:value-of
				select="//*[local-name() = 'Validity_Stop']" />
		</stopTime>

		<productType>
			<xsl:value-of select="//*[local-name() = 'File_Type']" />
		</productType>

		<processorVersion>
			<xsl:value-of
				select="//*[local-name() = 'File_Version']" />
		</processorVersion>

		<processingCenter>
			<xsl:value-of
				select="//*[local-name() = 'Processing_Station']" />
		</processingCenter>

		<creationTime>
			<xsl:value-of
				select="//*[local-name() = 'Generation_Time']" />
		</creationTime>

		<platformSerialIdentifier>
			<xsl:value-of
				select="//*[local-name() = 'Satellite_Code']" />
		</platformSerialIdentifier>

		<instrumentShortName>
			<xsl:value-of select="//*[local-name() = 'Sensor_Code']" />
		</instrumentShortName>

		<orbitNumber>
			<xsl:value-of
				select="//*[local-name() = 'Start_Orbit_Number']" />
		</orbitNumber>

		<productGroupId>
			<xsl:value-of select="//*[local-name() = 'Group_ID']" />
		</productGroupId>

		<qualityStatus>
			<xsl:value-of
				select="//*[local-name() = 'Quality_Info']" />
		</qualityStatus>

		<qualityInfo>
			<xsl:value-of
				select="//*[local-name() = 'Quality_Info']" />
		</qualityInfo>
    
        <cloudPercentage>
            <xsl:value-of
                select="//*[local-name() = 'CloudPercentage']" />
        </cloudPercentage>

		<coordinates>
			<xsl:for-each select="//*[local-name() = 'Geo_Pnt']">
				<xsl:value-of
				    select="./*[local-name() = 'LATITUDE']/text()" />
				<xsl:value-of select="' '" />
				<xsl:value-of
					select="./*[local-name() = 'LONGITUDE']/text()" />
				<xsl:value-of select="' '" />
			</xsl:for-each>
		</coordinates>

	</xsl:template>
</xsl:stylesheet>