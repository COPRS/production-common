S1-PDGS Ingestor
================
The ingestor ingests file from a FTP server to a local directory.
Then it processes files of its local directory:
- Check if the file/folder should be ignored
- Put the file in object storage
- Publish file/folder metadata
- Publish SESSION file

#### Files arborescence

##### In FTP server

	config_files/
		S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml
		S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF
		S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/
			data/
				s1a-aux-cal.xml
			support/
				s1-aux-cal.xsd
				s1-object-types.xsd
			manifest.safe

	erds_sessions/
		S1A/
			L20171109175634707000125/
				ch01/
					DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw
					DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw_iif.xml
					DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw
					DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw_iif.xml
					DCS_02_L20171109175634707000125_ch1_DSIB.xml
					DCS_02_L20171109175634707000125_ch1_DSIB.xml_iif.xml
				ch02/
					DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw
					DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw_iif.xml
					DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw
					DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw_iif.xml
					DCS_02_L20171109175634707000125_ch2_DSIB.xml
					DCS_02_L20171109175634707000125_ch2_DSIB.xml_iif.xml
				
		/S1B
			/L20171109175634707000125

##### In object storage

Bucket "config_files"

	S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml
	S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF
	S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/data/s1a-aux-cal.xml
	S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/support/s1-aux-cal.xsd
	S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/support/s1-object-types.xsd
	S1A_AUX_CAL_V20171017T080000_G20171013T101200.SAFE/manifest.safe

Bucket "session_files"

	L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw
	L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw
	L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSIB.xml
	L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw
	L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw
	L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSIB.xml
	
#### Configuration

Below the parameters to configure for the production

	kafka.bootstrap-servers: the bootstrap servers for KAFKA (example: kafka-svc:9092)
	kafka.topic.metadata: the name of the topic used for metadata (example: t_metadata)
	kafka.topic.sessions: the name of the topic used for metadata (example: t)
	
	ftp.host: FTP server hostname (example: input-ftpserver-svc)
	ftp.username: Username to access to the FTP
	ftp.password: Password to access to the FTP
	ftp.config-files.remote-directory: the remote directory where the configuration files shall be uploaded (example: config-files)
	ftp.config-files.upload-fixed-rate: The fixed rate in milliseconds to retrieve by FTP configuration files (example: 1000)
	ftp.session-files.remote-directory: the remote directory where the session files shall be uploaded (example: erds-sessions)
	ftp.session-files.upload-fixed-rate: The fixed rate in milliseconds to retrieve by FTP configuration files (example: 1000)
	
	storage.user.id: access key
	storage.user.secret: access secret
	storage.endpoint: object storage endpoint (example: http://oss.eu-west-0.prod-cloud-ocb.orange-business.com/)
	storage.region: object storage region (example: eu-west-0)
	storage.buckets.config-files: bucket name used for configuration files (example: config-files)
	storage.buckets.session-files: bucket name used for session files (example: session-files)
	
	file.config-files.read-fixed-rate: The fixed rate in milliseconds to read configuration files on local directory (example: 1000)
	file.config-files.read-fixed-rate: The fixed rate in milliseconds to read session files on local directory (example: 1000)

#### Logs
