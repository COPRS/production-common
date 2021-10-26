FROM twizzel/webdav

RUN sed -i 's,Dav On,Dav On\n  DavDepthInfinity on,g' /usr/local/apache2/conf/conf-enabled/dav.conf 

COPY s3cmd*.zip /tmp
RUN apk add --no-cache python py-setuptools py-dateutil py-magic && \
    unzip /tmp/s3cmd*.zip -d /tmp && rm -f /tmp/s3cmd*.zip && cd /tmp/s3cmd* && python setup.py install && cd - && rm -rf /tmp/s3cmd*

