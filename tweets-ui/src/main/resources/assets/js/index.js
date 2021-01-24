'use strict';

const e = React.createElement;

class TweetUI extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            audioTweets: [],
            articleTweets: [],
            excludedTweets: [],
            otherTweets: [],
            reloadInProgress: false,
            toolTweets: [],
            versionTweets: [],
            videoTweets: []
        };

        this.deleteTweet = this.deleteTweet.bind(this);
        this.deleteAllInCategory = this.deleteAllInCategory.bind(this);
        this.retrieveTweetsByCategory = this.retrieveTweetsByCategory.bind(this);
        this.retrieveTweetsCountByCategory = this.retrieveTweetsCountByCategory.bind(this);
        this.setFavourite = this.setFavourite.bind(this);

        this.prepareNewsletter = this.prepareNewsletter.bind(this);
        this.loadAllData = this.loadAllData.bind(this);
    }

    componentDidMount() {
        this.loadAllData();
    }

    loadAllData() {
        const audio = this.retrieveTweetsCountByCategory("audio", "audioCount");
        const article = this.retrieveTweetsCountByCategory("article", "articleCount");
        const tool = this.retrieveTweetsCountByCategory("tool", "toolCount");
        const version = this.retrieveTweetsCountByCategory("version", "versionCount");
        const video = this.retrieveTweetsCountByCategory("video", "videoCount");
        const other = this.retrieveTweetsCountByCategory("other", "otherCount");
        const excluded = this.retrieveTweetsCountByCategory("excluded", "excludedCount");

        const audioCount = this.retrieveTweetsByCategory("audio", "audioTweets");
        const articleCount = this.retrieveTweetsByCategory("article", "articleTweets");
        const toolCount = this.retrieveTweetsByCategory("tool", "toolTweets");
        const versionCount = this.retrieveTweetsByCategory("version", "versionTweets");
        const videoCount = this.retrieveTweetsByCategory("video", "videoTweets");
        const otherCount = this.retrieveTweetsByCategory("other", "otherTweets");
        const excludedCount = this.retrieveTweetsByCategory("excluded", "excludedTweets");

        return this.setState({reloadInProgress: true}, () => {
                return Promise.all([
                    audio, article, tool, version, video, other, excluded, audioCount, articleCount,
                    toolCount, versionCount, videoCount, otherCount, excludedCount
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

    render() {
        const {
            audioCount,
            videoCount,
            articleCount,
            toolCount,
            versionCount,
            otherCount,
            excludedCount,

            audioTweets,
            articleTweets,
            toolTweets,
            otherTweets,
            versionTweets,
            videoTweets,
            excludedTweets,

            reloadInProgress
        } = this.state;

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
                    <ReactBootstrap.Button className="mb-2 ml-2"
                                           variant="primary" onClick={this.prepareNewsletter}>
                        Move to Newsletter
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2"
                                           variant="success" href="/newsletter.html">
                        Check Newsletter
                    </ReactBootstrap.Button>
                </ReactBootstrap.Row>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Col>
                        <ReactBootstrap.Accordion>
                            {this.tweetsCard("audio", "0", "Audio", audioCount, audioTweets)}
                            {this.tweetsCard("video", "1", "Video", videoCount, videoTweets)}
                            {this.tweetsCard("article", "2", "Article", articleCount, articleTweets)}
                            {this.tweetsCard("tool", "3", "Tool", toolCount, toolTweets)}
                            {this.tweetsCard("version", "4", "Version", versionCount, versionTweets)}
                            {this.tweetsCard("other", "5", "Other", otherCount, otherTweets)}
                            {this.tweetsCard("excluded", "6", "Excluded", excludedCount, excludedTweets)}
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
                        <td><span title={tweet.reason}>{tweet.user}</span></td>
                        <td style={{textAlign: "center"}}>
                                <ReactBootstrap.Form.Check type="checkbox" checked={tweet.favourite}
                                                          onChange={(event) => this.setFavourite(tweet.id, category, event)}/>
                        </td>
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
                    return Promise.all([
                        this.retrieveTweetsByCategory(category, `${category}Tweets`),
                        this.retrieveTweetsCountByCategory(category, `${category}Count`)
                    ]);
                },
                (error) => {
                }
            )
    }

    deleteAllInCategory(category) {
        fetch(`/api/tweets/${category}`, {method: "DELETE"})
            .then(
                (res) => {
                    return Promise.all([
                        this.retrieveTweetsByCategory(category, `${category}Tweets`),
                        this.retrieveTweetsCountByCategory(category, `${category}Count`)
                    ])
                },
                (error) => {
                }
            )
    }

    setFavourite(tweetId, category, event) {
        fetch(`/api/tweets/${category}/${tweetId}`, {
            body: JSON.stringify({favourite: event.target.checked}),
            headers: {
                "Content-Type": "application/json"
            },
            method: "PUT"
        }).then(
                (res) => {
                    return this.retrieveTweetsByCategory(category, `${category}Tweets`)
                }
            )
    }

    prepareNewsletter() {
        const {articleTweets, audioTweets, toolTweets, versionTweets, otherTweets, videoTweets} = this.state;

        const tweetIds = {
            "article": articleTweets.map(t => t.id),
            "audio": audioTweets.map(t => t.id),
            "tool": toolTweets.map(t => t.id),
            "other": otherTweets.map(t => t.id),
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