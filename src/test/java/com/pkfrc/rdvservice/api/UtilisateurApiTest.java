package com.pkfrc.rdvservice.api;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pkfrc.rdvservice.dto.UtilisateurRequest;
import com.pkfrc.rdvservice.dto.UtilisateurResponse;
import com.pkfrc.rdvservice.enumeration.Role;
import com.pkfrc.rdvservice.serviceImpl.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilisateurApi.class)
class UtilisateurApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Configuration de l'ObjectMapper avec support des dates Java 8
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());  // Ajout du support LocalDate

    @MockitoBean
    private UtilisateurServiceImpl utilisateurService;

    private UtilisateurRequest utilisateurRequest;
    private UtilisateurResponse utilisateurResponse;
    private UtilisateurResponse utilisateurResponse2;

    @BeforeEach
    void setUp() {
        utilisateurRequest = new UtilisateurRequest(
                "john.doe@example.com",
                "johndoe",
                "password123",
                "0612345678",
                LocalDate.of(1990, 1, 1),
                Role.CLIENT
        );

        utilisateurResponse = new UtilisateurResponse(
                1L,
                "john.doe@example.com",
                "johndoe",
                "0612345678",
                LocalDate.of(1990, 1, 1),
                Role.CLIENT,
                false
        );

        utilisateurResponse2 = new UtilisateurResponse(
                2L,
                "jane.smith@example.com",
                "janesmith",
                "0698765432",
                LocalDate.of(1985, 5, 15),
                Role.RESPONSABLE,
                false
        );
    }

    @Test
    void creerUtilisateur_ShouldReturnCreatedUser_WhenValidRequest() throws Exception {
        when(utilisateurService.creerUtilisateur(any(UtilisateurRequest.class))).thenReturn(utilisateurResponse);

        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(utilisateurRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        verify(utilisateurService, times(1)).creerUtilisateur(any(UtilisateurRequest.class));
    }

    @Test
    void creerUtilisateur_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UtilisateurRequest invalidRequest = new UtilisateurRequest(
                "invalid-email",
                "",
                "123",
                "invalid-phone",
                LocalDate.now().plusDays(1),
                null
        );

        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(utilisateurService, never()).creerUtilisateur(any(UtilisateurRequest.class));
    }

    @Test
    void getUtilisateurById_ShouldReturnUser_WhenExists() throws Exception {
        when(utilisateurService.getUtilisateurById(1L)).thenReturn(utilisateurResponse);

        mockMvc.perform(get("/api/utilisateurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.username").value("johndoe"));

        verify(utilisateurService, times(1)).getUtilisateurById(1L);
    }

    @Test
    void getUtilisateurById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        when(utilisateurService.getUtilisateurById(999L)).thenThrow(new RuntimeException("Utilisateur non trouvé"));

        mockMvc.perform(get("/api/utilisateurs/999"))
                .andExpect(status().isNotFound());

        verify(utilisateurService, times(1)).getUtilisateurById(999L);
    }

    @Test
    void getAllUtilisateurs_ShouldReturnListOfUsers() throws Exception {
        List<UtilisateurResponse> users = Arrays.asList(utilisateurResponse, utilisateurResponse2);
        when(utilisateurService.getAllUtilisateurs()).thenReturn(users);

        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("jane.smith@example.com"));

        verify(utilisateurService, times(1)).getAllUtilisateurs();
    }

    @Test
    void updateUtilisateur_ShouldReturnUpdatedUser() throws Exception {
        when(utilisateurService.updateUtilisateur(eq(1L), any(UtilisateurRequest.class))).thenReturn(utilisateurResponse);

        mockMvc.perform(put("/api/utilisateurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(utilisateurRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(utilisateurService, times(1)).updateUtilisateur(eq(1L), any(UtilisateurRequest.class));
    }

    @Test
    void supprimerUtilisateur_ShouldReturnNoContent_WhenUserDeleted() throws Exception {
        doNothing().when(utilisateurService).supprimerUtilisateur(1L);

        mockMvc.perform(delete("/api/utilisateurs/1"))
                .andExpect(status().isNoContent());

        verify(utilisateurService, times(1)).supprimerUtilisateur(1L);
    }

    @Test
    void getUtilisateursByRole_ShouldReturnUsersByRole() throws Exception {
        List<UtilisateurResponse> users = Collections.singletonList(utilisateurResponse);
        when(utilisateurService.getUtilisateursByRole(Role.CLIENT)).thenReturn(users);

        mockMvc.perform(get("/api/utilisateurs/role/CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("CLIENT"));

        verify(utilisateurService, times(1)).getUtilisateursByRole(Role.CLIENT);
    }

    @Test
    void getUtilisateurByEmail_ShouldReturnUser_WhenEmailExists() throws Exception {
        when(utilisateurService.getUtilisateurByEmail("john.doe@example.com")).thenReturn(utilisateurResponse);

        mockMvc.perform(get("/api/utilisateurs/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(utilisateurService, times(1)).getUtilisateurByEmail("john.doe@example.com");
    }

    @Test
    void getUtilisateurByUsername_ShouldReturnUser_WhenUsernameExists() throws Exception {
        when(utilisateurService.getUtilisateurByUsername("johndoe")).thenReturn(utilisateurResponse);

        mockMvc.perform(get("/api/utilisateurs/username/johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));

        verify(utilisateurService, times(1)).getUtilisateurByUsername("johndoe");
    }

}