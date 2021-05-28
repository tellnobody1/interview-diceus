package store

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.Arrays
import codec.Codec

type Key = IArray[Byte]

class Store[V](using vc: Codec[V]):
  private val next = AtomicInteger(0)
  private val storage = ConcurrentHashMap[Bytes, IArray[Byte]]()
  private def i2b(i: Int): Key = IArray.unsafeFromArray(BigInt(i).toByteArray)

  def add(v: V): Key =
    val k = i2b(next.incrementAndGet())
    put(k, v)
    k

  def put(k: Key, v: V): Unit =
    storage.put(Bytes(k), vc.encode(v))

  def get(k: Key): Option[V] =
    val x = storage.get(Bytes(k))
    if x == null then None
    else Some(vc.decode(x))

  val head: Key = i2b(1)

  given [A]: CanEqual[A, A | Null] = CanEqual.derived

class Bytes(val array: IArray[Byte]) {
  override def equals(other: Any): Boolean =
    if other.isInstanceOf[Bytes] then
      val o = other.asInstanceOf[Bytes]
      Arrays.equals(array.toArray: Array[Byte], o.array.toArray: Array[Byte])
    else false
  override def hashCode(): Int = Arrays.hashCode(array.toArray: Array[Byte])
}
