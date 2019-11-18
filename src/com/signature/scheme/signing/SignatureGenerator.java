package com.signature.scheme.signing;

import com.signature.scheme.*;
import com.signature.scheme.merkleTree.Node;
import com.signature.scheme.tools.FSGenerator;
import com.signature.scheme.tools.HelperFunctions;
import com.signature.scheme.tools.PseudorndFunction;

import java.util.Arrays;

public class SignatureGenerator {

    KeysKeeper keysKeeper;

    public SignatureGenerator(KeysKeeper keysKeeper) {
        this.keysKeeper = keysKeeper;
    }

    private static byte[][] generateMsgSignature(byte[] seed, PseudorndFunction f, int l1, int l2, int w, byte[] x, byte[] msgDigest) {
        int l = l1 + l2;
        int n = f.n;
        int wBytes = HelperFunctions.ceilLogTwo(w);
        int actualMsgIndex = 0;
        byte[] privatePart;
        int msgPartBaseW;
        int controlSum = 0;
        byte[][] signature = new byte[n][l];
        for (int i = 0; i < l1; i++) {
            privatePart = WOTSkeyGenerator.getPrivPart(l, f, i, seed);
            msgPartBaseW = HelperFunctions.fromByteArray(Arrays.copyOfRange(msgDigest, actualMsgIndex, actualMsgIndex + wBytes));
            controlSum += (w - 1 - msgPartBaseW);
            actualMsgIndex += wBytes;
            signature[i] = f.composeFunction(x, privatePart, msgPartBaseW);
        }
        actualMsgIndex = 0;
        byte[] byteControlSum = HelperFunctions.intToByteArray(controlSum, l2);
        for (int i = 0; i < l2; i++) {
            privatePart = WOTSkeyGenerator.getPrivPart(l, f, i + l1, seed);
            msgPartBaseW = HelperFunctions.fromByteArray(Arrays.copyOfRange(byteControlSum, actualMsgIndex, actualMsgIndex + wBytes));
            actualMsgIndex += wBytes;
            signature[l1 + i] = f.composeFunction(x, privatePart, msgPartBaseW);
        }

        return signature;
    }

    public static Signature signLowerTree(PrivateKey privateKey, int n, int l1, int l2, int w, byte[] x, byte[] msg) {
        FSGenerator fsGenerator = new FSGenerator(new PseudorndFunction(n), new PseudorndFunction(n), privateKey.upperGenState);
        Node[] authPath = privateKey.upperPathComputation.auth;
        int index = privateKey.upperPathComputation.leafIndex;
        privateKey.upperPathComputation.doAlgorithm();
        byte[] seed = fsGenerator.nextStateAndSeed();
        byte[][] msgSignature = generateMsgSignature(seed, new PseudorndFunction(n), l1, l2, w, x, msg);
        Signature lowerSignature = new Signature(authPath, msgSignature, index);
        privateKey.lowerSignature = lowerSignature;
        return lowerSignature;
    }

    public Signature signMessage(String msg) {
        byte [] msgDigest = HelperFunctions.messageDigestSHA3_256(msg);
        PrivateKey privateKey = this.keysKeeper.privateKey;
        ParametersBase params = this.keysKeeper.params;
        int n = params.n;

        FSGenerator fsGenerator = new FSGenerator(new PseudorndFunction(n), new PseudorndFunction(n), privateKey.lowerGenState);
        Node[] authPath = privateKey.lowerPathComputation.auth;
        int index = privateKey.lowerPathComputation.leafIndex;
        privateKey.lowerPathComputation.doAlgorithm();
        byte[] seed = fsGenerator.nextStateAndSeed();
        byte[][] msgSignature = generateMsgSignature(seed, new PseudorndFunction(n), params.ll1, params.ll2, params.wU, params.X,msgDigest);
        return new Signature(authPath, msgSignature, index, privateKey.lowerSignature);
    }
}
