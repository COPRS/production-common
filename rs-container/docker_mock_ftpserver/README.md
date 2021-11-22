Simple ftp server based on vsftpd.

# Usage
	$ docker run -d -v <host folder>:/data \
					-p 20:20 -p 21:21 -p 47400-47470:47400-47470 \
					-e FTP_USER=<username> \
					-e FTP_PASS=<password> \
					-e PASV_ADDRESS=<ip address of your server> \
					--name ftp \
					--restart=always ftpserver
