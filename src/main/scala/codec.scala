package codec

trait Codec[A]:
  def encode(x: A): IArray[Byte]
  def decode(x: IArray[Byte]): A
