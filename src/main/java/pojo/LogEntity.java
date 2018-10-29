package pojo;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "log", schema = "dblab2", catalog = "")
public class LogEntity {
    private int lid;
    private Timestamp time;
    private int uid;
    private String operation;
    private int sid;
    private String mode;

    public LogEntity() {
    }

    public LogEntity(Timestamp time, int uid, String operation, int sid, String mode) {
        this.time = time;
        this.uid = uid;
        this.operation = operation;
        this.sid = sid;
        this.mode = mode;
    }

    @Id
    @Column(name = "lid")
    public int getLid() {
        return lid;
    }

    public void setLid(int lid) {
        this.lid = lid;
    }

    @Basic
    @Column(name = "time")
    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Basic
    @Column(name = "uid")
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Basic
    @Column(name = "operation")
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Basic
    @Column(name = "sid")
    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    @Basic
    @Column(name = "mode")
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntity logEntity = (LogEntity) o;

        if (lid != logEntity.lid) return false;
        if (uid != logEntity.uid) return false;
        if (sid != logEntity.sid) return false;
        if (time != null ? !time.equals(logEntity.time) : logEntity.time != null) return false;
        if (operation != null ? !operation.equals(logEntity.operation) : logEntity.operation != null) return false;
        if (mode != null ? !mode.equals(logEntity.mode) : logEntity.mode != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lid;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + uid;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + sid;
        result = 31 * result + (mode != null ? mode.hashCode() : 0);
        return result;
    }
}
