package controllers

import DAO.BakeryDatabase
import models.{BakeryTransactor, DatabaseExecutionContext}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Environment
import play.api.test._
import play.api.test.Helpers._
import services.StatusInfoService

import javax.inject.Inject

class APIControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  "APIController GET" should {

    "return the status of current environment" in {
      val controller = inject[APIController]
      val bakeryStatus = {
        controller.getStatus().apply(FakeRequest(GET, "/pass-bakery/status"))
      }
      status(bakeryStatus) mustBe OK
      contentAsString(bakeryStatus) must include("pass-bakery")
    }

    "return the json of the specified product" in {
      val controller = inject[APIController]
      val productResult = controller
        .getProduct("e69cf7fa-7b16-4636-a859-10d4675cfcc6")
        .apply(FakeRequest(GET, "/rest/bakery/product/:id"))
      status(productResult) mustBe OK
    }
  }
}
