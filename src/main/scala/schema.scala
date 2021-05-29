package schema

case class Prod
  ( itemId: ItemId
  , name: String
  , locale: String
  , click: Long
  , purchase: Long
  )

opaque type ItemId = String

object ItemId:
  def apply(x: String): ItemId = x

extension (x: ItemId)
  def get: String = x
