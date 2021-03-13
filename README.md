# Kafka News Aggregator

This project uses data from Twitter (and potentially data from other sources) to find articles and other interesting
publications related to Kafka and its ecosystem (articles, videos, version updates...).

The data retrieved is used to generate the Kafka "Weekly Topics" newsletter.

## Features

Stream tweets out of Twitter based on #kafka and #apachekafka keywords, then do one of the following (more details in
each sub-section):

- skip the tweet
- mark it as "excluded"
- categorise it (article, video, audio, version release, or miscellaneous)

### Identify Tweets to be Ignored

| Name | Description | State |
| ---- | ----------- | ----- |
| Not English | Drop any tweet in an identified language that is not english | Implemented |
| Retweets | Drop all retweets | Implemented |
| Replies | Drop any reply to another tweet | Implemented |
| No Link | Drop any tweet without at least one link (excluding twitter link), configurable | Implemented |
| Franz Kafka | Identify and exclude based on keywords and Books names | Implemented | 
| Known Good Sources | Systematically accept tweets from known good sources (ie. Confluent) | Implemented | 
| Known Bad Sources | Drop tweets from known bad sources (ie. Job Search companies) | Implemented | 
| Too Short | Drop any tweet with a text content of less than 10 chars | Implemented |
| No Kafka Mention | Drop any tweet that does not contain 'kafka' | Implemented |
| Deduplication | drop tweets containing recently seen links (1 day), using a Redis cache | Implemented |
| Ads | Identify and exclude based on keywords (.ie 'sponsored') | Implemented |
| Job ads | Identify and exclude based on keywords | Implemented |
| Certification ads | Identify and exclude based on keywords | Implemented |
| Discount/Course ads | Identify and exclude based on keywords | Implemented |
| Money Related | Identify and exclude based on money amount patterns (ie. 10$, £45...) | Implemented |
| Unrelated | Exclude any tweet containing keywords, or link with domains suggesting unrelated subjects | Implemented |

### Categorise Tweets

| Name | Description | State |
| ---- | ----------- | ----- |
| Audio/Podcasts | Identify based on keywords | Implemented |
| Tools | Identify based on keywords, link domains (ie. Github) or... | Implemented |
| Video | Identify based on keywords or link domain (ie. Youtube) | Implemented |
| Version Release | Identify based on keywords and presence of at least one number | Implemented |
| Article | Identify based on keywords or link domain (ie. Medium) | Implemented |
| Other | Any tweet not falling in any of the other categories | Implemented |

### Scoring Tweets

Not yet implemented. The idea is to calculate scores for each tweet, in order to facilitate the selection of the tweets
that should be included in the newsletter.

A few possible ideas:

- twitter account popular (followers count, ...)
- retweet and likes counts
- link domain (ie. Medium is likely to contain a proper article)
- article length (ie. articles with at least 1000 words are likely to contain more information)
- video length (ie. a video of at least 10 min is likely to not just be promotion, but real content)
- ... ?

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

To build and run the tests:

```
sbt "project tweetsCategorizer" coverage test coverageReport
```

To start the Tweets Categorizer service, run:

```
sbt "project tweetsCategorizer" run
```

### Tweets UI

To build and run the tests:

```
sbt "project tweetsUI" test
```

To start the Tweets UI service, run:

```
sbt "project tweetsUI" run
```

#### Configuration

Twitter Keys are configured via environment variables:

```
export TWITTER_CONSUMER_KEY=xxxxxx
export TWITTER_CONSUMER_SECRET=xxxxxx
export TWITTER_ACCESS_TOKEN=xxxxxx
export TWITTER_ACCESS_SECRET=xxxxxx
```

## Production

The TweetUI service needs the freemarker templates available in the file system (fatjar cannot access files in
resources). Copy the `tweets-ui/src/main/resourcse/newsletter-templates` folder onto the target system, then put the
absolute path to this folder into the environment variable:

```
export FREEMARKER_TEMPLATES_FOLDER=/var/freemarker/newsletter-templates
```

To build the fatjars:

```
sbt "project tweetsCategorizer" assembly
sbt "project tweetsUI" assembly
```

To start the apps:

```
java -jar tweets-categorizer/target/scala_2.13/categorizer.jar
java -jar tweets-ui/target/scala_2.13/tweetsui.jar
```