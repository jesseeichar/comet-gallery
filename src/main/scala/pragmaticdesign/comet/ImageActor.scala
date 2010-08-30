package pragmaticdesign.comet

import net.liftweb.actor.LiftActor
import scalax.io.Path
import scalax.io.Path.Matching.File
import pragmaticdesign.lib.ImageServices
import net.liftweb.common.{Full, Box}

/**
 * 
 * User: jeichar
 * Date: Aug 29, 2010
 * Time: 9:01:17 PM
 */

object ImageActor extends LiftActor {
  private val REFRESH_TIME = 500
  private val EXTENSIONS = List("png","jpg","jpeg","bmp","gif","tif","tiff")

  private var images = List[Path]()
  private var lastUpdate = 0L

  protected def messageHandler = {
    case Directories =>
      if(lastUpdate < (System.currentTimeMillis - REFRESH_TIME)) {
        images = ImageServices.imageDir match {
          case Full(dir) => dir.children().filter { p => p.isFile && (p.extension forall {e => EXTENSIONS contains e}) } toList
          case _ => Nil
      }
      }
      reply (images)
      
  }

}

case object Directories