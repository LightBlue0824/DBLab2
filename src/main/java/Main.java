import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import pojo.LogEntity;
import pojo.SchemeEntity;
import pojo.UserEntity;
import pojo.UserschemeEntity;
import sun.rmi.runtime.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class Main {
    private static SessionFactory sessionFactory;

    public static void main(String[] args){
        try{
            sessionFactory = new Configuration().configure().buildSessionFactory();
        }catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        Main main = new Main();

//        main.updateUserBalance(1, 1);
//        main.addUserScheme(1,1, 100, 0, 0, 0);
//        main.orderScheme(1,1);
        main.queryUserScheme(1);
//        main.queryOrderHistory(1);
//        main.addCost(1, "通话", 2, "南京");
    }

    /**
     * 用户订购套餐
     * @param uid 用户ID
     * @param sid 套餐ID
     */
    public void orderScheme(int uid, int sid){
        SchemeEntity scheme = getScheme(sid);       //获取套餐详情
        addUserScheme(uid, scheme.getSid(), scheme.getPhonecall(), scheme.getMessage(), scheme.getLocal(), scheme.getDomestic());       //新增用户订购的套餐
        addLog(uid, "订购", sid, "");
        System.out.println("用户"+uid+"\t订购套餐"+sid+"\t"+scheme.getSname());
    }

    /**
     * 用户查询套餐余额
     * @param uid 用户ID
     */
    public void queryUserScheme(int uid){
        String hql = "FROM UserschemeEntity WHERE uid="+uid;        //查询该用户订购的套餐
        List<UserschemeEntity> results = query(hql);
        System.out.println("查询套餐余额：");
        for(int i = 0; i < results.size(); i++){
            UserschemeEntity tempUs = results.get(i);
            System.out.println("套餐"+tempUs.getSid()+"\t剩余：\t通话："+tempUs.getPhonecall()+"分钟\t短信："+tempUs.getMessage()
                +"条\t本地流量："+tempUs.getLocal()+"M\t国内流量："+tempUs.getDomestic()+"M");
        }
    }

    /**
     * 用户查询套餐历史订购情况
     * @param uid 用户ID
     */
    public void queryOrderHistory(int uid){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String hql = "FROM LogEntity WHERE uid="+uid;
        List<LogEntity> results = query(hql);
        System.out.println("查询套餐历史：");
        for(int i = 0; i < results.size(); i++){
            LogEntity tempLog = results.get(i);
            SchemeEntity scheme = getScheme(tempLog.getSid());
            System.out.println("时间："+df.format(tempLog.getTime())+"\t"+tempLog.getOperation()+"\t套餐"+tempLog.getSid()+"\t"+scheme.getSname()+"\t"+tempLog.getMode());
        }
    }

    /**
     * 用户造成消费
     * @param uid 用户ID
     * @param type 消费类型{通话，短信，流量}
     * @param used 使用多少
     * @param city 消费时所在城市
     */
    public void addCost(int uid, String type, double used, String city){
        if(!type.equals("通话") && !type.equals("短信") && !type.equals("流量")){
            return;
        }

//        boolean isLocal = false;    //是否是本地
        double inScheme = 0;        //套餐内使用
        double outScheme = 0;       //套餐外使用
        double realCost = 0;        //实际费用

        if(type.equals("流量")){          //判断是否是本地
            UserEntity user = getUser(uid);
            if(city.equals(user.getCity())){
//                isLocal = true;
                type = "本地流量";
            }
            else{
//                isLocal = false;
                type = "国内流量";
            }
        }
        String hql = "FROM UserschemeEntiry WHERE uid="+uid;        //查询该用户订购的套餐
        List<UserschemeEntity> userScheme = query(hql);

        for(int i = 0; i < userScheme.size(); i++){         //在有剩余的套餐中扣除使用量
            UserschemeEntity tempUs = userScheme.get(i);
            if(type.equals("通话")){
                if(tempUs.getPhonecall() > 0){          //该套餐有剩余
                    if(tempUs.getPhonecall() > used){   //该套餐足够
                        tempUs.setPhonecall(tempUs.getPhonecall()-(int)used);
                        inScheme += used;
                        used = 0;
                    }
                    else{                 //该套餐有剩余但不足
                        inScheme += tempUs.getPhonecall();
                        used -= tempUs.getPhonecall();
                        tempUs.setPhonecall(0);
                    }
                }
            }else if(type.equals("短信")){
                if(tempUs.getMessage() > 0){          //该套餐有剩余
                    if(tempUs.getMessage() > used){   //该套餐足够
                        tempUs.setMessage(tempUs.getMessage()-(int)used);
                        inScheme += used;
                        used = 0;
                    }
                    else{                 //该套餐有剩余但不足
                        inScheme += tempUs.getMessage();
                        used -= tempUs.getMessage();
                        tempUs.setMessage(0);
                    }
                }
            }else if(type.equals("本地流量")){
                if(tempUs.getLocal() > 0){          //该套餐有剩余
                    if(tempUs.getLocal() > used){   //该套餐足够
                        tempUs.setLocal(tempUs.getLocal()-used);
                        inScheme += used;
                        used = 0;
                    }
                    else{                 //该套餐有剩余但不足
                        inScheme += tempUs.getLocal();
                        used -= tempUs.getLocal();
                        tempUs.setLocal(0);
                    }
                }
            }else if(type.equals("国内流量")){
                if(tempUs.getDomestic() > 0){          //该套餐有剩余
                    if(tempUs.getDomestic() > used){   //该套餐足够
                        tempUs.setDomestic(tempUs.getDomestic()-used);
                        inScheme += used;
                        used = 0;
                    }
                    else{                 //该套餐有剩余但不足
                        inScheme += tempUs.getDomestic();
                        used = used - tempUs.getDomestic();
                        tempUs.setDomestic(0);
                    }
                }
            }

            if(used == 0){          //扣完了可以提前结束循环
                break;
            }
        }

        if(used > 0 && type.equals("本地流量")){        //上面只先扣了本地流量，本地使用流量时，本地流量扣完后使用国内流量
            for(int i = 0; i < userScheme.size(); i++) {         //在有剩余的套餐中扣除使用量
                UserschemeEntity tempUs = userScheme.get(i);
                if(tempUs.getDomestic() > 0){          //该套餐有剩余
                    if(tempUs.getDomestic() > used){   //该套餐足够
                        tempUs.setDomestic(tempUs.getDomestic()-used);
                        inScheme += used;
                        used = 0;
                        break;
                    }
                    else{                 //该套餐有剩余但不足
                        inScheme += tempUs.getDomestic();
                        used = used - tempUs.getDomestic();
                        tempUs.setDomestic(0);
                    }
                }
            }
        }

        if(used > 0){           //套餐余额已用完，但还剩没扣除的使用量
            outScheme = used;       //used变量剩下的就是套餐外使用量
            if(type.equals("通话")){
                realCost = outScheme * 0.5;
            }else if(type.equals("短信")){
                realCost = outScheme * 0.1;
            }else if(type.equals("本地流量")){
                realCost = outScheme * 2;
            }else if(type.equals("国内流量")){
                realCost = outScheme * 5;
            }
        }

        Timestamp time = new Timestamp(new Date().getTime());

        System.out.println("消费"+"时间："+"\t类型："+type+"\t使用量"+used+"\t实际消费"+realCost);
    }

    /**
     * 更新用户余额
     * @param uid 用户ID
     * @param changed 改变金额
     */
    public void updateUserBalance(int uid, float changed){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            UserEntity user = session.get(UserEntity.class, uid);
            user.setBalance(user.getBalance()+changed);
            session.update(user);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
    }

    /**
     * 得到用户实体
     * @param uid
     * @return 用户实体对象
     */
    public UserEntity getUser(int uid){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        UserEntity user = null;
        try{
            tx = session.beginTransaction();
            user = session.get(UserEntity.class, uid);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
        return user;
    }

    /**
     * 添加用户套餐
     */
    public void addUserScheme(int uid, int sid, int phonecall, int message, double local, double domestic){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            UserschemeEntity userScheme = new UserschemeEntity(uid, sid, phonecall, message, local, domestic);
            session.save(userScheme);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
    }

    /**
     * 更新用户套餐的剩余
     */
    public void updateUserScheme(UserschemeEntity us){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.save(us);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
    }


    /**
     * 得到套餐实体
     * @param sid sid
     * @return 套餐实体对象
     */
    public SchemeEntity getScheme(int sid){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        SchemeEntity scheme = null;
        try{
            tx = session.beginTransaction();
            scheme = session.get(SchemeEntity.class, sid);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
        return scheme;
    }

    /**
     * 添加一条操作记录
     */
    public void addLog(int uid, String operation, int sid, String mode){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            Timestamp time = new Timestamp(new Date().getTime());
            LogEntity log = new LogEntity(time, uid, operation, sid, mode);
            session.save(log);
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
    }

    /**
     * 查询
     * @param hql
     * @return 查询结果
     */
    public List query(String hql){
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List results = query.list();
        session.close();
        return results;
    }
}
