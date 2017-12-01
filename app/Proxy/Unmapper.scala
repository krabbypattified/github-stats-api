package Proxy

case class Unmapper(var folders: String, ofType: String, arguments: Map[String, String] = Map(), fragment: Boolean = false) {

  if (arguments.nonEmpty) for((key,value) <- arguments) folders = (":\\s*"+key).r.replaceAllIn(folders, ":"+value.replace("$","\\$"))

  private val folderList = folders.split("\\.")

  def inlineFragment: (String, String) = (s"... on $ofType {\n", "}\n")

  def foldersToWrapper: (String, String) = {
    var left = ""
    for (f <- folderList) left += f + "{\n"
    if (fragment) left += inlineFragment._1
    var right = "}\n" * folderList.length
    if (fragment) right += inlineFragment._2
    (left, right)
  }

  def foldersToValue: String = {
    var value = folderList.last + "\n"
    if (fragment) value = inlineFragment._1 + value + inlineFragment._2
    for (f <- folderList.reverse.drop(1)) value = f + "{\n" + value + "}\n"
    value
  }

}