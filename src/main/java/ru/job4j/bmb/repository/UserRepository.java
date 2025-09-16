package ru.job4j.bmb.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.bmb.model.User;

import java.util.List;

@Repository
public interface UserRepository {
    List<User> findAll();

    User findByClientId(Long clientId);

    void save(User user);
}
