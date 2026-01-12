package ru.job4j.bmb.services;

import org.junit.jupiter.api.Test;
import ru.job4j.bmb.content.Content;
import ru.job4j.bmb.model.Mood;
import ru.job4j.bmb.model.MoodLog;
import ru.job4j.bmb.model.User;
import ru.job4j.bmb.repository.MoodFakeRepository;
import ru.job4j.bmb.repository.MoodLogFakeRepository;
import ru.job4j.bmb.repository.MoodLogRepository;
import ru.job4j.bmb.repository.MoodRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReminderServiceTest {

    @Test
    public void whenMoodGood() {
        var result = new ArrayList<Content>();
        var sentContent = new SentContent() {
            @Override
            public void sent(Content content) {
                result.add(content);
            }
        };
        var moodRepository = new MoodFakeRepository();
        moodRepository.save(new Mood("Good", true));
        var moodLogRepository = new MoodLogFakeRepository();
        var user = new User();
        user.setChatId(100);
        var moodLog = new MoodLog();
        moodLog.setUser(user);
        var yesterday = LocalDate.now()
                .minusDays(10)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1;
        moodLog.setCreatedAt(yesterday);
        moodLogRepository.save(moodLog);
        var tgUI = new TgUI(moodRepository);
        new ReminderService(sentContent, moodLogRepository, tgUI)
                .remindUsers();
        assertThat(result.iterator().next().getMarkup().getKeyboard()
                .iterator().next().iterator().next().getText()).isEqualTo("Good");
    }

    @Test
    public void whenMoodGoodAndUserVotedTenDaysAgoThenResultListContainsThisUser() {
        List<Content> result = new ArrayList<>();
        var sentContent = new SentContent() {
            @Override
            public void sent(Content content) {
                result.add(content);
            }
        };
        MoodRepository moodRepository = new MoodFakeRepository();
        MoodLogRepository moodLogRepository = new MoodLogFakeRepository();
        MoodLog moodLog = new MoodLog();
        User user = new User();
        user.setChatId(100);
        moodRepository.save(new Mood("Good", true));
        moodLog.setUser(user);
        long yesterday = LocalDate.now()
                .minusDays(10)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1;
        moodLog.setCreatedAt(yesterday);
        moodLogRepository.save(moodLog);
        TgUI tgUI = new TgUI(moodRepository);
        new ReminderService(sentContent, moodLogRepository, tgUI)
                .remindUsers();
        assertThat(result.iterator().next().getMarkup().getKeyboard()
                .iterator().next().iterator().next().getText()).isEqualTo("Good");
    }

    @Test
    public void whenMoodBadAndUserVotedTenDaysAgoThenResultListContainsThisUser() {
        List<Content> result = new ArrayList<>();
        var sentContent = new SentContent() {
            @Override
            public void sent(Content content) {
                result.add(content);
            }
        };
        MoodRepository moodRepository = new MoodFakeRepository();
        MoodLogRepository moodLogRepository = new MoodLogFakeRepository();
        MoodLog moodLog = new MoodLog();
        User user = new User();
        user.setChatId(100);
        moodRepository.save(new Mood("Bad", false));
        moodLog.setUser(user);
        long yesterday = LocalDate.now()
                .minusDays(2)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1;
        moodLog.setCreatedAt(yesterday);
        moodLogRepository.save(moodLog);
        TgUI tgUI = new TgUI(moodRepository);
        new ReminderService(sentContent, moodLogRepository, tgUI)
                .remindUsers();
        assertThat(result.iterator().next().getMarkup().getKeyboard()
                .iterator().next().iterator().next().getText()).isEqualTo("Bad");
    }

    @Test
    public void whenMoodGoodAndVotedTodayThenReturnEmptyResultList() {
        List<Content> result = new ArrayList<>();
        var sentContent = new SentContent() {
            @Override
            public void sent(Content content) {
                result.add(content);
            }
        };
        MoodRepository moodRepository = new MoodFakeRepository();
        MoodLogRepository moodLogRepository = new MoodLogFakeRepository();
        MoodLog moodLog = new MoodLog();
        User user = new User();
        user.setChatId(100);
        moodRepository.save(new Mood("Bad", false));
        moodLog.setUser(user);
        long yesterday = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .plusHours(5)
                .toInstant()
                .toEpochMilli() - 1;
        moodLog.setCreatedAt(yesterday);
        moodLogRepository.save(moodLog);
        TgUI tgUI = new TgUI(moodRepository);
        new ReminderService(sentContent, moodLogRepository, tgUI)
                .remindUsers();
        assertThat(result.iterator().hasNext()).isFalse();
    }
}