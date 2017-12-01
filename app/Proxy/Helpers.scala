package Proxy

import io.circe.Json
import io.circe.syntax._
import sangria.ast.{Field, OperationDefinition}

import scala.reflect.runtime.universe._

object Helpers {

  implicit class StringHelper(val string: String) {
    def escape: String = Literal(Constant(string)).toString
  }

  implicit class OperationDefinitionHelper(val op: OperationDefinition) {
    private val topLine = """(?s)(.*?)\s*\{.*""".r
    def renderName: String = {
      op.renderPretty match {
        case topLine(q) => q
        case _ => ""
      }
    }
  }

  implicit class FieldHelper(val field: Field) {
    def hasChildren: Boolean = field.children.nonEmpty
    def children: Vector[Any] = field.selections
  }

  implicit class JsonHelper(val json: Json) {
    def remap(oldRootKey: String, onType: String, mapper: (Json, String, String) => Remapper): (String, Json) = {

      val rmp = mapper(json, oldRootKey, onType)
      val recursor = (f:(String, Json)) => f._2.remap(f._1, rmp.returnType, mapper)
      def recursorIntoArray(arrItem: Json): Json = {
        if (arrItem.isObject) arrItem.asObject.get.toMap.map(recursor).asJson
        else if (arrItem.isArray) arrItem.asArray.get.map(recursorIntoArray).asJson
        else arrItem
      }

      (rmp.convertRootKey, {
        val m = rmp.convertValue
        if (m.isObject) m.asObject.get.toMap.map(recursor).asJson
        else if (m.isArray) m.asArray.get.map(recursorIntoArray).asJson
        else m
      })

    }
  }

}
