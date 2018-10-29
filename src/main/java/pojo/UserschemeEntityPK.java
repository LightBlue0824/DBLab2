package pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class UserschemeEntityPK implements Serializable {
    private int uid;
    private int sid;

    public UserschemeEntityPK() {
    }

    @Column(name = "uid")
    @Id
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Column(name = "sid")
    @Id
    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserschemeEntityPK that = (UserschemeEntityPK) o;

        if (uid != that.uid) return false;
        if (sid != that.sid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + sid;
        return result;
    }
}
