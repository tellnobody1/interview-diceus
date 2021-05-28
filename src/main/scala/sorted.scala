package sorted

import store.Store
import scala.annotation.tailrec

type Key = store.Key
case class Node[A](left: Option[Key], x: A, right: Option[Key])

class Sorted[A](store: Store[Node[A]])(index: A => Int):

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
  val sorted = Sorted[String](Store[Node[String]]())(_.toInt)
  assert(sorted.flatten == Nil)
  sorted.insert("5")
  assert(sorted.flatten == List("5"))
  sorted.insert("7")
  sorted.insert("4")
  sorted.insert("6")
  assert(sorted.flatten == List("4", "5", "6", "7"))
  println("ok")
