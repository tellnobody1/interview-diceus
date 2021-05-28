import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.{NotFound}
import akka.http.scaladsl.model.{HttpResponse, HttpMethod}
import scala.concurrent.Future

import http.*

@main def run(): Unit =
  implicit val system = ActorSystem(Behaviors.empty, "click")
  implicit val executionContext = system.executionContext

  val b = Binder()
  val bf =
    b.bind{
      case Request(GET, (Root / "products") ? ("configId" * x & "size" * y & "page" * z)) =>
        Future.successful(HttpResponse(status=NotFound))
    }(port=8080)

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "stop http"){ () =>
    println("Stopping web server...")
    b.unbind(bf)
  }

given CanEqual[HttpMethod, HttpMethod] = CanEqual.derived
