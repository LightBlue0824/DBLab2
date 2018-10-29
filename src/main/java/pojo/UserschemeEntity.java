package pojo;

import javax.persistence.*;

@Entity
@Table(name = "userscheme", schema = "dblab2", catalog = "")
@IdClass(UserschemeEntityPK.class)
public class UserschemeEntity {
    private int uid;
    private int sid;
    private int phonecall;
    private int message;
    private double local;
    private double domestic;

    public UserschemeEntity() {
    }

    public UserschemeEntity(int uid, int sid, int phonecall, int message, double local, double domestic) {
        this.uid = uid;
        this.sid = sid;
        this.phonecall = phonecall;
        this.message = message;
        this.local = local;
        this.domestic = domestic;
    }

    @Id
    @Column(name = "uid")
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Id
    @Column(name = "sid")
    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    @Basic
    @Column(name = "phonecall")
    public int getPhonecall() {
        return phonecall;
    }

    public void setPhonecall(int phonecall) {
        this.phonecall = phonecall;
    }

    @Basic
    @Column(name = "message")
    public int getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = message;
    }

    @Basic
    @Column(name = "local")
    public double getLocal() {
        return local;
    }

    public void setLocal(double local) {
        this.local = local;
    }

    @Basic
    @Column(name = "domestic")
    public double getDomestic() {
        return domestic;
    }

    public void setDomestic(double domestic) {
        this.domestic = domestic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserschemeEntity that = (UserschemeEntity) o;

        if (uid != that.uid) return false;
        if (sid != that.sid) return false;
        if (phonecall != that.phonecall) return false;
        if (message != that.message) return false;
        if (Double.compare(that.local, local) != 0) return false;
        if (Double.compare(that.domestic, domestic) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = uid;
        result = 31 * result + sid;
        result = 31 * result + phonecall;
        result = 31 * result + message;
        temp = Double.doubleToLongBits(local);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(domestic);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
