<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="utf-8" indent="yes" />
    <xsl:template match="/">
        <!-- Inventory_Metadata.xml -->
        <xsl:if test="string(//*[local-name() = 'Inventory_Metadata']) != ''">
            <datastripId>
                <xsl:value-of select="//*[local-name() = 'Parent_ID']" />
            </datastripId>

            <productGroupId>
                <xsl:value-of select="//*[local-name() = 'Group_ID']" />
            </productGroupId>

            <processorVersion>
                <xsl:value-of
                    select="//*[local-name() = 'File_Version']" />
            </processorVersion>

            <creationTime>
                <xsl:value-of
                    select="//*[local-name() = 'Generation_Time']" />
            </creationTime>

            <startTime>
                <xsl:value-of
                    select="//*[local-name() = 'Validity_Start']" />
            </startTime>

            <stopTime>
                <xsl:value-of
                    select="//*[local-name() = 'Validity_Stop']" />
            </stopTime>

            <orbitNumber>
                <xsl:value-of
                    select="//*[local-name() = 'Start_Orbit_Number']" />
            </orbitNumber>

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

            <xsl:variable name="IMqualityInfo" select="//*[local-name() = 'Quality_Info']"/>

            <qualityStatus>
                <xsl:choose>
                    <xsl:when test="$IMqualityInfo = 100.0">NOMINAL</xsl:when>
                    <xsl:otherwise>DEGRADED</xsl:otherwise>
                </xsl:choose>
            </qualityStatus>

            <qualityInfo>
                <xsl:value-of
                    select="//*[local-name() = 'Quality_Info']" />
            </qualityInfo>

            <productType>
                <xsl:value-of
                    select="//*[local-name() = 'File_Type']" />
            </productType>

            <instrumentShortName>
                <xsl:value-of select="//*[local-name() = 'Sensor_Code']" />
            </instrumentShortName>

            <processingCenter>
                <xsl:value-of
                    select="//*[local-name() = 'Processing_Station']" />
            </processingCenter>

            <xsl:variable name="IMsatelliteCode" select="//*[local-name() = 'Satellite_Code']" />

            <platformSerialIdentifier>
                <xsl:value-of select="substring($IMsatelliteCode, 3, 1)" />
            </platformSerialIdentifier>

            <cloudCover>
                <xsl:value-of
                    select="//*[local-name() = 'CloudPercentage']" />
            </cloudCover>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-0_DataStrip_ID']) != ''">
            <xsl:variable name="L0DSdatatakeIdentifier" select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/@datatakeIdentifier"/>

            <xsl:variable name="L0DSqualityInfo" select="//*[local-name() = 'Image_Data_Info']/*[local-name() = 'Sensor_Configuration']/*[local-name() = 'Source_Packet_Description']/*[local-name() = 'Degradation_Summary']/*[local-name() = 'NUMBER_OF_LOST_PACKETS']"/>


            <startTime>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datastrip_Time_Info']/*[local-name() = 'DATASTRIP_SENSING_START']"/>
            </startTime>

            <stopTime>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datastrip_Time_Info']/*[local-name() = 'DATASTRIP_SENSING_STOP']"/>
            </stopTime>

            <processorVersion></processorVersion>

            <processingCenter>RS</processingCenter>

            <orbitNumber>
                <xsl:value-of select="substring($L0DSdatatakeIdentifier, 22, 6)"/>
            </orbitNumber>

            <productGroupId>
                <xsl:value-of select="$L0DSdatatakeIdentifier"/>
            </productGroupId>

            <qualityInfo>
                <xsl:value-of select="$L0DSqualityInfo"/>
            </qualityInfo>

            <qualityStatus>
                <xsl:choose>
                    <xsl:when test="$L0DSqualityInfo = 0">NOMINAL</xsl:when>
                    <xsl:otherwise>DEGRADED</xsl:otherwise>
                </xsl:choose>
            </qualityStatus>

            <coordinates>
                <xsl:value-of select="//*[local-name() = 'Quality_Indicators_Info']/*[local-name() = 'Quicklook_Info']/*[local-name() = 'Footprint']/*[local-name() = 'EXT_POS_LIST']"/>
            </coordinates>

            <platformShortName>SENTINEL-2</platformShortName>

            <operationalMode>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'DATATAKE_TYPE']"/>
            </operationalMode>

            <relativeOrbitNumber>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'SENSING_ORBIT_NUMBER']"/>
            </relativeOrbitNumber>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-0_Granule_ID']) != ''">
            <xsl:variable name="L0GRqualityInfo" select="//*[local-name() = 'Quality_Indicators_Info']/*[local-name() = 'Image_Content_QI']/*[local-name() = 'Common_IMG_QI']/*[local-name() = 'DEGRADED_MSI_DATA_PERCENTAGE']"/>

            <startTime>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'SENSING_TIME']"/>
            </startTime>

            <stopTime>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'SENSING_TIME']"/>
            </stopTime>

            <processorVersion></processorVersion>

            <processingCenter>RS</processingCenter>

            <orbitNumber></orbitNumber>

            <productGroupId>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'DATASTRIP_ID']"/>
            </productGroupId>

            <qualityInfo>
                <xsl:value-of select="$L0GRqualityInfo"/>
            </qualityInfo>

            <qualityStatus>
                <xsl:choose>
                    <xsl:when test="$L0GRqualityInfo = 0">NOMINAL</xsl:when>
                    <xsl:otherwise>DEGRADED</xsl:otherwise>
                </xsl:choose>
            </qualityStatus>

            <coordinates>
                <xsl:value-of select="//*[local-name() = 'Geometric_Info']/*[local-name() = 'Granule_Footprint']/*[local-name() = 'Granule_Footprint']/*[local-name() = 'Footprint']/*[local-name() = 'EXT_POS_LIST']"/>
            </coordinates>

            <operationalMode></operationalMode>

            <relativeOrbitNumber></relativeOrbitNumber>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-1A_DataStrip_ID']) != ''">
            <operationalMode>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'DATATAKE_TYPE']"/>
            </operationalMode>

            <relativeOrbitNumber>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'SENSING_ORBIT_NUMBER']"/>
            </relativeOrbitNumber>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-1A_Granule_ID']) != ''">
            <illuminationZenithAngle>
                <xsl:value-of select="//*[local-name() = 'Solar_Angles']/*[local-name() = 'ZENITH_ANGLE']"/>
            </illuminationZenithAngle>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-1B_DataStrip_ID']) != ''">
            <operationalMode>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'DATATAKE_TYPE']"/>
            </operationalMode>

            <relativeOrbitNumber>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'SENSING_ORBIT_NUMBER']"/>
            </relativeOrbitNumber>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-1B_Granule_ID']) != ''">
            <illuminationZenithAngle>
                <xsl:value-of select="//*[local-name() = 'Solar_Angles']/*[local-name() = 'ZENITH_ANGLE']"/>
            </illuminationZenithAngle>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-1C_DataStrip_ID']) != ''">
            <operationalMode>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'DATATAKE_TYPE']"/>
            </operationalMode>

            <relativeOrbitNumber>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'SENSING_ORBIT_NUMBER']"/>
            </relativeOrbitNumber>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-1C_Tile_ID']) != ''">
            <illuminationZenithAngle>
                <xsl:value-of select="//*[local-name() = 'Geometric_Info']/*[local-name() = 'Tile_Angles']/*[local-name() = 'Mean_Sun_Angle']/*[local-name() = 'ZENITH_ANGLE']"/>
            </illuminationZenithAngle>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-2A_DataStrip_ID']) != ''">
            <operationalMode>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'DATATAKE_TYPE']"/>
            </operationalMode>

            <relativeOrbitNumber>
                <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/*[local-name() = 'SENSING_ORBIT_NUMBER']"/>
            </relativeOrbitNumber>
        </xsl:if>

        <xsl:if test="string(//*[local-name() = 'Level-2A_Tile_ID']) != ''">
            <illuminationZenithAngle>
                <xsl:value-of select="//*[local-name() = 'Geometric_Info']/*[local-name() = 'Tile_Angles']/*[local-name() = 'Mean_Sun_Angle']/*[local-name() = 'ZENITH_ANGLE']"/>
            </illuminationZenithAngle>
        </xsl:if>

        <platformShortName>SENTINEL-2</platformShortName>

    </xsl:template>
</xsl:stylesheet>