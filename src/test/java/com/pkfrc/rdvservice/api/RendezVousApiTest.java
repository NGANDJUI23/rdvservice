package com.pkfrc.rdvservice.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pkfrc.rdvservice.dto.RendezVousRequest;
import com.pkfrc.rdvservice.dto.RendezVousResponse;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.serviceImpl.RdvServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RendezVousApi.class)
class RendezVousApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private RdvServiceImpl rdvService;

    private RendezVousRequest rendezVousRequest;
    private RendezVousResponse rendezVousResponse;
    private RendezVousResponse rendezVousResponse2;

    @BeforeEach
    void setUp() {
        rendezVousRequest = new RendezVousRequest(
                1L,
                LocalDateTime.now().plusDays(3),
                "Consultation médicale",
                1L,
                2L
        );

        rendezVousResponse = new RendezVousResponse(
                1L,
                1L,
                1L,
                2L,
                LocalDateTime.now().plusDays(3),
                "Consultation médicale",
                false
        );

        rendezVousResponse2 = new RendezVousResponse(
                1L,
                2L,
                1L,
                2L,
                LocalDateTime.now().plusDays(4),
                "Consultation de suivi",
                false
        );
    }

    @Test
    void creerRendezVous_ShouldReturnCreatedAppointment_WhenValidRequest() throws Exception {
        when(rdvService.creerRendezVous(any(RendezVousRequest.class))).thenReturn(rendezVousResponse);

        mockMvc.perform(post("/api/rendez-vous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rendezVousRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.refRDV").value(1L))
                .andExpect(jsonPath("$.refClient").value(1L))
                .andExpect(jsonPath("$.refService").value(1L))
                .andExpect(jsonPath("$.motifRdv").value("Consultation médicale"));

        verify(rdvService, times(1)).creerRendezVous(any(RendezVousRequest.class));
    }

    @Test
    void creerRendezVous_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        RendezVousRequest invalidRequest = new RendezVousRequest(
                null,
                LocalDateTime.now().plusDays(1),
                "",
                null,
                null
        );

        mockMvc.perform(post("/api/rendez-vous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(rdvService, never()).creerRendezVous(any(RendezVousRequest.class));
    }

    @Test
    void getRendezVous_ShouldReturnAppointment_WhenExists() throws Exception {
        when(rdvService.getRendezVousById(1L)).thenReturn(rendezVousResponse);

        mockMvc.perform(get("/api/rendez-vous/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refRDV").value(1L))
                .andExpect(jsonPath("$.refClient").value(1L));

        verify(rdvService, times(1)).getRendezVousById(1L);
    }

    @Test
    void getRendezVous_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(rdvService.getRendezVousById(999L)).thenThrow(new BusinessException("NOT_FOUND", "Rendez-vous non trouvé"));

        mockMvc.perform(get("/api/rendez-vous/999"))
                .andExpect(status().isNotFound());

        verify(rdvService, times(1)).getRendezVousById(999L);
    }

    @Test
    void getRendezVousByClient_ShouldReturnListOfAppointments() throws Exception {
        List<RendezVousResponse> appointments = Arrays.asList(rendezVousResponse, rendezVousResponse2);
        when(rdvService.getRendezVousByClient(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/rendez-vous/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].refRDV").value(1L))
                .andExpect(jsonPath("$[1].refRDV").value(2L));

        verify(rdvService, times(1)).getRendezVousByClient(1L);
    }

    @Test
    void getRendezVousByResponsable_ShouldReturnListOfAppointments() throws Exception {
        List<RendezVousResponse> appointments = Arrays.asList(rendezVousResponse);
        when(rdvService.getRendezVousByResponsable(2L)).thenReturn(appointments);

        mockMvc.perform(get("/api/rendez-vous/responsable/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].refResponsable").value(2L));

        verify(rdvService, times(1)).getRendezVousByResponsable(2L);
    }

    @Test
    void annulerRendezVous_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        doNothing().when(rdvService).annulerRendezVous(1L, "Test annulation");

        mockMvc.perform(delete("/api/rendez-vous/1")
                        .param("motif", "Test annulation"))
                .andExpect(status().isNoContent());

        verify(rdvService, times(1)).annulerRendezVous(eq(1L), anyString());
    }

    @Test
    void annulerRendezVous_ShouldReturnBadRequest_WhenAppointmentNotFound() throws Exception {
        doThrow(new BusinessException("NOT_FOUND", "Rendez-vous non trouvé"))
                .when(rdvService).annulerRendezVous(999L, "Test");

        mockMvc.perform(delete("/api/rendez-vous/999")
                        .param("motif", "Test"))
                .andExpect(status().isNotFound());

        verify(rdvService, times(1)).annulerRendezVous(eq(999L), anyString());
    }
}