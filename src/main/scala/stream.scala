package stream

import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.*
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.{Future, ExecutionContext}

def json(uri: String)(using ActorSystem[Unit], ExecutionContext): Future[Done] =
  Http().singleRequest(HttpRequest(uri=uri)).flatMap{ response =>
    given jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    response.entity.dataBytes
      .via(jsonStreamingSupport.framingDecoder)
      .mapAsync(1){ bytes =>
//      val p: Product = Unmarshal(bytes).to[Product]
        val s = new String(bytes.toArray, "utf8")
        Future.successful(s)
      }
      .runForeach(println)
  }
