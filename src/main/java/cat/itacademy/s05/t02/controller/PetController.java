package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.ActionResultResponse;
import cat.itacademy.s05.t02.controller.dto.PetActionRequest;
import cat.itacademy.s05.t02.controller.dto.PetCreateRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse;
import cat.itacademy.s05.t02.controller.dto.PetUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.PetMapper;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Pets",
        description = "Virtual Pet CRUD. USER sees/manages only their own pets; ADMIN sees/manages all pets."
)
@RestController
@Slf4j
@RequestMapping(value = "/api/pets", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PetResponse> create(@RequestBody @Valid PetCreateRequest req, Authentication auth) {
        String email = auth.getName();
        log.info("User '{}' requested to create pet: name='{}', color='{}'", email, req.getName(), req.getColor());

        PetEntity created = service.create(email, req.getName(), req.getColor());
        log.debug("Pet created successfully with id={} for owner='{}'", created.getId(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body(PetMapper.toResponse(created));
    }

    @Operation(summary = "Update a pet's status (hunger/happiness)")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PetResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid PetUpdateRequest req,
            Authentication auth) {

        // Optional: guard if the request body carries an id different from the path id
        try {
            var method = req.getClass().getMethod("getId");
            Object bodyId = method.invoke(req);
            if (bodyId instanceof Long bodyLong && bodyLong != null && !bodyLong.equals(id)) {
                throw new BadRequestException("Path id and body id do not match");
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Exception reflectionIssue) {
            log.debug("Could not introspect PetUpdateRequest.getId(): {}", reflectionIssue.getMessage());
        }

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        log.info("User '{}' (admin={}) updating pet id={} hunger={} happiness={}",
                auth.getName(), isAdmin, id, req.getHunger(), req.getHappiness());

        PetEntity updated = service.updateMyPet(auth.getName(), isAdmin, id, req.getHunger(), req.getHappiness());
        log.debug("Pet id={} updated successfully by user='{}'", id, auth.getName());

        return ResponseEntity.ok(PetMapper.toResponse(updated));
    }

    @Operation(
            summary = "Apply an action to a pet (FEED/PLAY/TRAIN/REST)",
            description = """
                Updates core stats (hunger, stamina, happiness), grants XP, and may level up the pet.
                XP is capped at level 15. Evolution stage changes automatically on level up.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Action applied successfully",
                    content = @Content(
                            schema = @Schema(implementation = ActionResultResponse.class),
                            examples = @ExampleObject(name = "OK",
                                    value = """
                                    {
                                      "pet": {
                                        "id": 1,
                                        "name": "Axo",
                                        "color": "pink",
                                        "hunger": 40,
                                        "stamina": 50,
                                        "happiness": 75,
                                        "level": 1,
                                        "xpInLevel": 10,
                                        "stage": "BABY",
                                        "ownerEmail": "alex@example.com",
                                        "imageUrl": "/img/axolotl_baby.png"
                                      },
                                      "message": "Played: Happiness +15, Stamina -20, Hunger +10.",
                                      "xpGained": 10
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PostMapping("/{id}/actions")
    public ActionResultResponse act(
            @Parameter(description = "Pet ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PetActionRequest.class),
                            examples = {
                                    @ExampleObject(name = "Feed", value = "{ \"action\": \"FEED\" }"),
                                    @ExampleObject(name = "Play", value = "{ \"action\": \"PLAY\" }"),
                                    @ExampleObject(name = "Train", value = "{ \"action\": \"TRAIN\" }"),
                                    @ExampleObject(name = "Rest", value = "{ \"action\": \"REST\" }")
                            }
                    )
            )
            @RequestBody @Valid PetActionRequest request,
            Authentication auth) {

        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        log.info("Action {} requested by '{}' on pet id={} (admin={})", request.getAction(), email, id, isAdmin);

        return service.applyAction(id, email, request, isAdmin);
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



