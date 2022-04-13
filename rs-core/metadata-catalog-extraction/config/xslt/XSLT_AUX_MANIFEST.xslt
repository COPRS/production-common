<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="utf-8" indent="yes" />

    <xsl:template match="/">
    
    	<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'generation']/text() != ''">
    		<creationTime>
				<xsl:value-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'generation']/text()"/>
			</creationTime>
		</xsl:if>
    	<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'validity']/text() != ''">
    		<validityStartTime>
				<xsl:value-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'validity']/text()"/>
			</validityStartTime>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'instrumentConfigurationId']/text() != ''">
			<instrumentConfigurationId>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'instrumentConfigurationId']/text()"/>
			</instrumentConfigurationId>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/@site) != ''">
			<site>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'facility']/@site)"/>
			</site>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/@start) != ''">
			<processingDate>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/@start)"/>
			</processingDate>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/*[local-name() = 'software']/@name) != ''">
			<processorName>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'software']/@name)"/>
			</processorName>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'processing']/*[local-name() = 'software']/@version) != ''">
			<processorVersion>
				<xsl:copy-of select="string(//*[local-name() = 'processing']/*[local-name() = 'software']/@version)"/>
			</processorVersion>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'platform']/*[local-name() = 'familyName']/text() != ''">
			<platformShortName>
				<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'familyName']/text()"/>
			</platformShortName>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'platform']/*[local-name() = 'number']/text() != ''">
			<platformSerialIdentifier>
				<xsl:copy-of select="//*[local-name() = 'platform']/*[local-name() = 'number']/text()"/>
			</platformSerialIdentifier>
		</xsl:if>
		<xsl:if test="string(//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'familyName']/@abbreviation) != ''">
			<instrumentShortName>
				<xsl:copy-of select="string(//*[local-name() = 'platform']/*[local-name() = 'instrument']/*[local-name() = 'familyName']/@abbreviation)"/>
			</instrumentShortName>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'generation']/text() != ''">
			<productGeneration>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'generation']/text()"/>
			</productGeneration>
		</xsl:if>
		<xsl:if test="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'auxProductType']/text() != ''">
			<productType>
				<xsl:copy-of select="//*[local-name() = 'standAloneProductInformation']/*[local-name() = 'auxProductType']/text()"/>
			</productType>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>