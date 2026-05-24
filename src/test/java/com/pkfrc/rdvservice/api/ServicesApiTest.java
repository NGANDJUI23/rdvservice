package com.pkfrc.rdvservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkfrc.rdvservice.dto.ServiceRequest;
import com.pkfrc.rdvservice.dto.ServiceResponse;
import com.pkfrc.rdvservice.enumeration.NomService;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.serviceImpl.ServiceMetierImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServicesApi.class)
class ServicesApiTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ServiceMetierImpl serviceMetier;

    private ServiceRequest serviceRequest;
    private ServiceResponse serviceResponse;
    private ServiceResponse serviceResponse2;

    @BeforeEach
    void setUp() {
        serviceRequest = new ServiceRequest(
                NomService.RH
        );

        serviceResponse = new ServiceResponse(
                1L,
                NomService.RH,
                false
        );

        serviceResponse2 = new ServiceResponse(
                2L,
                NomService.DAF,
                false
        );
    }

    @Test
    void creerService_ShouldReturnCreatedService_WhenValidRequest() throws Exception {
        when(serviceMetier.creerService(any(ServiceRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value("RH"))
                .andExpect(jsonPath("$.isDeleted").value(false));

        verify(serviceMetier, times(1)).creerService(any(ServiceRequest.class));
    }

//    @Test
//    void creerService_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
//        ServiceRequest invalidRequest = new ServiceRequest("ss");
//
//        mockMvc.perform(post("/api/services")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andExpect(status().isBadRequest());
//
//        verify(serviceMetier, never()).creerService(any(ServiceRequest.class));
//    }

    @Test
    void getServiceById_ShouldReturnService_WhenExists() throws Exception {
        when(serviceMetier.getServiceById(1L)).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nom").value("RH"));

        verify(serviceMetier, times(1)).getServiceById(1L);
    }

    @Test
    void getServiceById_ShouldReturnNotFound_WhenServiceDoesNotExist() throws Exception {
        when(serviceMetier.getServiceById(999L)).thenThrow(new BusinessException("NOT_FOUND", "Service non trouvé"));

        mockMvc.perform(get("/api/services/999"))
                .andExpect(status().isNotFound());

        verify(serviceMetier, times(1)).getServiceById(999L);
    }

    @Test
    void getAllServices_ShouldReturnListOfServices() throws Exception {
        List<ServiceResponse> services = Arrays.asList(serviceResponse, serviceResponse2);
        when(serviceMetier.getAllServicesActifs()).thenReturn(services);

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nom").value("RH"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nom").value("DAF"));

        verify(serviceMetier, times(1)).getAllServicesActifs();
    }


    @Test
    void updateService_ShouldReturnUpdatedService() throws Exception {
        when(serviceMetier.updateService(eq(1L), any(ServiceRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(put("/api/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(serviceMetier, times(1)).updateService(eq(1L), any(ServiceRequest.class));
    }


    @Test
    void supprimerService_ShouldReturnNoContent_WhenServiceDeleted() throws Exception {
        doNothing().when(serviceMetier).supprimerService(1L);

        mockMvc.perform(delete("/api/services/1"))
                .andExpect(status().isNoContent());

        verify(serviceMetier, times(1)).supprimerService(1L);
    }

    @Test
    void supprimerService_ShouldReturnNotFound_WhenServiceDoesNotExist() throws Exception {
        doThrow(new BusinessException("NOT_FOUND", "Service non trouvé"))
                .when(serviceMetier).supprimerService(999L);

        mockMvc.perform(delete("/api/services/999"))
                .andExpect(status().isNotFound());

        verify(serviceMetier, times(1)).supprimerService(999L);
    }

    @Test
    void activerService_ShouldActivateService() throws Exception {
        ServiceResponse activatedResponse = new ServiceResponse(1L, NomService.ARCHIVE, false);
        when(serviceMetier.activerService(eq(1L), eq(true))).thenReturn(activatedResponse);

        mockMvc.perform(patch("/api/services/1/activer?actif=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isDeleted").value(false));

        verify(serviceMetier, times(1)).activerService(1L, true);
    }

    @Test
    void desactiverService_ShouldDeactivateService() throws Exception {
        ServiceResponse deactivatedResponse = new ServiceResponse(1L, NomService.RH, true);
        when(serviceMetier.activerService(eq(1L), eq(false))).thenReturn(deactivatedResponse);

        mockMvc.perform(patch("/api/services/1/activer?actif=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDeleted").value(true));

        verify(serviceMetier, times(1)).activerService(1L, false);
    }
}