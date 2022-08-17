package models

import cats.effect.IO
import doobie.Transactor

class BakeryTransactor {
  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // postgres driver
    "jdbc:postgresql://localhost:5432/pass_bakery_db", // connect URL
    "user",
    "pass"
  )
}
