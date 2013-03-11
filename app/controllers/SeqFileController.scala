package controllers;

import play.api.Play._;
import play.api.mvc._;

object SeqFileController extends Controller {
  import org.apache.commons.logging.Log
  import org.apache.hadoop.conf.Configuration
  import org.apache.hadoop.fs.{FileSystem, Path}
  import org.apache.hadoop.io.{BytesWritable, Text, Writable}
  import org.apache.hadoop.io.SequenceFile
  import org.apache.hadoop.io.SequenceFile.{Reader => SFReader}

  val AcceptText = Accepting("text")

  def index(path: String, offset: Option[Long], key: Option[String], limit: Option[Int]) = Action { implicit request =>
    val uri  = configuration.getString("hadoop.uri") // "hadoop.uri=hdfs://hadoop" in conf/application.conf file
    val conf = new Configuration()
    val fs = FileSystem.get(conf)
   
    val fsPath = new Path(if (path.startsWith("/")) path else "/" + path)
    val searchText = new Text(key.getOrElse(""))
    if (fs.exists(fsPath)) {
      val reader = new SFReader(conf, SFReader.file(fsPath))
      if (!offset.isEmpty) reader.sync(offset.get)
      val value = find(reader, searchText, limit.getOrElse(1000))
      val isText = classOf[Text].equals(reader.getValueClass())
      if (value.isEmpty) {
        NotFound("Could not find record for key = " + searchText.toString())
      } else if (isText) {
        Ok(new String(value.get.getBytes, 0, value.get.getLength))
      } else {
        Ok(value.get.copyBytes)
      }
    } else {
      NotFound("Not found: " + fsPath.toString())
    }
  }

  def find(reader: SFReader, searchText: Text, limit: Int): Option[BytesWritable] = {
    val key = new Text()
    val value = getValueInstance(reader.getValueClass)
    var checked: Int = 0
    while (reader.next(key) && checked < limit) {
      if (searchText.equals(key)) {
        reader.getCurrentValue(value)
        return Some(toBytesWritable(value))
      }
      checked += 1
    }
    return None
  }

  def getValueInstance(valueClass: Class[_]): Writable = {
    if (classOf[Text].equals(valueClass)) {
      return new Text()
    } else {
      return new BytesWritable()
    }
  }

  def toBytesWritable(value: Writable) = {
    if (value.isInstanceOf[Text]) {
      val txt = value.asInstanceOf[Text]
      new BytesWritable(txt.getBytes, txt.getLength)
    } else {
      value.asInstanceOf[BytesWritable]
    }
  }
}