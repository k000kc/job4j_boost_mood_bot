package ru.job4j.bmb.services;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.bmb.content.Content;
import ru.job4j.bmb.model.Achievement;
import ru.job4j.bmb.model.Award;
import ru.job4j.bmb.model.MoodLog;
import ru.job4j.bmb.model.User;
import ru.job4j.bmb.repository.AchievementRepository;
import ru.job4j.bmb.repository.AwardRepository;
import ru.job4j.bmb.repository.MoodLogRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService implements ApplicationListener<UserEvent> {

    private final MoodLogRepository moodLogRepository;
    private final AwardRepository awardRepository;
    private final AchievementRepository achievementRepository;
    private final SentContent sentContent;

    public AchievementService(MoodLogRepository moodLogRepository,
                              AwardRepository awardRepository,
                              AchievementRepository achievementRepository,
                              SentContent sentContent) {
        this.moodLogRepository = moodLogRepository;
        this.awardRepository = awardRepository;
        this.achievementRepository = achievementRepository;
        this.sentContent = sentContent;
    }

    @Transactional
    @Override
    public void onApplicationEvent(UserEvent event) {
        var user = event.getUser();
        long now = System.currentTimeMillis();
        long sixtyDaysAgo = now - 60L * 24 * 60 * 60 * 1000;
        List<MoodLog> logs = moodLogRepository
                .findByUserClientIdAndCreatedAtBetween(
                        user.getClientId(), sixtyDaysAgo, now
                );
        long streak = calculateStreak(logs);
        List<Award> awards = awardRepository.findAll();
        for (Award award : awards) {
            if (streak >= award.getDays()) {
                boolean alreadyReceived = achievementRepository
                        .existsByUserClientIdAndAwardId(user.getClientId(), award.getId());
                if (!alreadyReceived) {
                    Achievement achievemant = new Achievement();
                    achievemant.setAward(award);
                    achievemant.setUser(user);
                    achievemant.setCreateAt(now);
                    achievementRepository.save(achievemant);
                    sendAchivementNotification(user, award);
                }
            }
         }
    }

    private long calculateStreak(List<MoodLog> logs) {
        if (logs.isEmpty()) {
            return 0;
        }
        Map<LocalDate, Boolean> goodByDay = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> Instant.ofEpochMilli(log.getCreatedAt())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                        Collectors.mapping(
                                l -> l.getMood().isGood(),
                                Collectors.reducing(false, (a, b) -> a || b)
                        )
                ));
        long streak = 0;
        LocalDate today = LocalDate.now();
        while (goodByDay.getOrDefault(today, false)) {
            streak++;
            today = today.minusDays(1);
        }
        return streak;
    }

    private void sendAchivementNotification(User user, Award award) {
        Content content = new Content(user.getChatId());
        content.setText("Вы получили достижение:\n\n" + award.getTitle() + "\n\n" + award.getDescription());
        sentContent.sent(content);
    }
}
