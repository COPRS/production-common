FROM ubuntu:latest

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
