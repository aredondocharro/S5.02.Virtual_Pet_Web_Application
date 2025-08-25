package cat.itacademy.s05.t02.service.engine;

import cat.itacademy.s05.t02.domain.PetAction;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;

public final class PetRules {
    private PetRules() {}

    private static final int EXHAUSTION_THRESHOLD = 15;

    public record Result(int xpGained, String message) {}

    // ===== Precondition checks =====
    public static String precondition(PetEntity p, PetAction a) {
        int h = p.getHunger();
        int s = p.getStamina();

        // Global exhaustion rule
        if (s <= EXHAUSTION_THRESHOLD && a != PetAction.REST) {
            return "Your pet is exhausted: it must rest before doing anything else.";
        }

        boolean starving = h >= 80;

        switch (a) {
            case PLAY -> {
                if (starving) return "Too hungry to play. Feed it first.";
                if (h > 70) return "Too hungry to enjoy playing.";
                if (s < 20) return "Too tired to play. Needs rest.";
            }
            case TRAIN -> {
                if (starving) return "Too hungry to train. Feed it first.";
                if (h > 60) return "Hunger is too high to train.";
                if (s < 30) return "Not enough energy to train.";
            }
            case FEED -> {
                return null;
            }
            case REST -> {
                return null;
            }
        }
        return null;
    }

    // ===== Apply effects =====
    public static Result apply(PetEntity pet, PetAction action) {
        int beforeLevel = pet.getLevel();
        int gained = 0;
        String message = "";

        switch (action) {
            case FEED -> {
                int h = pet.getHunger();
                boolean veryLowHunger = h <= 10;
                boolean veryHungry = h >= 70;

                int hungerDelta = 30;
                int staminaDelta = 5;
                int happinessDelta = 5;
                int xp = 5;

                if (veryHungry) {
                    xp += 5;          // bonus XP when feeding a hungry pet
                    happinessDelta += 2;
                } else if (veryLowHunger) {
                    // overfeeding penalty
                    happinessDelta = Math.max(0, happinessDelta - 8); // effectively -3 from current typical 5
                    pet.setHappiness(dec(pet.getHappiness(), 3));     // explicit small drop
                    xp = 2;
                }

                pet.setHunger(dec(pet.getHunger(), hungerDelta));
                pet.setStamina(inc(pet.getStamina(), staminaDelta));
                pet.setHappiness(inc(pet.getHappiness(), happinessDelta));

                addXp(pet, xp);
                gained = xp;
                message = veryHungry
                        ? "Fed (really hungry): Hunger -30, Stamina +5, Happiness +" + happinessDelta + ", bonus XP."
                        : veryLowHunger
                        ? "Fed without hunger: Hunger -30, Stamina +5, slight mood drop due to overfeeding."
                        : "Fed: Hunger -30, Stamina +5, Happiness +" + happinessDelta + ".";
            }
            case PLAY -> {
                int s = pet.getStamina();
                int happy = pet.getHappiness();
                boolean lowStamina = s < 20;              // allowed? precondition prevents <20; kept for message shape
                boolean highHappiness = happy >= 85;      // diminishing returns

                int happinessDelta = 15;
                int staminaDelta = 20;
                int hungerDelta = 10;
                int xp = 10;

                if (s < 30) { // tired but still allowed (>=20)
                    happinessDelta = 8;
                    hungerDelta = 15;
                    xp = 8;
                }
                if (highHappiness) {
                    happinessDelta = Math.min(happinessDelta, 5);
                    xp = Math.max(1, Math.round(xp * 0.6f));
                }

                pet.setHappiness(inc(pet.getHappiness(), happinessDelta));
                pet.setStamina(dec(pet.getStamina(), staminaDelta));
                pet.setHunger(inc(pet.getHunger(), hungerDelta));

                addXp(pet, xp);
                gained = xp;

                if (s < 30 && !highHappiness) {
                    message = "Played while tired: Happiness +" + happinessDelta + ", Stamina -20, Hunger +" + hungerDelta + ".";
                } else if (highHappiness) {
                    message = "Already very happy: small mood gain, reduced XP.";
                } else {
                    message = "Played: Happiness +15, Stamina -20, Hunger +10.";
                }
            }
            case TRAIN -> {
                int s = pet.getStamina();
                int happy = pet.getHappiness();

                boolean lowStamina = s < 40;    // allowed if >=30 (precondition)
                boolean lowHappiness = happy <= 25;

                int staminaDelta = 30;
                int hungerDelta = lowStamina ? 25 : 20;
                int happinessDelta = lowHappiness ? 2 : 5;
                int xp = lowStamina ? 15 : 25;
                if (lowHappiness) xp = Math.max(1, Math.round(xp * 0.8f));

                pet.setStamina(dec(pet.getStamina(), staminaDelta));
                pet.setHunger(inc(pet.getHunger(), hungerDelta));
                pet.setHappiness(inc(pet.getHappiness(), happinessDelta));

                addXp(pet, xp);
                gained = xp;

                if (lowStamina && lowHappiness) {
                    message = "Training while tired and unmotivated: lower XP.";
                } else if (lowStamina) {
                    message = "Training while tired: Stamina -30, Hunger +" + hungerDelta + ", small XP.";
                } else if (lowHappiness) {
                    message = "Training while unmotivated: reduced XP.";
                } else {
                    message = "Training: Stamina -30, Hunger +20, Happiness +5, XP +25.";
                }
            }
            case REST -> {
                int s = pet.getStamina();
                int h = pet.getHunger();

                boolean wasExhausted = s < 30;
                boolean wasStarving = h >= 80;

                int staminaGain = wasStarving ? 20 : 30;
                int hungerGain = 10;
                int happinessGain = wasExhausted && !wasStarving ? 5 : 0;

                pet.setStamina(inc(pet.getStamina(), staminaGain));
                pet.setHunger(inc(pet.getHunger(), hungerGain));
                if (happinessGain > 0) pet.setHappiness(inc(pet.getHappiness(), happinessGain));

                gained = 0;
                message = wasExhausted
                        ? (wasStarving
                        ? "Rested, but too hungry to feel better: Stamina +20, Hunger +10."
                        : "Rested well: Stamina +30, Hunger +10, Happiness +5.")
                        : (wasStarving
                        ? "Short nap due to hunger: Stamina +20, Hunger +10."
                        : "Rested: Stamina +30, Hunger +10.");
            }
        }

        // Post-action global discomfort adjustments (small but ensure mood can go down)
        if (pet.getHunger() >= 90) {
            pet.setHappiness(dec(pet.getHappiness(), 2));
        }
        if (pet.getStamina() <= 10) {
            pet.setHappiness(dec(pet.getHappiness(), 2));
        }

        // Stage recalculation if level changed
        if (pet.getLevel() != beforeLevel) {
            pet.recalcStage();
        }
        return new Result(gained, message);
    }

    // ===== Helpers =====
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



