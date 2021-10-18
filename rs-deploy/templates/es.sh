export ES_NAMESPACE="monitoring";
export ES_SVC="elasticsearch-trace-elasticsearch-coordinating-only.${ES_NAMESPACE}.svc.cluster.local";
export ES_PORT="9200";
export ES_POD="elasticsearch-trace-elasticsearch-master-0";
export ES_CLUSTER_NAME="elasticsearch-trace";

# Set ES_EXCEPTION if you want to select a subset of indices (regex) to be NOT handled by the cluster configuration script (eg. modules/es.sh configuration/configuration.sh init)
# Set to "nothing" if no exception required (empty string will fail)
# Example: export ES_EXCEPTION="session|raw|aux|mpl|msk|landmask"
export ES_EXCEPTION="nothing"

# Set ES_FILTER if you want to select only a subset of indices (regex) to be handled by the cluster configuration script (eg. modules/es.sh configuration/configuration.sh init)
# Example: export ES_FILTER="session|raw|aux|mpl|msk|mask|prip|lifecycle|plan_and_report|spp|l0|l1|l2"
export ES_FILTER=""

TMP_ES_INDICES_INPUT="
mpl_orbpre
mpl_orbres
mpl_orbsct
aux_obmemc
aux_poeorb
aux_preorb
aux_resorb
aux_cal
aux_pp1
aux_pp2
aux_ins
aux_wnd
aux_ice
aux_wav
raw
session
msk_ew_slc
msk__land_
msk_ocean_
msk_ovrpas
aux_ece
aux_scs
plan_and_report
spp_mbu
spp_obs
";

TMP_ES_INDICES_TMP="
aux_att
";

TMP_ES_INDICES_SEGMENTS="
l0_segment
";

TMP_ES_INDICES_PRODUCTS="
l0_slice
l0_acn
l1_slice
l1_acn
l2_slice
l2_acn
";

TMP_ES_INDICES_EWSLCMASK="
ewslcmask
";

TMP_ES_INDICES_LANDMASK="
landmask
";

TMP_ES_INDICES_OCEANMASK="
oceanmask
";

TMP_ES_INDICES_OVERPASSMASK="
overpassmask
";

TMP_ES_INDICES_PRIP="
prip
";

TMP_ES_INDICES_DATA_LIFECYCLE_METADATA="
data-lifecycle-metadata
";

TMP_ES_ALL="
${TMP_ES_INDICES_INPUT}
${TMP_ES_INDICES_TMP}
${TMP_ES_INDICES_SEGMENTS}
${TMP_ES_INDICES_PRODUCTS}
${TMP_ES_INDICES_EWSLCMASK}
${TMP_ES_INDICES_LANDMASK}
${TMP_ES_INDICES_OCEANMASK}
${TMP_ES_INDICES_OVERPASSMASK}
${TMP_ES_INDICES_PRIP}
${TMP_ES_INDICES_DATA_LIFECYCLE_METADATA}
";

# Delete empty lines
export ES_INDICES_INPUT=$(                   echo "${TMP_ES_INDICES_INPUT}"                   | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_TMP=$(                     echo "${TMP_ES_INDICES_TMP}"                     | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_SEGMENTS=$(                echo "${TMP_ES_INDICES_SEGMENTS}"                | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_PRODUCTS=$(                echo "${TMP_ES_INDICES_PRODUCTS}"                | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_EWSLCMASK=$(               echo "${TMP_ES_INDICES_EWSLCMASK}"               | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_LANDMASK=$(                echo "${TMP_ES_INDICES_LANDMASK}"                | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_OCEANMASK=$(               echo "${TMP_ES_INDICES_OCEANMASK}"               | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_OVERPASSMASK=$(            echo "${TMP_ES_INDICES_OVERPASSMASK}"            | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_PRIP=$(                    echo "${TMP_ES_INDICES_PRIP}"                    | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_INDICES_DATA_LIFECYCLE_METADATA=$( echo "${TMP_ES_INDICES_DATA_LIFECYCLE_METADATA}" | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
export ES_ALL=$(                             echo "${TMP_ES_ALL}"                             | grep -E "${ES_FILTER}" | grep -E -v "${ES_EXCEPTION}" | grep -v -e '^$');
