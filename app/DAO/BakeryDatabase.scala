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
import controllers.ProductUpdateRequest

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
    sql"""SELECT * FROM product WHERE id::text = $id"""
      .query[Product]
      .option
      .transact(bakeryTransactor.xa)
      .unsafeToFuture()
  }

  def createProduct(newProduct: ProductUpdateRequest): Int = {

    /** Steps for creating a record
      *  1. bring in the json, everything should be provided except for the uuid
      *  2. use query.update to commit the record
      *  3. run the transaction
      *  4. return a variable the controller can check to know the transaction succeeded (201 Created)
      */
    val newStamp = OffsetDateTime.now()
    (newProduct.name, newProduct.quantity, newProduct.price) match {
      case (Some(name), Some(quantity), Some(price)) =>
        sql"""INSERT INTO product VALUES (gen_random_uuid(), $name, $quantity, $price, $newStamp, $newStamp)""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case _ => 0
    }
  }

  def updateProduct(id: String, changesProduct: ProductUpdateRequest): Int = {

    /** Steps for updating a record
      *  1. bring in the json, json should be the values to change (8 combinations):
      *     - none, name, qty, price, name+qty, name+price, qty+price, name+qty+price
      *  2. check what values are given to update, form query on these params
      *  3. query with .update on the given id
      *  4. run the transaction
      */
    val newName = changesProduct.name
    val newQty = changesProduct.quantity
    val newPrice = changesProduct.price
    val newStamp = OffsetDateTime.now()

    (newName, newQty, newPrice) match {
      case (Some(name), Some(quantity), Some(price)) =>
        sql"""UPDATE product SET name = $name, quantity = $quantity, price = $price, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (Some(name), Some(quantity), None) =>
        sql"""UPDATE product SET name = $name, quantity = $quantity, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (Some(name), None, Some(price)) =>
        sql"""UPDATE product SET name = $name, price = $price, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (None, Some(quantity), Some(price)) =>
        sql"""UPDATE product SET quantity = $quantity, price = $price, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (Some(name), None, None) =>
        sql"""UPDATE product SET name = $name, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (None, Some(quantity), None) =>
        sql"""UPDATE product SET quantity = $quantity, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (None, None, Some(price)) =>
        sql"""UPDATE product SET price = $price, updated_at = $newStamp WHERE id::text = $id""".update.run
          .transact(bakeryTransactor.xa)
          .unsafeRunSync()
      case (None, None, None) => 0
    }
  }

  def deleteProduct(id: String): Int = {
    sql"""DELETE FROM product WHERE id::text = $id""".update.run
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
