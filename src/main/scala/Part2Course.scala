import io.github.cdimascio.dotenv.Dotenv
import org.bson.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object Part2Course {
  def main(args: Array[String]): Unit = {

    // Part 2.1: Read and validate Course ID
    print("Enter Course ID: ")
    val input = StdIn.readLine()

    val courseId = input.trim.toIntOption.getOrElse {
      println("Invalid Course ID. Please enter an integer.")
      return
    }

    // Connect to MongoDB Atlas
    val uri = Dotenv.load().get("MONGO_URI")
    val client = MongoClient(uri)

    try {
      val database = client.getDatabase("university")
      val collection = database.getCollection[Document]("students")

      // Part 2.2: Find students enrolled in the selected course
      val query = Filters.eq("courseIds", courseId)

      val students = Await.result(
        collection
          .find(query)
          .sort(Sorts.ascending("lastName"))
          .toFuture(),
        30.seconds
      )

      // Display matching students
      println(s"\nStudents registered in course $courseId:")

      students.foreach { student =>
        val id = student.getInteger("_id")
        val firstName = student.getString("firstName")
        val lastName = student.getString("lastName")
        val gpa = student.getDouble("gpa")

        println(
          s"ID: $id | Name: $firstName $lastName | GPA: $gpa"
        )
      }
      println("\nTotal matching students: " + students.size)

      // Part 2.3: Explain the query execution plan
      val explainCommand = new Document(
        "explain",
        new Document("find", "students")
          .append("filter", new Document("courseIds", courseId))
          .append("sort", new Document("lastName", 1))
      ).append("verbosity", "queryPlanner")

      val explanation = Await.result(
        database.runCommand(explainCommand).toFuture(),
        30.seconds
      )

      val planText = explanation.toJson

      println("\nQuery Plan:")
      println(planText)

      // Check whether the courseIds index was used
      if (
        planText.contains("IXSCAN") &&
          planText.contains("courseIds_1")
      ) {
        println("\nIndex used: YES (courseIds_1)")
      } else {
        println("\nIndex used: courseIds_1 not confirmed")
      }
    } finally {
      client.close()
    }
  }
}