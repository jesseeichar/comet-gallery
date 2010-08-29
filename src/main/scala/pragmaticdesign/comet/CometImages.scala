


package pragmaticdesign.comet

import net.liftweb._
import http._
import SHtml._
import pragmaticdesign.lib.ImageServices
import net.liftweb.common.{Box, Full}
import net.liftweb.util._
import net.liftweb.actor._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds.{SetHtml}
import net.liftweb.http.js.JE.Str
import scalax.io.Path.Matching.File
import scalax.io.Path

class CometImages extends CometActor {
	
	// The following specifies a default prefix
	override def defaultPrefix = Full("message") 
	
	// Intial bindings
	def render = {
    bind("message" -> images)
	}

  val extensions = List("png","jpg","jpeg","bmp","gif","tif","tiff")

  var timestamps = Map[Path,Long]()

  def imgTag(path: Path, timeStamp:Option[String] = None) = {
    timestamps += (path -> path.lastModified)
    val timeParam = (timeStamp.map{t =>"?time=" + path.lastModified}.getOrElse(""))
    val src = "/api/img/" + path.name + timeParam
    <img src={src} alt={path.name} title={path.name}/>
  }

  def tagId(path: Path): String = "_img" + path.name.replaceAll("\\.", "_")

  def images = {
    ImageServices.imageDir match {
      case Full(dir) =>

        val images =
          dir.children().collect {
            case File(path) if path.extension forall {e => extensions contains e} =>
              <td id={tagId(path)}>{imgTag(path)}</td>
          }

        <span>
          <p id="time"></p>
          <table>
          {images.sliding(4) map {row => <tr>{row}</tr>}}
          </table>
        </span>
      case _ =>
      <p><strong>Configuration of the application is not correct.  The imageDir initParam in web.xml needs to be configured</strong></p>
    }
    
  }
	// this is called 10sec after the instance is created
	ActorPing.schedule(this, FindChanges, 500L)

	override def lowPriority: PartialFunction[Any,Unit] = {
		case FindChanges => {
      val changes = for {
          (path,lastModified) <- timestamps
          if lastModified < path.lastModified
        } yield  {
        val img = imgTag(path, Some(timeNow.toString))
			  partialUpdate(SetHtml(tagId(path), img))
        path
      }

      if(changes.nonEmpty) {
        partialUpdate(SetHtml("time", Str("Updated "+changes.mkString(", ")+" at "+timeNow.toString)))
      }
			ActorPing.schedule(this, FindChanges, 500L)
		}
	}
}
case object FindChanges