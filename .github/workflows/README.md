# GitHub Workflows directory

This directory contains the definition files for the GitHub Action Workflows used in the production-common repository.

## Currently known limitations

The delivered IPFs for the S3 SM2 chains are currently unfortunately too big for the GitHub Action Pipeline.
In order to prevent false negatives in the build logs, the builds for these processors are currently not activated.

In case, that the IPFs are updated and the size is usable for GitHub the following actions have to be performed in order to activate the builds for these processors:

File: `build-ci.yaml`, Line: 300 et seq.

Move the lines

```
{ image: rs-ipf-s3-sm2-hy, dir: docker_s3_ipf_sm2_hy },
{ image: rs-ipf-s3-sm2-li, dir: docker_s3_ipf_sm2_li },
{ image: rs-ipf-s3-sm2-si, dir: docker_s3_ipf_sm2_si },
```

Into the array above.

File: `build-release.yaml`, Line: 220 et seq.

Move the lines

```
{ image: rs-ipf-s3-sm2-hy, dir: docker_s3_ipf_sm2_hy },
{ image: rs-ipf-s3-sm2-li, dir: docker_s3_ipf_sm2_li },
{ image: rs-ipf-s3-sm2-si, dir: docker_s3_ipf_sm2_si },
```

Into the array above.
