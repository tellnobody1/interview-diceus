package store

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import codec.Codec

type Bytes = IArray[Byte]
type Key = Bytes

class Store[V](using vc: Codec[V]):
  private val next = AtomicInteger(0)
  private val storage = ConcurrentHashMap[Bytes, Bytes]()
  private def i2b(i: Int): Bytes = ???

  def add(v: V): Key =
    val k = i2b(next.incrementAndGet())
    put(k, v)
    k

  def put(k: Key, v: V): Unit =
    storage.put(k, vc.encode(v))

  def get(k: Key): Option[V] =
    val x = storage.get(k)
    if x == null then None
    else Some(vc.decode(x))

  val head: Key = i2b(1)

  given [A]: CanEqual[A, A | Null] = CanEqual.derived
