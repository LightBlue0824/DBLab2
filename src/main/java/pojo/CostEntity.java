package pojo;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "cost", schema = "dblab2", catalog = "")
public class CostEntity {
    private int cid;
    private Timestamp time;
    private int uid;
    private String type;
    private double inScheme;
    private double outScheme;
    private double realcost;

    public CostEntity(){

    }

    public CostEntity(Timestamp time, int uid, String type, double inScheme, double outScheme, double realcost) {
        this.time = time;
        this.uid = uid;
        this.type = type;
        this.inScheme = inScheme;
        this.outScheme = outScheme;
        this.realcost = realcost;
    }

    @Id
    @Column(name = "cid")
    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
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
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "inScheme")
    public double getInScheme() {
        return inScheme;
    }

    public void setInScheme(double inScheme) {
        this.inScheme = inScheme;
    }

    @Basic
    @Column(name = "outScheme")
    public double getOutScheme() {
        return outScheme;
    }

    public void setOutScheme(double outScheme) {
        this.outScheme = outScheme;
    }

    @Basic
    @Column(name = "realcost")
    public double getRealcost() {
        return realcost;
    }

    public void setRealcost(double realcost) {
        this.realcost = realcost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CostEntity that = (CostEntity) o;

        if (cid != that.cid) return false;
        if (uid != that.uid) return false;
        if (Double.compare(that.inScheme, inScheme) != 0) return false;
        if (Double.compare(that.outScheme, outScheme) != 0) return false;
        if (Double.compare(that.realcost, realcost) != 0) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = cid;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + uid;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        temp = Double.doubleToLongBits(inScheme);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(outScheme);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(realcost);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
