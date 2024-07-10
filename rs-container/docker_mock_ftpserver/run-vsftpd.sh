#!/bin/bash
# Copyright 2023 Airbus
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


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

echo "pasv_promiscuous=YES" >> /etc/vsftpd/vsftpd.conf

# Run vsftpd:
vsftpd /etc/vsftpd/vsftpd.conf
