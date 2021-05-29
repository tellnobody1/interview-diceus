package store

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.Arrays

import schema.{Prod, ItemId}

/* Database API */
trait Dba:
  type Key = Int
  
  case class Node(left: Option[Key], x: Prod, right: Option[Key], active: Boolean)
  
  def add(v: Node): Key
  def put(k: Key, v: Node): Unit
  def get(k: Key): Option[Node]
  def head: Key
  def key(v: Prod): Option[Key]
  def rem(k: Key): Unit
end Dba

class MemStore extends Dba:
  private val next = AtomicInteger(0)
  private val db = ConcurrentHashMap[Key, Node]()
  private val index = ConcurrentHashMap[ItemId, Key]()

  def add(v: Node): Key =
    val k = next.incrementAndGet()
    put(k, v)
    k

  def put(k: Key, v: Node): Unit =
    db.put(k, v)
    index.put(v.x.itemId, k)

  def get(k: Key): Option[Node] =
    Option(db.get(k)).map(_.nn)

  def head: Key = 1

  def key(v: Prod): Option[Key] =
    Option(index.get(v.itemId)).map(_.nn)

  def rem(k: Key): Unit =
    get(k) match
      case Some(v) if v.active =>
        db.put(k, v.copy(active=false))
      case _ =>

  given [A]: CanEqual[A, A | Null] = CanEqual.derived
end MemStore
