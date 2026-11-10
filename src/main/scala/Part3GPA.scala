import io.github.cdimascio.dotenv.Dotenv
import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object Part3GPA {

  def main(args: Array[String]): Unit = {

    print("Enter minimum GPA (0 - 4): ")
    val minGPAInput = StdIn.readLine().trim
    val minGPAOption = minGPAInput.toDoubleOption

    if (minGPAOption.isEmpty || minGPAOption.get < 0 || minGPAOption.get > 4) {
      println("Invalid GPA. Please enter a value between 0 and 4.")
      return
    }

    val minGPA = minGPAOption.get

    print("Enter Course ID (or press Enter to skip): ")
    val courseInput = StdIn.readLine().trim

    val courseIdOption =
      if (courseInput.isEmpty) None
      else courseInput.toIntOption

    if (courseInput.nonEmpty && courseIdOption.isEmpty) {
      println("Invalid Course ID. Please enter a number.")
      return
    }

    // Connect to MongoDB
    val uri = Dotenv.load().get("MONGO_URI")
    val client = MongoClient(uri)

    try {
      val database = client.getDatabase("university")
      val collection = database.getCollection[Document]("students")

      // Filter by GPA and optional Course ID
      val query = courseIdOption match {
        case Some(courseId) =>
          and(gt("gpa", minGPA), equal("courseIds", courseId))
        case None =>
          gt("gpa", minGPA)
      }

      val students = Await.result(
        collection.find(query)
          .sort(descending("gpa"))
          .toFuture(),
        30.seconds
      )

      // Display matching students
      println("\nMatching students:")

      students.foreach { student =>
        val id = student.getInteger("_id")
        val firstName = student.getString("firstName")
        val lastName = student.getString("lastName")
        val gpa = student.getDouble("gpa")

        println(s"ID: $id | Name: $firstName $lastName | GPA: $gpa")
      }

      println("\nTotal matching students: " + students.size)

    } finally {
      client.close()
    }
  }
}