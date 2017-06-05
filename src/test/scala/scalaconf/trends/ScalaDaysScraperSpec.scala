package scalaconf.trends

import java.time.ZoneId

import model.Talk
import org.scalatest._

class ScalaDaysScraperSpec extends WordSpec with Matchers {

  "The Scraper for Scala Days" should {

    "should find 50 talks in Scala Days 2017 Copenhagen" in {
      val timeZone       = ZoneId.of("GMT+2")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2017
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2017.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 50
    }

    "should find 50 talks in Scala Days 2017 Chicago" in {
      val timeZone       = ZoneId.of("GMT-5")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2017
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2017Chicago.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 50
    }

    "should find 50 talks in Scala Days 2016 Berlin" in {
      val timeZone       = ZoneId.of("GMT+2")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2017
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2017Chicago.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 50
    }

  }

}
