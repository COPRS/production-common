<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

    <!--================================== local variables ========================================= -->
    <xsl:variable name="PRODUCT_NAME"
        select="//*[local-name()='General_Info']//*[local-name()='GRANULE_ID']/text()" />

    <xsl:variable name="datatakeIdentifier" select="//*[local-name() = 'General_Info']/*[local-name() = 'Datatake_Info']/@datatakeIdentifier"/>

    <xsl:variable name="qualityInfo" select="//*[local-name() = 'Image_Data_Info']/*[local-name() = 'Sensor_Configuration']/*[local-name() = 'Source_Packet_Description']/*[local-name() = 'Degradation_Summary']/*[local-name() = 'NUMBER_OF_LOST_PACKETS']"/>

    <!--======= Create a flat XML structure with the necessary information ======= -->
    <xsl:template match="/">

        <startTime>
            <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datastrip_Time_Info']/*[local-name() = 'DATASTRIP_SENSING_START']"/>
        </startTime>

        <stopTime>
            <xsl:value-of select="//*[local-name() = 'General_Info']/*[local-name() = 'Datastrip_Time_Info']/*[local-name() = 'DATASTRIP_SENSING_STOP']"/>
        </stopTime>

        <processorVersion></processorVersion>

        <processingCenter>RS</processingCenter>

        <orbitNumber>
            <xsl:value-of select="substring($datatakeIdentifier, 22, 6)"/>
        </orbitNumber>

        <productGroupId>
            <xsl:value-of select="$datatakeIdentifier"/>
        </productGroupId>

        <qualityInfo>
            <xsl:value-of select="$qualityInfo"/>
        </qualityInfo>

        <qualityStatus>
            <xsl:choose>
                <xsl:when test="$qualityInfo = 0">NOMINAL</xsl:when>
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
    </xsl:template>
</xsl:stylesheet>