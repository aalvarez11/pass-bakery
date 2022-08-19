package DAO

import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import doobie.postgres.pgisimplicits._
import cats._
import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits._

import javax.inject.Inject
import play.api.db.Database

import scala.concurrent.Future
import models.{BakeryTransactor, DatabaseExecutionContext}
import play.api.libs.json._

import java.time.OffsetDateTime

class BakeryDatabase @Inject() (
    db: Database,
    bakeryTransactor: BakeryTransactor,
    implicit val databaseExecutionContext: DatabaseExecutionContext
) {

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
    sql"""SELECT * FROM product where id::text = $id"""
      .query[Product]
      .option
      .transact(bakeryTransactor.xa)
      .unsafeToFuture()
  }

  def createProduct(newProduct: JsValue): Int = {

    /** Steps for creating a record
      *  1. bring in the json, everything should be provided except for the uuid
      *  2. use query.update to commit the record
      *  3. run the transaction
      *  4. return a variable the controller can check to know the transaction succeeded (201 Created)
      */
    val newName = (newProduct \ "name").asOpt[String]
    val newQty = (newProduct \ "quantity").asOpt[Int]
    val newPrice = (newProduct \ "price").asOpt[Double]
    val newStamp = OffsetDateTime.now()
    sql"""INSERT INTO product VALUES (gen_random_uuid(), $newName, $newQty, $newPrice, $newStamp, $newStamp)""".update.run
      .transact(bakeryTransactor.xa)
      .unsafeRunSync()
  }
}

case class Product(
    id: String,
    name: String,
    quantity: Int,
    price: Double,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)

object Product {
  implicit val productWrites: OWrites[Product] = Json.writes[Product]
}
