package sorted

import scala.annotation.tailrec
import scala.math.Ordering
import scala.math.Ordering.Implicits.infixOrderingOps

import store.MemStore

case class Node[A](left: Option[IArray[Byte]], x: A, right: Option[IArray[Byte]])

class Sorted[A, I: Ordering](index: A => I)(dba: store.Dba[Node[A]]):

  def insert(x: A): Unit =
    dba.get(dba.head) match
      case None => dba.add(newNode(x))
      case Some(node) => insert(x, node, dba.head)

  @tailrec private def insert(x: A, node: Node[A], nodeKey: IArray[Byte]): Unit =
    node match
      case Node(None, y, _) if index(x) <= index(y) =>
        val k = dba.add(newNode(x))
        dba.put(nodeKey, node.copy(left=Some(k)))

      case Node(Some(t), y, _) if index(x) <= index(y) =>
        dba.get(t) match
          case None =>
            val k = dba.add(newNode(x))
            dba.put(nodeKey, node.copy(left=Some(k)))
          case Some(node1) =>
            insert(x, node1, t)

      case Node(_, _, None) =>
        val k = dba.add(newNode(x))
        dba.put(nodeKey, node.copy(right=Some(k)))

      case Node(_, _, Some(s)) =>
        dba.get(s) match
          case None =>
            val k = dba.add(newNode(x))
            dba.put(nodeKey, node.copy(right=Some(k)))
          case Some(node1) =>
            insert(x, node1, s)

  def flatten: LazyList[A] = flatten(dba.head)

  private def flatten(nodeKey: IArray[Byte]): LazyList[A] =
    dba.get(nodeKey) match
      case None => LazyList.empty
      case Some(Node(t, x, s)) => flatten(t) #::: LazyList(x) #::: flatten(s)

  inline private def flatten(nodeKey: Option[IArray[Byte]]): LazyList[A] =
    nodeKey match
      case None => LazyList.empty
      case Some(nodeKey) => flatten(nodeKey)

  inline private def newNode[A](x: A): Node[A] = Node(None, x, None)

  given [A]: CanEqual[None.type, Option[A]] = CanEqual.derived

// @main
def test(): Unit =
  import codec.{Codec, given}
  import schema.Product
  val sorted1 = Sorted[Product, Long](_.click)(MemStore())
  sorted1.insert(Product("399", "Single", "tr_TR", 122, 904))
  sorted1.insert(Product("1086", "Woo Album #4", "tr_TR", 203, 606))
  sorted1.insert(Product("1116", "Patient Ninja", "tr_TR", 470, 298))
  sorted1.insert(Product("397", "Polo", "tr_TR", 604, 674))
  sorted1.insert(Product("386", "V-Neck T-Shirt", "tr_TR", 592, 740))
  assert(sorted1.flatten.map(_.item_id) == List("399", "1086", "1116", "386", "397"))
  println("ok")
