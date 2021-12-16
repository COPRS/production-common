#!/bin/bash

mkdir -p /data/NOMINAL
chown -R s1pdgs:s1pdgs /data

# reset vsftpd parameter force_local_logins_ssl to YES (default value)
sed -i s/^\\s*force_local_logins_ssl\\s*=\\s*NO// /etc/vsftpd/vsftpd.conf

#### reset vsftpd parameter force_local_data_ssl to YES (default value)
### sed -i s/^\\s*force_local_data_ssl\\s*=\\s*NO// /etc/vsftpd/vsftpd.conf
