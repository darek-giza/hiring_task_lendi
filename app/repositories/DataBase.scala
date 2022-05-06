package repositories

import com.google.inject.Singleton
import models.{ BankMargin, Config, Settings }

import java.util.concurrent.ConcurrentHashMap

@Singleton
class DataBase {

  val db = new ConcurrentHashMap[String, BankMargin]

  db.put("pkobp", BankMargin(Config(Settings(0, Set.empty, 100), Settings(0, Set.empty, 1000000)), Nil))
}
