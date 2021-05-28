import http.*
import akka.actor.typed.ActorSystem
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.HttpMethods.GET
import scala.concurrent.{Future}
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.StatusCodes.{NotImplemented, NotFound}
import scala.io.StdIn

@main def run(): Unit =
  implicit val system = ActorSystem(Behaviors.empty, "click")
  implicit val executionContext = system.executionContext

  val b = Binder()

  val bf =
    b.bind{
      case HttpRequest(GET, Path(Root), _, _, _) => Future.successful(HttpResponse(status=NotFound))
    }(port=8080)

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "stop http"){ () =>
    println("Stopping web server...")
    b.unbind(bf)
  }

given CanEqual[HttpMethod, HttpMethod] = CanEqual.derived
given CanEqual[Root.type, Path] = CanEqual.derived
