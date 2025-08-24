package cat.itacademy.s05.t02.service.engine;

import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PetStatsScheduler {

    private final PetRepository pets;

    // Cada 15 minutos (cron fijo), ajusta a tu gusto
    @Scheduled(fixedRate = 1 * 5 * 1000L, initialDelay = 30 * 1000L)
    @Transactional
    public void degradeStats() {
        List<PetEntity> all = pets.findAll();
        if (all.isEmpty()) return;

        for (PetEntity p : all) {
            p.setHunger(clamp(p.getHunger() + 2)); // +1 cada 15 min => +4/h aprox si lo prefieres sube a +2
            int staminaDrop = (p.getHunger() >= 80) ? 2 : 1;
            p.setStamina(clamp(p.getStamina() - staminaDrop));

            int happinessDrop = 0;
            if (p.getHunger() >= 70 && p.getStamina() <= 30) happinessDrop = 2;
            else if (p.getHunger() >= 80 || p.getStamina() <= 20) happinessDrop = 2;

            if (happinessDrop > 0) {
                p.setHappiness(clamp(p.getHappiness() - happinessDrop));
            }
        }
        pets.saveAll(all);
        log.debug("Pet stats degraded for {} pets", all.size());
    }

    private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}
