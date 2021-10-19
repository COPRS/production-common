#!/bin/bash

COMPOSE_FILE="docker-compose.standalone"
GROUP_LABEL="de.werum.eo.group=reference-system"
ELASTIC_CONTAINER_NAME="elastic4api"


function print_usage() {
    printf "\nusage: %s [up | down | start | stop | remove [container | image | all] | update [api]]\n" "$(basename "$0")";
    printf "       up: build docker images (if necessary) and start containers (for api and elasticsearch) with docker-compose\n";
    printf "     down: stop and remove containers, networks, ... with docker-compose\n";
    printf "    start: start the containers with docker-compose, need to already exist\n";
    printf "     stop: stop the running containers with docker-compose\n";
    printf "   remove:\n";
    printf "        container: delete the docker containers\n";
    printf "            image: delete the docker images\n";
    printf "              all: delete docker containers and images\n";
    printf "   update: stops and deletes containers, deletes old and builds new images and starts containers with docker-compose \n";
    printf "              api: same as 'update' but does 'mvn clean package' before, so that code changes are taken into account\n";
    printf "\nrequirements:\n";
    printf "         - docker (installed and running)\n";
    printf "         - docker-compose (installed)\n";
    printf "         - mvn (Maven, installed)\n";
    printf "\n      ==--> first time or new API code? use 'update api' <--==\n";
    printf "\n==--> from then use 'start' and 'stop' to keep database changes <--==\n\n";
}

function check_requirements() {
    printf "checking if requirements for this tool are met ...\n"
    
    if ! command -v docker &> /dev/null ; then
        printf "docker could not be found, but is needed to use this tool. exit.\n"
        exit 1
    fi
    printf "    - docker found\n"

    if ! command -v docker-compose &> /dev/null ; then
        printf "docker-compose could not be found, but is needed to use this tool. exit.\n"
        exit 1
    fi
    printf "    - docker-compose found\n"

    if ! command -v mvn &> /dev/null ; then
        printf "maven could not be found, but is needed to use this tool. exit.\n"
        exit 1
    fi
    printf "    - mvn found\n"
    printf "all requirements met.\n"
}

function docker_list_containers() {
    printf "listing docker containers for: %s\n" $GROUP_LABEL
    docker ps -a --filter "label=$GROUP_LABEL"
}

function docker_list_images() {
    printf "listing docker images for: %s\n" $GROUP_LABEL
    docker images --filter "label=$GROUP_LABEL"
}

function docker_remove_images() {
    printf "removing all docker images with label: %s ...\n" $GROUP_LABEL

    docker images --filter=label=$GROUP_LABEL --format "{{.ID}}" | while read -r line ; do
        docker image rm "$line"
    done

    docker_list_images;
}

function docker_remove_containers() {
    printf "removing all docker containers with label: %s ...\n" $GROUP_LABEL

    docker ps --filter=label=$GROUP_LABEL --format "{{.ID}}" | while read -r line ; do
        docker rm "$line"
    done

    docker_list_containers;
}

function docker_compose_up() {
    printf "running docker containers with docker-compose ...\n"
    docker-compose --file $COMPOSE_FILE up -d
    docker_list_containers;
}

function docker_compose_start() {
    printf "starting docker containers with docker-compose: ...\n"
    docker-compose --file $COMPOSE_FILE start
    docker_list_containers;
}

function docker_compose_stop() {
    printf "stopping docker containers with docker-compose ...\n"
    docker-compose --file $COMPOSE_FILE stop
    docker_list_containers;
}

function docker_compose_down() {
    printf "stopping docker containers with docker-compose ...\n"
    docker-compose --file $COMPOSE_FILE down
    docker_list_containers;
}

function init_prip_index() {
    printf "creating prip elasticserach index ...\n"
    docker exec $ELASTIC_CONTAINER_NAME bash -c "curl -XPUT \"http://localhost:9200/prip\" -H 'Content-Type: application/json' -d '{\"mappings\":{\"properties\":{\"id\": {\"type\":\"keyword\"},\"obsKey\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"productFamily\":{\"type\":\"keyword\"},\"contentType\":{\"type\":\"keyword\"},\"contentLength\":{\"type\":\"long\"},\"contentDateStart\":{\"type\":\"date\"},\"contentDateEnd\":{\"type\":\"date\"},\"creationDate\":{\"type\":\"date\"},\"evictionDate\":{\"type\":\"date\"},\"checksum\":{\"type\":\"nested\",\"properties\":{\"algorithm\":{\"type\":\"keyword\"},\"value\":{\"type\":\"keyword\"},\"checksum_date\":{\"type\":\"date\"}}},\"footprint\":{\"type\":\"geo_shape\",\"tree\":\"geohash\"}}}}' 2>&1"

    printf "\nimporting test data into prip index ...\n"
    docker exec $ELASTIC_CONTAINER_NAME bash -c "curl -XPOST \"http://localhost:9200/_bulk\" -H 'Content-Type: application/json' --data-binary @prip-testdata.json 2>&1"
    printf "\n"
}

if [ "$#" -eq 1 ]; then
    check_requirements;
    # handling one arg
    action="$1"
    case $action in
        up)
            docker_compose_up;
            ;;
        down)
            docker_compose_down;
            ;;
        start)
            docker_compose_start;
            ;;
        stop)
            docker_compose_stop;
            ;;
        update|u)
            docker_compose_down;
            docker_remove_containers;
            docker_remove_images;
            docker_compose_up;
            printf "give elasticsearch some time (30s) to come up ..."
            sleep 30s
            init_prip_index;
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
    check_requirements;
    # handling two args
    action="$1"
    target="$2"
    case $action in
        remove|rm)
            case $target in
                container|c)
                    docker_remove_containers;
                    ;;
                image|i)
                    docker_remove_images;
                    ;;
                all|a)
                    #docker_compose_down;
                    docker_remove_containers;
                    docker_remove_images;
                    ;;
                *)
                    printf "unknown target for %s action: %s\n" "$action" "$target";
                    print_usage;
                    ;;
            esac
            ;;
        update|u)
            case $target in
                api|a)
                    mvn clean package
                    docker_compose_down;
                    docker_remove_containers;
                    docker_remove_images;
                    docker_compose_up;
                    printf "give elasticsearch some time (30s) to come up ..."
                    sleep 30s
                    init_prip_index;
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
    printf "missing arguments\n"
    print_usage;
fi
