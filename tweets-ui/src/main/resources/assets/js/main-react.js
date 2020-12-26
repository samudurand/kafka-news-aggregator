'use strict';

const e = React.createElement;

class TweetUI extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            audioTweets: [],
            articleTweets: [],
            versionTweets: [],
            interestingTweets: [],
            videoTweets: [],
            excludedTweets: [],
            newsletterTweets: [],
            creationInProgress: false,
            reloadInProgress: false
        };

        this.deleteTweet = this.deleteTweet.bind(this);
        this.deleteAllInCategory = this.deleteAllInCategory.bind(this);
        this.moveCategory = this.moveCategory.bind(this);
        this.retrieveTweetsByCategory = this.retrieveTweetsByCategory.bind(this);
        this.retrieveTweetsCountByCategory = this.retrieveTweetsCountByCategory.bind(this);

        this.loadAllData = this.loadAllData.bind(this);
        this.prepareNewsletter = this.prepareNewsletter.bind(this);
        this.resetNewsletter = this.resetNewsletter.bind(this);
        this.retrieveTweetsIncludedInNewsletter = this.retrieveTweetsIncludedInNewsletter.bind(this);
        this.createNewsletterDraft = this.createNewsletterDraft.bind(this);
    }

    componentDidMount() {
        this.loadAllData();
    }

    loadAllData() {
        const audio = this.retrieveTweetsCountByCategory("audio", "audioCount");
        const article = this.retrieveTweetsCountByCategory("article", "articleCount");
        const version = this.retrieveTweetsCountByCategory("version", "versionCount");
        const video = this.retrieveTweetsCountByCategory("video", "videoCount");
        const other = this.retrieveTweetsCountByCategory("interesting", "interestingCount");
        const excluded = this.retrieveTweetsCountByCategory("excluded", "excludedCount");

        const audioCount = this.retrieveTweetsByCategory("audio", "audioTweets");
        const articleCount = this.retrieveTweetsByCategory("article", "articleTweets");
        const versionCount = this.retrieveTweetsByCategory("version", "versionTweets");
        const videoCount = this.retrieveTweetsByCategory("video", "videoTweets");
        const otherCount = this.retrieveTweetsByCategory("interesting", "interestingTweets");
        const excludedCount = this.retrieveTweetsByCategory("excluded", "excludedTweets");

        const included = this.retrieveTweetsIncludedInNewsletter();

        return this.setState({reloadInProgress: true}, () => {
                return Promise.all([
                    audio, article, version, video, other, excluded,
                    audioCount, articleCount, versionCount, videoCount, otherCount, excludedCount,
                    included
                ]).then(() => this.setState({reloadInProgress: false}))
            }
        )
    }

    retrieveTweetsByCategory(category, listName) {
        return fetch(`/api/tweets/${category}`)
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        [`${listName}`]: result
                    });
                },
                (error) => {
                    console.log(`Unable to load '${category}' tweets: ${error}`);
                }
            );
    }

    retrieveTweetsCountByCategory(category, countName) {
        return fetch(`/api/tweets/${category}/count`)
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        [`${countName}`]: result.count
                    });
                },
                (error) => {
                    console.log(`Unable to get '${category}' tweets count: ${error}`);
                }
            );
    }

    retrieveTweetsIncludedInNewsletter() {
        return fetch(`/api/newsletter/included`)
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        newsletterTweets: result
                    });
                },
                (error) => {
                    console.log(`Unable to retrieve current tweets included in newsletter: ${error}`);
                }
            );
    }

    render() {
        const {
            audioCount,
            videoCount,
            articleCount,
            versionCount,
            interestingCount,
            excludedCount,

            audioTweets,
            videoTweets,
            articleTweets,
            versionTweets,
            interestingTweets,
            excludedTweets,

            newsletterTweets,
            creationInProgress,
            reloadInProgress
        } = this.state;

        const newsletterDataPresent = newsletterTweets.length > 0

        return (
            <ReactBootstrap.Container fluid>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Button className="mb-2 ml-3" variant="info" onClick={this.loadAllData}>
                        {
                            reloadInProgress ?
                                <ReactBootstrap.Spinner animation="border" role="status">
                                    <span className="sr-only">Loading...</span>
                                </ReactBootstrap.Spinner>
                                : <span>Refresh</span>
                        }
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2" disabled={newsletterDataPresent}
                                           variant="primary" onClick={this.prepareNewsletter}>
                        Move to Newsletter
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2" disabled={!newsletterDataPresent}
                                           variant="secondary" target="_blank" href="/api/newsletter/html">
                        Check Newsletter
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2" disabled={!newsletterDataPresent}
                                           variant="success" onClick={this.createNewsletterDraft}>
                        {
                            creationInProgress ?
                                <ReactBootstrap.Spinner animation="border" role="status">
                                    <span className="sr-only">Creating...</span>
                                </ReactBootstrap.Spinner>
                                : <span>Create Newsletter Draft</span>
                        }
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2" disabled={!newsletterDataPresent}
                                           variant="warning" onClick={this.resetNewsletter}>
                        Reset Newsletter
                    </ReactBootstrap.Button>
                </ReactBootstrap.Row>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Col>
                        <ReactBootstrap.Accordion>
                            {this.tweetsCard("audio", "0", "Audio", audioCount, audioTweets)}
                            {this.tweetsCard("video", "1", "Video", videoCount, videoTweets)}
                            {this.tweetsCard("article", "2", "Article", articleCount, articleTweets)}
                            {this.tweetsCard("version", "3", "Version", versionCount, versionTweets)}
                            {this.tweetsCard("interesting", "4", "Interesting", interestingCount, interestingTweets)}
                            {this.tweetsCard("excluded", "5", "Excluded", excludedCount, excludedTweets)}
                        </ReactBootstrap.Accordion>
                    </ReactBootstrap.Col>
                </ReactBootstrap.Row>
            </ReactBootstrap.Container>
        )
    }

    tweetsCard(category, cardKey, cardTitle, count, tweets) {
        return <ReactBootstrap.Card>
            <ReactBootstrap.Accordion.Toggle as={ReactBootstrap.Card.Header} eventKey={cardKey}>
                <span><b>Tweets:</b> {cardTitle}</span>
                <ReactBootstrap.Button style={{float: "right"}} className="mb-2" variant="warning"
                                       onClick={(event) => {
                                           event.stopPropagation();
                                           this.deleteAllInCategory(category)
                                       }}>
                    Clean <b>({count})</b>
                </ReactBootstrap.Button>
            </ReactBootstrap.Accordion.Toggle>
            <ReactBootstrap.Accordion.Collapse eventKey={cardKey}>
                <ReactBootstrap.Card.Body>{this.tweetsTable(category, tweets)}</ReactBootstrap.Card.Body>
            </ReactBootstrap.Accordion.Collapse>
        </ReactBootstrap.Card>
    }

    tweetsTable(category, tweets) {
        return <ReactBootstrap.Table striped bordered hover>
            <thead>
            <tr>
                <th width={120}>Date</th>
                <th>Text</th>
                <th>User</th>
                <th>Delete</th>
            </tr>
            </thead>
            <tbody>
            {
                tweets.map((tweet) =>
                    <tr key={tweet.id}>
                        <td><a target="_blank"
                               href={`https://twitter.com/${tweet.user}/status/${tweet.id}`}>{moment.unix(tweet.createdAt / 1000).format("DD/MM hh:mm")}</a>
                        </td>
                        <td><Linkify>{tweet.text}</Linkify></td>
                        <td>{tweet.user}</td>
                        <td>
                            <ReactBootstrap.Button variant="danger"
                                                   onClick={() => this.deleteTweet(category, tweet.id)}>
                                Del
                            </ReactBootstrap.Button>
                        </td>
                    </tr>
                )
            }
            </tbody>
        </ReactBootstrap.Table>
    }

    deleteTweet(category, tweetId) {
        fetch(`/api/tweets/${category}/${tweetId}`, {method: "DELETE"})
            .then(
                (res) => {
                    this.retrieveTweetsByCategory(category, `${category}Tweets`)
                    this.retrieveTweetsCountByCategory(category, `${category}Count`)
                },
                (error) => {
                }
            )
    }

    deleteAllInCategory(category) {
        fetch(`/api/tweets/${category}`, {method: "DELETE"})
            .then(
                (res) => {
                    this.retrieveTweetsByCategory(category, `${category}Tweets`)
                    this.retrieveTweetsCountByCategory(category, `${category}Count`)
                },
                (error) => {
                }
            )
    }

    resetNewsletter() {
        fetch(`/api/newsletter/reset`, {method: "DELETE"})
            .then(
                (res) => {
                    this.retrieveTweetsIncludedInNewsletter()
                }
            )
    }

    createNewsletterDraft() {
        this.setState({creationInProgress: true},
            () => fetch(`/api/newsletter/create`, {method: "POST"})
                .then(
                    (res) => {
                        this.setState({
                            creationInProgress: false
                        })
                    }
                )
        )
    }

    moveCategory(source, target, tweetId) {
        fetch(`/api/tweets/move/${tweetId}?source=${source}&target=${target}`, {method: "PUT"})
            .then(
                (res) => {
                    this.retrieveTweetsByCategory(source, `${source}Tweets`)
                    this.retrieveTweetsCountByCategory(source, `${source}Count`)
                },
                (error) => {
                }
            )
    }

    prepareNewsletter() {
        const {articleTweets, audioTweets, versionTweets, interestingTweets, videoTweets} = this.state;

        const tweetIds = {
            "article": articleTweets.map(t => t.id),
            "audio": audioTweets.map(t => t.id),
            "interesting": interestingTweets.map(t => t.id),
            "version": versionTweets.map(t => t.id),
            "video": videoTweets.map(t => t.id)
        }

        fetch(`/api/newsletter/prepare`, {
            body: JSON.stringify({"tweetIds": tweetIds}),
            headers: {
                "Content-Type": "application/json"
            },
            method: "PUT"
        })
            .then(
                (res) => {
                    this.loadAllData();
                },
                (error) => {
                    console.error(`Unable to prepare the newsletter: ${error}`)
                }
            )
    }
}

const domContainer = document.querySelector('#react-container');
ReactDOM.render(e(TweetUI), domContainer);