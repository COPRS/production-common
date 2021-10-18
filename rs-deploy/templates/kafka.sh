export KAFKA_NAMESPACE="infra";
export KAFKA_POD_PREFIX="kafka";
export KAFKA_POD=$(kubectl -n ${KAFKA_NAMESPACE} get pods | grep ${KAFKA_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
export KAFKA_CONTAINER="kafka";
export KAFKA_SCRIPT_TOPIC="/opt/kafka/bin/kafka-topics.sh";
export KAFKA_SCRIPT_CONFIG="/opt/kafka/bin/kafka-configs.sh";
export KAFKA_RUN_CLASS_SCRIPT_TOPIC="/opt/kafka/bin/kafka-run-class.sh";
export KAFKA_URL="kafka-headless.${KAFKA_NAMESPACE}.svc.cluster.local:9092";
export ZOOKEEPER_LIST="zookeeper-0.zookeeper-headless.${KAFKA_NAMESPACE}.svc.cluster.local:2181,zookeeper-1.zookeeper-headless.${KAFKA_NAMESPACE}.svc.cluster.local:2181,zookeeper-2.zookeeper-headless.${KAFKA_NAMESPACE}.svc.cluster.local:2181"
export KAFKA_TOPIC_ERROR="t-pdgs-errors";

# Set KAFKA_EXCEPTION if you want to select a subset of topics (regex) to be NOT handled by the cluster configuration script (eg. modules/kafka.sh configuration/configuration.sh init)
# Set to "nothing" if no exception required (empty string will fail)
export KAFKA_EXCEPTION="nothing"

# Set KAFKA_FILTER if you want to select only a subset of topics (regex) to be handled by the cluster configuration script (eg. modules/kafka.sh configuration/configuration.sh init)
# Example: export KAFKA_FILTER="t-pdgs-"
export KAFKA_FILTER="t-pdgs-"

TMP_TOPIC_LIST_DETAILS="
t-pdgs-ingestion-jobs-s1pro-ingestion-0                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-1                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-2                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-3                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-4                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-5                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-6                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-7                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-8                  2	259200000
t-pdgs-ingestion-jobs-s1pro-ingestion-9                  2	259200000
t-pdgs-ingestion-jobs-xbip-nominal                      20	259200000
t-pdgs-ingestion-jobs-xbip-retransfer                   20	259200000
t-pdgs-ingestion-jobs-auxip-nominal                     10	259200000
t-pdgs-ingestion-jobs-edip-pedc-nominal                  6	259200000
t-pdgs-ingestion-jobs-edip-pedc-retransfer               6	259200000
t-pdgs-ingestion-jobs-edip-bedc-nominal                  6	259200000
t-pdgs-ingestion-jobs-edip-bedc-retransfer               6	259200000
t-pdgs-contingency-ingestion-jobs                        2	259200000
t-pdgs-plan-and-report-ingestion-jobs                    2	259200000
t-pdgs-qcss-ingestion-jobs                               2	259200000
t-pdgs-qcss-ingestion-zip-jobs                           2      259200000
t-pdgs-aux-ingestion-events                              2	259200000
t-pdgs-session-file-ingestion-events                     2	259200000
t-pdgs-contingency-ingestion-events                      2  	259200000
t-pdgs-plan-and-report-ingestion-events                  2	259200000
t-pdgs-plan-and-report-zip-ingestion-events              2	259200000
t-pdgs-auxip-aux-ingestion-events                        2	259200000
t-pdgs-auxip-plan-and-report-ingestion-events            2	259200000
t-pdgs-metadata-extraction-jobs                         10	259200000
t-pdgs-session-file-catalog-events                       2	259200000
t-pdgs-l0-segment-catalog-events-fast                    2	259200000
t-pdgs-l0-segment-catalog-events-nrt                     2	259200000
t-pdgs-l0-segment-catalog-events-pt                      2	259200000
t-pdgs-l0-segment-catalog-events-operator-demand         2	259200000 
t-pdgs-l0-slice-catalog-events-fast                      2	259200000
t-pdgs-l0-slice-catalog-events-nrt                       2	259200000
t-pdgs-l0-slice-catalog-events-pt                        2	259200000
t-pdgs-l0-slice-catalog-events-operator-demand           2	259200000 
t-pdgs-plan-and-report-catalog-events                    2	259200000
t-pdgs-other-catalog-events                              2	259200000
t-pdgs-aio-preparation-jobs                              2	259200000
t-pdgs-aio-execution-jobs                                6	259200000
t-pdgs-aio-l0-segment-production-events                  2	259200000
t-pdgs-aio-l0-blank-production-events                    2	259200000
t-pdgs-l0asp-preparation-jobs-fast                       2	259200000
t-pdgs-l0asp-preparation-jobs-nrt                        2	259200000
t-pdgs-l0asp-preparation-jobs-pt                         2	259200000
t-pdgs-l0asp-preparation-jobs-operator-demand            2  	259200000
t-pdgs-l0asp-preparation-jobs-late                       2	259200000
t-pdgs-l0asp-execution-jobs-fast                         6	259200000
t-pdgs-l0asp-execution-jobs-nrt                          6	259200000
t-pdgs-l0asp-execution-jobs-pt                           6	259200000
t-pdgs-l0asp-execution-jobs-operator-demand              6  	259200000
t-pdgs-l0asp-execution-jobs-late                         6	259200000
t-pdgs-l0asp-l0-acn-production-events-fast               2 	259200000
t-pdgs-l0asp-l0-acn-production-events-nrt                2	259200000
t-pdgs-l0asp-l0-acn-production-events-pt                 2	259200000
t-pdgs-l0asp-l0-acn-production-events-operator-demand    2	259200000
t-pdgs-l0asp-l0-acn-production-events-late               2	259200000
t-pdgs-l0asp-l0-slice-production-events-fast             2	259200000
t-pdgs-l0asp-l0-slice-production-events-nrt              2	259200000
t-pdgs-l0asp-l0-slice-production-events-pt               2	259200000
t-pdgs-l0asp-l0-slice-production-events-operator-demand  2	259200000
t-pdgs-l0asp-l0-slice-production-events-late             2	259200000
t-pdgs-l1-preparation-jobs-fast                          2	259200000
t-pdgs-l1-preparation-jobs-nrt                           2	259200000
t-pdgs-l1-preparation-jobs-pt                            2	259200000
t-pdgs-l1-preparation-jobs-operator-demand               2  	259200000
t-pdgs-l1-preparation-jobs-late                          2	259200000
t-pdgs-l1-execution-jobs-fast                           50	259200000
t-pdgs-l1-execution-jobs-nrt                            50	259200000
t-pdgs-l1-execution-jobs-pt                             50	259200000
t-pdgs-l1-execution-jobs-operator-demand                50  	259200000
t-pdgs-l1-execution-jobs-late                           50	259200000
t-pdgs-l1-acn-production-events-fast                     2	259200000
t-pdgs-l1-acn-production-events-nrt                      2	259200000
t-pdgs-l1-acn-production-events-pt                       2	259200000
t-pdgs-l1-acn-production-events-operator-demand          2  	259200000
t-pdgs-l1-acn-production-events-late                     2	259200000
t-pdgs-l1-slices-production-events-fast                  2	259200000
t-pdgs-l1-slices-production-events-nrt                   2	259200000
t-pdgs-l1-slices-production-events-pt                    2	259200000
t-pdgs-l1-slices-production-events-operator-demand       2	259200000
t-pdgs-l1-slices-production-events-late                  2	259200000
t-pdgs-l2-preparation-jobs-fast                          2	259200000
t-pdgs-l2-preparation-jobs-nrt                           2	259200000
t-pdgs-l2-preparation-jobs-pt                            2	259200000
t-pdgs-l2-preparation-jobs-operator-demand               2 	259200000
t-pdgs-l2-preparation-jobs-late                          2	259200000
t-pdgs-l2-execution-jobs-fast                           20	259200000
t-pdgs-l2-execution-jobs-nrt                            20	259200000
t-pdgs-l2-execution-jobs-pt                             20	259200000
t-pdgs-l2-execution-jobs-operator-demand                20  	259200000
t-pdgs-l2-execution-jobs-late                           20	259200000
t-pdgs-l2-acn-production-events-fast                     2	259200000
t-pdgs-l2-acn-production-events-nrt                      2	259200000
t-pdgs-l2-acn-production-events-pt                       2	259200000
t-pdgs-l2-acn-production-events-operator-demand          2  	259200000
t-pdgs-l2-acn-production-events-late                     2	259200000
t-pdgs-l2-slices-production-events-fast                  2	259200000
t-pdgs-l2-slices-production-events-nrt                   2	259200000
t-pdgs-l2-slices-production-events-pt                    2	259200000
t-pdgs-l2-slices-production-events-operator-demand       2	259200000
t-pdgs-l2-slices-production-events-late                  2	259200000
t-pdgs-obs-preparation-jobs                              2	259200000
t-pdgs-obs-execution-jobs                                2  	259200000
t-pdgs-obs-production-events                             2	259200000
t-pdgs-compression-jobs-fast                            50	259200000
t-pdgs-compression-jobs-nrt                             50	259200000
t-pdgs-compression-jobs-pt                              50	259200000
t-pdgs-compression-jobs-operator-demand                 40	259200000
t-pdgs-compression-jobs-late                            40	259200000
t-pdgs-uncompression-jobs                               10  	259200000
t-pdgs-uncompression-jobs-pt                            10  	259200000
t-pdgs-uncompression-jobs-data-request                   2  	259200000
t-pdgs-compression-events                                2	259200000
t-pdgs-uncompression-events-data-request                 2	259200000
t-pdgs-publishing-jobs                                  10	259200000
t-pdgs-errors                                            2	259200000
t-pdgs-eviction-management-jobs                          2  259200000
t-pdgs-eviction-events                                   2  259200000
t-pdgs-data-request-jobs                                 2  259200000
t-pdgs-data-request-events                               2  259200000
t-pdgs-lta-download-events                               2  	259200000
t-pdgs-aio-production-report-events                      2	259200000
t-pdgs-l0asp-production-report-events                    2	259200000
t-pdgs-l1-production-report-events                       2	259200000
t-pdgs-l2-production-report-events                       2	259200000
t-pdgs-operator-demand-events                           10  	259200000
t-pdgs-myocean-jobs                                      2  	259200000
t-pdgs-mbu-preparation-jobs                              2	259200000
t-pdgs-mbu-execution-jobs                                2  	259200000
t-pdgs-mbu-production-events                             2	259200000
t-pdgs-mbu-dissemination-jobs                            2	259200000
";

# Delete empty lines and choose columns
export TOPIC_LIST_DETAILS=$( echo "${TMP_TOPIC_LIST_DETAILS}" | grep -E "${KAFKA_FILTER}" | grep -E -v "${KAFKA_EXCEPTION}" | grep -v -e '^$' | awk '{print $1":"$2":"$3}');
export TOPIC_LIST=$(         echo "${TMP_TOPIC_LIST_DETAILS}" | grep -E "${KAFKA_FILTER}" | grep -E -v "${KAFKA_EXCEPTION}" | grep -v -e '^$' | awk '{print $1}');
