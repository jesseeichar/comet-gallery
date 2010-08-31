package pragmaticdesign.lib

import net.liftweb.http._
import net.liftweb.http.rest._
import scalax.io.Path
import net.liftweb.util.Props
import net.liftweb.common._
import net.liftweb.imaging.ImageResizer._
import net.liftweb.imaging.ImageOutFormat._
import java.io.OutputStream
import scalax.io.resource.{InputStreamResource, Resource}

object ImageServices {
  val thumbSize=256
  lazy val imageDir = {
    val dir = Props.get("imageDir") map Path.apply
    assert(dir.isDefined, "image dir property must be defined in a default.prop (or other) properties file")
    dir.foreach { dir =>
      assert(dir.exists, "the image dir: "+dir+" does not exist")
      assert(dir.isDirectory, "the image dir: "+dir+" must be a directory")
    }

    dir
  }

  def dispatchMatcher: LiftRules.DispatchPF = {
    case Req(List("api", "img", name), ext, GetRequest) => () => servImage(name, ext, false)
    case Req(List("api", "thumb", name), ext, GetRequest) => () => servImage(name, ext, true)
  }
  def servImage(name: String, ext: String, thumbnail:Boolean): Box[LiftResponse] = {
    imageDir map {imgDir =>
      val img = imgDir \ (name+"."+ext)
      val mimetype = if(thumbnail) "image/png" else "image/"+ext
      val headers =("Content-Type" -> mimetype) :: Nil

      val imageResource = if(thumbnail) {
        img.ops.inputStream.acquireAndGet{in =>
          val image = getImageFromStream(in).image
          val resized = max(None, image, thumbSize, thumbSize)
          Resource.fromInputStream(imageToStream(png,resized))
        }
      } else {
        img.ops.inputStream
      }

      val pump = (out:OutputStream) => {
        Resource.fromOutputStream(out).writeInts(imageResource.bytesAsInts)
      }
      OutputStreamResponse(pump,headers)
    }
  }

}