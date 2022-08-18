package models

import cats.effect.IO
import doobie.Transactor
import play.api.Configuration

import javax.inject.Inject

class BakeryTransactor @Inject() (configuration: Configuration) {
  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    configuration.get[String]("db.default.driver"), // postgres driver
    configuration.get[String]("db.default.url"), // connect URL
    configuration.get[String]("db.default.username"), // db username
    configuration.get[String]("db.default.password") // db password
  )
}
