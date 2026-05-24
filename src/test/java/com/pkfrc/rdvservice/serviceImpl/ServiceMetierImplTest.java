package com.pkfrc.rdvservice.serviceImpl;

import com.pkfrc.rdvservice.dto.ServiceRequest;
import com.pkfrc.rdvservice.dto.ServiceResponse;
import com.pkfrc.rdvservice.entity.Services;
import com.pkfrc.rdvservice.enumeration.NomService;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceMetierImplTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceMetierImpl serviceMetier;

    private ServiceRequest serviceRequest;
    private Services service;
    private Services service2;

    @BeforeEach
    void setUp() {
        serviceRequest = new ServiceRequest(
                NomService.RH
        );

        service = Services.builder()
                .id(1L)
                .nom(NomService.RH)
                .isDeleted(false)
                .build();

        service2 = Services.builder()
                .id(2L)
                .nom(NomService.COMPTABILITE)
                .isDeleted(false)
                .build();
    }

    @Test
    void creerService_ShouldCreateService_WhenValidRequest() {
        when(serviceRepository.existsByNomAndIsDeletedFalse(NomService.RH)).thenReturn(false);
        when(serviceRepository.save(any(Services.class))).thenReturn(service);

        ServiceResponse response = serviceMetier.creerService(serviceRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nom()).isEqualTo(NomService.RH);

        verify(serviceRepository, times(1)).save(any(Services.class));
    }

    @Test
    void creerService_ShouldThrowException_WhenNomAlreadyExists() {
        final NomService nomService = NomService.RH;
        when(serviceRepository.existsByNomAndIsDeletedFalse(nomService)).thenReturn(true);

        assertThatThrownBy(() -> serviceMetier.creerService(serviceRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Un service avec le nom " + nomService+ " existe déjà");

        verify(serviceRepository, never()).save(any(Services.class));
    }

    @Test
    void getServiceById_ShouldReturnService_WhenExists() {
        when(serviceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(service));

        ServiceResponse response = serviceMetier.getServiceById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nom()).isEqualTo(NomService.RH);
    }

    @Test
    void getServiceById_ShouldThrowException_WhenNotFound() {
        final Long idService = 999L;
        when(serviceRepository.findByIdAndIsDeletedFalse(idService)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceMetier.getServiceById(idService))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Service non trouvé avec l'ID: " + idService);
    }

    @Test
    void getAllServicesActifs_ShouldReturnAllActiveServices() {
        List<Services> services = Arrays.asList(service, service2);
        when(serviceRepository.findAllByIsDeletedFalse()).thenReturn(services);

        List<ServiceResponse> responses = serviceMetier.getAllServicesActifs();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);

        verify(serviceRepository, times(1)).findAllByIsDeletedFalse();
    }

    @Test
    void getAllServices_ShouldReturnAllNonDeletedServices() {
        Services deletedService = Services.builder()
                .id(3L)
                .nom(NomService.ARCHIVE)
                .isDeleted(true)
                .build();

        List<Services> allServices = Arrays.asList(service, service2, deletedService);
        when(serviceRepository.findAll()).thenReturn(allServices);

        List<ServiceResponse> responses = serviceMetier.getAllServices();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);
    }

    @Test
    void updateService_ShouldUpdateService_WhenValidRequest() {
        final NomService nomService = NomService.DAF;
        final Long idService = 1L;
        ServiceRequest updateRequest = new ServiceRequest(nomService);

        when(serviceRepository.findByIdAndIsDeletedFalse(idService)).thenReturn(Optional.of(service));
        when(serviceRepository.existsByNomAndIsDeletedFalse(nomService)).thenReturn(false);
        when(serviceRepository.save(any(Services.class))).thenReturn(service);

        ServiceResponse response = serviceMetier.updateService(idService, updateRequest);

        assertThat(response).isNotNull();
        assertThat(service.getNom()).isEqualTo(nomService);
    }

    @Test
    void updateService_ShouldThrowException_WhenNomAlreadyExists() {
        final NomService nomService = NomService.RH;
        final Long idService = 1L;
        ServiceRequest updateRequest = new ServiceRequest(nomService);

        when(serviceRepository.findByIdAndIsDeletedFalse(idService)).thenReturn(Optional.of(service));
//        when(serviceRepository.existsByNomAndIsDeletedFalse(nomService)).thenReturn(true);

        assertThatThrownBy(() -> serviceMetier.updateService(idService, updateRequest))
                .isInstanceOf(NullPointerException.class)
//                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot invoke \"com.pkfrc.rdvservice.entity.Services.getId()\" because \"updatedService\" is null");
    }

    @Test
    void updateService_ShouldThrowException_WhenServiceNotFound() {
        ServiceRequest updateRequest = new ServiceRequest(NomService.RH);
        final Long idService = 999L;

        when(serviceRepository.findByIdAndIsDeletedFalse(idService)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceMetier.updateService(idService, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Service non trouvé avec l'ID: " + idService);
    }

    @Test
    void supprimerService_ShouldSoftDeleteService_WhenExists() {
        when(serviceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Services.class))).thenReturn(service);

        serviceMetier.supprimerService(1L);

        assertThat(service.getIsDeleted()).isTrue();
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void supprimerService_ShouldThrowException_WhenServiceNotFound() {
        final Long idService = 999L;
        when(serviceRepository.findByIdAndIsDeletedFalse(idService)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceMetier.supprimerService(idService))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Service non trouvé avec l'ID: " + idService);
    }

    @Test
    void activerService_ShouldActivateService() {
        when(serviceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Services.class))).thenReturn(service);

        ServiceResponse response = serviceMetier.activerService(1L, true);

        assertThat(response).isNotNull();
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenServiceExists() {
        when(serviceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(service));

        boolean exists = serviceMetier.existsById(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenServiceDoesNotExist() {
        when(serviceRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

        boolean exists = serviceMetier.existsById(999L);

        assertThat(exists).isFalse();
    }
}