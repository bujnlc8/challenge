package site.haihui.challenge.daily;

import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class DailyChallengeSchedule {
    private final DailyChallengeService service;
    public DailyChallengeSchedule(DailyChallengeService service) {
        this.service = service;
    }

    @Bean
    public static Clock dailyChallengeClock() {
        return Clock.systemUTC();
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Shanghai")
    public void sendAtEight() {
        try {
            service.sendToday();
        } catch (RuntimeException e) {
            log.warn("Daily reminder batch could not complete; existing claims remain protected");
        }
    }
}
