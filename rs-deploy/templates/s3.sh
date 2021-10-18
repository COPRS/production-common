export S3_ENDPOINT="http://oss.eu-west-0.prod-cloud-ocb.orange-business.com";
export S3_REGION="eu-west-0";
export S3_DISABLE_CHUNKED_ENCODING="false"
export S3_CONFIGURATION_FILE_PATH="/share/.s3cfg"

export S3_CLEAN_HARD="rb --force --recursive";
export S3_CLEAN_SOFT="rm -r --force --recursive";
export S3_CLEAN="${S3_CLEAN_HARD}";

export S3_PREFIX="ops-c1"; # ex: "ivv-c2"
export S3_SUFFIX_ZIP="zip";
export S3_EXCEPTION_CLEAN="nothing"; # Example: "session|auxiliary". Set to "nothing" if no exception required (empty string will fail)
TMP_S3_EXCEPTION_RESET=""; 
# Example: "bucket_name:filter1|filter2|...". 1 bucket per line. No line if no filter.
#          "${S3_PREFIX}-auxiliary-files:auxiliary-files
#           ${S3_PREFIX}-auxiliary-files-zip:auxiliary-files
#           ${S3_PREFIX}-session-files:raw"

export S3_BUCKET_AUX="${S3_PREFIX}-auxiliary-files";
export S3_BUCKET_SESSIONS="${S3_PREFIX}-session-files";

export S3_BUCKET_L0_SEGMENTS="${S3_PREFIX}-l0-segments";
export S3_BUCKET_L0_SLICES="${S3_PREFIX}-l0-slices";
export S3_BUCKET_L0_ACNS="${S3_PREFIX}-l0-acns";

export S3_BUCKET_L1_SLICES="${S3_PREFIX}-l1-slices";
export S3_BUCKET_L1_ACNS="${S3_PREFIX}-l1-acns";
export S3_BUCKET_L2_SLICES="${S3_PREFIX}-l2-slices";
export S3_BUCKET_L2_ACNS="${S3_PREFIX}-l2-acns";

export S3_BUCKET_L0_BLANKS="${S3_PREFIX}-l0-blanks";
export S3_BUCKET_SPP="${S3_PREFIX}-spp";
export S3_BUCKET_SPP_MBU="${S3_PREFIX}-spp-mbu";
export S3_BUCKET_INVALID="${S3_PREFIX}-invalid";
export S3_BUCKET_GHOST="${S3_PREFIX}-ghost";
export S3_BUCKET_DEBUG="${S3_PREFIX}-debug";
export S3_BUCKET_FAILED_WORKDIR="${S3_PREFIX}-failed-workdir";
export S3_BUCKET_SESSION_RETRANSFER="${S3_PREFIX}-session-retransfer";
export S3_BUCKET_PLANS_AND_REPORTS="${S3_PREFIX}-plans-and-reports";

export S3_BUCKET_ZIP_AUX="${S3_BUCKET_AUX}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L0_SEGMENTS="${S3_BUCKET_L0_SEGMENTS}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L0_SLICES="${S3_BUCKET_L0_SLICES}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L0_ACNS="${S3_BUCKET_L0_ACNS}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L1_SLICES="${S3_BUCKET_L1_SLICES}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L1_ACNS="${S3_BUCKET_L1_ACNS}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L2_SLICES="${S3_BUCKET_L2_SLICES}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L2_ACNS="${S3_BUCKET_L2_ACNS}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_L0_BLANKS="${S3_BUCKET_L0_BLANKS}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_SPP="${S3_BUCKET_SPP}-${S3_SUFFIX_ZIP}";
export S3_BUCKET_ZIP_PLANS_AND_REPORTS="${S3_BUCKET_PLANS_AND_REPORTS}-${S3_SUFFIX_ZIP}";

TMP_BUCKETS_INPUT="
${S3_BUCKET_AUX}
${S3_BUCKET_SESSIONS}
${S3_BUCKET_PLANS_AND_REPORTS}
${S3_BUCKET_SESSION_RETRANSFER}
";

TMP_BUCKETS_PRODUCED="
${S3_BUCKET_L0_SEGMENTS}
${S3_BUCKET_L0_SLICES}
${S3_BUCKET_L0_ACNS}

${S3_BUCKET_L1_SLICES}
${S3_BUCKET_L1_ACNS}
${S3_BUCKET_L2_SLICES}
${S3_BUCKET_L2_ACNS}

${S3_BUCKET_L0_BLANKS}
${S3_BUCKET_SPP}
${S3_BUCKET_SPP_MBU}
${S3_BUCKET_INVALID}
${S3_BUCKET_GHOST}

${S3_BUCKET_DEBUG}
${S3_BUCKET_FAILED_WORKDIR}
";

TMP_BUCKETS_COMPRESSED="
${S3_BUCKET_ZIP_AUX}
${S3_BUCKET_ZIP_L0_SEGMENTS}
${S3_BUCKET_ZIP_L0_SLICES}
${S3_BUCKET_ZIP_L0_ACNS}
${S3_BUCKET_ZIP_L1_SLICES}
${S3_BUCKET_ZIP_L1_ACNS}
${S3_BUCKET_ZIP_L2_SLICES}
${S3_BUCKET_ZIP_L2_ACNS}
${S3_BUCKET_ZIP_L0_BLANKS}
${S3_BUCKET_ZIP_SPP}
${S3_BUCKET_ZIP_PLANS_AND_REPORTS}
";

TMP_BUCKETS_ALL="
${TMP_BUCKETS_INPUT}
${TMP_BUCKETS_PRODUCED}
${TMP_BUCKETS_COMPRESSED}
";

# Delete empty lines
export BUCKETS_INPUT=$(     echo "${TMP_BUCKETS_INPUT}"      | grep -v -e '^$');
export BUCKETS_PRODUCED=$(  echo "${TMP_BUCKETS_PRODUCED}"   | grep -v -e '^$');
export BUCKETS_COMPRESSED=$(echo "${TMP_BUCKETS_COMPRESSED}" | grep -v -e '^$');
export BUCKETS_ALL=$(       echo "${TMP_BUCKETS_ALL}"        | grep -v -e '^$');
export S3_EXCEPTION_RESET=$(echo "${TMP_S3_EXCEPTION_RESET}" | grep -v -e '^$');
