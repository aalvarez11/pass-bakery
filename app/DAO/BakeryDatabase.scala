package DAO

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits._
import doobie.util.transactor.Transactor.Aux

import javax.inject.Inject
import play.api.db.Database

import scala.concurrent.Future
import models.DatabaseExecutionContext
import play.api.libs.json.JsValue

class BakeryDatabase @Inject() (
    db: Database,
    implicit val databaseExecutionContext: DatabaseExecutionContext
) {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // postgres driver
    "jdbc:postgresql://localhost:5432/pass_bakery_db", // connect URL
    "user",
    "pass"
  )

  def getDatabaseTables: Future[String] = {
    Future {
      db.withConnection { conn =>
        val result = conn.getMetaData.getTables(
          null,
          null,
          null,
          Array("TABLE")
        )
        var tableListStr = ""

        while (result.next()) {
          val tableName = result.getString("TABLE_NAME")
          if (tableListStr == "") {
            tableListStr += tableName
          } else {
            tableListStr = tableListStr + ", " + tableName
          }
        }
        tableListStr
      }
    }
  }

  def getAllProducts: Future[String] = {
    Future {
      db.withConnection { conn =>
        try {
          val statement = conn.prepareStatement("SELECT * FROM product;")
          val selectResult = statement.executeQuery()
          var returnStr = ""
          var idx = 1
          while (selectResult.next()) {
            returnStr += "Item " + idx + ":: id: " + selectResult.getString(
              "id"
            ) + ", name: " + selectResult.getString(
              "name"
            ) + ", quantity: " + selectResult.getInt(
              "quantity"
            ) + ", price: " + selectResult.getDouble(
              "price"
            ) + ", created at: " + selectResult.getTimestamp(
              "created_at"
            ) + ", updated at: " + selectResult.getTimestamp(
              "updated_at"
            ) + "\n"
            idx += 1
          }
          returnStr
        } catch {
          case e: Exception => e.printStackTrace().toString
        }
      }
    }
  }

  def getProductById(id: String): Future[Option[Product]] = {
    val query =
      sql"""SELECT * FROM product where id::text = $id""".query[Product]
    val action = query.option
    action.transact(xa).unsafeToFuture()
  }
}

case class Product(
    id: String,
    name: String,
    quantity: Int,
    price: Double,
    createdAt: String,
    updatedAt: String
)
