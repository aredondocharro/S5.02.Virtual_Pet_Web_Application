package cat.itacademy.s05.t02.persistence.entity;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "pets")
public class PetEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String color;


    @Column(nullable = false) private int hunger;
    @Column(nullable = false) private int stamina;
    @Column(nullable = false) private int happiness;


    @Column(nullable = false) private int level;       // 1..15
    @Column(nullable = false) private int xpInLevel;   // 0..100

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EvolutionStage stage;                      // BABY / TEEN / ADULT


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (level <= 0) level = 1;
        if (stage == null) stage = EvolutionStage.fromLevel(level);
        hunger = clampDefault(hunger, 30);
        stamina = clampDefault(stamina, 70);
        happiness = clampDefault(happiness, 60);
        xpInLevel = Math.max(0, Math.min(xpInLevel, 100));
    }

    public void recalcStage() {
        this.stage = EvolutionStage.fromLevel(this.level);
    }

    private int clampDefault(int v, int def) {
        if (v < 0 || v > 100) return def;
        return v;
    }
}
