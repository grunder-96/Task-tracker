package org.edu.pet.task_tracker_scheduler.repository;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_scheduler.model.User;
import org.edu.pet.task_tracker_scheduler.model.UserTask;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportDataRepository {

    private static final String FIND_TASKS_FOR_REPORT_SQL = """
            SELECT u.id, u.email, t.title, t.done from users u
            JOIN user_tasks t ON t.user_id = u.id
            WHERE (t.done_at >= :start AND t.done_at < :end) OR t.done_at IS NULL
            """;

    private final JdbcClient jdbcClient;

    public Map<User, List<UserTask>> findTasksForReportMap(Instant start, Instant end) {

        return jdbcClient.sql(FIND_TASKS_FOR_REPORT_SQL)
            .param("start", start.atOffset(ZoneOffset.UTC))
            .param("end", end.atOffset(ZoneOffset.UTC))
            .query(rs -> {

                Map<User, List<UserTask>> map = new HashMap<>();

                while (rs.next()) {

                    User user = new User(rs.getLong("id"), rs.getString("email"));
                    UserTask userTask = new UserTask(rs.getString("title"), rs.getBoolean("done"));

                    map.computeIfAbsent(user, key -> new ArrayList<>())
                        .add(userTask);
                }

                return map;
            });
    }
}