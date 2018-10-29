package pojo;

import javax.persistence.*;

@Entity
@Table(name = "scheme", schema = "dblab2", catalog = "")
public class SchemeEntity {
    private int sid;
    private String sname;
    private int phonecall;
    private int message;
    private double local;
    private double domestic;

    public SchemeEntity() {
    }

    public SchemeEntity(String sname, int phonecall, int message, double local, double domestic) {
        this.sname = sname;
        this.phonecall = phonecall;
        this.message = message;
        this.local = local;
        this.domestic = domestic;
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
    @Column(name = "sname")
    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
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

        SchemeEntity that = (SchemeEntity) o;

        if (sid != that.sid) return false;
        if (phonecall != that.phonecall) return false;
        if (message != that.message) return false;
        if (Double.compare(that.local, local) != 0) return false;
        if (Double.compare(that.domestic, domestic) != 0) return false;
        if (sname != null ? !sname.equals(that.sname) : that.sname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = sid;
        result = 31 * result + (sname != null ? sname.hashCode() : 0);
        result = 31 * result + phonecall;
        result = 31 * result + message;
        temp = Double.doubleToLongBits(local);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(domestic);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
