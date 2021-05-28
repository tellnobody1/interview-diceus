package http

import akka.http.scaladsl.model.Uri
import akka.actor.typed.ActorSystem
import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.{NotImplemented, NotFound}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, GraphDSL}
import akka.stream.{FlowShape, Materializer}
import scala.concurrent.{Future}
import scala.util.Failure
import scala.concurrent.ExecutionContext

class Binder(using ActorSystem[Unit]):
  type Hr = HttpRequest
  type Hs = Future[HttpResponse]
  type R = PartialFunction[Hr, Hs]
  
  def bind(route: R)(port: Int): Future[ServerBinding] =
    Http().newServerAt("0.0.0.0", port).bind(route)

  def unbind(bf: Future[ServerBinding])(using ExecutionContext): Future[Done] =
    bf.flatMap(_.unbind())

sealed trait Path
case object Root extends Path
type Segment = String
final case class SegmentPath(prev: Path, head: Segment) extends Path

object Path {
  def unapply(uri: Uri): Option[Path] = {
    def path(p: Uri.Path): Path = {
      p.reverse
      p match {
        case Uri.Path.Empty => Root
        case Uri.Path.Segment(head, slashOrEmpty) => SegmentPath(path(slashOrEmpty), head)
        case Uri.Path.Slash(tail) => path(tail)
      }
    }

    if (uri.isEmpty) None else Option(path(uri.path.reverse))
  }
}

object / {
  def unapply(l: Path): Option[(Path, String)] = l match {
    case Root => None
    case s: SegmentPath => Option((s.prev, s.head))
  }
}

given CanEqual[Uri.Path.Empty.type, Uri.Path] = CanEqual.derived
given CanEqual[Root.type, Path] = CanEqual.derived