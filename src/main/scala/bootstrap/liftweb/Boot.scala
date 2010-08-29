

package bootstrap.liftweb

import _root_.net.liftweb.sitemap.{SiteMap, Menu, Loc}
import pragmaticdesign.lib.ImageServices
import net.liftweb.common.Full
import net.liftweb.http.{Req, LiftRules}

class Boot {
  def boot {

    println("initParams", LiftRules.context.attributes)
    LiftRules.dispatch.append(ImageServices.dispatchMatcher) 

    val defaultDetermineContentType = LiftRules.determineContentType

    LiftRules.determineContentType = {
      case (Full(Req(List("api", "img", _),mime,_)),_) => ""
      case a => "text/html; charset=utf-8"//defaultDetermineContentType(a)
    }

    LiftRules.determineContentType.orElse(defaultDetermineContentType)

    // where to search snippet
    LiftRules.addToPackages("pragmaticdesign")

    // build sitemap
    val entries = List(Menu("Home") / "index") :::
                  Nil
                  
    LiftRules.setSiteMap(SiteMap(entries:_*))

    // set character encoding
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    
  }
}