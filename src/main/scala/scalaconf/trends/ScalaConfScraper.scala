package scalaconf.trends

import model.Talk

trait ScalaConfScraper {
  def scrape(htmlPath: String, year: Int): Seq[Talk]
}
