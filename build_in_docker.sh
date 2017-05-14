#!/usr/bin/env  bash


function error {
        printf "\e[31m$*\e[0m\n" >&2
}

function info {
        printf "\e[32m$*\e[0m\n"
}

function show_build_details {
        info "##  Lint reports:"
        find ./ -type f -name lint-results.html | while read f; do info "##           - \e[1m$f\e[21m"; done
        info "##"
        info "##  Test reports:  "
        find ./ -type f -name index.html | while read f; do info "##           - \e[1m$f\e[21m"; done
        info "##"
        info "##  APKs:  "
        find ./ -type f -name \*apk | while read f; do info "##           - \e[1m$f\e[21m"; done
        info "##"
        info "## Inspect image by running"
        info "##   \e[1m docker run -ti $DOCKER_MOUNT -w ${BUILD_ROOT} $DOCKER_IMAGE /bin/bash \e[21m"
}

DOCKER_IMAGE=pockethub/build_pockethub:1.0
BUILD_ROOT=/root/pockethub

# Build without context
docker build  -t $DOCKER_IMAGE - < Dockerfile

ERR=$?
if [ ! $ERR -eq 0 ];
then
        error "################################## docker build ERROR"
        error "##"
        error "## ERROR: Please check the *docker build* output for errors"
        error "##"
        exit $ERR
fi


if [ "x${DOCKER_MACHINE_NAME}" == "x" ]
then
        DOCKER_MOUNT="-v ${PWD}:${BUILD_ROOT}"
else
        DOCKER_MOUNT="-v /mnt/hgfs/${PWD}:${BUILD_ROOT}"
fi

docker run $DOCKER_MOUNT -w ${BUILD_ROOT} $DOCKER_IMAGE sh -c './gradlew clean && ./gradlew build'


if [ $? -eq 0 ];
then

        info "################################## build complete"
        info "##"
        info "## Build output can be found in ./build"
        info "##"
        show_build_details
else
        error "################################## build ERROR"
        error "##"
        error "## ERROR: Please check the build output for errors"
        error "##"
        show_build_details
fi
