package org.zcx.netty.coap.entity;

import org.zcx.netty.coap.utils.BytesUtils;

import java.util.Map;

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
        this.SZX = result[2];
    }

    public CoapBlock copy() {
        CoapBlock copy = new CoapBlock();
        copy.setNUM(this.NUM);
        copy.setSZX(this.SZX);
        copy.setM(this.M);
        copy.setBlockSize(this.blockSize);
        copy.setPayload(new StringBuffer(this.payload));
        return copy;
    }

    public static CoapBlock requestBlock2(int blockSize) {
        CoapBlock block = new CoapBlock();
        block.setNUM(-1);//使用-1表示只记录块大小的
        block.setSZX(blockSize);
        return block;
    }

    public int[] getBlockInfo() {
        return new int[]{NUM, M, SZX};
    }

    public CoapBlock(int NUM, int SZX, StringBuffer payload) {
        this.NUM = NUM;
        this.SZX = SZX;
        this.payload = payload;
        if ((NUM + 1) * SZX >= payload.length()) {
            this.M = 0;
        } else {
            this.M = 1;
        }
    }

    public CoapBlock() {
    }

    public int getNUM() {
        return NUM;
    }

    public int getResizeNUM(int newSZX) {
        if (newSZX > SZX) {
            return -1;//todo 是否应该这样处理？
        }
        return ((NUM + 1) * SZX / newSZX) - 1;
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

    public void setM(boolean m) {
        if (m) {
            M = 1;
        } else {
            M = 0;
        }
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

    private int offset = 0;//num总是连续的，无法通过num和szx计算offset

    public String getCurrentPayload() {
        int start = this.offset;
        int end = Math.min(this.offset + this.SZX, this.payload.length());
        String currentPayload = this.payload.substring(start, end);
        this.offset += this.SZX;
        return currentPayload;
    }

    public void resetOffset(int requestNUM, int requestSZX) {
        this.NUM = requestNUM;
        this.SZX = requestSZX;
        if (this.offset + this.SZX >= this.payload.length()) {
            this.M = 0;
        } else {
            this.M = 1;
        }
    }
}
