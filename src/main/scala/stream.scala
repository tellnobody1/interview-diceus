package stream

import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.*
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.stream.scaladsl.{Source, Sink}
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.{Future, ExecutionContext}

import schema.Product
import codec.given

def json(uri: String, f: Product => Unit)(using ActorSystem[Unit], ExecutionContext): Future[Done] =
  Http().singleRequest(HttpRequest(uri=uri)).flatMap{ response =>
    given jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    response.entity.dataBytes
      .via(jsonStreamingSupport.framingDecoder)
      .mapAsync(1){ bytes =>
        import argonaut.*, Argonaut.*
        new String(bytes.toArray, "utf8").decodeOption[Product] match
          case None => Future.failed[Unit](throw new RuntimeException("bad json"))
          case Some(p) =>
            f(p)
            Future.successful(())
      }
      .runWith(Sink.ignore)
  }

given [A]: CanEqual[None.type, A] = CanEqual.derived
