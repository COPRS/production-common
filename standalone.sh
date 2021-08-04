#!/bin/bash

IMAGE_FILE_API="Dockerfile.standalone"
IMAGE_TAG_API="rs-native-api-img"
IMAGE_GROUP_LABEL="de.werum.eo.image.group=reference-system"

CONTAINER_NAME_API="rs-native-api"
CONTAINER_GROUP_LABEL="de.werum.eo.container.group=reference-system"
CONTAINER_PORT_API=8888

TARGET_DIR="target"


function print_usage() {
    printf "usage: %s [build | save | load | run | start | stop | remove [container | image | all]]\n" "$(basename "$0")";
}

function docker_list_containers() {
    printf "\nlisting docker containers for: $CONTAINER_GROUP_LABEL\n"
    docker ps -a --filter "label=$CONTAINER_GROUP_LABEL"
}

function docker_list_images() {
    printf "\nlisting docker images for: $IMAGE_GROUP_LABEL\n"
    docker images --filter "label=$IMAGE_GROUP_LABEL"
}

function docker_build_image() {
    printf "\nbuilding docker image: $IMAGE_TAG_API\n"

    if [ ! -f $IMAGE_FILE_API ]; then
        printf "\n$IMAGE_FILE_API does not exist. stopping execution.\n"
        exit 1
    fi

    if [ ! -f $TARGET_DIR/*.jar ]; then
        printf "\nthe directory $TARGET_DIR/ with application jar does not exist. stopping execution.\ndo: mvn package\n"
        exit 1
    fi

    docker build --tag $IMAGE_TAG_API --label $IMAGE_GROUP_LABEL --file $IMAGE_FILE_API .
    docker_list_images;
}

function docker_save_image() {
    zipped_image="$TARGET_DIR/$IMAGE_TAG_API.tar.gz"
    printf "\nsaving docker image: $IMAGE_TAG_API -> $zipped_image\n"

    if [ ! -d $TARGET_DIR ]; then
        printf "\nthe directory $TARGET_DIR/ does not exist. trying to create it ...\n"
        mkdir $TARGET_DIR

        if [ ! -d $TARGET_DIR ]; then
            printf "\ndirectory $TARGET_DIR/ couldn't be created. stopping execution.\n"
            exit 1
        fi
    fi

    #docker save --output $TARGET_DIR/$IMAGE_TAG_API.tar $IMAGE_TAG_API:latest
    docker save $IMAGE_TAG_API:latest | gzip > $zipped_image
    ls -la $TARGET_DIR | grep $zipped_image
}

function docker_load_image() {
    zipped_image="$TARGET_DIR/$IMAGE_TAG_API.tar.gz"
    printf "\nloading docker image: $zipped_image\n"

    if [ ! -f $zipped_image ]; then
        printf "\n$zipped_image could not be found. stopping execution.\n"
        exit 1
    fi

    docker load --input $zipped_image
    docker_list_images;
}

function docker_remove_image() {
    printf "\nremoving docker image: $IMAGE_TAG_API ...\n"
    docker image rm $IMAGE_TAG_API
    docker_list_images;
}

function docker_run() {
    printf "\nrunning docker container: $CONTAINER_NAME_API\n"
    docker run -d -p $CONTAINER_PORT_API:8080 --name $CONTAINER_NAME_API --label $CONTAINER_GROUP_LABEL $IMAGE_TAG_API
    docker_list_containers;
}

function docker_start_container() {
    printf "\nstarting docker container: $CONTAINER_NAME_API\n"
    docker start $CONTAINER_NAME_API
    docker_list_containers;
}

function docker_stop_container() {
    printf "\nstopping docker container: $CONTAINER_NAME_API\n"
    docker stop $CONTAINER_NAME_API
    docker_list_containers;
}

function docker_remove_container() {
    printf "\nremoving docker container: $CONTAINER_NAME_API\n"
    docker rm $CONTAINER_NAME_API
    docker_list_containers;
}

if [ "$#" -eq 1 ]; then
    # handling one arg
    action="$1"
    case $action in
        build|b)
            docker_build_image;
            ;;
        save|s)
            docker_save_image;
            ;;
        load|l)
            docker_load_image;
            ;;
        run|r)
            docker_run;
            ;;
        start)
            docker_start_container;
            ;;
        stop)
            docker_stop_container;
            ;;
        remove|rm)
            printf "more arguments required for action: %s\n" "$action";
            print_usage;
            ;;
        *)
            printf "unknown action: %s\n" "$action";
            print_usage;
            ;;
    esac
elif [ "$#" -eq 2 ]; then
    # handling two args
    action="$1"
    target="$2"
    case $action in
        remove|rm)
            echo "remove"
            
            case $target in
                container|c)
                    docker_remove_container;
                    ;;
                image|i)
                    docker_remove_image;
                    ;;
                all|a)
                    docker_stop_container;
                    docker_remove_container;
                    docker_remove_image;
                    ;;
                *)
                    printf "unknown target for %s action: %s\n" "$action" "$target";
                    print_usage;
                    ;;
            esac
            ;;
        *)
            printf "unknown action (with target): %s\n" "$action";
            print_usage;
            ;;
    esac
else
    echo "missing arguments"
    print_usage;
fi
