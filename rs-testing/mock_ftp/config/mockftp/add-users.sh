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


mkdir -p /data/NOMINAL
chown -R s1pdgs:s1pdgs /data

# reset vsftpd parameter force_local_logins_ssl to YES (default value)
sed -i s/^\\s*force_local_logins_ssl\\s*=\\s*NO// /etc/vsftpd/vsftpd.conf

#### reset vsftpd parameter force_local_data_ssl to YES (default value)
### sed -i s/^\\s*force_local_data_ssl\\s*=\\s*NO// /etc/vsftpd/vsftpd.conf
