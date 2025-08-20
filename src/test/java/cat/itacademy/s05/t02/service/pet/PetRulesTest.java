package cat.itacademy.s05.t02.service.pet;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetAction;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.service.engine.PetRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetRulesTest {

    private PetEntity petBase() {
        return PetEntity.builder()
                .name("Axo")
                .color("pink")
                .hunger(50)
                .stamina(50)
                .happiness(50)
                .level(1)
                .xpInLevel(0)
                .stage(EvolutionStage.BABY)
                .build();
    }

    // ===== FEED =====

    @Test
    @DisplayName("FEED: Hunger -30 (clamp >=0), +5 stamina, +5 happiness, XP +5 (+5 extra si hunger>=70)")
    void feed_basic_bonus_when_hungry() {
        PetEntity p = petBase();
        p.setHunger(80); // activa bonus XP

        var res = PetRules.apply(p, PetAction.FEED);

        assertEquals(50, p.getHunger());   // 80 - 30
        assertEquals(55, p.getStamina());  // +5
        assertEquals(55, p.getHappiness()); // +5
        assertEquals(1, p.getLevel());
        assertEquals(10, p.getXpInLevel()); // 5 base +5 bonus
        assertTrue(res.message.contains("Fed"));
        assertEquals(10, res.xpGained);
    }

    @Test
    @DisplayName("FEED: clamp en 0 cuando hunger <30")
    void feed_clamp_floor() {
        PetEntity p = petBase();
        p.setHunger(10);

        var res = PetRules.apply(p, PetAction.FEED);

        assertEquals(0, p.getHunger());    // 10 - 30 -> clamp 0
        assertEquals(55, p.getStamina());  // +5
        assertEquals(55, p.getHappiness()); // +5
        assertEquals(5, p.getXpInLevel()); // sin bonus (hunger<70)
        assertTrue(res.message.contains("Fed"));
        assertEquals(5, res.xpGained);
    }

    // ===== PLAY =====

    @Test
    @DisplayName("PLAY: rama normal (stamina>=20): happiness +15, stamina -20, hunger +10, XP +10")
    void play_normal_branch() {
        PetEntity p = petBase();
        p.setStamina(40); // no lowStamina

        var res = PetRules.apply(p, PetAction.PLAY);

        assertEquals(20, p.getStamina());    // 40 - 20
        assertEquals(65, p.getHappiness());  // 50 + 15
        assertEquals(60, p.getHunger());     // 50 + 10
        assertEquals(10, p.getXpInLevel());  // +10
        assertEquals(1, p.getLevel());
        assertTrue(res.message.startsWith("Played:"));
        assertEquals(10, res.xpGained);
    }

    @Test
    @DisplayName("PLAY: rama 'tired' (stamina<20): happiness +8, stamina -20 (clamp 0), hunger +15, XP +10")
    void play_low_stamina_branch() {
        PetEntity p = petBase();
        p.setStamina(15);

        var res = PetRules.apply(p, PetAction.PLAY);

        assertEquals(0, p.getStamina());
        assertEquals(58, p.getHappiness());
        assertEquals(65, p.getHunger());
        assertEquals(10, p.getXpInLevel());  // +10
        assertTrue(res.message.contains("tired"));
        assertEquals(10, res.xpGained);
    }

    // ===== TRAIN =====

    @Test
    @DisplayName("TRAIN: rama normal (stamina>=30): stamina -30, hunger +20, happiness +5, XP +25")
    void train_normal_branch() {
        PetEntity p = petBase();
        p.setStamina(35);

        var res = PetRules.apply(p, PetAction.TRAIN);

        assertEquals(5, p.getStamina());     // 35 - 30
        assertEquals(70, p.getHunger());     // 50 + 20
        assertEquals(55, p.getHappiness());  // 50 + 5
        assertEquals(25, p.getXpInLevel());  // +25
        assertEquals(1, p.getLevel());
        assertTrue(res.message.startsWith("Training:"));
        assertEquals(25, res.xpGained);
    }

    @Test
    @DisplayName("TRAIN: rama 'tired' (stamina<30): stamina -30 (clamp 0), hunger +20, happiness +5, XP +15")
    void train_low_stamina_branch() {
        PetEntity p = petBase();
        p.setStamina(25); // lowStamina=true

        var res = PetRules.apply(p, PetAction.TRAIN);

        assertEquals(0, p.getStamina());     // 25 - 30 -> 0
        assertEquals(70, p.getHunger());     // 50 + 20
        assertEquals(55, p.getHappiness());  // +5
        assertEquals(15, p.getXpInLevel());  // +15
        assertTrue(res.message.contains("tired"));
        assertEquals(15, res.xpGained);
    }

    // ===== REST =====

    @Test
    @DisplayName("REST: exhausted (<30) => stamina +30, hunger +10, happiness +5 extra, no XP")
    void rest_changes_with_bonus_when_exhausted() {
        PetEntity p = petBase();
        p.setStamina(20); // exhausto

        var res = PetRules.apply(p, PetAction.REST);

        assertEquals(50, p.getStamina());   // 20 + 30
        assertEquals(60, p.getHunger());    // 50 + 10
        assertEquals(55, p.getHappiness()); // 50 + 5
        assertEquals(0, p.getXpInLevel());  // sin XP
        assertEquals(0, res.xpGained);
        assertTrue(res.message.startsWith("Rested well:"));
    }

    @Test
    @DisplayName("REST: not exhausted (>=30) => no happiness bonus, no XP")
    void rest_not_exhausted() {
        PetEntity p = petBase();
        p.setStamina(35); // no exhausto

        var res = PetRules.apply(p, PetAction.REST);

        assertEquals(65, p.getStamina());   // 35 + 30
        assertEquals(60, p.getHunger());    // 50 + 10
        assertEquals(50, p.getHappiness()); // sin +5
        assertEquals(0, p.getXpInLevel());  // sin XP
        assertEquals(0, res.xpGained);
        assertTrue(res.message.startsWith("Rested:"));
    }

    // ===== XP / Niveles / Cap =====

    @Test
    @DisplayName("Level Up: Collect XP, level up, and keep surplus XP; recalcStage has you covered. PetService")
    void level_up_accumulates_and_carries_over() {
        PetEntity p = petBase();
        p.setXpInLevel(95); // a 5 del nivel 2

        PetRules.apply(p, PetAction.PLAY); // +10 XP

        assertEquals(2, p.getLevel());
        assertEquals(5, p.getXpInLevel()); // 95+10=105 => sube a 2 y quedan 5
    }

    @Test
    @DisplayName("Level 15 Cap: Upon reaching level 15, level XP resets to 0 and no longer grows.")
    void level_cap_at_15() {
        PetEntity p = petBase();

        p.setLevel(15);
        p.setXpInLevel(90);

        PetRules.apply(p, PetAction.PLAY); // intenta sumar +10

        assertEquals(15, p.getLevel());
        assertEquals(0, p.getXpInLevel()); // cap
    }
}

