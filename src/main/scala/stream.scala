package stream

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.{Source, Sink}
import akka.{Done, NotUsed}
import scala.concurrent.{Future, ExecutionContext}

import schema.Product
import codec.given

def json(uri: String)(using ActorSystem[Unit], ExecutionContext): Future[Source[Product, Any]] =
  Http().singleRequest(HttpRequest(uri=uri)).map{ response =>
    given jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    response.entity.dataBytes
      .via(jsonStreamingSupport.framingDecoder)
      .mapAsync(1){ bytes =>
        import argonaut.Argonaut.*
        String(bytes.toArray, "utf8").decodeOption[Product] match
          case None => Future.failed(new RuntimeException("bad json"))
          case Some(p) => Future(p)
      }
  }

given [A]: CanEqual[None.type, A] = CanEqual.derived
