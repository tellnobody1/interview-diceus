package codec

trait Codec[A]:
  def encode(x: A): IArray[Byte]
  def decode(x: IArray[Byte]): A
end Codec

import sorted.Node
import schema.Product
import argonaut.*, Argonaut.*

given Codec[Node[Product]] = new Codec[Node[Product]] {
  import proto.*
  given MessageCodec[Product] = caseCodecIdx
  given MessageCodec[Node[Product]] = caseCodecIdx
  def encode(x: Node[Product]): IArray[Byte] = encodeI(x)
  def decode(x: IArray[Byte]): Node[Product] = decodeI(x)
}

given DecodeJson[Product] = jdecode5L(Product.apply)("item_id", "name", "locale", "click", "purchase")
given EncodeJson[Product] = jencode5L((p: Product) => (p.item_id, p.name, p.locale, p.click, p.purchase))("item_id", "name", "locale", "click", "purchase")
