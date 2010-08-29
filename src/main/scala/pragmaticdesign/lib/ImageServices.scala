package pragmaticdesign.lib

import net.liftweb.http._
import net.liftweb.http.rest._
import scalax.io.Path
import scalax.io.resource.Resource
import net.liftweb.util.Props
import net.liftweb.common._
import java.io.OutputStream

object ImageServices {
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
    case r @ Req(List("api", "img", name), ext, GetRequest) => () => servImage(name, ext)
  }
  def servImage(name: String, ext: String): Box[LiftResponse] = {
    imageDir map {imgDir =>
      val img = imgDir \ (name+"."+ext)
      val mimetype = ("image/"+ext)
      val headers =("Content-Type" -> mimetype) :: Nil

      val imageResource = img.ops.inputStream
      val pump = (out:OutputStream) => {
        Resource.fromOutputStream(out).writeInts(imageResource.bytesAsInts)
      }
      OutputStreamResponse(pump,img.length, headers)
    }
  }

}