# Big Data Homework 2 – Student Records with MongoDB and Spark SQL

## Project Overview

This project manages and analyzes student records using MongoDB and Apache Spark SQL. It is implemented in Scala and covers data importing, indexing, querying, data analysis, and exporting results.

## Technologies Used

- Scala 2.13.18
- Apache Spark 4.2.0
- MongoDB Atlas
- MongoDB Scala Driver 5.6.0
- sbt
- IntelliJ IDEA

## Dataset

The dataset contains 100 student records with the following fields:

- ID
- FirstName
- LastName
- DateOfBirth
- CourseIDs
- GPA

## Project Implementation

### Part 1 – Data Import and Indexing

- Read and parse the CSV dataset.
- Convert student records to JSON.
- Insert 100 students into MongoDB.
- Create a single-field index on `gpa`.
- Create a multikey index on `courseIds`.

### Part 2 – Course Query

- Accept a Course ID from the user.
- Find students enrolled in the selected course.
- Sort students by last name.
- Examine the MongoDB query execution plan.

### Part 3 – GPA Query

- Accept a minimum GPA from the user.
- Optionally filter students by Course ID.
- Sort matching students by GPA in descending order.

### Part 4 – Spark SQL Analysis

- Read student records from `students.json` using Option B.
- Convert the date and course ID fields to appropriate data types.
- Create temporary SQL views.
- Query students by course and GPA.
- Analyze course enrollments and GPA statistics.
- Calculate student counts and average GPA by birth year.
- Rank students using `DENSE_RANK()`.
- Identify students with matching full names.
- Find students whose GPA exceeds the average for every course they take.
- Save course statistics to Parquet and read them back.

## Results

- Total students: 100
- Students enrolled in Course 103: 36
- Students with GPA above 3.5: 49
- Total course enrollments: 218
- Total courses in the statistics output: 5

## How to Run

1. Open the project in IntelliJ IDEA.
2. Install JDK 21 and allow sbt to load dependencies.
3. Create a `.env` file based on `.env.example`.
4. Set `MONGO_URI` to a valid MongoDB connection string.
5. Run `Part1Import.scala` to import the dataset and create indexes.
6. Run `Part2Course.scala` and `Part3GPA.scala` to execute MongoDB queries.
7. Run `Part4Spark.scala` to execute Spark SQL analysis and generate Parquet output.

**Note:** Part 1 recreates the MongoDB students collection. Part 4 uses the generated `students.json` file and does not require a direct MongoDB Spark Connector connection.

## Output Files

- `students.json` – Exported student records.
- `output/course_stats/` – Saved course statistics in Parquet format.
- `screenshots/` – Screenshots documenting execution results.

## Security

The `.env` file is excluded from GitHub to protect MongoDB credentials. The `.env.example` file provides a safe configuration template.