package model

object MessageConstants {

  sealed trait MessageConstants {
    def home: String
  }

  object EnglishMessageConstants extends MessageConstants {
    val home = "Home"
  }

  object WelshMessageConstants extends MessageConstants {
    val home = "Cartref"
  }

}
