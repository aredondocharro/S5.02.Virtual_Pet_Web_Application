package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.PetCreateRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse;
import cat.itacademy.s05.t02.controller.dto.PetUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.PetMapper;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/pets")
@SecurityRequirement(name = "bearerAuth")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @Operation(
            summary = "Pet List",
            description = "USER: returns only yours. ADMIN: returns all.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = PetResponse.class)))),
                    @ApiResponse(responseCode = "401", description = "No authenticated"),
                    @ApiResponse(responseCode = "403", description = "No permission to see all pets")
            }
    )
    @GetMapping
    public ResponseEntity<List<PetResponse>> list(Authentication auth) {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        List<PetResponse> out = service.listMine(email, isAdmin)
                .stream().map(PetMapper::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    @Operation(
            summary = "Pet creation",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PetCreateRequest.class),
                            examples = @ExampleObject(name = "create",
                                    value = """
                            {
                              "name": "Neko",
                              "color": "blue"
                            }
                            """))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = PetResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "401", description = "No authenticated"),
            }
    )
    @PostMapping
    public ResponseEntity<PetResponse> create(@RequestBody @Valid PetCreateRequest req, Authentication auth) {
        String email = auth.getName();
        PetEntity created = service.create(email, req.getName(), req.getColor());
        return ResponseEntity.status(HttpStatus.CREATED).body(PetMapper.toResponse(created));
    }

    @Operation(
            summary = "Update a pet's status (hunger/happiness)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PetUpdateRequest.class),
                            examples = @ExampleObject(name = "update",
                                    value = """
                            {
                              "hunger": 25,
                              "happiness": 90
                            }
                            """))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = PetResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No authenticated"),
                    @ApiResponse(responseCode = "403", description = "You are not the owner and you are not an ADMIN"),
                    @ApiResponse(responseCode = "404", description = "There is no pet")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid PetUpdateRequest req,
            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        PetEntity updated = service.updateMyPet(auth.getName(), isAdmin, id, req.getHunger(), req.getHappiness());
        return ResponseEntity.ok(PetMapper.toResponse(updated));
    }

    @Operation(
            summary = "Delete pet",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted"),
                    @ApiResponse(responseCode = "401", description = "No authenticated"),
                    @ApiResponse(responseCode = "403", description = "You are not the owner and you are not an ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No pet found")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        service.deleteMyPet(auth.getName(), isAdmin, id);
        return ResponseEntity.noContent().build();
    }
}
