package Proxy

case class Unmapper(var folders: String, ofType: String, arguments: Map[String, String] = Map()) {

  if (arguments.nonEmpty) for((key,value) <- arguments) folders = folders.replaceAll(":\\s*"+key, ":"+value)

  private val folderList = folders.split("\\.")

  def foldersToWrapper: (String, String) = {
    var left = ""
    for (f <- folderList) left += f + "{\n"
    val right = "}\n" * folderList.length
    (left, right)
  }

  def foldersToValue: String = {
    var value = folderList.last + "\n"
    for (f <- folderList.reverse.drop(1)) value = f + "{\n" + value + "}\n"
    value
  }

}