package com.kafka.experiments.tweetsui.client

import cats.effect.IO
import com.kafka.experiments.shared.LinkUtils.noParamsUrl
import com.kafka.experiments.tweetsui.config.GithubConfig
import com.typesafe.scalalogging.StrictLogging
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Accept
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{MediaType, Uri}

case class RepoMetadata(stars: Long, watchers: Long)
case object RepoMetadata {
    implicit val codec: Codec[RepoMetadata] = deriveCodec
}

trait GithubClient {
  def retrieveRepoMetadata(githubUrl: String): IO[Option[RepoMetadata]]
}

object GithubClient {
  def apply(config: GithubConfig, httpClient: Client[IO]): GithubClient = new DefaultGithubClient(config, httpClient)
}

class DefaultGithubClient(config: GithubConfig, httpClient: Client[IO])
    extends GithubClient
    with Http4sClientDsl[IO]
    with StrictLogging {

  private val baseRepoApiUri: Uri = Uri.fromString(s"${config.baseUrl}/repos/").getOrElse(uri"")
  private val githubRootUrl = "https://github.com/"
  private val userAndRepoRegex = "[A-z0-9]+\\/[A-z0-9]+"

  override def retrieveRepoMetadata(githubUrl: String): IO[Option[RepoMetadata]] = {
    extractRepoPath(githubUrl) match {
      case Some(repoPath) =>
        val repoUri = baseRepoApiUri.withPath(repoPath)
        val request = GET(
          repoUri,
          Accept(MediaType.unsafeParse("application/vnd.github.v3+json"))
        )

        logger.info(s"New github metadata query with request: $request")

        httpClient
          .expect[String](request)
          .map(response => parseResponse(response))

      case None => IO(None)
    }
  }

  private def parseResponse(response: String): Option[RepoMetadata] = {
    import io.circe.parser._

    parse(response) match {
      case Left(parsingError) =>
        throw new RuntimeException("Unable to parse Github Repo metadata response", parsingError)
      case Right(json) =>
        json.as[RepoMetadata] match {
          case Right(metadata) => Some(metadata)
          case Left(error) =>
            logger.error("Error while retrieving github metadata", error)
            None
        }
    }
  }

  private def extractRepoPath(githubUrl: String): Option[String] = {
    val maybeUserAndRepo = noParamsUrl(githubUrl).replace(githubRootUrl, "")
    if (maybeUserAndRepo.matches(userAndRepoRegex)) {
      Some(maybeUserAndRepo)
    } else {
      None
    }
  }
}
