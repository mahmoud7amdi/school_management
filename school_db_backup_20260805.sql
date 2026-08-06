-- MySQL dump 10.13  Distrib 8.4.3, for Win64 (x86_64)
--
-- Host: localhost    Database: school_db
-- ------------------------------------------------------
-- Server version	8.4.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `about_page`
--

DROP TABLE IF EXISTS `about_page`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `about_page` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `body` text,
  `contact_email` varchar(150) DEFAULT NULL,
  `contact_phone` varchar(30) DEFAULT NULL,
  `mission` text,
  `tagline` varchar(300) DEFAULT NULL,
  `title` varchar(150) NOT NULL,
  `updated_by` varchar(120) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `about_page`
--

LOCK TABLES `about_page` WRITE;
/*!40000 ALTER TABLE `about_page` DISABLE KEYS */;
INSERT INTO `about_page` VALUES (1,'2026-08-03 20:14:24.902035','2026-08-04 01:04:59.724860','123 Nile Street, Cairo 3','We are a community school.','hello@example.com','01000000000','Every child supported.','Learning together since 1998','About Our School','Mahmoud','https://example.com');
/*!40000 ALTER TABLE `about_page` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `absence_notes`
--

DROP TABLE IF EXISTS `absence_notes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `absence_notes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `absence_date` date NOT NULL,
  `reason` varchar(1000) NOT NULL,
  `review_note` varchar(500) DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `status` enum('SUBMITTED','ACKNOWLEDGED','REJECTED') NOT NULL,
  `reviewed_by_id` binary(16) DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `submitted_by_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_absence_note_student_date` (`student_id`,`absence_date`),
  KEY `idx_absence_note_school` (`school_id`),
  KEY `idx_absence_note_student` (`student_id`),
  KEY `idx_absence_note_status` (`status`),
  KEY `idx_absence_note_date` (`absence_date`),
  KEY `FKjucjmxtvxns1v2y17920dt1at` (`reviewed_by_id`),
  KEY `FKe2qrjmsfmdolggx1u5h6jktp2` (`submitted_by_id`),
  CONSTRAINT `FK78198gn34wr72hrtd7cof9gyg` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKe2qrjmsfmdolggx1u5h6jktp2` FOREIGN KEY (`submitted_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj7qwkqa4u5ql4t5k2o290o34c` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKjucjmxtvxns1v2y17920dt1at` FOREIGN KEY (`reviewed_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `absence_notes`
--

LOCK TABLES `absence_notes` WRITE;
/*!40000 ALTER TABLE `absence_notes` DISABLE KEYS */;
/*!40000 ALTER TABLE `absence_notes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `academic_year`
--

DROP TABLE IF EXISTS `academic_year`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_year` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `year` varchar(255) DEFAULT NULL,
  `school_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmqs1eiare42u0axa8nwygg0jy` (`school_id`),
  CONSTRAINT `FKmqs1eiare42u0axa8nwygg0jy` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `academic_year`
--

LOCK TABLES `academic_year` WRITE;
/*!40000 ALTER TABLE `academic_year` DISABLE KEYS */;
/*!40000 ALTER TABLE `academic_year` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `academic_years`
--

DROP TABLE IF EXISTS `academic_years`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_years` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `start_date` date DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `is_current` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_academic_year_school_name` (`school_id`,`name`),
  UNIQUE KEY `UK5cbe6cdpkcenvfsah496sxq3y` (`name`,`school_id`),
  KEY `idx_academic_year_school` (`school_id`),
  CONSTRAINT `FKeh3xckk0s8t44khlgba0o4t3j` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `academic_years`
--

LOCK TABLES `academic_years` WRITE;
/*!40000 ALTER TABLE `academic_years` DISABLE KEYS */;
/*!40000 ALTER TABLE `academic_years` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendances`
--

DROP TABLE IF EXISTS `attendances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendances` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `attendance_date` date NOT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `status` enum('PRESENT','ABSENT','LATE','EXCUSED','HALF_DAY') NOT NULL,
  `classroom_id` bigint NOT NULL,
  `recorded_by_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `subject_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_student_date` (`student_id`,`attendance_date`),
  KEY `idx_attendance_school` (`school_id`),
  KEY `idx_attendance_student` (`student_id`),
  KEY `idx_attendance_classroom` (`classroom_id`),
  KEY `idx_attendance_date` (`attendance_date`),
  KEY `idx_attendance_status` (`status`),
  KEY `FKpqyu5g4y7ac26njx3eksngcvl` (`recorded_by_id`),
  KEY `FKgpnn0q2bpd2ocr92u2sf7lytc` (`subject_id`),
  CONSTRAINT `FK7bm4q4wptspkenhrsjgatdmk0` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKg26nb6on7i5lrqmgmyiu89ohh` FOREIGN KEY (`classroom_id`) REFERENCES `classrooms` (`id`),
  CONSTRAINT `FKgpnn0q2bpd2ocr92u2sf7lytc` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `FKn09c9ycnkn9uj7coud2m0goo7` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKpqyu5g4y7ac26njx3eksngcvl` FOREIGN KEY (`recorded_by_id`) REFERENCES `teachers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendances`
--

LOCK TABLES `attendances` WRITE;
/*!40000 ALTER TABLE `attendances` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classroom`
--

DROP TABLE IF EXISTS `classroom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classroom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `school_id` bigint DEFAULT NULL,
  `academic_year_id` bigint DEFAULT NULL,
  `grade_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKehym87aowb6ten08u65larwql` (`school_id`),
  KEY `FK8fm6qfqh2c9mc1p1o1ovdmmhc` (`grade_id`),
  KEY `FKg4acw7eufdr5x4brey3ekcrfs` (`academic_year_id`),
  CONSTRAINT `FK617vwbaycmlkhfnr4ge75gwy` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FK8fm6qfqh2c9mc1p1o1ovdmmhc` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`),
  CONSTRAINT `FKehym87aowb6ten08u65larwql` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKg4acw7eufdr5x4brey3ekcrfs` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classroom`
--

LOCK TABLES `classroom` WRITE;
/*!40000 ALTER TABLE `classroom` DISABLE KEYS */;
/*!40000 ALTER TABLE `classroom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classrooms`
--

DROP TABLE IF EXISTS `classrooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classrooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `capacity` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `room_number` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `academic_year_id` bigint DEFAULT NULL,
  `class_teacher_id` binary(16) DEFAULT NULL,
  `grade_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `section_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_classroom_school_year_name` (`school_id`,`academic_year_id`,`name`),
  UNIQUE KEY `uk_classroom_grade_year_name` (`grade_id`,`academic_year_id`,`name`),
  KEY `FKimlljm1cij5pw2otoguwwiqq5` (`class_teacher_id`),
  KEY `idx_classroom_school` (`school_id`),
  KEY `idx_classroom_grade` (`grade_id`),
  KEY `idx_classroom_year` (`academic_year_id`),
  KEY `idx_classroom_section` (`section_id`),
  CONSTRAINT `FKa919165pe8mqragt20o7nxyu3` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`),
  CONSTRAINT `FKi317vco72foddhahlhnfcgn5b` FOREIGN KEY (`section_id`) REFERENCES `sections` (`id`),
  CONSTRAINT `FKimlljm1cij5pw2otoguwwiqq5` FOREIGN KEY (`class_teacher_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKjxfywqifk1i7gnseysspehj6t` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`),
  CONSTRAINT `FKstm3c7u1s7l0t42gywsldaani` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classrooms`
--

LOCK TABLES `classrooms` WRITE;
/*!40000 ALTER TABLE `classrooms` DISABLE KEYS */;
/*!40000 ALTER TABLE `classrooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `completion_date` date DEFAULT NULL,
  `enrollment_date` date NOT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `roll_number` varchar(20) DEFAULT NULL,
  `status` enum('ENROLLED','COMPLETED','REPEATING','WITHDRAWN','TRANSFERRED') NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `classroom_id` bigint DEFAULT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enrollment_student_year` (`student_id`,`academic_year_id`),
  KEY `idx_enrollment_school` (`school_id`),
  KEY `idx_enrollment_student` (`student_id`),
  KEY `idx_enrollment_year` (`academic_year_id`),
  KEY `idx_enrollment_classroom` (`classroom_id`),
  KEY `idx_enrollment_status` (`status`),
  KEY `FKube0t8hvfshln29fsjjhvs0v` (`grade_id`),
  CONSTRAINT `FK5me5sdtbcnxj8bb5jxw72df44` FOREIGN KEY (`classroom_id`) REFERENCES `classrooms` (`id`),
  CONSTRAINT `FK8kf1u1857xgo56xbfmnif2c51` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKd035k6ap0eol4rqdb7a21iyih` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKr4akji031thjyvok61sjcoja6` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`),
  CONSTRAINT `FKube0t8hvfshln29fsjjhvs0v` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_results`
--

DROP TABLE IF EXISTS `exam_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_results` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `absent` bit(1) NOT NULL,
  `marks_obtained` decimal(6,2) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `exam_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_result_exam_student` (`exam_id`,`student_id`),
  KEY `idx_exam_result_school` (`school_id`),
  KEY `idx_exam_result_exam` (`exam_id`),
  KEY `idx_exam_result_student` (`student_id`),
  CONSTRAINT `FK7jb223xkbx0iusj9xvu9m36o0` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKr7qgl670f47u65kkdm8ex5119` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKtf85ht7yquiorwjx2xbdx3fxw` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_results`
--

LOCK TABLES `exam_results` WRITE;
/*!40000 ALTER TABLE `exam_results` DISABLE KEYS */;
/*!40000 ALTER TABLE `exam_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exams`
--

DROP TABLE IF EXISTS `exams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `duration_minutes` int DEFAULT NULL,
  `exam_date` date NOT NULL,
  `exam_type` enum('QUIZ','ASSIGNMENT','MIDTERM','FINAL','PRACTICAL','MOCK') NOT NULL,
  `max_marks` decimal(6,2) NOT NULL,
  `pass_marks` decimal(6,2) NOT NULL,
  `start_time` time(6) DEFAULT NULL,
  `title` varchar(150) NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `classroom_id` bigint DEFAULT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_exam_school` (`school_id`),
  KEY `idx_exam_subject` (`subject_id`),
  KEY `idx_exam_classroom` (`classroom_id`),
  KEY `idx_exam_year` (`academic_year_id`),
  KEY `idx_exam_date` (`exam_date`),
  KEY `FKg2wxl2xnpat5as17yyjgrxs5m` (`grade_id`),
  CONSTRAINT `FK3gdrl267p4meyvb9f8lf2ld21` FOREIGN KEY (`classroom_id`) REFERENCES `classrooms` (`id`),
  CONSTRAINT `FK58snu3x30ly9owm86j8bqbrd2` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKddehgdbvhn56aeo9hempt82qq` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`),
  CONSTRAINT `FKg2wxl2xnpat5as17yyjgrxs5m` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`),
  CONSTRAINT `FKopre4n7j7fpxqbtbwpv8ywn1y` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `exams_chk_1` CHECK (((`duration_minutes` >= 5) and (`duration_minutes` <= 600)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exams`
--

LOCK TABLES `exams` WRITE;
/*!40000 ALTER TABLE `exams` DISABLE KEYS */;
/*!40000 ALTER TABLE `exams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fee_acknowledgements`
--

DROP TABLE IF EXISTS `fee_acknowledgements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_acknowledgements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `acknowledged_at` datetime(6) NOT NULL,
  `acknowledged_by_id` binary(16) NOT NULL,
  `fee_structure_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fee_acknowledgement` (`student_id`,`fee_structure_id`,`acknowledged_by_id`),
  KEY `idx_fee_ack_school` (`school_id`),
  KEY `idx_fee_ack_student` (`student_id`),
  KEY `idx_fee_ack_structure` (`fee_structure_id`),
  KEY `FKewhk7qrr7jhayi1o0tlo6c6yr` (`acknowledged_by_id`),
  CONSTRAINT `FK5ma44pv3e6mmrrgmndvedqehf` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FK84y78gia6cklsvpp50h2g7spm` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKewhk7qrr7jhayi1o0tlo6c6yr` FOREIGN KEY (`acknowledged_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKht3ckaryo9qaccjomvxyhdj2i` FOREIGN KEY (`fee_structure_id`) REFERENCES `fee_structures` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fee_acknowledgements`
--

LOCK TABLES `fee_acknowledgements` WRITE;
/*!40000 ALTER TABLE `fee_acknowledgements` DISABLE KEYS */;
/*!40000 ALTER TABLE `fee_acknowledgements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fee_payments`
--

DROP TABLE IF EXISTS `fee_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `amount_paid` decimal(12,2) NOT NULL,
  `method` enum('CASH','CARD','BANK_TRANSFER','MOBILE_MONEY','CHEQUE','WAIVER') NOT NULL,
  `payment_date` date NOT NULL,
  `receipt_number` varchar(40) NOT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `status` enum('COMPLETED','PENDING','FAILED','REFUNDED') NOT NULL,
  `fee_structure_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fee_payment_school_receipt` (`school_id`,`receipt_number`),
  KEY `idx_fee_payment_school` (`school_id`),
  KEY `idx_fee_payment_student` (`student_id`),
  KEY `idx_fee_payment_structure` (`fee_structure_id`),
  KEY `idx_fee_payment_date` (`payment_date`),
  KEY `idx_fee_payment_status` (`status`),
  CONSTRAINT `FK6k0lkod8mk082lnbapghhrx0j` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKfwud4nns9c1rtxdvbwt9t2y52` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKm0gdar1j14en6am9pe6dlmt7w` FOREIGN KEY (`fee_structure_id`) REFERENCES `fee_structures` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fee_payments`
--

LOCK TABLES `fee_payments` WRITE;
/*!40000 ALTER TABLE `fee_payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `fee_payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fee_structures`
--

DROP TABLE IF EXISTS `fee_structures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_structures` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `amount` decimal(12,2) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `due_date` date NOT NULL,
  `fee_type` enum('TUITION','TRANSPORT','EXAM','LIBRARY','LABORATORY','HOSTEL','UNIFORM','ACTIVITY','OTHER') NOT NULL,
  `name` varchar(150) NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fee_structure_year_grade_name` (`academic_year_id`,`grade_id`,`name`),
  KEY `idx_fee_structure_school` (`school_id`),
  KEY `idx_fee_structure_year` (`academic_year_id`),
  KEY `idx_fee_structure_grade` (`grade_id`),
  CONSTRAINT `FKd0lyfgppgqlec5ywh0ebnm8g0` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`),
  CONSTRAINT `FKgmb1e4axfm1bau8kwqaaw6gxs` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKgvda8q1mibfvp8wfptqk0dfiu` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fee_structures`
--

LOCK TABLES `fee_structures` WRITE;
/*!40000 ALTER TABLE `fee_structures` DISABLE KEYS */;
/*!40000 ALTER TABLE `fee_structures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grade`
--

DROP TABLE IF EXISTS `grade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `school_id` bigint DEFAULT NULL,
  `academic_year_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKieapbcv07ouhexuv4w8r0u57i` (`school_id`),
  KEY `FKd9jhxu8blw5a7e39nkwe5bpk5` (`academic_year_id`),
  CONSTRAINT `FKd9jhxu8blw5a7e39nkwe5bpk5` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKieapbcv07ouhexuv4w8r0u57i` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grade`
--

LOCK TABLES `grade` WRITE;
/*!40000 ALTER TABLE `grade` DISABLE KEYS */;
/*!40000 ALTER TABLE `grade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `grade_level` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `academic_year_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `level_order` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grade_school_name` (`school_id`,`name`),
  UNIQUE KEY `uk_grade_school_year_name` (`school_id`,`academic_year_id`,`name`),
  KEY `FKcpyah66tlvfqke2w0l6b1p5wc` (`academic_year_id`),
  KEY `idx_grade_school` (`school_id`),
  CONSTRAINT `FKcpyah66tlvfqke2w0l6b1p5wc` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`),
  CONSTRAINT `FKk9qgl9w0cqoytrphl4sp5io8b` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (1,'2026-08-01 23:49:01.244667',NULL,'Grade 1','2026-08-01 23:49:01.244667',NULL,10,NULL,1),(2,'2026-08-02 00:20:17.100088',NULL,'test 1','2026-08-02 00:20:17.100088',NULL,10,'test',2),(3,'2026-08-02 21:38:39.615729',NULL,'one','2026-08-02 21:38:39.615729',NULL,1,'test',1);
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parent_students`
--

DROP TABLE IF EXISTS `parent_students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parent_students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `primary_contact` bit(1) NOT NULL,
  `relationship` enum('MOTHER','FATHER','GUARDIAN','OTHER') NOT NULL,
  `parent_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_student` (`parent_id`,`student_id`),
  KEY `idx_parent_student_parent` (`parent_id`),
  KEY `idx_parent_student_student` (`student_id`),
  CONSTRAINT `FKgarkq0aw212e2noi8mfoitij4` FOREIGN KEY (`parent_id`) REFERENCES `parents` (`id`),
  CONSTRAINT `FKjajcco9ifsue0aejy2hulyf9y` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parent_students`
--

LOCK TABLES `parent_students` WRITE;
/*!40000 ALTER TABLE `parent_students` DISABLE KEYS */;
/*!40000 ALTER TABLE `parent_students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parents`
--

DROP TABLE IF EXISTS `parents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `first_name` varchar(60) NOT NULL,
  `last_name` varchar(60) NOT NULL,
  `occupation` varchar(120) DEFAULT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `user_account_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_school_email` (`school_id`,`email`),
  UNIQUE KEY `UK_7vo6694ybttrix9xiqmjp022s` (`user_account_id`),
  KEY `idx_parent_school` (`school_id`),
  CONSTRAINT `FKatq0lg3m5wavlfe3p7kfkvwpj` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKhwqewu2nsgrywj3sra774ainn` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parents`
--

LOCK TABLES `parents` WRITE;
/*!40000 ALTER TABLE `parents` DISABLE KEYS */;
/*!40000 ALTER TABLE `parents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `revoked_tokens`
--

DROP TABLE IF EXISTS `revoked_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `revoked_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expires_at` datetime(6) NOT NULL,
  `revoked_at` datetime(6) NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_2ele31feocsuq970bht2vmy4j` (`token_hash`),
  KEY `idx_revoked_tokens_expires` (`expires_at`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `revoked_tokens`
--

LOCK TABLES `revoked_tokens` WRITE;
/*!40000 ALTER TABLE `revoked_tokens` DISABLE KEYS */;
INSERT INTO `revoked_tokens` VALUES (16,'2026-08-04 02:11:40.000000','2026-08-03 19:09:28.421458','419c5539a962652b6e0f446907b71ab4c3b3fcccc792195a8664e6a01e7bcab3'),(17,'2026-08-04 05:09:30.000000','2026-08-03 19:10:13.911543','da65e3635a59ff6ea2e2a26e02cda5903466d9fa8bbada3f84d24600f7741091'),(18,'2026-08-04 05:10:27.000000','2026-08-03 19:10:48.351500','e73f6b65b1e5261afb1a057282c84edd5db683f048e96741fc89399109b80c50'),(19,'2026-08-04 05:11:13.000000','2026-08-03 19:11:29.644919','b727f7441c265a117b399ceb64a4e9cf5b22e11074d83c6579521a20b1b60c1d'),(20,'2026-08-04 05:11:35.000000','2026-08-03 19:11:55.288353','8d8a49a38677b07d79d5668b93e62b6e64b5495a5aca683d261006604008fd48'),(21,'2026-08-04 05:12:04.000000','2026-08-03 19:12:47.416749','3b1ebdb82ca56459ddd5e18c872a75b3d39518c777708d7b39c97008aa1dd8b7'),(22,'2026-08-04 07:57:19.000000','2026-08-03 22:02:38.586485','eaf421b41c9ec31bc9c50a1e535c185fd4ea17846803aecb9eb32072a21edf0e'),(23,'2026-08-04 08:02:40.000000','2026-08-03 22:03:08.384897','e8e3123f13e2b5527563f70bb20b6ab0a1a177fc81d045ae1fc2637b492e425b'),(24,'2026-08-04 08:13:45.000000','2026-08-03 22:14:02.463681','f6c17306e07f8303635bb84f8e751db34e581ab481f0f96b1afaa6bfc23deef3'),(25,'2026-08-04 08:14:19.000000','2026-08-04 01:05:27.668743','6373484d89c76a538d4ebdec9ebc9bed54087f756c7679ae9db2604b66896ab1'),(26,'2026-08-04 11:05:29.000000','2026-08-04 01:05:51.813344','4d179814d94299754d5856db2975e5f06978ef6bd24a38cea97eb006fbc750ae'),(27,'2026-08-04 11:06:06.000000','2026-08-04 01:11:15.385760','47e1c33dd5eb4ab5f0bd1f381564091ff4a22a4abf07bc8139541160bfdfaf8e'),(28,'2026-08-04 11:11:21.000000','2026-08-04 01:12:31.014059','8f4c19ea29d4cb59d991bf2723212b5a3c59c65d300fcc8b57f1809d19bf737a'),(29,'2026-08-04 11:12:37.000000','2026-08-04 01:12:57.519081','f9154020b6730f3478cee65c2fe41492ff60db2f836377af839d097030aa444c'),(30,'2026-08-04 11:13:02.000000','2026-08-04 01:13:22.271690','165755a46c5b2432514fdecc4af69f5699a2f9f599054928fd8331795afbe51b'),(31,'2026-08-05 02:10:53.000000','2026-08-04 22:43:34.939654','f0d0b3d8b5d550ceee5df3bfee736a5402055283ca54fb04dd291f37be1cbf6f'),(32,'2026-08-05 02:24:35.000000','2026-08-04 23:43:47.934982','4b9cdba0755e1a571cb2cfe850f59cdb07d8185d62b6fae1c64d42d12cf19a60'),(33,'2026-08-05 08:43:37.000000','2026-08-04 23:54:29.347887','83e2aabfee4052cbd8db28852a695932b100fb87d0715178fd01ffc23777d5e9'),(34,'2026-08-05 09:54:32.000000','2026-08-05 00:19:52.137281','fe170157aef60a3f69c7e431280af2ae892a4e9c7a28fdb81dbc88076b6030a0'),(35,'2026-08-05 10:19:54.000000','2026-08-05 00:26:18.000974','a5fb70497a038b65915ff28695e0a2f29c5117be21e0c5f820857125a3c99180'),(36,'2026-08-05 10:26:27.000000','2026-08-05 00:26:39.554436','699a063be9cc97f5717696fd12aae5aa94708397c89b4047b2c2970754e181bb'),(37,'2026-08-05 03:33:53.000000','2026-08-05 03:11:48.981852','52513be09811893ed928c60f72fdbc1050fb292385526dc59897785bddbb4985');
/*!40000 ALTER TABLE `revoked_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `school_admins`
--

DROP TABLE IF EXISTS `school_admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school_admins` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `department` varchar(120) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `first_name` varchar(60) NOT NULL,
  `job_title` varchar(120) NOT NULL,
  `last_name` varchar(60) NOT NULL,
  `office` varchar(60) DEFAULT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `user_account_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_admin_school_email` (`school_id`,`email`),
  UNIQUE KEY `UK_783sb47iyvwp9nnlgsh72pb5m` (`user_account_id`),
  KEY `idx_school_admin_school` (`school_id`),
  CONSTRAINT `FKb89xryb0v2dfwbnaxakrp4drb` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKjtaj5dp0jm0fc8sxlnhpfa470` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `school_admins`
--

LOCK TABLES `school_admins` WRITE;
/*!40000 ALTER TABLE `school_admins` DISABLE KEYS */;
INSERT INTO `school_admins` VALUES (2,'2026-08-05 16:36:34.015347','2026-08-05 16:36:34.015347',NULL,'2026-08-05','new-d','s-admin@gmail.com','s-admin','s-admin','s-admin','1','01200000522344',11,_binary '1RI∑BGvù	ò\Ãu%\‰\‹');
/*!40000 ALTER TABLE `school_admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schools`
--

DROP TABLE IF EXISTS `schools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schools` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKehwqlfa7xseucba45p6wlqfgn` (`name`),
  UNIQUE KEY `UKjwg1c30unxe6ee4ssancp14qh` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schools`
--

LOCK TABLES `schools` WRITE;
/*!40000 ALTER TABLE `schools` DISABLE KEYS */;
INSERT INTO `schools` VALUES (1,'123 Nile Street, Cairo','2026-04-11 17:31:24.851000','info@smartedu55.com',NULL,'Smart Education School 11','0101234567899','https://smartedu.com','2026-08-04 16:14:58.438054',_binary ''),(4,'123 Nile Street, Cairo 3','2026-04-22 12:19:03.863000','info3@smartedu.com',NULL,'Smart Education School 3','01012345673','https://smartedux.com','2026-08-04 16:15:35.620637',_binary ''),(6,'123 Nile Street, Cairo 5','2026-04-22 13:07:19.958000','info5@smartedu.com',NULL,'Smart Education School 5','01012345675','https://smartedu.com','2026-08-04 16:15:55.043855',_binary ''),(10,'egypt','2026-08-01 19:55:16.890789','default@gmail.com',NULL,'test-school','01200000522','https://mmmmnghnn.com','2026-08-04 16:16:48.040156',_binary ''),(11,'egypt','2026-08-05 16:35:23.488809','test-s@gmail.com',NULL,'test-s','0120000052254','https://test-s.com','2026-08-05 16:35:23.488809',_binary '');
/*!40000 ALTER TABLE `schools` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sections`
--

DROP TABLE IF EXISTS `sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sections` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `capacity` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `section_head_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_section_grade_name` (`grade_id`,`name`),
  KEY `idx_section_school` (`school_id`),
  KEY `idx_section_grade` (`grade_id`),
  KEY `FK5w55hn5ytomcuttkisqbfc2tv` (`section_head_id`),
  CONSTRAINT `FK19900ynxu4ssf4n3t8v6nwgoh` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`),
  CONSTRAINT `FK5w55hn5ytomcuttkisqbfc2tv` FOREIGN KEY (`section_head_id`) REFERENCES `teachers` (`id`),
  CONSTRAINT `FKob22i8tnkvrk56r77epe2ks7c` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `sections_chk_1` CHECK (((`capacity` <= 500) and (`capacity` >= 1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sections`
--

LOCK TABLES `sections` WRITE;
/*!40000 ALTER TABLE `sections` DISABLE KEYS */;
/*!40000 ALTER TABLE `sections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `date_of_birth` date NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `classroom_id` bigint DEFAULT NULL,
  `grade_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `user_id` binary(16) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `admission_number` varchar(40) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `enrollment_date` date NOT NULL,
  `guardian_email` varchar(150) DEFAULT NULL,
  `guardian_name` varchar(120) DEFAULT NULL,
  `guardian_phone` varchar(30) DEFAULT NULL,
  `photo_url` varchar(500) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','GRADUATED','TRANSFERRED','SUSPENDED') NOT NULL,
  `user_account_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK155o2w0j9vt8etgaq09ki4nh3` (`school_id`),
  UNIQUE KEY `uk_student_school_admission` (`school_id`,`admission_number`),
  UNIQUE KEY `UK_g4fwvutq09fjdlb4bb0byp7t` (`user_id`),
  UNIQUE KEY `UK_e7x67evhrjo67no1xrb4sjnbl` (`user_account_id`),
  KEY `idx_student_school` (`school_id`),
  KEY `idx_student_classroom` (`classroom_id`),
  KEY `idx_student_grade` (`grade_id`),
  KEY `idx_student_status` (`status`),
  CONSTRAINT `FKdojmg8v3rw2ow4dev2b8q5oqq` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKdt1cjx5ve5bdabmuuf3ibrwaq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKexo2cgxoe0p8p60y4m6g9hent` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`),
  CONSTRAINT `FKft6lq7muld68ltfte4sa9br2h` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKn4i882kjg6fdyg2e641yh3jmk` FOREIGN KEY (`classroom_id`) REFERENCES `classrooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `school_id` bigint DEFAULT NULL,
  `academic_year_id` bigint DEFAULT NULL,
  `grade_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk7bhr856cd70d600f3g5lc6lf` (`school_id`),
  KEY `FKayb4n3qybso2vwx4nop8r4snb` (`academic_year_id`),
  KEY `FKam8igh6e0xp9r1tl7d8r1wneb` (`grade_id`),
  CONSTRAINT `FKam8igh6e0xp9r1tl7d8r1wneb` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`),
  CONSTRAINT `FKayb4n3qybso2vwx4nop8r4snb` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKk7bhr856cd70d600f3g5lc6lf` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subject`
--

LOCK TABLES `subject` WRITE;
/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `grade_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `weekly_hours` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_school_code` (`school_id`,`code`),
  UNIQUE KEY `uk_subject_grade_name` (`grade_id`,`name`),
  KEY `idx_subject_school` (`school_id`),
  KEY `idx_subject_grade` (`grade_id`),
  CONSTRAINT `FKmuktvnrq4ft25nduvev1wseqd` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKp5gpt39lxgg443ws2tnvnukpa` FOREIGN KEY (`grade_id`) REFERENCES `grades` (`id`),
  CONSTRAINT `subjects_chk_1` CHECK (((`weekly_hours` <= 60) and (`weekly_hours` >= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjects`
--

LOCK TABLES `subjects` WRITE;
/*!40000 ALTER TABLE `subjects` DISABLE KEYS */;
INSERT INTO `subjects` VALUES (1,'1222','2026-08-02 00:20:43.553632',NULL,'Default','2026-08-02 00:20:43.553632',2,10,9),(2,'math101','2026-08-02 21:39:27.769603',NULL,'math','2026-08-02 21:39:27.769603',1,10,5);
/*!40000 ALTER TABLE `subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_subjects`
--

DROP TABLE IF EXISTS `teacher_subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_subjects` (
  `teacher_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  PRIMARY KEY (`teacher_id`,`subject_id`),
  KEY `FKdweqkwxroox2u7pbmksehx04i` (`subject_id`),
  CONSTRAINT `FK6dcl3ihufp4v0j1fuxlw4ksoj` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`id`),
  CONSTRAINT `FKdweqkwxroox2u7pbmksehx04i` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_subjects`
--

LOCK TABLES `teacher_subjects` WRITE;
/*!40000 ALTER TABLE `teacher_subjects` DISABLE KEYS */;
INSERT INTO `teacher_subjects` VALUES (1,2),(3,2);
/*!40000 ALTER TABLE `teacher_subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teachers`
--

DROP TABLE IF EXISTS `teachers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teachers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `employee_number` varchar(40) NOT NULL,
  `first_name` varchar(60) NOT NULL,
  `gender` enum('MALE','FEMALE') NOT NULL,
  `hire_date` date NOT NULL,
  `last_name` varchar(60) NOT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  `photo_url` varchar(500) DEFAULT NULL,
  `qualification` varchar(150) DEFAULT NULL,
  `specialization` varchar(150) DEFAULT NULL,
  `status` enum('ACTIVE','ON_LEAVE','SUSPENDED','RESIGNED','RETIRED') NOT NULL,
  `school_id` bigint NOT NULL,
  `user_account_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_school_employee` (`school_id`,`employee_number`),
  UNIQUE KEY `UK_7oloetwvwcshf0g1k0rqn1tjx` (`user_account_id`),
  KEY `idx_teacher_school` (`school_id`),
  KEY `idx_teacher_status` (`status`),
  CONSTRAINT `FK25tvrvw3ww2p7mbt62abrbwev` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FK55r4yuxnb5ylf8jmm7sbo7406` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teachers`
--

LOCK TABLES `teachers` WRITE;
/*!40000 ALTER TABLE `teachers` DISABLE KEYS */;
INSERT INTO `teachers` VALUES (1,'2026-08-03 00:40:21.110975','2026-08-04 17:36:15.781434','egypt','2021-05-03','teacher@gmail.com','1','Mahmoud','MALE','2026-08-01','hamdy','01200000522',NULL,'eng','arabic','ACTIVE',10,_binary '{\Ò∑ùq-BUïòqR\⁄\Z0\⁄'),(3,'2026-08-05 00:24:03.189585','2026-08-05 00:24:03.189585','egypt','2006-05-05','rrrrrrr@gmail.com','22','Mahmoud','MALE','2026-08-05','man','01200000522',NULL,'eng','arabc','ACTIVE',10,_binary 'Ü±\Ïr˙BRæJö_\ˆ\‰\ﬂ');
/*!40000 ALTER TABLE `teachers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teaching_assignments`
--

DROP TABLE IF EXISTS `teaching_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teaching_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `classroom_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `subject_id` bigint DEFAULT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teaching_assignment` (`teacher_id`,`classroom_id`,`subject_id`,`academic_year_id`),
  KEY `idx_teaching_assignment_school` (`school_id`),
  KEY `idx_teaching_assignment_teacher` (`teacher_id`),
  KEY `idx_teaching_assignment_classroom` (`classroom_id`),
  KEY `idx_teaching_assignment_year` (`academic_year_id`),
  KEY `FK3xdxo1bu1pmp99lnjsgxh7foq` (`subject_id`),
  CONSTRAINT `FK3xdxo1bu1pmp99lnjsgxh7foq` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `FKc6e0n8cn3v562jta4esm1bns2` FOREIGN KEY (`classroom_id`) REFERENCES `classrooms` (`id`),
  CONSTRAINT `FKepv5ovgwxvdahd7f01cs40a6t` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`id`),
  CONSTRAINT `FKjk4iiedxc7jbss3u5vycfg2k6` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`),
  CONSTRAINT `FKogatj428g5re2g3l3u4x0yte2` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teaching_assignments`
--

LOCK TABLES `teaching_assignments` WRITE;
/*!40000 ALTER TABLE `teaching_assignments` DISABLE KEYS */;
/*!40000 ALTER TABLE `teaching_assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `token_blacklist`
--

DROP TABLE IF EXISTS `token_blacklist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NOT NULL,
  `token` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_bff28eugoihk2swcdiybdej20` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token_blacklist`
--

LOCK TABLES `token_blacklist` WRITE;
/*!40000 ALTER TABLE `token_blacklist` DISABLE KEYS */;
INSERT INTO `token_blacklist` VALUES (1,'2026-05-05 22:12:39.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc3OTcyMzU5LCJleHAiOjE3NzgwMDgzNTl9.0iX_U0D5a-q5z6kZ44UyiqxwRPIlZhKpqbguh9j0M9w'),(2,'2026-05-05 22:18:37.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc3OTcyNzE3LCJleHAiOjE3NzgwMDg3MTd9.Jia5k1IxFeznMW9HvZfKcjo2pUN-4XTuzunm9qXzs7M'),(3,'2026-05-11 04:51:40.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NDI4MzAwLCJleHAiOjE3Nzg0NjQzMDB9.GaeIBrw9TeSupJc7VOSocrrxTs2_QuYJT9vSPNeo7aU'),(4,'2026-05-11 05:02:36.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NDI4OTU2LCJleHAiOjE3Nzg0NjQ5NTZ9.2wg_psf9JD99OceXd8mgx3GNvBZZKnYGx9miGO6FLN4'),(5,'2026-05-11 05:31:12.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NDMwNjcyLCJleHAiOjE3Nzg0NjY2NzJ9.CrNfCSC4zIdhddRg7dSb1_48dW3kAuXMrlUeBw6ZxOY'),(6,'2026-05-11 05:37:00.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzYWRtaW4iLCJpYXQiOjE3Nzg0MzEwMjAsImV4cCI6MTc3ODQ2NzAyMH0.jdLyZSKsAFquGywOmXXf77sslP_aJ-O0jF27gMabJuY'),(7,'2026-05-12 03:55:13.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NTExMzEzLCJleHAiOjE3Nzg1NDczMTN9.iLBhRy3UkeK-hP-CbVypJOGQv-cd2lJOyPruAali9ZE'),(8,'2026-05-12 05:12:55.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NTE1OTc1LCJleHAiOjE3Nzg1NTE5NzV9.VjD2jHEd0tohjZDBvA6qudV9gdJm1dSP4XcyyhTvDJg'),(9,'2026-05-12 05:19:00.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NTE2MzQwLCJleHAiOjE3Nzg1NTIzNDB9.6EMcu7p1A6e-DF9g0xbk360rSv4JK57f-q4Rv6-R5LQ'),(10,'2026-05-12 22:23:32.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NTc3ODEyLCJleHAiOjE3Nzg2MTM4MTJ9.pV_-NFrpD60101e_fXD7cBNnmpCp95uYuK_9564Pi88'),(11,'2026-05-12 23:36:47.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NTgyMjA3LCJleHAiOjE3Nzg2MTgyMDd9.krQ53c6l1aYumE0yJE5aTTNEf19b8osegGT3A746bNE'),(12,'2026-05-14 04:16:00.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4Njg1MzYwLCJleHAiOjE3Nzg3MjEzNjB9.GE7t81Zb5Nf2VFzSBzlBko7pGXGJyzuTG_KCKjDYF9w'),(13,'2026-05-14 04:19:02.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1RVREVOVCJ9XSwic3ViIjoidGVzdHh4byIsImlhdCI6MTc3ODY4NTU0MiwiZXhwIjoxNzc4NzIxNTQyfQ.CdsEzH7BYxvxPqcxtPCCST_BpxdmARQq7PLPkMP5wog'),(14,'2026-05-14 04:19:26.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzYWRtaW4iLCJpYXQiOjE3Nzg2ODU1NjYsImV4cCI6MTc3ODcyMTU2Nn0.X6Uj88qxCQo-QqqGogoyAk_rbKoWzHOCKZb40If-vR0'),(15,'2026-05-14 04:44:01.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4Njg3MDQxLCJleHAiOjE3Nzg3MjMwNDF9.1QAKOW_aNlgZ7eUJPfkIVeGnOBO8fcruWdzOxrzdiMw'),(16,'2026-05-14 05:45:59.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NjkwNzU5LCJleHAiOjE3Nzg3MjY3NTl9.dUEt_q1rfls63FtLmi8eLrS8lrLbyKnaNcBbLrIk4DI'),(17,'2026-05-14 08:30:19.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzYWRtaW4iLCJpYXQiOjE3Nzg3MDA2MTksImV4cCI6MTc3ODczNjYxOX0.kTtwFR5TmxIY8ZcIBHOF0ORYQXV31WM9cUZpf8tr9y8'),(18,'2026-05-14 08:31:31.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1RVREVOVCJ9XSwic3ViIjoidGVzdDQ0NCIsImlhdCI6MTc3ODcwMDY5MSwiZXhwIjoxNzc4NzM2NjkxfQ.3B8rbuk6inTDuJUtq3E4ayd6KOcBs93pRK42UuCjWXo'),(19,'2026-05-14 08:32:15.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NzAwNzM1LCJleHAiOjE3Nzg3MzY3MzV9.yxNqgokcgozGvwbswJxVvy_uvyHfK_wfZHjkU3qtovc'),(20,'2026-05-14 10:27:31.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzYWRtaW4iLCJpYXQiOjE3Nzg3MDc2NTEsImV4cCI6MTc3ODc0MzY1MX0.ieQBXFko-Pu_gUBvGQyz5qkGzocS0CPWxatHZblXtK0'),(21,'2026-05-14 10:36:22.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzc4NzA4MTgyLCJleHAiOjE3Nzg3NDQxODJ9._ZqFgvmTRNoLIIevnIIq5ZCIheyxNkyrHVc4GCJWno0'),(22,'2026-05-14 13:05:10.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3Nzg3MTcxMTAsImV4cCI6MTc3ODc1MzExMH0.-lcjLH4MnHmCRdBzc8QtEARqMEFWIDNro2B_vmFlz44'),(23,'2026-07-25 11:30:32.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3ODQ5MzIyMzIsImV4cCI6MTc4NDk2ODIzMn0.PoatJwxKC6BE9p9nuLDTVGdBgM-WEBXcanYlxuInICw'),(24,'2026-08-02 01:31:45.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzg1NTg3NTA1LCJleHAiOjE3ODU2MjM1MDV9.P5avOtxbudgNDkqL9n_7smzeh7Jldv7icZDduGBJWuQ'),(25,'2026-08-02 02:18:11.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3ODU1OTAyOTEsImV4cCI6MTc4NTYyNjI5MX0.ZdM9tVPrap6YQyWEw4VXJeyCCSRRHAhph38151qonuI'),(26,'2026-08-02 02:18:42.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzg1NTkwMzIyLCJleHAiOjE3ODU2MjYzMjJ9.k-x1mo1kvBvdDRbNs1iEprOcn6X4Z8GEpIMcjonRfyM'),(27,'2026-08-02 02:36:50.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3ODU1OTE0MTAsImV4cCI6MTc4NTYyNzQxMH0.pu-XamcoMEOFrjVzoQzXRt40p7-rPAI5FHOWpuUiFRw'),(28,'2026-08-02 02:37:23.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzg1NTkxNDQzLCJleHAiOjE3ODU2Mjc0NDN9.A_eJsKnPVaiPJmq7Zig4Al_ncau7Svkp-B1mV1PXOKM'),(29,'2026-08-02 02:48:21.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzg1NTkyMTAxLCJleHAiOjE3ODU2MjgxMDF9.J4akVTf53w3twMpxHcks2bVEiavOz4HCcR7P_OOJm-g'),(30,'2026-08-02 02:55:54.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3ODU1OTI1NTQsImV4cCI6MTc4NTYyODU1NH0.1tyqNLMBgv8RrkepY11QnPdJL-KyrEiyqmrwJCA4n_o'),(31,'2026-08-02 02:57:44.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU1VQRVJfQURNSU4ifV0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzg1NTkyNjY0LCJleHAiOjE3ODU2Mjg2NjR9.zOnHLkt2TIxoDrbJVH3ykgUCoEGle28I_RkCOwbyd5M'),(32,'2026-08-02 03:10:45.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3ODU1OTM0NDUsImV4cCI6MTc4NTYyOTQ0NX0.LEghqgNh4gQTx_KXDVfTt4hJwK44HVjA-hh2_PwO3Hg'),(33,'2026-08-02 05:29:10.000000','eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiU0NIT09MX0FETUlOIn1dLCJzdWIiOiJzY2hvb2xfYWRtaW4iLCJpYXQiOjE3ODU2MDE3NTAsImV4cCI6MTc4NTYzNzc1MH0.s0qv3SRq9C0MvG6EzUJ8fMAqhEYZTqDW2lKk22lQb5A');
/*!40000 ALTER TABLE `token_blacklist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` binary(16) NOT NULL,
  `active` bit(1) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('PARENT','SCHOOL_ADMIN','STUDENT','SUPER_ADMIN','TEACHER') NOT NULL,
  `username` varchar(255) NOT NULL,
  `school_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `full_name` varchar(120) NOT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  KEY `idx_users_school` (`school_id`),
  KEY `idx_users_role` (`role`),
  CONSTRAINT `FK3gj5j7vnsoxf1wp9n5hsqdiq3` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (_binary '\ EÄ\Ÿ\ﬂL¬ØëÖ3o|',_binary '','test-student@gmail.com','$2a$10$ndfutPud32OTTsA/gb64juvXp2uqlgAyf1z2gyP2pFMuj109DCiXa','STUDENT','test-student',10,'2026-08-03 04:58:52.881857','2026-08-03 04:58:52.881857',NULL,'student','0120000052244'),(_binary 'gá\Î\»O7å\Œ^µD\Ï\„x',_binary '','newsadmin@gmail.com','$2a$10$xGBXddv3LNKbAe5pj9fxluWZphhYe6GpbRVQxjbOV6kujKhfEluCC','SCHOOL_ADMIN','newsadmin',10,'2026-08-03 00:38:17.068147','2026-08-04 17:30:52.230711','/uploads/avatars/1b678715-ebc8-4f37-8cce-5eb544ece378.png','school-admin','01200000522'),(_binary '1RI∑BGvù	ò\Ãu%\‰\‹',_binary '','s-admin@gmail.com','$2a$10$6rsIFWrRkGETeC77ucZhwOrkep7XQkKn7Rmyhnd4D1U8.oyR3Lhfi','SCHOOL_ADMIN','s-admin',11,'2026-08-05 16:36:34.010356','2026-08-05 16:36:34.010356',NULL,'s-admin','01200000522344'),(_binary '10\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','testxxo@smartedu.com','$2a$10$2QLsjM8FAYr8q9nz7eMuGuJ84G8Ml8.IBgCP8Rz43aARMXbLNSQ5q','STUDENT','testxxo',NULL,NULL,NULL,NULL,'',NULL),(_binary '11\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','vvvv@gmail.com','$2a$10$5G.ZSwDb.dJKm9un2zZPoODPt0OFuWz1aycKpY5.wKxOGsDHMiowa','TEACHER','vvvv',6,NULL,NULL,NULL,'',NULL),(_binary '12\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','mahmoudhamdyam@gmail.com','$2a$10$UnaNuD28jXV2NmVkZDLs.OoquL6X.1j7ZzqO1tvSFZ0mUx.WmF446','STUDENT','mahmoud',6,NULL,NULL,NULL,'',NULL),(_binary '2\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','mahmoudx@gmail.com','$2a$10$/fgAFZ/2oCZJwvxfxokGFubFQX9JyFB2qjH4wq6bpk6lbxK.EvyS6','SUPER_ADMIN','mahmoudx',NULL,NULL,NULL,NULL,'',NULL),(_binary '3\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','admin@smartedu.com','$2a$10$y2PYqJPYKJYyzUdXTmh11u1SW7cRo6M2XUUK483cLpJAO5rRO/uwS','SUPER_ADMIN','admin',NULL,NULL,'2026-08-04 17:29:35.157128','/uploads/avatars/33000000-0000-0000-0000-000000000000.png','Admin-Mahmoud','01288888880'),(_binary '4\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','test@smartedu.com','$2a$10$qYby4CDtoJNAXT01ce06yOCheY42Xa2mcpnNtRbua63Py3IzDklYS','STUDENT','test444',NULL,NULL,NULL,NULL,'',NULL),(_binary '5\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','test1@smartedu.com','$2a$10$SFIYdZ70v7zB0w9sCNjx8eN8wAZrc1dbLAoa5HUDfcraAGEzm1Aoq','STUDENT','test2',NULL,NULL,NULL,NULL,'',NULL),(_binary '7\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0',_binary '','testsssss@smartedu.com','$2a$10$uiomYEGmy.tpVJEB/0ugruaBoLBGnx6CTLB48NY23Kp.zbWvgKDSa','STUDENT','testxxxv',NULL,NULL,NULL,NULL,'',NULL),(_binary '<”ºëyFMÇªMX3',_binary '','admin@gmail.com','$2a$10$aWhKbJ/yLw.STp.IlYn3d.JUnBzRIiYoUS6LMkLD2r/Q3S4s3Dl/W','SCHOOL_ADMIN','school_admin2',4,'2026-08-04 01:09:20.253720','2026-08-04 01:09:20.253720',NULL,'Mahmoud','01200000522696'),(_binary '?Dè£tJ3™\∆	Ö\‘¯\0',_binary '','test-parant@gmail.com','$2a$10$yxsdtiYLfWiFd2WbbqHAceCeWpG4Jdk6tQi8ROFQNZpPb6u7LSk8O','PARENT','test-parant',10,'2026-08-03 04:58:14.576159','2026-08-03 04:58:14.576159',NULL,'parant','0120000052211'),(_binary 'ràe˚⁄∑Dÿ•Øº¸â/',_binary '','teacher-hamdy@gmail.com','$2a$10$jjLN528D9E4pDdepgMtFruEwRqRQY.eOpnKyjNisiE4H5N86vvFlu','TEACHER','teacher-hamdy',10,'2026-08-04 17:40:02.428479','2026-08-04 17:40:02.428479',NULL,'teacher-hamdy','01200636336'),(_binary '{\Ò∑ùq-BUïòqR\⁄\Z0\⁄',_binary '','test-teacher@gmail.com','$2a$10$Qi//MzoLpDan3Zl36maFHeySgevvZrfwNeDJ1RTAfYuRdZ5JbBYIS','TEACHER','test-teacher',10,'2026-08-03 04:57:09.548685','2026-08-04 17:38:17.079834',NULL,'Teacher-Mahmoud','0120000052222'),(_binary 'Ü±\Ïr˙BRæJö_\ˆ\‰\ﬂ',_binary '','rrrrrrr@gmail.com','$2a$10$Q/R6Bl1rISk7ys.ESgwlmuPvhF03Nm28dKLg5ywgzgzTuHgRmB9ee','TEACHER','rrrrr',10,'2026-08-05 00:24:03.166557','2026-08-05 00:24:03.166557',NULL,'Mahmoud man','01200000522'),(_binary 'ã§2\⁄_D∞∑7*w⁄òà',_binary '','school_admin@gmail.com','$2a$10$Qaq86a9hA647eTIdLk6.ROV.nkmNYNT8/2CHMybd1NQQ5.Oj2j7L2','SCHOOL_ADMIN','school_admin',1,'2026-08-04 01:08:40.256922','2026-08-04 01:08:40.256922',NULL,'test','01200000522342'),(_binary '\€\”˚B\≈\ƒE¬¨:&MJ OÑ',_binary '','school_admin3@gmail.com','$2a$10$OX9VUsA6KEuDZeKal3aoyuMUP4dWxcWq029jR4sdRWDBG.q6UI2SW','SCHOOL_ADMIN','school_admin3',6,'2026-08-04 01:09:53.616292','2026-08-04 01:09:53.616292',NULL,'Mahmoudx','012000005228998');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-05 19:51:47
