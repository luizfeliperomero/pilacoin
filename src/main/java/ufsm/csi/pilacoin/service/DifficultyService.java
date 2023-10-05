package ufsm.csi.pilacoin.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DifficultyService {
    protected void divulgaDificuldade() {
        Random rnd = new Random();
        byte[] bArr = new byte[256/8];
        rnd.nextBytes(bArr);
    }
}
