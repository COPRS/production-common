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

FROM twizzel/webdav

RUN sed -i 's,Dav On,Dav On\n  DavDepthInfinity on,g' /usr/local/apache2/conf/conf-enabled/dav.conf 

COPY s3cmd*.zip /tmp
RUN apk add --no-cache python py-setuptools py-dateutil py-magic && \
    unzip /tmp/s3cmd*.zip -d /tmp && rm -f /tmp/s3cmd*.zip && cd /tmp/s3cmd* && python setup.py install && cd - && rm -rf /tmp/s3cmd*

