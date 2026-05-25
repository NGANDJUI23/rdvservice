package com.pkfrc.rdvservice.serviceFace;

import com.pkfrc.rdvservice.dto.ServiceRequest;
import com.pkfrc.rdvservice.dto.ServiceResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceMetierFace {

    ServiceResponse creerService(ServiceRequest request);

    ServiceResponse getServiceById(Long id);

    List<ServiceResponse> getAllServicesActifs();

    List<ServiceResponse> getAllServices();

    ServiceResponse updateService(Long id, ServiceRequest request);

    void supprimerService(Long id);

    ServiceResponse activerService(Long id, boolean actif);

    boolean existsById(Long id);
}
