package ru.job4j.bmb.services;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementService implements ApplicationListener<UserEvent> {


    @Transactional
    @Override
    public void onApplicationEvent(UserEvent event) {
        var user = event.getUser();
    }
}
