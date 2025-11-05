package ru.job4j.bmb.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.job4j.bmb.repository.UserRepository;

@Service
public class ReminderService implements BeanNameAware {

    private final SentContent sendContent;

    private final UserRepository userRepository;

    public ReminderService(SentContent sendContent, UserRepository userRepository) {
        this.sendContent = sendContent;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRateString = "${remind.period}")
    public void ping() {
    }

    @PostConstruct
    public void init() {
        System.out.println("ReminderService is going through init.");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("ReminderService will be destroyed now");
    }

    @Override
    public void setBeanName(String name) {
    }
}
