


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
	val outerId = "outer"
	// The following specifies a default prefix
	override def defaultPrefix = Full("message") 
	
	// Intial bindings
	def render = {
    bind("message" -> <span id={outerId}>{body}</span>)
	}

  var timestamps = Map[Path,Long]()

  def imgTag(path: Path) = {
    timestamps += (path -> path.lastModified)
    val src = "/api/thumb/" + path.name + "?time=" + path.lastModified
    <img src={src} alt={path.name} title={path.name}/>
  }

  def tagId(path: Path): String = "_img" + path.name.replaceAll("\\.", "_")

  def images = (ImageActor !? Directories).asInstanceOf[List[Path]]

  def body = {
    timestamps = Map()
    ImageServices.imageDir match {
      case Full(dir) =>

        val imagesCells = images map { path => <td id={tagId(path)}>{imgTag(path)}</td> }
        <span>
          <p id="time"></p>
          <table>
          {imagesCells.sliding(4,4) map {row => <tr>{row}</tr>}}
          </table>
        </span>
      case _ =>
      <p><strong>Configuration of the application is not correct.  The imageDir initParam in web.xml needs to be configured</strong></p>
    }
    
  }
	// this is called 10sec after the instance is created
	ActorPing.schedule(this, FindChanges, ImageActor.REFRESH_TIME)

	override def lowPriority: PartialFunction[Any,Unit] = {
		case FindChanges => {
      val all = images
      val remaining = timestamps.filter{all contains _._1}
      val added = all.collect{case path if !timestamps.keySet.contains(path) => imgTag(path)}
      val removed = timestamps -- remaining.keySet

      if(added.nonEmpty || removed.nonEmpty) {
        partialUpdate(SetHtml(outerId, body))
      } else {
        val changes = for {
            (path,lastModified) <- remaining
            if lastModified < path.lastModified
          } yield  {
          val img = imgTag(path)
          partialUpdate(SetHtml(tagId(path), img))
          path.name
        }

        if(changes.nonEmpty) {
          partialUpdate(SetHtml("time", Str("Updated "+changes.mkString(", ")+" at "+timeNow.toString)))
        }
      }
			ActorPing.schedule(this, FindChanges, ImageActor.REFRESH_TIME)
		}
	}
}
case object FindChanges