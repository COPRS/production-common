# RS Init

This script is a helper to bootstrap a Reference System environment in
an easy way. To set it up correctly, it is required to check out multiple
parts from different repositories. This script is handling this in an
automatic way. Execute it on the Kubernetes cluster that shall be used
in order to run the Reference system.

# Usage

## Introduction
The script rs_init will be creating two folders. The folder 'repos' does
contain the actual github repositories that had been checked out. Another
folder 'env' will be created containing the expected structure for the
environment.

A typical layout for an environment will be look like:
```
.
├── env -> /home/user/repos/rs-deploy/rs-deploy
├── repos
│   ├── conf
│   │   ├── env
│   │   ├── replicaCount.yaml
│   │   ├── values.yaml
│   │   └── wrapper
│   ├── processing-sentinel-1
│   │   ├── 41_l0_aio_production_trigger
│   │   ├── 42_l0_aio_ipf_preparation_worker
│   │   ├── 43_l0_asp_production_trigger
│   │   ├── 44_l0_asp_ipf_preparation_worker
│   │   ├── 52_l0_aio_ipf_execution_worker
│   │   ├── 54_l0_asp_ipf_execution_worker
│   │   └── README.md
│   ├── processing-sentinel-3
│   │   └── README.md
│   ├── rs-deploy
│   │   └── rs-deploy
│   ├── rs-processing-common
│   │   └── rs-processing-common
│   └── rs-testing
│       ├── 98_mock_webdav_cgs01
│       ├── 98_mock_webdav_s3
│       ├── 99_mock_edip_bedc
│       └── 99_mock_edip_pedc
├── rs_init
└── setup.conf
```
For further information on the content of the different directories, please consult the documentation
for the specific parts.

## Prepare

In order to use the script, it needs to be ensured that the script is
installed within the target cluster. This can be down e.g. by downloading
it using the following curl command
```
curl -s https://raw.githubusercontent.com/COPRS/production-common/develop/rs-init/rs_init --output rs_init && chmod 770 ./rs_init
```
The scripts need to be placed in the home directory top level. 

## Init

To init the environment please execute:
```
./rs_init init
```
The script will search for a file called "setup.conf" for giving some additional hints on the bootstrap process. 
If this fil cannot be found, it will automatically generate it.

This file will look like:
```
REPO_COMMON="https://github.com/COPRS/production-common.git"
REPO_S1="https://github.com/COPRS/processing-sentinel-1.git"
REPO_S3="https://github.com/COPRS/processing-sentinel-3.git"
REPO_CONF=""
BRANCH=""
```
Use your favorized text editor to give at least the information on:
* REPO_CONF containing the information where the environmental specific configuration is located.
* BRANCH specifying the branch or version that shall be used.

The other variables can be used as is and shall be normally not modified.

Once these information are specified, the script will be executed and starting to generate the environment.
The output will look like:
```
[user@host ~]# ./rs_init init
Init Reference System environment ...
Fetching repositories...
Checking out processing-sentinel-1
Checking out processing-sentinel-3
Checking out configuration
Create structure ...
Done.
```


## Update

Because the environment consist of multiple repository the script 'rs_init' can be also used to perform a git pull on
all the repositories to keep it up to date with the origin repository by executing the following command:
```
./rs_init update
```
An typical output of this process shall look like:
```
[user@host ~]# ./rs_init update
Already up to date.
Already up to date.
From https://github.com/COPRS/production-common
 * branch                develop    -> FETCH_HEAD
Already up to date.
Already up to date.
Done.
```

## Clean

Performing a clean operation using the script will remove the repos directory and the env structure from the system
again and can be used to purge the existing environment. Please be aware that executing this operation will remove the
directories and all modification you manually performed within them. So use this command with care!

```
./rs_init clean
```
A successful execution will have the following output:
```
[user@host ~]# ./rs_init clean
Cleaning environment ...
Done.
```
Please note that this will not remove the file "setup.conf".