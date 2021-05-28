package sorted

import store.Store

type Key = store.Key
case class Node[A](left: Option[Key], x: A, right: Option[Key])

inline def newNode[A](x: A): Node[A] = Node(None, x, None)

class Sorted[A](store: Store[Node[A]])(index: A => Int):

  def insert(x: A): Unit =
    store.get(store.head) match
      case None => store.add(newNode(x))
      case Some(node) => insert(x, node, store.head)

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

  given [A]: CanEqual[None.type, Option[A]] = CanEqual.derived