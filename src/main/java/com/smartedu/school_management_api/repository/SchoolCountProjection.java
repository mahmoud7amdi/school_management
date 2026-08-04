package com.smartedu.school_management_api.repository;

/**
 * One aggregate count keyed by school, for the grouped report queries.
 *
 * <p>Exists so the platform report can total students, staff and admins for every school
 * in a fixed number of queries instead of one per school.
 */
public interface SchoolCountProjection {

    Long getSchoolId();

    long getTotal();
}
