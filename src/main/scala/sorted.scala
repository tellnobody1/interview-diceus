package sorted

import store.Store
import scala.annotation.tailrec
import scala.math.Ordering
import scala.math.Ordering.Implicits.infixOrderingOps

type Key = store.Key
case class Node[A](left: Option[Key], x: A, right: Option[Key])

class Sorted[A, I: Ordering](store: Store[Node[A]])(index: A => I):

  def insert(x: A): Unit =
    store.get(store.head) match
      case None => store.add(newNode(x))
      case Some(node) => insert(x, node, store.head)

  @tailrec
  private def insert(x: A, node: Node[A], nodeKey: Key): Unit =
    node match
      case Node(None, y, _) if index(x) <= index(y) =>
        val k = store.add(newNode(x))
        store.put(nodeKey, node.copy(left=Some(k)))

      case Node(Some(t), y, _) if index(x) <= index(y) =>
        store.get(t) match
          case None =>
            val k = store.add(newNode(x))
            store.put(nodeKey, node.copy(left=Some(k)))
          case Some(node1) =>
            insert(x, node1, t)

      case Node(_, _, None) =>
        val k = store.add(newNode(x))
        store.put(nodeKey, node.copy(right=Some(k)))

      case Node(_, _, Some(s)) =>
        store.get(s) match
          case None =>
            val k = store.add(newNode(x))
            store.put(nodeKey, node.copy(right=Some(k)))
          case Some(node1) =>
            insert(x, node1, s)

  def flatten: List[A] = flatten(store.head)

  private def flatten(nodeKey: Key): List[A] =
    store.get(nodeKey) match
      case None => Nil
      case Some(Node(t, x, s)) => flatten(t) ++ List(x) ++ flatten(s)

  inline private def flatten(nodeKey: Option[Key]): List[A] =
    nodeKey match
      case None => Nil
      case Some(nodeKey) => flatten(nodeKey)

  inline private def newNode[A](x: A): Node[A] = Node(None, x, None)

  given [A]: CanEqual[None.type, Option[A]] = CanEqual.derived

// @main
def test(): Unit =
  import codec.{Codec, given}
  import schema.Product
  val sorted1 = Sorted[Product, Long](Store())(_.click)
  sorted1.insert(Product("399", "Single", "tr_TR", 122, 904))
  sorted1.insert(Product("1086", "Woo Album #4", "tr_TR", 203, 606))
  sorted1.insert(Product("1116", "Patient Ninja", "tr_TR", 470, 298))
  sorted1.insert(Product("397", "Polo", "tr_TR", 604, 674))
  sorted1.insert(Product("386", "V-Neck T-Shirt", "tr_TR", 592, 740))
  assert(sorted1.flatten.map(_.item_id) == List("399", "1086", "1116", "386", "397"))
  println("ok")
