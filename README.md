# Kafka News Aggregator

This project uses data from Twitter (and potentially data from other sources) to find articles and other interesting publications related to Kafka and its ecosystem (articles, videos, version updates...). 

The data retrieved is used to generate the Kafka "Weekly Topics" newsletter.

## Local Setup

### Dependencies

You need docker-compose installed, as well as the kafka tools available in the path:

```
export PATH=$PATH:~/{path to kafka tools folder}/bin
```

You also need to add your Twitter configuration credentials in your environment variables:

```
export TWITTER_CONSUMER_KEY=
export TWITTER_CONSUMER_SECRET=
export TWITTER_ACCESS_TOKEN=
export TWITTER_ACCESS_SECRET=
``` 

### Starting up

Start Kafka and all other required containers via docker-compose, and create all other required elements:

```
./startup.sh
```

### Connect

Useful commands:

```
curl -s http://localhost:8083/connectors
curl -s http://localhost:8083/connectors/twitter_source/status
curl -s -X PUT http://localhost:8083/connectors/twitter_source/pause
curl -s -X PUT http://localhost:8083/connectors/twitter_source/resume
```

### Topics

| Topic              | Usage                                                  | 
| ------------------ |:------------------------------------------------------:|
| kafka_tweets       | receives the tweets collected by the Twitter connector |
| excluded_tweets     |       |
| interesting_tweets |      |

### Tweets Categorizer

To start the Tweets Categorizer service, run:

```
sbt "project tweetsCategorizer" run
```

## Production 

To build the jars:
```
sbt "project tweetsCategorizer" assembly
sbt "project tweetsUI" assembly
```

To start the apps:
```
java -jar tweets-categorizer/target/scala_2.13/categorizer.jar
java -jar tweets-ui/target/scala_2.13/tweetsui.jar
```