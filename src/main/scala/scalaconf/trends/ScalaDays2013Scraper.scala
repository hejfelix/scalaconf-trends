package scalaconf.trends
import java.time.{Instant, ZoneId, ZonedDateTime}

import model.Talk
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

import scala.collection.immutable

class ScalaDays2013Scraper extends ScalaConfScraper {

  override def scrape(htmlPath: String, year: Int, zoneID: ZoneId): Seq[Talk] = {
    val doc                = process(htmlPath)
    val days: Seq[Element] = findDays(doc)
    days.flatMap(talksOfDay(year, zoneID))
  }

  private def talksOfDay(year: Int, zoneID: ZoneId)(day: Element) = {
    val daySplit   = day.attr("data-day").split("-")
    val monthAsInt = daySplit.headOption.flatMap(monthToInt).getOrElse(-1)
    val dayOfMonth = daySplit.drop(1).headOption.map(_.toInt).getOrElse(-1)

    val talks: immutable.Seq[Element] = (day >> elementList(".keynote")) ++ (day >> elementList(
      ".track"))

    talks
      .map(elem => {
        val title               = (elem >?> text("h2"))
        val speaker             = (elem >?> text(".speaker"))
        val twitter             = (elem >?> text(".twitter-follow-button"))
        val date: ZonedDateTime = timeOfTalk(year, zoneID, monthAsInt, dayOfMonth, elem)
        Talk(date.toInstant, speaker.mkString, None, title.mkString, twitter)
      })
      .distinct
      .filter(_.speaker.nonEmpty)
  }

  private def timeOfTalk(year: Int,
                         zoneID: ZoneId,
                         monthAsInt: Int,
                         dayOfMonth: Int,
                         elem: Element) = {
    val timeSplit =
      (elem >?> attr("data-time")).getOrElse("00:00").takeWhile(_ != '-').split(":")
    val hours   = timeSplit.headOption.map(_.toInt).getOrElse(-1)
    val minutes = timeSplit.drop(1).headOption.map(_.toInt).getOrElse(-1)
    val date    = ZonedDateTime.of(year, monthAsInt, dayOfMonth, hours, minutes, 0, 0, zoneID)
    date
  }

  private def monthToInt(month: String) =
    List(
      "january",
      "february",
      "march",
      "april",
      "may",
      "june",
      "july",
      "august",
      "september",
      "october",
      "november",
      "december"
    ).zipWithIndex
      .map {
        case (str, index) => (str, index + 1)
      }
      .toMap
      .get(month)

  private def findDays(doc: Browser#DocumentType): Seq[Element] =
    doc >> elementList(".day")

  private def process(htmlPath: String): Browser#DocumentType =
    JsoupBrowser().parseFile(htmlPath)

}
