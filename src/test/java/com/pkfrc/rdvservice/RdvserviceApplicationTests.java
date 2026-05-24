package com.pkfrc.rdvservice;

import com.pkfrc.rdvservice.repository.RendezVousRepository;
import com.pkfrc.rdvservice.repository.ServiceRepository;
import com.pkfrc.rdvservice.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RdvserviceApplication.class)
@ActiveProfiles("test")
class RdvserviceApplicationTests {

    @Test
    void contextLoads() {
    }

}
