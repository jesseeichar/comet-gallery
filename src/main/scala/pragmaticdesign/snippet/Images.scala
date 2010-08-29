


package pragmaticdesign.snippet

import _root_.pragmaticdesign.lib.ImageServices
import xml.{Text, NodeSeq}
import net.liftweb.util.Helpers._
import net.liftweb.http.S._
import net.liftweb.http.SHtml._
import net.liftweb.common._

import scalax.io._ 
import scalax.io.Path.Matching._

class Images {

  val extensions = List("png","jpg","jpeg","bmp","gif","tif","tiff")

	def render(xhtml: NodeSeq): NodeSeq = {
    ImageServices.imageDir match {
      case Full(dir) =>
        require(dir.exists, "the "+dir+" does not exist")
        require(dir.isDirectory, "the "+dir+" must be a directory")
        val images =
          dir.children().collect {
            case File(path) if path.extension forall {e => extensions contains e} =>
              <tr><td><img src={"/api/img/"+path.name} alt={path.name} title={path.name}/></td></tr>
          }

        <span>
          <p>count:{images.size}</p>
          <table>
          {images}
          </table>
        </span>
      case _ =>
      <p><strong>Configuration of the application is not correct.  The imageDir initParam in web.xml needs to be configured</strong></p>
    }
	}

}