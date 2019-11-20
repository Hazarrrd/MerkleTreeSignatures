package com.signature.scheme.tests;

import com.signature.scheme.ParametersBase;
import com.signature.scheme.WOTSkeyGenerator;
import com.signature.scheme.signing.SignatureGenerator;
import com.signature.scheme.tools.HelperFunctions;
import com.signature.scheme.tools.PseudorndFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WOTSkeyGeneratorTest {

    @Test
    void computeOTSPublicKey() {
        ParametersBase params = new ParametersBase();
        byte[][] signature = new byte[params.lL][params.n];
        for(int i =0;i<params.lL;i++){
            byte[] array = new byte[params.n];
            HelperFunctions.fillBytesRandomly(array);
            signature[i] = array;
        }
        byte[][] pk = WOTSkeyGenerator.computeOTSPublicKey(params.seed,params.ll1,params.ll2,params.wL,params.X,signature);
        assertEquals(pk.length,params.lL);
        for (byte [] array:pk) {
            assertEquals(array.length,params.n);
        }
    }
}