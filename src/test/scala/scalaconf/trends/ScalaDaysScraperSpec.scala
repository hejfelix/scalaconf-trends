package scalaconf.trends

import java.time.ZoneId

import model.Talk
import org.scalatest._

import scala.collection.immutable
import scala.io.Source

//noinspection ScalaStyle
class ScalaDaysScraperSpec extends WordSpec with Matchers {

  "The Scraper for Scala Days" should {

    "find 50 talks in Scala Days 2017 Copenhagen" in {
      val timeZone       = ZoneId.of("GMT+2")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2017
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2017.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 50
    }

    "find 50 talks in Scala Days 2017 Chicago" in {
      val timeZone       = ZoneId.of("GMT-5")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2016
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2017Chicago.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 50
    }

    "find 50 talks in Scala Days 2016 Berlin" in {
      val timeZone       = ZoneId.of("GMT+2")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2017
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2016Berlin.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 50
    }

    "find 53 talks in Scala Days 2015 San Francisco" in {
      val timeZone       = ZoneId.of("GMT-7")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2015
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2015SanFran.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 53
    }

    "find 54 talks in Scala Days 2015 Amsterdam" in {
      val timeZone       = ZoneId.of("GMT+2")
      val scraper        = new ScalaDaysScraper()
      val yearOfTestFile = 2015
      val talks: Seq[Talk] =
        scraper.scrape("src/test/resources/ScalaDays2015Amsterdam.html", yearOfTestFile, timeZone)
      println(talks.mkString("\n"))
      talks should have size 54
    }

  }

  "extra" should {
    "word count" in {
      case class Resource(year: Int, path: String, timeZone: ZoneId)

      val resources = List(
        Resource(2017, "ScalaDays2017", ZoneId.of("GMT+2")),
        Resource(2017, "ScalaDays2017Chicago", ZoneId.of("GMT-5")),
        Resource(2016, "ScalaDays2016Berlin", ZoneId.of("GMT+2")),
        Resource(2015, "ScalaDays2015Amsterdam", ZoneId.of("GMT+2")),
        Resource(2015, "ScalaDays2015SanFran", ZoneId.of("GMT-7"))
      )

      val scraper = new ScalaDaysScraper()
      val talks: Seq[Talk] = resources.flatMap(r =>
        scraper.scrape(s"src/test/resources/${r.path}.html", r.year, r.timeZone))

      val maxEntries                                = 20
      val top20Words: Seq[String]                   = topTitleWords(maxEntries)(talks)
      val top20SpeakersByNumber: Seq[(String, Int)] = topSpeakersByNumber(maxEntries)(talks)

      println("Top 20 speakers by number of talks:")
      println(
        top20SpeakersByNumber.zipWithIndex
          .map {
            case ((speaker, numTalks), index) => s"${index + 1}: $speaker, with $numTalks talks"
          }
          .mkString("\n")
      )

      println("Top 20 title topics:")
      println(
        top20Words.zipWithIndex.map { case (word, index) => s"${index + 1}: $word" }.mkString("\n")
      )

      top20Words shouldEqual top20Words.distinct
      top20SpeakersByNumber shouldEqual top20SpeakersByNumber.distinct
    }
  }

  private def topSpeakersByNumber(max: Int)(talks: Seq[Talk]) =
    talks
      .map(_.speaker)
      .filter(!_.isEmpty)
      .groupBy(identity)
      .mapValues(_.size)
      .toSeq
      .sortBy {
        case (speaker, numTalks) => -numTalks
      }
      .take(max)

  private def topTitleWords(max: Int)(talks: Seq[Talk]) = {
    val ignoreWords: Seq[String] =
      Source
        .fromFile("src/test/resources/stopwords.lsv")
        .getLines()
        .map(_.trim.toLowerCase)
        .toSeq ++ Seq("scala", "programming", "-")

    val words: Map[String, Int] = talks
      .flatMap(_.subject.split("\\s+").map(_.toLowerCase))
      .filter(!ignoreWords.contains(_))
      .groupBy(identity)
      .mapValues(_.size)

    val sortedByOccurrence =
      words.toSeq
        .sortBy {
          case (_, count) => count
        }

    sortedByOccurrence.reverse.take(max).map { case (word, _) => word }
  }
}
