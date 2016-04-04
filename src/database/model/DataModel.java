package database.model;

/**
 * Created by manthan on 4/3/16.
 */
public class DataModel {
    String name;
    int seqNumber;
    byte[] dataChunk;
    int seqSize;


    public DataModel(String name, byte[] dataChunk){
        this.name = name;
        this.seqNumber = -1;
        this.dataChunk = dataChunk;
        this.seqSize=0;
    }

    public DataModel(String name, int seqNumber, byte[] dataChunk){
        this.name = name;
        this.seqNumber = seqNumber;
        this.dataChunk = dataChunk;
        this.seqSize=0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public byte[] getDataChunk() {
        return dataChunk;
    }

    public void setDataChunk(byte[] dataChunk) {
        this.dataChunk = dataChunk;
    }

    public int getSeqSize() {
        return seqSize;
    }

    public void setSeqSize(int seqSize) {
        this.seqSize = seqSize;
    }
}
