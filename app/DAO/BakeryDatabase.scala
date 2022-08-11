package DAO

import javax.inject.Inject
import play.api.db.Database

import scala.concurrent.Future
import models.DatabaseExecutionContext

class BakeryDatabase @Inject() (
    db: Database,
    implicit val databaseExecutionContext: DatabaseExecutionContext
) {

  def getDatabaseTables: Future[String] = {
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
    }
  }
}
