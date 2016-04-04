package database.model;

/**
 * Created by manthan on 4/3/16.
 */
public class MongoDataModel {
    String name;
    int seqNumber;
    byte[] dataChunk;

    public MongoDataModel(String name, byte[] dataChunk){
        this.name = name;
        this.seqNumber = 0;
        this.dataChunk = dataChunk;
    }

    public MongoDataModel(String name, int seqNumber, byte[] dataChunk){
        this.name = name;
        this.seqNumber = seqNumber;
        this.dataChunk = dataChunk;
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
}
