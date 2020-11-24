'use strict';

const e = React.createElement;

class LikeButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            audioTweets: [],
            articleTweets: [],
            versionTweets: [],
            interestingTweets: [],
            videoTweets: [],
            droppedTweets: []
        };

        this.deleteTweet = this.deleteTweet.bind(this);
        this.downloadTxtFile = this.downloadTxtFile.bind(this);
        this.retrieveTweetsByCategory = this.retrieveTweetsByCategory.bind(this);
        this.retrieveTweetsCountByCategory = this.retrieveTweetsCountByCategory.bind(this);
    }

    componentDidMount() {
        this.retrieveTweetsCountByCategory("audio", "audioCount");
        this.retrieveTweetsCountByCategory("article", "articleCount");
        this.retrieveTweetsCountByCategory("version", "versionCount");
        this.retrieveTweetsCountByCategory("video", "videoCount");
        this.retrieveTweetsCountByCategory("interesting", "interestingCount");
        this.retrieveTweetsCountByCategory("dropped", "droppedCount");

        this.retrieveTweetsByCategory("audio", "audioTweets");
        this.retrieveTweetsByCategory("article", "articleTweets");
        this.retrieveTweetsByCategory("version", "versionTweets");
        this.retrieveTweetsByCategory("video", "videoTweets");
        this.retrieveTweetsByCategory("interesting", "interestingTweets");
        this.retrieveTweetsByCategory("dropped", "droppedTweets");
    }

    retrieveTweetsByCategory(category, listName) {
        fetch(`/api/${category}`)
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
            )
    }

    retrieveTweetsCountByCategory(category, countName) {
        fetch(`/api/${category}/count`)
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
            )
    }

    render() {
        const {
            audioCount,
            videoCount,
            articleCount,
            versionCount,
            interestingCount,
            droppedCount,

            audioTweets,
            videoTweets,
            articleTweets,
            versionTweets,
            interestingTweets,
            droppedTweets
        } = this.state;

        return (
            <ReactBootstrap.Container>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Col>
                        <ReactBootstrap.Button className="mb-2" variant="primary" onClick={this.downloadTxtFile}>
                            Weekly Report
                        </ReactBootstrap.Button>
                    </ReactBootstrap.Col>
                </ReactBootstrap.Row>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Col>
                        <ReactBootstrap.Accordion>
                            {this.tweetsCard("audio", "0", "Audio", audioCount, audioTweets)}
                            {this.tweetsCard("video", "1", "Video", videoCount, videoTweets)}
                            {this.tweetsCard("article", "2", "Article", articleCount, articleTweets)}
                            {this.tweetsCard("version", "3", "Version", versionCount, versionTweets)}
                            {this.tweetsCard("interesting", "4", "Interesting", interestingCount, interestingTweets)}
                            {this.tweetsCard("dropped", "5", "Dropped", droppedCount, droppedTweets)}
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
                <span style={{float: "right"}}><i>({count})</i></span>
            </ReactBootstrap.Accordion.Toggle>
            <ReactBootstrap.Accordion.Collapse eventKey={cardKey}>
                <ReactBootstrap.Card.Body>{this.tweetsTable(category, tweets)}</ReactBootstrap.Card.Body>
            </ReactBootstrap.Accordion.Collapse>
        </ReactBootstrap.Card>
    }

    tweetsTable(category, tweets) {
        const reasonCol = (category === "dropped")
        return <ReactBootstrap.Table striped bordered hover>
            <thead>
            <tr>
                <th width={120}>Date</th>
                <th>Text</th>
                <th>User</th>
                {/*<th>Link</th>*/}
                <th>Action</th>
                { reasonCol ? <th>Reason</th> : '' }
            </tr>
            </thead>
            <tbody>
            {
                tweets.map((tweet) =>
                    <tr key={tweet.id}>
                        <td>{moment.unix(tweet.createdAt / 1000).format("DD/MM hh:mm")}</td>
                        <td><Linkify>{tweet.text}</Linkify></td>
                        <td>{tweet.user}</td>
                        {/*<td>*/}
                        {/*    <a target="_blank" href={`https://twitter.com/${tweet.user}/status/${tweet.id}`}>Link</a>*/}
                        {/*</td>*/}
                        <td>
                            <ReactBootstrap.Button variant="danger" onClick={() => this.deleteTweet(category, tweet.id)}>
                                Del
                            </ReactBootstrap.Button>
                        </td>
                        { reasonCol ? <td>{tweet.reason.substr(0, 17)}</td> : '' }
                    </tr>
                )
            }
            </tbody>
        </ReactBootstrap.Table>
    }

    deleteTweet(category, tweetId) {
        fetch(`/api/${category}/${tweetId}`, {method: "DELETE"})
            .then(
                (res) => {
                    this.retrieveTweetsByCategory(category, `${category}Tweets`)
                    this.retrieveTweetsCountByCategory(category, `${category}Count`)
                },
                (error) => {
                }
            )
    }

    downloadTxtFile() {
        const element = document.createElement("a");
        const file = new Blob(["hello\nhi"],
            {type: 'text/plain;charset=utf-8'});
        element.href = URL.createObjectURL(file);
        element.download = "myFile.txt";
        document.body.appendChild(element);
        element.click();
    }
}

const domContainer = document.querySelector('#react-container');
ReactDOM.render(e(LikeButton), domContainer);