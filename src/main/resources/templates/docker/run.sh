#!/bin/bash

echo && echo "==================== Build packages, images and launch containers ========================="
cd ../ && mvn clean package -Dmaven.test.skip=true && mvn dockerfile:build && cd docker && docker-compose up --build