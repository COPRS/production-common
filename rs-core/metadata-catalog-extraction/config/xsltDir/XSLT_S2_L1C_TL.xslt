<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

    <!--================================== local variables ========================================= -->
    <xsl:variable name="PRODUCT_NAME" select="//*[local-name()='General_Info']//*[local-name()='GRANULE_ID']/text()" />

    <xsl:variable name="qualityInfo" select="//*[local-name() = 'Quality_Indicators_Info']/*[local-name() = 'Image_Content_QI']/*[local-name() = 'Common_IMG_QI']/*[local-name() = 'DEGRADED_MSI_DATA_PERCENTAGE']"/>

    <!--======= Create a flat XML structure with the necessary information ======= -->
    <xsl:template match="/">

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
            <xsl:value-of select="$qualityInfo"/>
        </qualityInfo>

        <qualityStatus>
            <xsl:choose>
                <xsl:when test="$qualityInfo = 0">NOMINAL</xsl:when>
                <xsl:otherwise>DEGRADED</xsl:otherwise>
            </xsl:choose>
        </qualityStatus>

        <coordinates></coordinates>

        <platformShortName>SENTINEL-2</platformShortName>

        <operationalMode></operationalMode>

        <relativeOrbitNumber></relativeOrbitNumber>
    </xsl:template>
</xsl:stylesheet>