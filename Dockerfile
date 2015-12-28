#
# Setup a Linux VM to compile PocketHub
# 
# - Clarity of structure is prefered over build-performance. 

FROM ubuntu:15.04
MAINTAINER Jens Neuhalfen <Jens@neuhalfen.name>

# CONFIG

ENV ANDROID_HOME /opt/android-sdk-linux
ENV ANDROID_SDK_VERSION 24.4.1
ENV ANDROID_BUILD_TOOLS_VERSION 23.0.2
ENV ANDROID_COMPILE_SDK_VERSION android-23

ENV GRADLE_VERSION      2.10


# Bootstrapping

RUN apt-get update
RUN apt-get -y upgrade

RUN apt-get -y install software-properties-common bzip2 unzip curl wget


# ------------------------------------------------------
# Build Deps

# Android Dependencies
RUN apt-get install -y openjdk-7-jdk libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 lib32z1
#
# Java buid tools
#

# gradle
RUN mkdir -p /root/opt

RUN cd /root/opt; curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip >/tmp/gradle.zip && unzip /tmp/gradle.zip
ENV PATH /root/opt/gradle-${GRADLE_VERSION}/bin:${PATH}


# ------------------------------------------------------
# Android SDK


RUN cd /opt && wget -q https://dl.google.com/android/android-sdk_r${ANDROID_SDK_VERSION}-linux.tgz -O android-sdk.tgz
RUN cd /opt && tar -xvzf android-sdk.tgz && rm android-sdk.tgz

ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

# ------------------------------------------------------
# --- Install Android SDKs and other build packages

# Other tools and resources of Android SDK
# To get a full list of available options you can use:
#  android list sdk --no-ui --all --extended
RUN echo y | android update sdk --no-ui --all --filter \
  platform-tools,extra-android-support

# SDKs
RUN echo y | android update sdk --no-ui --all --filter \
  ${ANDROID_COMPILE_SDK_VERSION}
# build tools
RUN echo y | android update sdk --no-ui --all --filter \
  build-tools-${ANDROID_BUILD_TOOLS_VERSION}

# Android System Images, for emulators
# RUN echo y | android update sdk --no-ui --all --filter \
# sys-img-armeabi-v7a-android-23,sys-img-armeabi-v7a-android-22,sys-img-armeabi-v7a-android-21,sys-img-armeabi-v7a-android-19,sys-img-armeabi-v7a-android-17,sys-img-armeabi-v7a-android-16,sys-img-armeabi-v7a-android-15

# Extras
RUN echo y | android update sdk --no-ui --all --filter \
  extra-android-m2repository
  #extra-android-m2repository,extra-google-m2repository,extra-google-google_play_services


