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

FROM ubuntu:focal-20211006

COPY s3cmd*.zip /tmp
RUN apt-get update && \
		apt-get install -y --no-install-recommends vsftpd db-util cron vim-tiny curl lftp python python-setuptools python-dateutil python-magic unzip && \
		apt-get clean && \
		unzip /tmp/s3cmd*.zip -d /tmp && rm -f /tmp/s3cmd*.zip && cd /tmp/s3cmd* && python setup.py install && cd - && rm -rf /tmp/s3cmd*

COPY vsftpd.conf /etc/vsftpd/
COPY run-vsftpd.sh /usr/sbin/

RUN chmod +x /usr/sbin/run-vsftpd.sh && \
		mkdir -p /var/run/vsftpd/empty

VOLUME /data
VOLUME /var/log/vsftpd

EXPOSE 20 21

CMD /usr/sbin/cron && /usr/sbin/run-vsftpd.sh
