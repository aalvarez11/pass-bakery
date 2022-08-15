package DAO

import javax.inject.Inject
import play.api.db.Database

import scala.concurrent.Future
import models.DatabaseExecutionContext
import play.api.libs.json.JsValue

class BakeryDatabase @Inject() (
    db: Database,
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

  def addNewProduct(inJson: JsValue): Future[String] = {
    Future {
      db.withConnection { conn =>
        val t = ""

        try {

          /** the following must be done to add a new record/row to a table:
            *    1. establish a connection to the database
            *    2. connect to the table that will be updated (should this be a const val within this class?)
            *    3. parse json object for column values to be inserted in the new row
            *    4. insert the values to the table
            *    4. commit the transaction, rollback if you can't
            */
          val statement =
            conn.prepareStatement("INSERT INTO product() VALUES ()")
          val insertResult = statement.executeQuery()
        } catch {
          case e: Exception => e.printStackTrace().toString
        }

        t
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

  def getProductById(id: String): Future[Option[String]] = {
    Future {
      db.withConnection { conn =>
        try {
          val statement = {
            conn.prepareStatement("SELECT * FROM product WHERE id::text = ?")
          }
          statement.setString(1, id)

          println(statement.toString)
          val selectResult = statement.executeQuery()
          val result = if (selectResult.next()) {
            Some(
              "id: " + selectResult.getString(
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
              )
            )
          } else {
            None
          }
          result
        } catch {
          case e: Exception => Option(e.printStackTrace().toString)
        }
      }
    }
  }
}
