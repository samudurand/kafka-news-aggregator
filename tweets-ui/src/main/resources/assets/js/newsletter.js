'use strict';

const e = React.createElement;

class TweetUI extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            audioTweets: [],
            articleTweets: [],
            creationInProgress: false,
            otherTweets: [],
            scoreCalculationInProgress: false,
            toolTweets: [],
            versionTweets: [],
            videoTweets: [],

            showSwitchModal: false,
            tweetSwitchingCategory: null
        };

        this.deleteTweet = this.deleteTweet.bind(this);

        this.resetNewsletter = this.resetNewsletter.bind(this);
        this.retrieveTweets = this.retrieveTweets.bind(this);
        this.createNewsletterDraft = this.createNewsletterDraft.bind(this);
        this.calculateScores = this.calculateScores.bind(this);

        this.switchTweetCategory = this.switchTweetCategory.bind(this);
        this.hideSwitchModal = this.hideSwitchModal.bind(this);
        this.showSwitchModal = this.showSwitchModal.bind(this);
    }

    componentDidMount() {
        this.retrieveTweets();
    }

    retrieveTweets() {
        return fetch(`/api/newsletter/included`)
            .then(res => res.json())
            .then(
                (tweets) => {
                    const tweetsByCategory = tweets.reduce((result, tweet) => {
                        result[tweet.category] = [...result[tweet.category] || [], tweet];
                        return result;
                    }, {});

                    this.setState({
                        audioTweets: tweetsByCategory.audio || [],
                        articleTweets: tweetsByCategory.article || [],
                        toolTweets: tweetsByCategory.tool || [],
                        versionTweets: tweetsByCategory.version || [],
                        videoTweets: tweetsByCategory.video || [],
                        otherTweets: tweetsByCategory.other || []
                    });
                },
                (error) => {
                    console.log(`Unable to retrieve current tweets included in newsletter: ${error}`);
                }
            );
    }

    render() {
        const {
            audioTweets,
            articleTweets,
            toolTweets,
            otherTweets,
            versionTweets,
            videoTweets,

            creationInProgress,
            scoreCalculationInProgress
        } = this.state;

        return (
            <ReactBootstrap.Container fluid>
                <ReactBootstrap.Row>
                    {this.switchCategoryModal()}
                </ReactBootstrap.Row>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Button className="mb-2 ml-3"
                                           variant="secondary" href="/index.html">
                        Back to Tweets
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2" variant="info" onClick={this.calculateScores}>
                        {
                            scoreCalculationInProgress ?
                                <ReactBootstrap.Spinner animation="border" role="status">
                                    <span className="sr-only">Loading...</span>
                                </ReactBootstrap.Spinner>
                                : <span>Calculate Scores</span>
                        }
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2"
                                           variant="primary" target="_blank" href="/api/newsletter/html">
                        Check Email
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2"
                                           variant="success" onClick={this.createNewsletterDraft}>
                        {
                            creationInProgress ?
                                <ReactBootstrap.Spinner animation="border" role="status">
                                    <span className="sr-only">Creating...</span>
                                </ReactBootstrap.Spinner>
                                : <span>Create Newsletter Draft</span>
                        }
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button className="mb-2 ml-2"
                                           variant="danger" onClick={this.resetNewsletter}>
                        Reset Newsletter
                    </ReactBootstrap.Button>
                </ReactBootstrap.Row>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Col>
                        <ReactBootstrap.Accordion>
                            {this.tweetsCard("0", "Audio", audioTweets.length, audioTweets)}
                            {this.tweetsCard("1", "Video", videoTweets.length, videoTweets)}
                            {this.tweetsCard("2", "Article", articleTweets.length, articleTweets)}
                            {this.tweetsCard("3", "Tool", toolTweets.length, toolTweets)}
                            {this.tweetsCard("4", "Version", versionTweets.length, versionTweets)}
                            {this.tweetsCard("5", "Other", otherTweets.length, otherTweets)}
                        </ReactBootstrap.Accordion>
                    </ReactBootstrap.Col>
                </ReactBootstrap.Row>
            </ReactBootstrap.Container>
        )
    }

    switchCategoryModal() {
        const {newCategory, showSwitchModal} = this.state;
        return (
            <ReactBootstrap.Modal show={showSwitchModal} onHide={this.hideSwitchModal}>
                <ReactBootstrap.Modal.Header closeButton>
                    <ReactBootstrap.Modal.Title>Change Category</ReactBootstrap.Modal.Title>
                </ReactBootstrap.Modal.Header>
                <ReactBootstrap.Modal.Body>
                    <ReactBootstrap.Form.Group>
                        <ReactBootstrap.Form.Control onChange={(event) => this.setNewCategory(event)}
                                                     defaultValue={newCategory}
                                                     as="select">
                            <option value="audio">Audio</option>
                            <option value="video">Video</option>
                            <option value="article">Article</option>
                            <option value="tool">Tool</option>
                            <option value="version">Version</option>
                            <option value="other">Other</option>
                        </ReactBootstrap.Form.Control>
                    </ReactBootstrap.Form.Group>
                </ReactBootstrap.Modal.Body>
                <ReactBootstrap.Modal.Footer>
                    <ReactBootstrap.Button variant="secondary" onClick={this.hideSwitchModal}>
                        Close
                    </ReactBootstrap.Button>
                    <ReactBootstrap.Button variant="primary" onClick={this.switchTweetCategory}>
                        Apply Change
                    </ReactBootstrap.Button>
                </ReactBootstrap.Modal.Footer>
            </ReactBootstrap.Modal>
        );
    }

    setNewCategory(event) {
        console.log("Setting category value " + event.target.value)
        this.setState({newCategory: event.target.value})
    }

    tweetsCard(cardKey, cardTitle, count, tweets) {
        return <ReactBootstrap.Card>
            <ReactBootstrap.Accordion.Toggle as={ReactBootstrap.Card.Header} eventKey={cardKey}>
                <span><b>Tweets:</b> {cardTitle}</span>
                <ReactBootstrap.Button style={{float: "right"}} className="mb-2" variant="warning"
                                       onClick={(event) => {
                                           event.stopPropagation();
                                       }}>
                    Clean <b>({count})</b>
                </ReactBootstrap.Button>
            </ReactBootstrap.Accordion.Toggle>
            <ReactBootstrap.Accordion.Collapse eventKey={cardKey}>
                <ReactBootstrap.Card.Body>{this.tweetsTable(tweets)}</ReactBootstrap.Card.Body>
            </ReactBootstrap.Accordion.Collapse>
        </ReactBootstrap.Card>
    }

    tweetsTable(tweets) {
        return <ReactBootstrap.Table striped bordered hover>
            <thead>
            <tr>
                <th width={120}>Date</th>
                <th>Text</th>
                <th>User</th>
                <th>Score</th>
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
                        <td style={{textAlign: "center"}}>{tweet.score}</td>
                        <td>
                            <ReactBootstrap.Button variant="warning"
                                                   onClick={() => this.showSwitchModal(tweet.id, tweet.category)}>
                                Switch
                            </ReactBootstrap.Button>
                            <ReactBootstrap.Button variant="danger"
                                                   onClick={() => this.deleteTweet(tweet.id)}>
                                Del
                            </ReactBootstrap.Button>
                        </td>
                    </tr>
                )
            }
            </tbody>
        </ReactBootstrap.Table>
    }

    deleteTweet(tweetId) {
        fetch(`/api/newsletter/tweet/${tweetId}`, {method: "DELETE"})
            .then(
                (res) => {
                    return this.retrieveTweets();
                },
                (error) => {
                }
            )
    }

    resetNewsletter() {
        fetch(`/api/newsletter/reset`, {method: "DELETE"})
            .then(
                (res) => {
                    return this.retrieveTweets()
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

    calculateScores() {
        this.setState({scoreCalculationInProgress: true},
            () => fetch(`/api/newsletter/score`, {method: "PUT"})
                .then(
                    (res) => {
                        this.retrieveTweets()
                        this.setState({
                            scoreCalculationInProgress: false
                        })
                    }
                )
        )
    }

    showSwitchModal(tweetId, category) {
        this.setState({
            newCategory: category,
            showSwitchModal: true,
            tweetSwitchingCategory: tweetId
        })
    }

    hideSwitchModal() {
        this.setState({
            showSwitchModal: false,
            tweetSwitchingCategory: null
        })
    }

    switchTweetCategory() {
        const {newCategory, tweetSwitchingCategory} = this.state;
        fetch(`/api/newsletter/tweet/${tweetSwitchingCategory}/${newCategory}`, {method: "PUT"})
            .then(
                (res) => {
                    this.setState({
                        showSwitchModal: false,
                        tweetSwitchingCategory: null
                    }, this.retrieveTweets)
                },
                (error) => {
                }
            )
    }
}

const domContainer = document.querySelector('#react-container');
ReactDOM.render(e(TweetUI), domContainer);