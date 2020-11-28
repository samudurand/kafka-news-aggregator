#!/bin/bash

echo 'Starting all containers...'
docker-compose up -d
echo 'Waiting for startup to complete...'
sleep 60
echo 'Startup completed'

echo 'Creating topics...'
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic kafka_tweets --partitions 1 --replication-factor 1

kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic audio_tweets --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic article_tweets --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic excluded_tweets --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic interesting_tweets --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic version_tweets --partitions 1 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic video_tweets --partitions 1 --replication-factor 1

echo 'Topics created'

echo 'Starting twitter connect'
./connect/start-connectors.sh

echo 'Setup complete!'