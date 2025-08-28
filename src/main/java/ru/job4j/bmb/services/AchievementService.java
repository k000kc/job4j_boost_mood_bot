package ru.job4j.bmb.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Service;

@Service
public class AchievementService implements BeanNameAware {

    private String beanName;

    @PostConstruct
    public void init() {
        System.out.println("AchievementService is going through init.");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("AchievementService will be destroyed now");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println(beanName);
    }
}
