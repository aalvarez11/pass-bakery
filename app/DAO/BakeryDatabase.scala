package DAO

import javax.inject.Inject
import play.api.db.Database

import scala.concurrent.Future
import models.DatabaseExecutionContext
import play.api.libs.json._

import java.sql.ResultSet

class BakeryDatabase @Inject() (
    db: Database,
    databaseExecutionContext: DatabaseExecutionContext
) {

  def getDatabaseName: Future[String] = {
    Future {
      db.withConnection { conn =>
        // do something with your db
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
    }(databaseExecutionContext)
  }
}
