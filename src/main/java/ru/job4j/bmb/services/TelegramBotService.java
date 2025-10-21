package ru.job4j.bmb.services;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotService implements BeanNameAware {

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println(beanName);
    }
}
