package codec

import argonaut.*, Argonaut.*

import schema.{Prod, ItemId, get}

given DecodeJson[Prod] = jdecode5L((x1: String, x2: String, x3: String, x4: Long, x5: Long) => Prod(ItemId(x1), x2, x3, x4, x5))("item_id", "name", "locale", "click", "purchase")
given EncodeJson[Prod] = jencode5L((p: Prod) => (p.itemId.get, p.name, p.locale, p.click, p.purchase))("item_id", "name", "locale", "click", "purchase")
