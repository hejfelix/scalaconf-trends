package scalaconf.trends

import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import java.util.{Calendar, Date}

import model.Talk
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

class ScalaDaysScraper extends ScalaConfScraper {

  private def process(htmlPath: String): Browser#DocumentType =
    JsoupBrowser().parseFile(htmlPath)

  private def findTalks(element: Element): Seq[Element] =
    element >> elementList(".scheduleClickToPop")

  private def findDays(doc: Browser#DocumentType): Seq[Element] =
    doc >> elementList(".schedule")

  private def dayOfCaption(caption: String): Int =
    caption
      .dropWhile(!_.isDigit)
      .takeWhile(_.isDigit)
      .toInt

  private def monthOfCaption(caption: String): Option[Int] =
    caption
      .split(" ")
      .drop(2)
      .headOption
      .map(_.takeWhile(_ != '.').toLowerCase)
      .flatMap(monthToInt)

  private def monthToInt(month: String) =
    List(
      "jan",
      "feb",
      "mar",
      "apr",
      "may",
      "jun",
      "jul",
      "aug",
      "sep",
      "oct",
      "nov",
      "dec"
    ).zipWithIndex
      .map {
        case (str, index) => (str, index + 1)
      }
      .toMap
      .get(month)

  private def elementToTalk(time: Instant)(element: Element) = {
    val speaker = element >?> text(".speaker")
    val twitter = (element >?> text(".twitter")).map(handle =>
      if (!handle.startsWith("@")) s"@$handle" else handle)
    val company = (element >?> text(".speakercompany")).filter(_.nonEmpty)
    val subject = element >> text(".subject")
    Talk(time, speaker.mkString, company, subject, twitter)
  }

  private def timeOfSlot(year: Int, month: Int, day: Int, zoneID: ZoneId)(slot: Element) = {
    val timeOfDay = slot >> text(".time")
    val timeSplit = timeOfDay.split(":")
    val hours     = timeSplit.headOption.map(_.toInt).getOrElse(-1)
    val minutes   = timeSplit.drop(1).headOption.map(_.toInt).getOrElse(-1)
    val date      = ZonedDateTime.of(year, month, day, hours, minutes, 0, 0, zoneID)
    date.toInstant
  }

  private def slotsForSchedule(schedule: Element): Seq[Element] = schedule >> elementList(".slots")

  def scrape(htmlPath: String, year: Int, zoneID: ZoneId): Seq[Talk] =
    findDays(process(htmlPath)).flatMap(talksForSchedule(year, zoneID))

  private def talksForSchedule(year: Int, zoneID: ZoneId)(schedule: Element) = {
    val caption        = schedule >> text("caption")
    val month          = monthOfCaption(caption).getOrElse(-1)
    val day            = dayOfCaption(caption)
    val slots          = slotsForSchedule(schedule)
    val slotTimeOnDate = timeOfSlot(year, month, day, zoneID) _
    slots.flatMap(slot => findTalks(slot).map(elementToTalk(slotTimeOnDate(slot))))
  }
}
