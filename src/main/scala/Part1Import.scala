import scala.io.Source
import java.time.{LocalDate, ZoneOffset}
import java.util.Date
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import org.bson.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.model.Indexes
import io.github.cdimascio.dotenv.Dotenv
import scala.concurrent.Await
import scala.concurrent.duration._

object Part1Import {
  def main(args: Array[String]): Unit = {

    // Part 1.1: Read CSV and convert records to MongoDB documents
    val source = Source.fromFile("data/hw2-data.csv")

    val students = try {
      val lines = source.getLines().drop(1).toList

      lines.map { line =>
        val parts = line.split(",", 5)

        val id = parts(0).trim.toInt
        val firstName = parts(1).trim
        val lastName = parts(2).trim
        val birthDate = LocalDate.parse(parts(3).trim)

        // Extract course IDs and GPA
        val remaining = parts(4)
        val lastComma = remaining.lastIndexOf(",")
        val coursesText = remaining.substring(0, lastComma)
        val gpa = remaining.substring(lastComma + 1).trim.toDouble

        val courseIds = coursesText
          .replace("[", "")
          .replace("]", "")
          .split(",")
          .map(_.trim.toInt)
          .toList

        val bsonDate = Date.from(
          birthDate.atStartOfDay(ZoneOffset.UTC).toInstant
        )

        new Document("_id", id)
          .append("firstName", firstName)
          .append("lastName", lastName)
          .append("dateOfBirth", bsonDate)
          .append(
            "courseIds",
            java.util.Arrays.asList(courseIds.map(Int.box): _*)
          )
          .append("gpa", gpa)
      }
    } finally {
      source.close()
    }

    println("Parsed students: " + students.size)

    // Save records as JSON Lines
    val jsonLines = students.map(_.toJson).mkString("\n")

    Files.write(
      Paths.get("students.json"),
      jsonLines.getBytes(StandardCharsets.UTF_8)
    )

    println("JSON file saved: students.json")

    // Part 1.2: Connect to MongoDB Atlas
    val dotenv = Dotenv.load()
    val uri = dotenv.get("MONGO_URI")

    if (uri == null || uri.trim.isEmpty) {
      throw new IllegalArgumentException(
        "MONGO_URI is missing from the .env file."
      )
    }

    val client = MongoClient(uri)

    try {
      val database = client.getDatabase("university")
      val collection = database.getCollection[Document]("students")

      // Replace existing student records
      Await.result(
        collection.drop().toFuture(),
        30.seconds
      )

      Await.result(
        collection.insertMany(students).toFuture(),
        30.seconds
      )

      println("Inserted students: " + students.size)

      // Create multikey and single-field indexes
      Await.result(
        collection.createIndex(
          Indexes.ascending("courseIds")
        ).toFuture(),
        30.seconds
      )

      Await.result(
        collection.createIndex(
          Indexes.ascending("gpa")
        ).toFuture(),
        30.seconds
      )

      // Display created indexes
      println("Indexes:")

      val indexes = Await.result(
        collection.listIndexes().toFuture(),
        30.seconds
      )
      indexes.foreach(index => println(index.toJson))

    } finally {
      client.close()
    }
    println("Part 1 completed successfully!")
  }
}
