package scalaconf.trends

import model.Talk
import org.scalatest._

class ScalaDaysScraperSpec extends FlatSpec with Matchers {

  "The Scraper for Scala Days" should "should find 50 talks in Scala Days 2017 Copenhagen" in {
    val scraper          = new ScalaDaysScraper()
    val yearOfTestFile   = 2017
    val talks: Seq[Talk] = scraper.scrape("src/test/resources/ScalaDays2017.html", yearOfTestFile)
    println(talks.mkString("\n"))
    talks should have size 50
  }

}
