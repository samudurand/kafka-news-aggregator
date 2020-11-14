#!/bin/bash

config=$(cat connect/connect-twitter.json)

config=${config/ACCESS_TOKEN/$TWITTER_ACCESS_TOKEN}
config=${config/ACCESS_TOKEN_SECRET/$TWITTER_ACCESS_SECRET}
config=${config/CONSUMER_KEY/$TWITTER_CONSUMER_KEY}
config=${config/CONSUMER_SECRET/$TWITTER_CONSUMER_SECRET}

# Twitter connector
curl -s -o /dev/null -v -X POST -H 'Content-Type: application/json' --data "$config" http://localhost:8083/connectors 2>&1

# Mongo sink connector
curl -s -o /dev/null -v -X POST -H 'Content-Type: application/json' --data @connect/connect-mongo-interesting.json http://localhost:8083/connectors 2>&1
curl -s -o /dev/null -v -X POST -H 'Content-Type: application/json' --data @connect/connect-mongo-dropped.json http://localhost:8083/connectors 2>&1