package com.snapswap.telesign.unmarshaller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.snapswap.telesign.model.internal.{Error, Risk, Status}
import spray.json._

trait CommonJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  protected def enumNameFormat(enum: Enumeration) = new RootJsonFormat[enum.Value] {
    def read(value: JsValue): enum.Value = value match {
      case JsString(s) => enum.withName(s)
      case x => deserializationError("Expected Enum as JsString, but got " + x)
    }

    def write(v: enum.Value): JsValue = JsString(v.toString)
  }

  implicit val errorFormat = jsonFormat2(Error)

  implicit val statusFormat = jsonFormat(Status.apply, "updated_on", "code", "description")

  implicit val riskFormat = jsonFormat3(Risk)
}