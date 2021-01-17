package com.kafka.experiments.tweetsui.client

import cats.data.Kleisli
import cats.effect.{IO, Resource}
import org.http4s.{EntityDecoder, HttpApp, HttpService, Request, Response, Service, Status, Uri}
import org.http4s.client.Client
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class MediumClientTest extends AnyFlatSpec with Matchers with MockFactory {

  "Client" should "extract claps count" in {
    val html = Source.fromFile(getClass.getResource("/medium-article.html").getFile()).getLines().mkString
    val httpClient = new MockClient(html)
    val mediumClient = MediumClient(httpClient)

    val claps = mediumClient.retrieveClapCount("").unsafeRunSync()

    claps shouldBe Some(266)
  }

}

class MockClient(result: String) extends Client[IO] {
  override def run(req: Request[IO]): Resource[IO, Response[IO]] = ???

  override def fetch[A](req: Request[IO])(f: Response[IO] => IO[A]): IO[A] = ???

  override def fetch[A](req: IO[Request[IO]])(f: Response[IO] => IO[A]): IO[A] = ???

  override def toKleisli[A](f: Response[IO] => IO[A]): Kleisli[IO, Request[IO], A] = ???

  override def toService[A](f: Response[IO] => IO[A]): Service[IO, Request[IO], A] = ???

  override def toHttpApp: HttpApp[IO] = ???

  override def toHttpService: HttpService[IO] = ???

  override def stream(req: Request[IO]): fs2.Stream[IO, Response[IO]] = ???

  override def streaming[A](req: Request[IO])(f: Response[IO] => fs2.Stream[IO, A]): fs2.Stream[IO, A] = ???

  override def streaming[A](req: IO[Request[IO]])(f: Response[IO] => fs2.Stream[IO, A]): fs2.Stream[IO, A] = ???

  override def expectOr[A](req: Request[IO])(onError: Response[IO] => IO[Throwable])(implicit
      d: EntityDecoder[IO, A]
  ): IO[A] = ???

  override def expect[A](req: Request[IO])(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def expectOr[A](req: IO[Request[IO]])(onError: Response[IO] => IO[Throwable])(implicit
      d: EntityDecoder[IO, A]
  ): IO[A] = ???

  override def expect[A](req: IO[Request[IO]])(implicit d: EntityDecoder[IO, A]): IO[A] = {
    IO.pure(result).asInstanceOf[IO[A]]
  }

  override def expectOr[A](uri: Uri)(onError: Response[IO] => IO[Throwable])(implicit d: EntityDecoder[IO, A]): IO[A] =
    ???

  override def expect[A](uri: Uri)(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def expectOr[A](s: String)(onError: Response[IO] => IO[Throwable])(implicit d: EntityDecoder[IO, A]): IO[A] =
    ???

  override def expect[A](s: String)(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def expectOptionOr[A](req: Request[IO])(onError: Response[IO] => IO[Throwable])(implicit
      d: EntityDecoder[IO, A]
  ): IO[Option[A]] = ???

  override def expectOption[A](req: Request[IO])(implicit d: EntityDecoder[IO, A]): IO[Option[A]] = ???

  override def fetchAs[A](req: Request[IO])(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def fetchAs[A](req: IO[Request[IO]])(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def status(req: Request[IO]): IO[Status] = ???

  override def status(req: IO[Request[IO]]): IO[Status] = ???

  override def statusFromUri(uri: Uri): IO[Status] = ???

  override def statusFromString(s: String): IO[Status] = ???

  override def successful(req: Request[IO]): IO[Boolean] = ???

  override def successful(req: IO[Request[IO]]): IO[Boolean] = ???

  override def prepAs[A](req: Request[IO])(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def get[A](uri: Uri)(f: Response[IO] => IO[A]): IO[A] = ???

  override def get[A](s: String)(f: Response[IO] => IO[A]): IO[A] = ???

  override def getAs[A](uri: Uri)(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def getAs[A](s: String)(implicit d: EntityDecoder[IO, A]): IO[A] = ???

  override def prepAs[T](req: IO[Request[IO]])(implicit d: EntityDecoder[IO, T]): IO[T] = ???
}
