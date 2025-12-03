package ru.job4j.bmb.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.job4j.bmb.content.Content;
import ru.job4j.bmb.model.Achievemant;
import ru.job4j.bmb.model.Mood;
import ru.job4j.bmb.model.MoodLog;
import ru.job4j.bmb.model.User;
import ru.job4j.bmb.repository.AchievementRepository;
import ru.job4j.bmb.repository.MoodLogRepository;
import ru.job4j.bmb.repository.UserRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MoodService {
    private final ApplicationEventPublisher publisher;
    private final MoodLogRepository moodLogRepository;
    private final RecommendationEngine recommendationEngine;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("dd-MM-yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    public MoodService(ApplicationEventPublisher publisher,
                       MoodLogRepository moodLogRepository,
                       RecommendationEngine recommendationEngine,
                       UserRepository userRepository,
                       AchievementRepository achievementRepository) {
        this.publisher = publisher;
        this.moodLogRepository = moodLogRepository;
        this.recommendationEngine = recommendationEngine;
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
    }

    public Content chooseMood(User user, Long moodId) {
        Mood mood = new Mood();
        mood.setId(moodId);
        MoodLog moodLog = new MoodLog();
        moodLog.setMood(mood);
        moodLog.setUser(user);
        moodLog.setCreateAt(Instant.now().getEpochSecond());
        moodLogRepository.save(moodLog);
        publisher.publishEvent(new UserEvent(this, user));
        return recommendationEngine.recommendFor(user.getChatId(), moodId);
    }

    public Optional<Content> weekMoodLogCommand(long chatId, Long clientId) {
        Instant sevenDay = Instant.now().minus(7, ChronoUnit.DAYS);
        List<MoodLog> logs = moodLogRepository.findAll()
                .stream()
                .filter(moodLog -> moodLog.getId().equals(clientId))
                .filter(moodLog -> moodLog.getCreateAt() == sevenDay.getEpochSecond())
                .toList();
        var content = new Content(chatId);
        content.setText(formatMoodLogs(logs, "Лог за 7 дней"));
        return Optional.of(content);
    }

    public Optional<Content> monthMoodLogCommand(long chatId, Long clientId) {
        Instant oneMonth = Instant.now().minus(1, ChronoUnit.MONTHS);
        List<MoodLog> logs = moodLogRepository.findAll()
                .stream()
                .filter(moodLog -> moodLog.getId().equals(clientId))
                .filter(moodLog -> moodLog.getCreateAt() == oneMonth.getEpochSecond())
                .toList();
        var content = new Content(chatId);
        content.setText(formatMoodLogs(logs, "Лог за 1 месяц"));
        return Optional.of(content);
    }

    private String formatMoodLogs(List<MoodLog> logs, String title) {
        if (logs.isEmpty()) {
            return title + ":\nNo mood logs found";
        }
        var sb = new StringBuilder(title + ":\n");
        logs.forEach(log -> {
            String formattedDate = formatter.format(Instant.ofEpochSecond(log.getCreateAt()));
            sb.append(formattedDate).append(": ").append(log.getMood().getText()).append("\n");
        });
        return sb.toString();
    }

    public Optional<Content> awards(long chatId, Long clientId) {
        List<Achievemant> achievemants = achievementRepository.findAll()
                .stream()
                .filter(achievemant -> achievemant.getUser().getClientId() == clientId)
                .toList();
        var content = new Content(chatId);
        content.setText(formatAwardLogs(achievemants, "Awards"));
        return Optional.of(content);
    }

    private String formatAwardLogs(List<Achievemant> logs, String title) {
        if (logs.isEmpty()) {
            return title + ":\nNo award logs found";
        }
        var sb = new StringBuilder(title + ":\n");
        logs.forEach(log -> {
            String formattedDate = formatter.format(Instant.ofEpochSecond(log.getCreateAt()));
            sb.append(formattedDate).append(": ").append(log.getAward().getTitle()).append("\n");
        });
        return sb.toString();
    }
}
