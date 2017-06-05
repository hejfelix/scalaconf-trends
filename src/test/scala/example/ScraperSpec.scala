package example

import model.Talk
import org.scalatest._

class ScraperSpec extends FlatSpec with Matchers {

  "The Hello object" should "say hello" in {
    val scraper = new Scraper()
    val schedules: Seq[Talk] = scraper.scrape("src/test/resources/ScalaDays2017.html", 2017)


      println(schedules.mkString("\n"))


    1 shouldEqual 1
  }

}
