package cat.itacademy.s05.t02.service.pet;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetAction;
import cat.itacademy.s05.t02.domain.PetColor;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.service.engine.PetRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetRulesTest {

    private PetEntity basePet() {
        return PetEntity.builder()
                .name("Axo")
                .color(PetColor.PINK)
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
    @DisplayName("FEED: Hunger -30, +5 stamina, +7 happiness if very hungry, XP 10 if hunger>=70")
    void feed_with_bonus_when_hungry() {
        PetEntity p = basePet();
        p.setHunger(80); // bonus XP active

        var res = PetRules.apply(p, PetAction.FEED);

        assertEquals(50, p.getHunger());
        assertEquals(55, p.getStamina());
        assertEquals(57, p.getHappiness());
        assertEquals(1, p.getLevel());
        assertEquals(10, p.getXpInLevel());
        assertTrue(res.message().contains("Fed"));
        assertEquals(10, res.xpGained());
    }

    @Test
    @DisplayName("FEED: clamp hunger to 0 when <30, slight penalty for overfeeding, XP 2")
    void feed_with_overfeeding() {
        PetEntity p = basePet();
        p.setHunger(10); // veryLowHunger

        var res = PetRules.apply(p, PetAction.FEED);

        assertEquals(0, p.getHunger());
        assertEquals(55, p.getStamina());
        assertEquals(47, p.getHappiness());
        assertEquals(2, p.getXpInLevel());
        assertTrue(res.message().contains("Fed"));
        assertEquals(2, res.xpGained());
    }

    // ===== PLAY =====

    @Test
    @DisplayName("PLAY: normal branch (stamina>=30): happiness +15, stamina -20, hunger +10, XP +10")
    void play_normal_branch() {
        PetEntity p = basePet();
        p.setStamina(40);

        var res = PetRules.apply(p, PetAction.PLAY);

        assertEquals(20, p.getStamina());
        assertEquals(65, p.getHappiness());
        assertEquals(60, p.getHunger());
        assertEquals(10, p.getXpInLevel());
        assertEquals(1, p.getLevel());
        assertTrue(res.message().startsWith("Played"));
        assertEquals(10, res.xpGained());
    }

    @Test
    @DisplayName("PLAY: tired branch (stamina<30): happiness +8 (then -2 global), stamina -20 → clamp 0, hunger +15, XP +8")
    void play_tired_branch() {
        PetEntity p = basePet();
        p.setStamina(15);

        var res = PetRules.apply(p, PetAction.PLAY);

        assertEquals(0, p.getStamina());
        assertEquals(56, p.getHappiness());
        assertEquals(65, p.getHunger());
        assertEquals(8, p.getXpInLevel());
        assertTrue(res.message().toLowerCase().contains("tired"));
        assertEquals(8, res.xpGained());
    }

    // ===== TRAIN =====

    @Test
    @DisplayName("TRAIN (stamina=35): considered 'tired' with current rules (threshold <40): stamina -30, hunger +25, happiness 53, XP +15")
    void train_with_stamina_35_treated_as_tired() {
        PetEntity p = basePet();
        p.setStamina(35);

        var res = PetRules.apply(p, PetAction.TRAIN);

        assertEquals(5, p.getStamina());
        assertEquals(75, p.getHunger());
        assertEquals(53, p.getHappiness());
        assertEquals(15, p.getXpInLevel());
        assertEquals(1, p.getLevel());
        assertTrue(res.message().toLowerCase().contains("tired"));
        assertEquals(15, res.xpGained());
    }

    @Test
    @DisplayName("TRAIN: tired branch (stamina<30): stamina -30 → 0, hunger +25, happiness 53 (after -2 global), XP +15")
    void train_low_stamina_branch() {
        PetEntity p = basePet();
        p.setStamina(25);

        var res = PetRules.apply(p, PetAction.TRAIN);

        assertEquals(0, p.getStamina());
        assertEquals(75, p.getHunger());
        assertEquals(53, p.getHappiness());
        assertEquals(15, p.getXpInLevel());
        assertTrue(res.message().toLowerCase().contains("tired"));
        assertEquals(15, res.xpGained());
    }

    // ===== REST =====

    @Test
    @DisplayName("REST: exhausted (<30) => stamina +30, hunger +10, +5 happiness, no XP")
    void rest_when_exhausted() {
        PetEntity p = basePet();
        p.setStamina(20);

        var res = PetRules.apply(p, PetAction.REST);

        assertEquals(50, p.getStamina());
        assertEquals(60, p.getHunger());
        assertEquals(55, p.getHappiness());
        assertEquals(0, p.getXpInLevel());
        assertEquals(0, res.xpGained());
        assertTrue(res.message().startsWith("Rested well"));
    }

    @Test
    @DisplayName("REST: not exhausted (>=30) => no happiness bonus, no XP")
    void rest_not_exhausted() {
        PetEntity p = basePet();
        p.setStamina(35);

        var res = PetRules.apply(p, PetAction.REST);

        assertEquals(65, p.getStamina());
        assertEquals(60, p.getHunger());
        assertEquals(50, p.getHappiness());
        assertEquals(0, p.getXpInLevel());
        assertEquals(0, res.xpGained());
        assertTrue(res.message().startsWith("Rested"));
    }

    // ===== XP / Levels / Cap =====

    @Test
    @DisplayName("Level Up: collects XP, levels up, and carries over surplus")
    void level_up_accumulates_and_carries_over() {
        PetEntity p = basePet();
        p.setXpInLevel(95);

        PetRules.apply(p, PetAction.PLAY);

        assertEquals(2, p.getLevel());
        assertEquals(5, p.getXpInLevel());
    }

    @Test
    @DisplayName("Level 15 Cap: once level 15 is reached, XP stays at 0 and does not increase further")
    void level_cap_at_15() {
        PetEntity p = basePet();

        p.setLevel(15);
        p.setXpInLevel(90);

        PetRules.apply(p, PetAction.PLAY);

        assertEquals(15, p.getLevel());
        assertEquals(0, p.getXpInLevel());
    }
}
