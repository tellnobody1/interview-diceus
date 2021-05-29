package store

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.Arrays
import codec.Codec

/* Database API */
trait Dba[V] {
  def add(v: V): IArray[Byte]
  def put(k: IArray[Byte], v: V): Unit
  def get(k: IArray[Byte]): Option[V]
  val head: IArray[Byte]
}

class MemStore[V](using vc: Codec[V]) extends Dba[V]:
  private val next = AtomicInteger(0)
  private val storage = ConcurrentHashMap[Bytes, IArray[Byte]]()
  private val index = ConcurrentHashMap[Bytes, Bytes]()

  def add(v: V): IArray[Byte] =
    val k = i2b(next.incrementAndGet())
    put(k, v)
    k

  def put(k: IArray[Byte], v: V): Unit =
    storage.put(Bytes(k), vc.encode(v))
    index.put(Bytes(vc.encode(v)), Bytes(k))

  def get(k: IArray[Byte]): Option[V] =
    val x = storage.get(Bytes(k))
    if x == null then None
    else Some(vc.decode(x))

  val head: IArray[Byte] = i2b(1)

  private def i2b(i: Int): IArray[Byte] = IArray.unsafeFromArray(BigInt(i).toByteArray)

  given [A]: CanEqual[A, A | Null] = CanEqual.derived
end MemStore

class Bytes(val array: IArray[Byte]):
  override def equals(other: Any): Boolean =
    if other.isInstanceOf[Bytes] then
      val o = other.asInstanceOf[Bytes]
      Arrays.equals(array.toArray: Array[Byte], o.array.toArray: Array[Byte])
    else false
  override def hashCode(): Int = Arrays.hashCode(array.toArray: Array[Byte])
end Bytes
