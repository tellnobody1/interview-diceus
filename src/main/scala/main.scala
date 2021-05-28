import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.HttpMethods.{GET, POST} //hide scaladsl under http
import akka.http.scaladsl.model.StatusCodes.{OK, NotFound}
import akka.http.scaladsl.model.{HttpResponse, HttpMethod}
import scala.concurrent.{Future, ExecutionContext}

import http.*
import sorted.Sorted
import store.Store
import schema.Product
import codec.given

@main def run(): Unit =
  given system: ActorSystem[Unit] = ActorSystem(Behaviors.empty, "click")
  given ExecutionContext = system.executionContext

  val sorted1 = Sorted[Product, Long](Store())(_.click)

  val b = Binder()
  val bf =
    b.bind{
      case Request(POST, Root / "fetch") =>
        stream.json(
          "https://insider-sample-data.s3-eu-west-1.amazonaws.com/scala-api-design/sample.json"
        , p => sorted1.insert(p)
        ).map{ _ =>
          HttpResponse(status=OK)
        }
      case Request(GET, (Root / "products") ? ("configId" * x & "size" * y & "page" * z)) =>
        Future.successful(HttpResponse(status=NotFound))
    }(port=8080)

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "stop http"){ () =>
    println("Stopping web server...")
    b.unbind(bf)
  }

  given CanEqual[HttpMethod, HttpMethod] = CanEqual.derived
