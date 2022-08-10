package DAO

import javax.inject.Inject
import play.api.db.Database

import scala.concurrent.Future
import models.DatabaseExecutionContext
import play.api.libs.json._

class BakeryDatabase @Inject() (
    db: Database,
    databaseExecutionContext: DatabaseExecutionContext
) {
  def getDatabaseName(): Future[JsValue] = {
    Future {
      db.withConnection { conn =>
        // do something with your db
        val dbName: String = db.name
        Json.parse("""
            {
              "database name" : dbName
            }
            """)
      }
    }(databaseExecutionContext)
  }
}

object BakeryDatabase {
  implicit val dbWrites: OWrites[Future[JsValue]] = Json.writes[Future[JsValue]]
}
