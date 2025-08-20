package cat.itacademy.s05.t02.service.engine;

import cat.itacademy.s05.t02.domain.PetAction;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;

public final class PetRules {
    private PetRules() {}

    public record Result(int xpGained, String message) {}

    public static Result apply(PetEntity pet, PetAction action) {
        int beforeLevel = pet.getLevel();
        int gained = 0;
        String message = "";

        switch (action) {
            case FEED -> {
                int xp = 5 + (pet.getHunger() >= 70 ? 5 : 0);
                pet.setHunger(dec(pet.getHunger(), 30));
                pet.setStamina(inc(pet.getStamina(), 5));
                pet.setHappiness(inc(pet.getHappiness(), 5));
                addXp(pet, xp);
                gained = xp;
                message = "Fed: Hunger -30, Stamina +5, Happiness +5.";
            }
            case PLAY -> {
                boolean lowStamina = pet.getStamina() < 20;
                int hap = lowStamina ? 8 : 15;
                int hung = lowStamina ? 15 : 10;
                int xp = 10;
                pet.setHappiness(inc(pet.getHappiness(), hap));
                pet.setStamina(dec(pet.getStamina(), 20));
                pet.setHunger(inc(pet.getHunger(), hung));
                addXp(pet, xp);
                gained = xp;
                message = lowStamina
                        ? "Played (tired): Happiness +8, Stamina -20, Hunger +15."
                        : "Played: Happiness +15, Stamina -20, Hunger +10.";
            }
            case TRAIN -> {
                boolean lowStamina = pet.getStamina() < 30;
                int xp = lowStamina ? 15 : 25;
                pet.setStamina(dec(pet.getStamina(), 30));
                pet.setHunger(inc(pet.getHunger(), 20));
                pet.setHappiness(inc(pet.getHappiness(), 5));
                addXp(pet, xp);
                gained = xp;
                message = lowStamina
                        ? "Training (tired): XP +15, Stamina -30, Hunger +20, Happiness +5."
                        : "Training: XP +25, Stamina -30, Hunger +20, Happiness +5.";
            }
            case REST -> {
                boolean wasExhausted = pet.getStamina() < 30;
                pet.setStamina(inc(pet.getStamina(), 30));
                pet.setHunger(inc(pet.getHunger(), 10));
                if (wasExhausted) pet.setHappiness(inc(pet.getHappiness(), 5));
                gained = 0;
                message = wasExhausted
                        ? "Rested well: Stamina +30, Hunger +10, Happiness +5."
                        : "Rested: Stamina +30, Hunger +10.";
            }
        }

        if (pet.getLevel() != beforeLevel) {
            pet.recalcStage();
        }
        return new Result(gained, message);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }
    private static int inc(int v, int d) { return clamp(v + d); }
    private static int dec(int v, int d) { return clamp(v - d); }

    private static void addXp(PetEntity pet, int delta) {
        if (pet.getLevel() >= 15) { pet.setXpInLevel(0); return; }
        int xp = pet.getXpInLevel() + delta;
        while (xp >= 100 && pet.getLevel() < 15) {
            xp -= 100;
            pet.setLevel(pet.getLevel() + 1);
        }
        if (pet.getLevel() >= 15) {
            pet.setLevel(15);
            pet.setXpInLevel(0);
        } else {
            pet.setXpInLevel(xp);
        }
    }
}

