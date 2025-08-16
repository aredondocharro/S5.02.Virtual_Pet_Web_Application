package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.PetCreateRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse;
import cat.itacademy.s05.t02.controller.dto.PetUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.PetMapper;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pets", description = "Virtual Pet CRUD. USER sees/manages only their own pets; ADMIN sees/manages all pets.")
@RestController
@Slf4j
@RequestMapping("/api/pets")
@SecurityRequirement(name = "bearerAuth")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @Operation(summary = "Pet List", description = "USER: returns only yours. ADMIN: returns all.")
    @GetMapping
    public ResponseEntity<List<PetResponse>> list(Authentication auth) {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        log.debug("User '{}' (admin={}) requested pet list", email, isAdmin);

        List<PetResponse> out = service.listMine(email, isAdmin)
                .stream().map(PetMapper::toResponse).toList();

        log.info("Returning {} pets for user='{}' (admin={})", out.size(), email, isAdmin);
        return ResponseEntity.ok(out);
    }

    @Operation(summary = "Pet creation")
    @PostMapping
    public ResponseEntity<PetResponse> create(@RequestBody @Valid PetCreateRequest req, Authentication auth) {
        String email = auth.getName();
        log.info("User '{}' requested to create pet: name='{}', color='{}'", email, req.getName(), req.getColor());

        PetEntity created = service.create(email, req.getName(), req.getColor());
        log.debug("Pet created successfully with id={} for owner='{}'", created.getId(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body(PetMapper.toResponse(created));
    }

    @Operation(summary = "Update a pet's status (hunger/happiness)")
    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid PetUpdateRequest req,
            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        log.info("User '{}' (admin={}) updating pet id={} hunger={} happiness={}",
                auth.getName(), isAdmin, id, req.getHunger(), req.getHappiness());

        PetEntity updated = service.updateMyPet(auth.getName(), isAdmin, id, req.getHunger(), req.getHappiness());
        log.debug("Pet id={} updated successfully by user='{}'", id, auth.getName());

        return ResponseEntity.ok(PetMapper.toResponse(updated));
    }

    @Operation(summary = "Delete pet")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        log.warn("User '{}' (admin={}) requested deletion of pet id={}", auth.getName(), isAdmin, id);

        service.deleteMyPet(auth.getName(), isAdmin, id);
        log.info("Pet id={} deleted successfully by user='{}'", id, auth.getName());

        return ResponseEntity.noContent().build();
    }
}

