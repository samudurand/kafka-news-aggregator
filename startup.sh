#!/bin/bash

echo 'Starting all containers...'
docker-compose up -d
echo 'Waiting for startup to complete...'
sleep 60
echo 'Startup completed'

echo 'Creating topics...'
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic kafka_tweets --partitions 1 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic category_audio --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic category_article --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic category_excluded --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic category_other --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic category_version --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic category_video --partitions 1 --replication-factor 1

echo 'Topics created'

echo 'Starting twitter connect'
./connect/start-connectors.sh

echo 'Setup complete!'