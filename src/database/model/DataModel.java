package database.model;

/**
 * Created by manthan on 4/3/16.
 */
public class DataModel {
    String name;
    String seqNumber;
    byte[] dataChunk;


    public DataModel(String name, byte[] dataChunk){
        this.name = name;
        this.seqNumber = "";
        this.dataChunk = dataChunk;
    }

    public DataModel(String name, String seqNumber, byte[] dataChunk){
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

    public String getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(String seqNumber) {
        this.seqNumber = seqNumber;
    }

    public byte[] getDataChunk() {
        return dataChunk;
    }

    public void setDataChunk(byte[] dataChunk) {
        this.dataChunk = dataChunk;
    }
}
