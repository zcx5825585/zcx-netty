package org.zcx.netty.coap.entity;

import org.zcx.netty.coap.utils.BytesUtils;

public class CoapBlock {
    private int NUM;
    private int M;
    private int SZX;
    private int blockSize;
    private StringBuffer payload;

    public CoapBlock(byte[] blockOption) {
        int[] result = BytesUtils.handlerBlock(blockOption);
        this.NUM = result[0];
        this.M = result[1];
        this.SZX = (1 << (result[2] + 4));
    }

    public int getNUM() {
        return NUM;
    }

    public void setNUM(int NUM) {
        this.NUM = NUM;
    }

    public int getM() {
        return M;
    }

    public void setM(int m) {
        M = m;
    }

    public int getSZX() {
        return SZX;
    }

    public void setSZX(int SZX) {
        this.SZX = SZX;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public StringBuffer getPayload() {
        return payload;
    }

    public void setPayload(StringBuffer payload) {
        this.payload = payload;
    }
}
