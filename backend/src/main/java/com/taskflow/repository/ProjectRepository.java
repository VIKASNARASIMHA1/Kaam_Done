package com.taskflow.repository;

import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    Optional<Project> findByIdAndOwner(Long id, User owner);

    @Query("select distinct p from Project p left join p.members m " +
            "where p.owner = :user or m.user = :user order by p.createdAt desc")
    List<Project> findAccessibleByUser(@Param("user") User user);
}
