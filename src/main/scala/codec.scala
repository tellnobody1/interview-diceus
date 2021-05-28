package codec

trait Codec[A]:
  def encode(x: A): IArray[Byte]
  def decode(x: IArray[Byte]): A

import sorted.Node

given Codec[Node[String]] = new Codec[Node[String]] {
  import proto.*
  given MessageCodec[Node[String]] = caseCodecIdx
  def encode(x: Node[String]): IArray[Byte] = encodeI(x)
  def decode(x: IArray[Byte]): Node[String] = decodeI(x)
}
