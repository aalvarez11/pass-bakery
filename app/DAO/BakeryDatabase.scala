package DAO

import javax.inject.Inject
import play.api.db.Database
import scala.concurrent.Future
import models.DatabaseExecutionContext

class BakeryDatabase @Inject() (
    db: Database,
    databaseExecutionContext: DatabaseExecutionContext
) {
  def updateSomething(): Unit = {
    Future {
      db.withConnection { conn =>
        // do something with your db
        db.name
      }
    }(databaseExecutionContext)
  }
}
