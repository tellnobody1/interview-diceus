package codec

import argonaut.*, Argonaut.*

import schema.Prod

given DecodeJson[Prod] = jdecode5L(Prod.apply)("item_id", "name", "locale", "click", "purchase")
given EncodeJson[Prod] = jencode5L((p: Prod) => (p.item_id, p.name, p.locale, p.click, p.purchase))("item_id", "name", "locale", "click", "purchase")
