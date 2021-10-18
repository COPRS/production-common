HELM_CHART_VERSION="3.33.0-SNAPSHOT";

HELM_LIST_TMP="
00  YES s1pro-s1pdgs-base                    ${HELM_CHART_VERSION}   helm/00_s1pdgs_base/values.yaml
30  YES s1pro-app-catalog                    ${HELM_CHART_VERSION}   helm/30_app_catalog/values.yaml
31  YES s1pro-metadata-catalog-trigger       ${HELM_CHART_VERSION}   helm/31_metadata_catalog_trigger/values.yaml
32  YES s1pro-metadata-catalog-worker        ${HELM_CHART_VERSION}   helm/32_metadata_catalog_worker/values.yaml
33  YES s1pro-disseminator-fos               ${HELM_CHART_VERSION}   helm/33_disseminator_fos/values.yaml
33  YES s1pro-disseminator-pod               ${HELM_CHART_VERSION}   helm/33_disseminator_pod/values.yaml
34  YES s1pro-disseminator-mp                ${HELM_CHART_VERSION}   helm/34_disseminator_mp/values.yaml
34  YES s1pro-disseminator-unav              ${HELM_CHART_VERSION}   helm/34_disseminator_unav/values.yaml
36  YES s1pro-disseminator-errmat            ${HELM_CHART_VERSION}   helm/36_disseminator_errmat/values.yaml
41  YES s1pro-l0-aio-production-trigger      ${HELM_CHART_VERSION}   helm/41_l0_aio_production_trigger/values.yaml
42  YES s1pro-l0-aio-ipf-preparation-worker  ${HELM_CHART_VERSION}   helm/42_l0_aio_ipf_preparation_worker/values.yaml
43  YES s1pro-l0-asp-production-trigger      ${HELM_CHART_VERSION}   helm/43_l0_asp_production_trigger/values.yaml
44  YES s1pro-l0-asp-ipf-preparation-worker  ${HELM_CHART_VERSION}   helm/44_l0_asp_ipf_preparation_worker/values.yaml
45  YES s1pro-l1-production-trigger          ${HELM_CHART_VERSION}   helm/45_l1_production_trigger/values.yaml
46  YES s1pro-l1-ipf-preparation-worker      ${HELM_CHART_VERSION}   helm/46_l1_ipf_preparation_worker/values.yaml
47  YES s1pro-l2-production-trigger          ${HELM_CHART_VERSION}   helm/47_l2_production_trigger/values.yaml
48  YES s1pro-l2-ipf-preparation-worker      ${HELM_CHART_VERSION}   helm/48_l2_ipf_preparation_worker/values.yaml
52  YES s1pro-l0-aio-ipf-execution-worker    ${HELM_CHART_VERSION}   helm/52_l0_aio_ipf_execution_worker/values.yaml
54  YES s1pro-l0-asp-ipf-execution-worker    ${HELM_CHART_VERSION}   helm/54_l0_asp_ipf_execution_worker/values.yaml
56  YES s1pro-l1-ipf-execution-worker        ${HELM_CHART_VERSION}   helm/56_l1_ipf_execution_worker/values.yaml
58  YES s1pro-l2-ipf-execution-worker        ${HELM_CHART_VERSION}   helm/58_l2_ipf_execution_worker/values.yaml
59  YES s1pro-obs-production-trigger         ${HELM_CHART_VERSION}   helm/59_obs_production_trigger/values.yaml
60  YES s1pro-obs-ipf-preparation-worker     ${HELM_CHART_VERSION}   helm/60_obs_ipf_preparation_worker/values.yaml
61  YES s1pro-obs-ipf-execution-worker       ${HELM_CHART_VERSION}   helm/61_obs_ipf_execution_worker/values.yaml
62  YES s1pro-contingency-ingestion          ${HELM_CHART_VERSION}   helm/62_contingency_ingestion/values.yaml
63  YES s1pro-ingestion                      ${HELM_CHART_VERSION}   helm/63_ingestion/values.yaml
64  YES s1pro-ingestion-xbip-cgs01-trigger   ${HELM_CHART_VERSION}   helm/64_ingestion_xbip_cgs01_trigger/values.yaml
64  YES s1pro-ingestion-xbip-cgs02-trigger   ${HELM_CHART_VERSION}   helm/64_ingestion_xbip_cgs02_trigger/values.yaml
64  YES s1pro-ingestion-xbip-cgs03-trigger   ${HELM_CHART_VERSION}   helm/64_ingestion_xbip_cgs03_trigger/values.yaml
64  YES s1pro-ingestion-xbip-cgs04-trigger   ${HELM_CHART_VERSION}   helm/64_ingestion_xbip_cgs04_trigger/values.yaml
64  NO  s1pro-ingestion-xbip-cgs05-trigger   ${HELM_CHART_VERSION}   helm/64_ingestion_xbip_cgs05_trigger/values.yaml
64  YES s1pro-ingestion-xbip-cgs10-trigger   ${HELM_CHART_VERSION}   helm/64_ingestion_xbip_cgs10_trigger/values.yaml
65  YES s1pro-ingestion-xbip-worker          ${HELM_CHART_VERSION}   helm/65_ingestion_xbip_worker/values.yaml
66  YES s1pro-ingestion-auxip-trigger        ${HELM_CHART_VERSION}   helm/66_ingestion_auxip_trigger/values.yaml
67  YES s1pro-ingestion-auxip-worker         ${HELM_CHART_VERSION}   helm/67_ingestion_auxip_worker/values.yaml
68  YES s1pro-plan-and-report-ingestion      ${HELM_CHART_VERSION}   helm/68_plan_and_report_ingestion/values.yaml
70  YES s1pro-data-request-worker            ${HELM_CHART_VERSION}   helm/70_data_request_worker/values.yaml
70  YES s1pro-datalifecycle-trigger          ${HELM_CHART_VERSION}   helm/70_datalifecycle_trigger/values.yaml
70  YES s1pro-eviction-management-worker     ${HELM_CHART_VERSION}   helm/70_eviction_management_worker/values.yaml
74  YES s1pro-compression-trigger            ${HELM_CHART_VERSION}   helm/74_compression_trigger/values.yaml
75  YES s1pro-compression-worker             ${HELM_CHART_VERSION}   helm/75_compression_worker/values.yaml
76  YES s1pro-prip-trigger                   ${HELM_CHART_VERSION}   helm/76_prip_trigger/values.yaml
77  YES s1pro-prip-worker                    ${HELM_CHART_VERSION}   helm/77_prip_worker/values.yaml
78  YES s1pro-prip-frontend                  ${HELM_CHART_VERSION}   helm/78_prip_frontend/values.yaml
80  YES s1pro-myocean-trigger                ${HELM_CHART_VERSION}   helm/80_myocean_trigger/values.yaml
81  YES s1pro-myocean-worker                 ${HELM_CHART_VERSION}   helm/81_myocean_worker/values.yaml
82  NO  s1pro-myocean-cleaner                ${HELM_CHART_VERSION}   helm/82_myocean_cleaner/values.yaml
83  NO  s1pro-report                         ${HELM_CHART_VERSION}   helm/83_report/values.yaml
83  YES s1pro-ingestion-edip-pedc-trigger    ${HELM_CHART_VERSION}   helm/84_ingestion_edip_pedc_trigger/values.yaml
85  YES s1pro-ingestion-edip-pedc-worker     ${HELM_CHART_VERSION}   helm/85_ingestion_edip_pedc_worker/values.yaml
84  YES s1pro-ingestion-edip-bedc-trigger    ${HELM_CHART_VERSION}   helm/84_ingestion_edip_bedc_trigger/values.yaml
85  YES s1pro-ingestion-edip-bedc-worker     ${HELM_CHART_VERSION}   helm/85_ingestion_edip_bedc_worker/values.yaml
86  YES s1pro-ingestion-edip-worker          ${HELM_CHART_VERSION}   helm/86_ingestion_edip_worker/values.yaml
90  YES s1pro-on-demand-interface-provider   ${HELM_CHART_VERSION}   helm/90_on_demand_interface_provider/values.yaml
91  YES s1pro-request-repository             ${HELM_CHART_VERSION}   helm/91_request_repository/values.yaml
92  YES s1pro-queue-watcher                  ${HELM_CHART_VERSION}   helm/92_queue_watcher/values.yaml
93  YES s1pro-validation                     ${HELM_CHART_VERSION}   helm/93_validation/values.yaml
96  YES s1pro-qcss-online-disseminator       ${HELM_CHART_VERSION}   helm/96_qcss_online_disseminator/values.yaml
96  YES s1pro-qcss-online-disseminator-geo   ${HELM_CHART_VERSION}   helm/96_qcss_online_disseminator_geo/values.yaml
96  YES s1pro-qcss-online-ingestion-trigger  ${HELM_CHART_VERSION}   helm/96_qcss_online_ingestion_trigger/values.yaml
96  YES s1pro-qcss-online-ingestion-worker   ${HELM_CHART_VERSION}   helm/96_qcss_online_ingestion_worker/values.yaml
96  YES s1pro-qcss-webserver-disseminator    ${HELM_CHART_VERSION}   helm/96_qcss_webserver_disseminator/values.yaml
97  NO  s1pro-prometheus-adapter             ${HELM_CHART_VERSION}   helm/97_prometheus_adapter/values.yaml
98  NO  s1pro-mock-webdav-cgs01              ${HELM_CHART_VERSION}   helm/98_mock_webdav_cgs01/values.yaml
98  NO  s1pro-mock-webdav-cgs02              ${HELM_CHART_VERSION}   helm/98_mock_webdav_cgs02/values.yaml
98  NO  s1pro-mock-webdav-cgs03              ${HELM_CHART_VERSION}   helm/98_mock_webdav_cgs03/values.yaml
98  NO  s1pro-mock-webdav-cgs04              ${HELM_CHART_VERSION}   helm/98_mock_webdav_cgs04/values.yaml
98  NO  s1pro-mock-webdav-cgs05              ${HELM_CHART_VERSION}   helm/98_mock_webdav_cgs05/values.yaml
98  NO  s1pro-mock-webdav-cgs10              ${HELM_CHART_VERSION}   helm/98_mock_webdav_cgs10/values.yaml
99  YES s1pro-mock-dissemination             ${HELM_CHART_VERSION}   helm/99_mock_dissemination/values.yaml
"

export HELM_CONF_GLOBAL="$(dirname ${BASH_SOURCE})/values.yaml";
export HELM_CONF_REPLICAS="$(dirname ${BASH_SOURCE})/replicaCount.yaml";

export HELM_LIST=$(echo "${HELM_LIST_TMP}" | grep -v -e '^$');
