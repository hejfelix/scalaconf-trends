package scalaconf.trends

import model.Talk
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

class ScalaDaysScraper extends ScalaConfScraper {

  private def process(htmlPath: String): Browser#DocumentType =
    JsoupBrowser().parseFile("src/test/resources/ScalaDays2017.html")

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

  private def elementToTalk(year: Int, month: Int, day: Int, timeOfDay: String)(element: Element) = {
    val time    = s"$timeOfDay-$day-$month-$year"
    val speaker = element >?> text(".speaker")
    val twitter = element >?> text(".twitter")
    val company = element >?> text(".speakercompany")
    val subject = element >> text(".subject")
    Talk(time, speaker.mkString, company, subject, twitter)
  }

  private def timeOfSlot(slot: Element) = slot >> text(".time")

  private def slotsForSchedule(schedule: Element): Seq[Element] = schedule >> elementList(".slots")

  def scrape(htmlPath: String, year: Int): Seq[Talk] =
    findDays(process(htmlPath)).flatMap(talksForSchedule(year))

  private def talksForSchedule(year: Int)(schedule: Element) = {
    val caption = schedule >> text("caption")
    val month   = monthOfCaption(caption).getOrElse(-1)
    val day     = dayOfCaption(caption)
    val slots   = slotsForSchedule(schedule)
    slots.flatMap(slot =>
      findTalks(slot).map(elementToTalk(year, month, day, timeOfSlot(slot).toString)))
  }
}
