package Proxy

import io.circe.Json
import io.circe.syntax._

case class Remapper(json: Json, before: String, after: String, returnType: String) {

  private val oldFolders = before.split("\\.")

  val convertRootKey: String = after

  def convertValue: Json = _convertValue(json, oldFolders.drop(1))

  def _convertValue(json: Json, folders: Array[String]): Json = {

    // Value
    if (folders.isEmpty) return json

    // Array
    if (json.isArray) return json.asArray.get.map(j => _convertValue(j, folders.drop(1))).asJson

    // Object
    val child = json.hcursor.downField(folders.head).focus
    if (child.isDefined && child.get.isArray) return child.get.asArray.get.map(j => _convertValue(j, folders.drop(1))).asJson
    else if (child.isDefined) return _convertValue(child.get, folders.drop(1))

    Json.Null
  }

}
