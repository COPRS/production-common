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

ARG BRANCH
FROM artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-base:${BRANCH}

# Use user root to use apt-get
USER root
RUN apt-get update && \
    apt-get install xsltproc rpm wget patch -y 

RUN mkdir -p /rpms && mkdir -p /data/localWD
COPY S1PD-DYN-IPF-SIM-*.rpm /rpms/
RUN rpm --nodeps -ivh /rpms/S1PD-DYN-IPF-SIM-*.rpm
RUN rm -rf /rpms/

RUN mkdir -p /usr/local/COTS/pic/java1.8/bin && ln -s /usr/lib/jvm/java-11-openjdk-amd64/bin/java /usr/local/COTS/pic/java1.8/bin/java

# overwrite createSoftLinksForTaskTable.sh from S1PD-DYN-IPF-SIM-*.rpm with a modified version
COPY createSoftLinksForTaskTable.sh /usr/local/components/IPFSimulator/etc/createSoftLinksForTaskTable.sh

# copy task tables
RUN mkdir -p /usr/local/conf/integ/AIO/TaskTables /usr/local/conf/integ/L0ASP/TaskTables /usr/local/conf/integ/S1IPF/TaskTables /usr/local/conf/integ/MBU/TaskTables
COPY tasktables/aio/* /usr/local/conf/integ/AIO/TaskTables/
COPY tasktables/asp/* /usr/local/conf/integ/L0ASP/TaskTables/
COPY tasktables/l1/* /usr/local/conf/integ/S1IPF/TaskTables/
COPY tasktables/l2/* /usr/local/conf/integ/S1IPF/TaskTables/
COPY tasktables/mbu/* /usr/local/conf/integ/MBU/TaskTables/

# create soft links for task table
RUN for f in /usr/local/conf/integ/AIO/TaskTables/*; do /usr/local/components/IPFSimulator/etc/createSoftLinksForTaskTable.sh $f; done
RUN for f in /usr/local/conf/integ/L0ASP/TaskTables/*; do /usr/local/components/IPFSimulator/etc/createSoftLinksForTaskTable.sh $f; done
RUN for f in /usr/local/conf/integ/S1IPF/TaskTables/*; do /usr/local/components/IPFSimulator/etc/createSoftLinksForTaskTable.sh $f; done
RUN for f in /usr/local/conf/integ/MBU/TaskTables/*; do /usr/local/components/IPFSimulator/etc/createSoftLinksForTaskTable.sh $f; done

# remove stats from jobOrder
COPY removeStats.patch /usr/local/components/IPFSimulator/etc/
COPY removeStats.xsl /usr/local/components/IPFSimulator/etc/
RUN cp /usr/local/components/IPFSimulator/bin/first.sh /usr/local/components/IPFSimulator/bin/first.sh.bak
RUN patch /usr/local/components/IPFSimulator/bin/first.sh /usr/local/components/IPFSimulator/etc/removeStats.patch && chmod +x /usr/local/components/IPFSimulator/bin/first.sh

# bypass OQC
RUN mkdir -p /app/amalfi/bin /usr/local/components/amalfi/bin /usr/local/components/OQCSimulator
COPY oqc /usr/local/components/OQCSimulator
RUN ln -s /usr/local/components/OQCSimulator/bin/oqcSimulator.sh /app/amalfi/bin/IpfJobSubmitter.sh
RUN ln -s /usr/local/components/OQCSimulator/bin/oqcSimulator.sh /usr/local/components/amalfi/bin/IpfJobSubmitter.sh

USER rsuser
WORKDIR /data/localWD/
ENTRYPOINT ["/bin/bash"]
