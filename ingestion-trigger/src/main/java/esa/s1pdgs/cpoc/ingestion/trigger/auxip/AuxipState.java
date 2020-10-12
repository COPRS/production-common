package esa.s1pdgs.cpoc.ingestion.trigger.auxip;

import java.util.Date;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auxipState")
public class AuxipState {

    @Id
    private ObjectId id; //necessary for repository.delete(entry)

    Date nextWindowStart;
    String processingPod;
    String pripUrl;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getProcessingPod() {
        return processingPod;
    }

    public void setProcessingPod(String processingPod) {
        this.processingPod = processingPod;
    }

    public String getPripUrl() {
        return pripUrl;
    }

    public void setPripUrl(String pripUrl) {
        this.pripUrl = pripUrl;
    }

    public Date getNextWindowStart() {
        return nextWindowStart;
    }

    public void setNextWindowStart(Date nextWindowStart) {
        this.nextWindowStart = nextWindowStart;
    }

    @Override
    public String toString() {
        return "AuxipState{" +
                "nextWindowStart=" + nextWindowStart +
                ", processingPod='" + processingPod + '\'' +
                ", pripHost='" + pripUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuxipState that = (AuxipState) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(nextWindowStart, that.nextWindowStart) &&
                Objects.equals(processingPod, that.processingPod) &&
                Objects.equals(pripUrl, that.pripUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nextWindowStart, processingPod, pripUrl);
    }
}
