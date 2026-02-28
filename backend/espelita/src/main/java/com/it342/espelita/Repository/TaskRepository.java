package com.it342.espelita.Repository;

import com.it342.espelita.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndProjectId(Long userId, Long projectId);
}
