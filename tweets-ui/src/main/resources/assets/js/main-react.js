'use strict';

const e = React.createElement;

class LikeButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            tweetsAudio: [],
            tweetsArticle: [],
            tweetsVersion: [],
            tweetsOther: [],
            tweetsDropped: []
        };
    }

    componentDidMount() {
        this.retrieveTweetsByCategory("audio", "tweetsAudio");
        this.retrieveTweetsByCategory("article", "tweetsArticle");
        this.retrieveTweetsByCategory("version", "tweetsVersion");
        this.retrieveTweetsByCategory("interesting", "tweetsOther");
        this.retrieveTweetsByCategory("dropped", "tweetsDropped");
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

    render() {
        const {
            tweetsAudio,
            tweetsArticle,
            tweetsVersion,
            tweetsOther,
            tweetsDropped
        } = this.state;

        return (
            <ReactBootstrap.Container>
                <ReactBootstrap.Row>
                    <ReactBootstrap.Col>
                        <ReactBootstrap.Accordion>
                            {this.tweetsCard("0", "Audio", tweetsAudio)}
                            {this.tweetsCard("1", "Article", tweetsArticle)}
                            {this.tweetsCard("2", "Version", tweetsVersion)}
                            {this.tweetsCard("3", "Other Interesting", tweetsOther)}
                            {this.tweetsCard("4", "Dropped", tweetsDropped)}
                        </ReactBootstrap.Accordion>
                    </ReactBootstrap.Col>
                </ReactBootstrap.Row>
            </ReactBootstrap.Container>
        )
    }

    tweetsCard(cardKey, cardTitle, tweets) {
        return <ReactBootstrap.Card>
            <ReactBootstrap.Card.Header>
                <ReactBootstrap.Accordion.Toggle as={ReactBootstrap.Button} variant="link" eventKey={cardKey}>
                    {cardTitle}
                </ReactBootstrap.Accordion.Toggle>
            </ReactBootstrap.Card.Header>
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
                <th>Link</th>
            </tr>
            </thead>
            <tbody>
            {
                tweets.map((tweet) =>
                    <tr key={tweet.id}>
                        <td>{moment.unix(tweet.createdAt / 1000).format("DD/MM hh:mm")}</td>
                        <td><Linkify>{tweet.text}</Linkify></td>
                        <td>{tweet.user}</td>
                        <td><a target="_blank"
                               href={`https://twitter.com/${tweet.user}/status/${tweet.id}`}>Link</a></td>
                    </tr>
                )
            }
            </tbody>
        </ReactBootstrap.Table>
    }
}

const domContainer = document.querySelector('#react-container');
ReactDOM.render(e(LikeButton), domContainer);