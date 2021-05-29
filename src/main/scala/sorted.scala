package sorted

import scala.annotation.tailrec
import scala.math.Ordering
import scala.math.Ordering.Implicits.infixOrderingOps

import schema.Prod

class Sorted[I: Ordering](index: Prod => I)(dba: store.Dba):
  import dba.{Node, Key}

  def insert(x: Prod): Unit =
    dba.key(x) match
      case None =>
      case Some(k) =>
        dba.rem(k)
    dba.get(dba.head) match
      case None => dba.add(toNode(x))
      case Some(node) => insert(x, node, dba.head)

  @tailrec private def insert(x: Prod, node: Node, nodeKey: Key): Unit =
    node match
      case Node(None, y, _, _) if index(x) <= index(y) =>
        val k = dba.add(toNode(x))
        dba.put(nodeKey, node.copy(left=Some(k)))

      case Node(Some(t), y, _, _) if index(x) <= index(y) =>
        dba.get(t) match
          case None =>
            val k = dba.add(toNode(x))
            dba.put(nodeKey, node.copy(left=Some(k)))
          case Some(node1) =>
            insert(x, node1, t)

      case Node(_, _, None, _) =>
        val k = dba.add(toNode(x))
        dba.put(nodeKey, node.copy(right=Some(k)))

      case Node(_, _, Some(s), _) =>
        dba.get(s) match
          case None =>
            val k = dba.add(toNode(x))
            dba.put(nodeKey, node.copy(right=Some(k)))
          case Some(node1) =>
            insert(x, node1, s)

  def flatten: LazyList[Prod] = flatten(dba.head)

  private def flatten(nodeKey: dba.Key): LazyList[Prod] =
    dba.get(nodeKey) match
      case None => LazyList.empty
      case Some(Node(t, x, s, a)) =>
        lazy val xs = if a then LazyList(x) else LazyList.empty
        lazy val ts = t.map(flatten).getOrElse(LazyList.empty)
        lazy val ss = s.map(flatten).getOrElse(LazyList.empty)
        ts #::: xs #::: ss

  inline private def toNode(x: Prod): Node = Node(None, x, None, true)

  given [A]: CanEqual[None.type, Option[A]] = CanEqual.derived

// @main
def test(): Unit =
  import schema.{Prod, ItemId, get}
  import store.MemStore
  val sorted1 = Sorted[Long](_.click)(MemStore())
  sorted1.insert(Prod(ItemId("399"), "Single", "tr_TR", 122, 904))
  sorted1.insert(Prod(ItemId("1086"), "Woo Album #4", "tr_TR", 203, 606))
  sorted1.insert(Prod(ItemId("1116"), "Patient Ninja", "tr_TR", 470, 298))
  sorted1.insert(Prod(ItemId("397"), "Polo", "tr_TR", 604, 674))
  sorted1.insert(Prod(ItemId("386"), "V-Neck T-Shirt", "tr_TR", 592, 740))
  assert(sorted1.flatten.map(_.itemId.get).toList == List("399", "1086", "1116", "386", "397"))
  sorted1.insert(Prod(ItemId("1086"), "Woo Album #4", "tr_TR", 1203, 606))
  assert(sorted1.flatten.map(_.itemId.get).toList == List("399", "1116", "386", "397", "1086"))
  println("OK.")
