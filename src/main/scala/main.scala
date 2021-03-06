import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.Source
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model.StatusCodes.{OK, BadRequest}
import akka.http.scaladsl.model.{HttpResponse, HttpMethod, HttpEntity, ContentTypes}
import scala.concurrent.{Future, ExecutionContext}

import http.*
import sorted.Sorted
import store.MemStore
import schema.Prod
import codec.given

@main def run(): Unit =
  given system: ActorSystem[Unit] = ActorSystem(Behaviors.empty, "click")
  given ExecutionContext = system.executionContext

  val sorted1 = Sorted[Long](_.click)(MemStore())
  val sorted2 = Sorted[Long](_.purchase)(MemStore())

  val bf = Http().newServerAt("0.0.0.0", 8080).bind{
    case Request(POST, Root / "fetch") =>
      stream.json(
        "https://insider-sample-data.s3-eu-west-1.amazonaws.com/scala-api-design/sample.json"
      ).flatMap(_.runForeach{ p =>
        sorted1.insert(p)
        sorted2.insert(p)
      }).map(_ =>
        HttpResponse(status=OK)
      )

    case Request(GET, (Root / "products") ? ("configId" * x & "size" * y & "page" * z)) =>
      (x.toIntOption, y.toIntOption, z.toIntOption) match
        case (Some(x), Some(y), Some(z)) if (x == 1 || x == 2) && y >= 1 && y <= 10_000 && z >= 1 =>
          import argonaut.Argonaut.*
          val s =
            Source((if x == 1 then sorted1 else sorted2).flatten.drop((z-1)*y).take(y))
              .map(_.asJson.spaces2)
              .intersperse("[\n", ",\n", "\n]")
              .map(HttpEntity.Chunk.apply)
          Future(HttpResponse(status=OK, entity=HttpEntity.Chunked(ContentTypes.`application/json`, s)))
        case _ =>
          Future(HttpResponse(status=BadRequest))

    case _ => Future(HttpResponse(status=BadRequest))
  }

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "stop http"){ () =>
    println("Stopping web server...")
    bf.flatMap(_.unbind)
  }

  given CanEqual[HttpMethod, HttpMethod] = CanEqual.derived
