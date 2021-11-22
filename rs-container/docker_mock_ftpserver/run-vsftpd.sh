#!/bin/bash

# create ftp root dir

export ftp_root_dir="/data"
mkdir -p ${ftp_root_dir}

# create ftp user

ftp_user_uid=1000
adduser --gecos "" --disabled-password --no-create-home --home "${ftp_root_dir}" --uid "${ftp_user_uid}" "${ftp_user}"
echo "${ftp_user}:${ftp_pass}" | chpasswd

# create directories
lisdir=$(echo ${ftp_dir} | tr "," "\n")
for fold in $lisdir
do
	mkdir -p "${ftp_root_dir}/$fold"
	chown "${ftp_user}:${ftp_user}" "${ftp_root_dir}/$fold"
done

# additional user related configuration

if [ -f /etc/mockftp/add-users.sh ]; then
    /etc/mockftp/add-users.sh 
fi

if [[ -z "${ftp_pasv_address}" ]] ; then
	echo "Mode actif"
else
	echo "pasv_address=${ftp_pasv_address}" >> /etc/vsftpd/vsftpd.conf
	echo "pasv_enable=YES" >> /etc/vsftpd/vsftpd.conf
	echo "pasv_addr_resolve=YES" >> /etc/vsftpd/vsftpd.conf
	echo "pasv_promiscuous=YES" >> /etc/vsftpd/vsftpd.conf
	echo "pasv_min_port=${ftp_pasv_min_port}" >> /etc/vsftpd/vsftpd.conf
	echo "pasv_max_port=${ftp_pasv_max_port}" >> /etc/vsftpd/vsftpd.conf
fi

# Run vsftpd:
vsftpd /etc/vsftpd/vsftpd.conf
