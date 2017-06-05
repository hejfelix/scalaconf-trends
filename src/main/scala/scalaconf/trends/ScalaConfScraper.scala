package scalaconf.trends

import java.time.ZoneId

import model.Talk

trait ScalaConfScraper {
  def scrape(htmlPath: String, year: Int, zoneID: ZoneId): Seq[Talk]
}
