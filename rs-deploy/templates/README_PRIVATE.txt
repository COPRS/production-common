############################
## PRIVATE DATA 
############################
cat > private << \EOF
export DOCKER_REGISTRY_PASS=XXXXXXX
export HELM_REPO_PASS=XXXXXXX

export MONGODB_PASS=XXXXXXX

export AUXIP_PASS=XXXXXXX
export AUXIP_OAUTHCLIENTID=XXXXXXX
export AUXIP_OAUTHCLIENTSECRET=XXXXXXX

export EDIP_PEDC_PASS=XXXXXXX
export EDIP_BEDC_PASS=XXXXXXX

export XBIP_01_PASS=XXXXXXX
export XBIP_02_PASS=XXXXXXX
export XBIP_03_PASS=XXXXXXX
export XBIP_04_PASS=XXXXXXX
export XBIP_05_PASS=XXXXXXX
export XBIP_10_PASS=XXXXXXX

export AMALFI_DB_PASS=XXXXXXX
EOF

############################
## ENCRYPTION / DECRYPTION
############################
vim ~/gpg_pass_werum
gpg --batch --passphrase-file ~/gpg_pass_werum --symmetric --cipher-algo AES256 -o private.gpg -c private.txt
gpg --batch --passphrase-file ~/gpg_pass_werum -d private.gpg

vim private.txt && gpg --batch --passphrase-file ~/gpg_pass_werum --symmetric --cipher-algo AES256 -o private.gpg -c private.txt && rm -f private.txt

# !!! REMOVE THE private in clear text after encryption !!!
# !!! The gpg password must be protected in the users HOME FOLDERS !!!
