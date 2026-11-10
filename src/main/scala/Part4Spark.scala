
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Part4Spark {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("BigData HW2 - Spark SQL")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    try {

      // Load JSON and prepare the schema
      val studentsRaw = spark.read.json("students.json")

      println("Total students: " + studentsRaw.count())

      println("\nOriginal Schema:")
      studentsRaw.printSchema()

      val df = studentsRaw
        .withColumn(
          "dateOfBirth",
          to_date(col("dateOfBirth.`$date`"))
        )
        .withColumn(
          "courseIds",
          expr("transform(courseIds, x -> cast(x as int))")
        )

      println("\nCorrected Schema:")
      df.printSchema()

      df.createOrReplaceTempView("students")

      // Part 4.1 - Basic queries
      println("\n4.1.1 - Students in Course 103")

      spark.sql("""
        SELECT _id, firstName, lastName, gpa
        FROM students
        WHERE array_contains(courseIds, 103)
        ORDER BY lastName, firstName, _id
      """).show(100, truncate = false)

      println("\n4.1.2 - Students with GPA > 3.5")

      spark.sql("""
        SELECT _id, firstName, lastName, gpa
        FROM students
        WHERE gpa > 3.5
        ORDER BY gpa DESC, _id
      """).show(100, truncate = false)

      // Part 4.2 - Enrollment and course statistics
      println("\n4.2.1 - Student Enrollments")

      val enrollments = spark.sql("""
        SELECT
          _id AS studentId,
          firstName,
          lastName,
          gpa,
          explode(courseIds) AS courseId
        FROM students
      """)

      enrollments.createOrReplaceTempView("enrollments")
      enrollments.show(30, truncate = false)

      println("Total enrollments: " + enrollments.count())

      println("\n4.2.2 - Course Statistics")

      val courseStats = spark.sql("""
        SELECT
          courseId,
          COUNT(*) AS studentCount,
          ROUND(AVG(gpa), 2) AS avgGPA,
          MIN(gpa) AS minGPA,
          MAX(gpa) AS maxGPA
        FROM enrollments
        GROUP BY courseId
        ORDER BY studentCount DESC
      """)

      courseStats.show(false)

      println("\n4.2.3 - Number of Courses per Student")

      spark.sql("""
        SELECT
          size(courseIds) AS numberOfCourses,
          COUNT(*) AS studentCount
        FROM students
        WHERE size(courseIds) IN (1, 2, 3)
        GROUP BY size(courseIds)
        ORDER BY numberOfCourses
      """).show(false)

      // Part 4.3 - Advanced SQL queries
      println("\n4.3.1 - Students and Average GPA by Birth Year")

      spark.sql("""
        SELECT
          year(dateOfBirth) AS birthYear,
          COUNT(*) AS studentCount,
          ROUND(AVG(gpa), 2) AS avgGPA
        FROM students
        GROUP BY year(dateOfBirth)
        ORDER BY birthYear
      """).show(false)

      println("\n4.3.2 - Top 3 Students per Course")

      spark.sql("""
        SELECT
          courseId,
          studentId,
          firstName,
          lastName,
          gpa,
          rank
        FROM (
          SELECT
            courseId,
            studentId,
            firstName,
            lastName,
            gpa,
            DENSE_RANK() OVER (
              PARTITION BY courseId
              ORDER BY gpa DESC
            ) AS rank
          FROM enrollments
        ) rankedStudents
        WHERE rank <= 3
        ORDER BY courseId, rank, studentId
      """).show(100, truncate = false)

      println("\n4.3.3 - Students with Same Full Name")

      spark.sql("""
        SELECT
          a._id AS student1_ID,
          b._id AS student2_ID,
          a.firstName,
          a.lastName,
          a.dateOfBirth AS student1_DOB,
          b.dateOfBirth AS student2_DOB
        FROM students a
        JOIN students b
          ON a.firstName = b.firstName
          AND a.lastName = b.lastName
          AND a._id < b._id
        ORDER BY a.firstName, a.lastName, a._id
      """).show(100, truncate = false)

      println("\n4.3.4 - Students Above Every Course Average")

      spark.sql("""
        SELECT
          e.studentId,
          e.firstName,
          e.lastName,
          e.gpa
        FROM enrollments e
        JOIN (
          SELECT
            courseId,
            AVG(gpa) AS avgGPA
          FROM enrollments
          GROUP BY courseId
        ) c
        ON e.courseId = c.courseId
        GROUP BY
          e.studentId,
          e.firstName,
          e.lastName,
          e.gpa
        HAVING COUNT(*) = SUM(
          CASE
            WHEN e.gpa > c.avgGPA THEN 1
            ELSE 0
          END
        )
        ORDER BY e.gpa DESC, e.studentId
      """).show(100, truncate = false)

      // Part 4.4 - Save and verify Parquet output
      println("\n4.4 - Writing Results to Parquet")

      courseStats.write
        .mode("overwrite")
        .parquet("output/course_stats")

      println("Course statistics saved successfully!")

      val savedStats = spark.read
        .parquet("output/course_stats")

      println("\nReading saved course statistics:")
      savedStats.show(false)

      println("\nSaved data schema:")
      savedStats.printSchema()

      println("\nTotal courses saved: " + savedStats.count())

      println("\nPart 4.4 completed successfully!")

    } finally {
      spark.stop()
    }
  }
}
