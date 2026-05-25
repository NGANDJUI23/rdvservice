package com.pkfrc.rdvservice.serviceFace;

import com.pkfrc.rdvservice.dto.RendezVousRequest;
import com.pkfrc.rdvservice.dto.RendezVousResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RdvServiceFace {
    
    RendezVousResponse creerRendezVous(RendezVousRequest request);

    RendezVousResponse getRendezVousById(Long id);

    void annulerRendezVous(Long id, String motifAnnulation);


    @Transactional
    RendezVousResponse effectuerRendezVous(Long id);

    List<RendezVousResponse> getRendezVousByClient(Long clientId);

    List<RendezVousResponse> getRendezVousByResponsable(Long responsableId);

    List<RendezVousResponse> listRentezvous();
}
