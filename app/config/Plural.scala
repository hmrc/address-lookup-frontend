package config

object Plural {
  def plural(count: Int, singular: String, pluralS: String): String = {
    if (count == 1) singular else pluralS
  }

  def plural(collection: TraversableOnce[_], singular: String, pluralS: String): String = {
    plural(collection.size, singular, pluralS)
  }
}
