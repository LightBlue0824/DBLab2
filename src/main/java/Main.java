import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import pojo.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class Main {
    private static SessionFactory sessionFactory;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
//        main.orderScheme(1,2);
//        main.orderScheme(1,3);
//        main.orderScheme(1,4);
//        main.orderScheme(1,5);
//        main.orderScheme(1,6);
        main.queryUserScheme(1);
//        main.queryOrderHistory(1);
//        main.makeCost(1, "通话", 2, "南京");
//        main.makeCost(1, "通话", 105, "南京");
//        main.makeCost(1, "短信", 3, "杭州");
//        main.makeCost(1, "短信", 203, "南京");
//        main.makeCost(1, "流量", 20, "南京");
//        main.makeCost(1, "流量", 20, "北京");
//        main.makeCost(1, "流量", 2500, "南京");
//        main.makeCost(1, "流量", 4100, "南京");
//        main.queryCost(1, "全部");
//        main.queryCost(1, "通话");
//        main.queryCost(1, "短信");
//        main.queryCost(1, "流量");
//        main.cancelScheme(1,1,"立即生效");
//        main.cancelScheme(1,1,"次月生效");
//        main.queryUserScheme(1);
        main.queryMonthBill(1);
    }

    /**
     * 用户订购套餐
     * @param uid 用户ID
     * @param sid 套餐ID
     */
    public void orderScheme(int uid, int sid){
        //判断是否重复订购同一套餐
        String hql = "FROM UserschemeEntity WHERE uid="+uid+" AND sid="+sid;
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List results = query.list();
        session.close();

        if(results.size() > 0){
            System.out.println("已订购过套餐"+sid);
            return;
        }

        SchemeEntity scheme = getScheme(sid);       //获取套餐详情
        addUserScheme(uid, scheme.getSid(), scheme.getPhonecall(), scheme.getMessage(), scheme.getLocal(), scheme.getDomestic());       //新增用户订购的套餐

        Timestamp time = new Timestamp(new Date().getTime());
        addLog(time, uid, "订购", sid, "");
        updateUserBalance(uid, -scheme.getCost());               //更新用户余额

        System.out.println(df.format(time)+"\t用户"+uid+"\t订购\t套餐"+sid+"\t"+scheme.getSname());
    }

    /**
     * 用户退订套餐
     * @param uid 用户ID
     * @param sid 套餐ID
     * @param mode 退订模式{立即生效，次月生效}
     */
    public void cancelScheme(int uid, int sid, String mode){
        //判断是否订购了这一套餐
        String hql = "FROM UserschemeEntity WHERE uid="+uid+" AND sid="+sid;
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<UserschemeEntity> results = query.list();

        if(results.size() == 0){
            System.out.println("用户"+sid+"未订购套餐"+sid);
            return;
        }

        if(mode.equals("立即生效")){
            //如果已订购该套餐，size肯定为1
            deleteUserScheme(uid, sid);
        }
        session.close();

        SchemeEntity scheme = getScheme(sid);       //获取套餐详情

        Timestamp time = new Timestamp(new Date().getTime());
        addLog(time, uid, "退订", sid, mode);

        System.out.println(df.format(time)+"\t用户"+uid+"\t退订\t套餐"+sid+"\t"+scheme.getSname()+"\t"+mode);
    }

    /**
     * 用户查询套餐余额
     * @param uid 用户ID
     */
    public void queryUserScheme(int uid){
        String hql = "FROM UserschemeEntity WHERE uid="+uid;        //查询该用户订购的套餐
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<UserschemeEntity> results = query.list();
        session.close();

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
        String hql = "FROM LogEntity WHERE uid="+uid;
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<LogEntity> results = query.list();
        session.close();

        System.out.println("查询套餐历史：");
        for(int i = 0; i < results.size(); i++){
            LogEntity tempLog = results.get(i);
            SchemeEntity scheme = getScheme(tempLog.getSid());
            System.out.println(df.format(tempLog.getTime())+"\t"+tempLog.getOperation()+"\t套餐"+tempLog.getSid()+"\t"+scheme.getSname()+"\t"+tempLog.getMode());
        }
    }

    /**
     * 用户造成消费
     * @param uid 用户ID
     * @param type 消费类型{通话，短信，流量}
     * @param used 使用多少
     * @param city 消费时所在城市
     */
    public void makeCost(int uid, String type, double used, String city){
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
        String hql = "FROM UserschemeEntity WHERE uid="+uid;        //查询该用户订购的套餐
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<UserschemeEntity> userScheme = query.list();
        session.close();

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

        //更新用户套餐余额
        for(int i = 0; i < userScheme.size(); i++){
            updateUserScheme(userScheme.get(i));
        }

        Timestamp time = new Timestamp(new Date().getTime());
        addCost(time, uid, type, inScheme, outScheme, realCost);
        System.out.println("消费\t"+df.format(time)+"\t类型："+type+"\t套餐内使用量"+inScheme+"\t套餐外使用量"+outScheme+"\t实际消费"+realCost);
    }

    /**
     * 查询用户的消费情况
     * @param uid uid
     */
    public void queryCost(int uid, String type){
        String hql = "FROM CostEntity WHERE uid="+uid;
        if(!type.equals("全部")){
            hql += " AND type LIKE '%"+type+"'";
        }
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<CostEntity> results = query.list();
        session.close();

        System.out.println("查询用户消费情况："+type);
        for(int i = 0; i < results.size(); i++){
            CostEntity tempCost = results.get(i);
            System.out.println(df.format(tempCost.getTime())+"\t类型："+tempCost.getType()+"\t套餐内使用量"+tempCost.getInScheme()
                    +"\t套餐外使用量"+tempCost.getOutScheme()+"\t实际消费"+tempCost.getRealcost());
        }
    }

    /**
     * 查询用户当前月的账单
     * @param uid uid
     */
    public void queryMonthBill(int uid){
        Session session = sessionFactory.openSession();
        String hql;
        Query query;

        //余额
        UserEntity user = getUser(uid);
        double balance = user.getBalance();

        //套餐月费
        hql = "SELECT us.uid, SUM(s.cost) FROM UserschemeEntity AS us, SchemeEntity AS s WHERE us.sid = s.sid AND us.uid="+uid+" GROUP BY us.uid";        //查询该用户订购的套餐
        query = session.createQuery(hql);
        List<Object[]> results_schemeCost = query.list();
        int schemeCost = 0;
        if(results_schemeCost.get(0)[0] != null){
            schemeCost = ((Long) results_schemeCost.get(0)[1]).intValue();
        }


        //套餐外消费
        hql = "SELECT uid, SUM(realcost) FROM CostEntity WHERE uid="+uid;
        query = session.createQuery(hql);
        List<Object[]> results_outScheme = query.list();
        double outSchemeCost = 0;
        if(results_outScheme.get(0)[0] != null){
            outSchemeCost = (Double) results_outScheme.get(0)[1];
        }


        //通话
        hql = "SELECT uid, SUM(inScheme), SUM(outScheme) FROM CostEntity WHERE type = '通话' AND uid="+uid;
        query = session.createQuery(hql);
        List<Object[]> results_phonecall = query.list();
        double phonecallSum = 0;
        if(results_phonecall.get(0)[0] != null){
            phonecallSum = (Double) results_phonecall.get(0)[1] + (Double) results_phonecall.get(0)[2];
        }

        //短信
        hql = "SELECT uid, SUM(inScheme), SUM(outScheme) FROM CostEntity WHERE type = '短信' AND uid="+uid;
        query = session.createQuery(hql);
        List<Object[]> results_message = query.list();
        double messageSum = 0;
        if(results_message.get(0)[0] != null){
            messageSum = (Double) results_message.get(0)[1] + (Double) results_message.get(0)[2];
        }

        //本地流量
        hql = "SELECT uid, SUM(inScheme), SUM(outScheme) FROM CostEntity WHERE type = '本地流量' AND uid="+uid;
        query = session.createQuery(hql);
        List<Object[]> results_local = query.list();
        double localSum = 0;
        if(results_local.get(0)[0] != null){
            localSum = (Double) results_local.get(0)[1] + (Double) results_local.get(0)[2];
        }

        //国内流量
        hql = "SELECT uid, SUM(inScheme), SUM(outScheme) FROM CostEntity WHERE type = '国内流量' AND uid="+uid;
        query = session.createQuery(hql);
        List<Object[]> results_domestic = query.list();
        double domesticSum = 0;
        if(results_domestic.get(0)[0] != null){
            domesticSum = (Double) results_domestic.get(0)[1] + (Double) results_domestic.get(0)[2];
        }

        System.out.println("查询本月账单：");
        System.out.println("余额："+balance+"元\t总消费："+(schemeCost+outSchemeCost)+"元\t套餐月费："+schemeCost +"元\t套餐外消费："+outSchemeCost
                +"元\t通话"+phonecallSum+"分钟\t短信"+messageSum+"条\t本地流量"+localSum+"M\t国内流量"+domesticSum+"M");
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
     * @param uid uid
     * @param sid sid
     * @param phonecall phonecall
     * @param message message
     * @param local local
     * @param domestic domestic
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
     * 删除用户订购的套餐
     * @param uid uid
     * @param sid sid
     */
    public void deleteUserScheme(int uid, int sid){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            String hql = "DELETE FROM UserschemeEntity WHERE uid="+uid+" AND sid="+sid;
            Query query = session.createQuery(hql);
            query.executeUpdate();
//            int affected = query.executeUpdate();
//            System.out.println(affected+"rows");
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
     * @param us 要更新的实体
     */
    public void updateUserScheme(UserschemeEntity us){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.update(us);
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
     * @param time time
     * @param uid uid
     * @param operation operation
     * @param sid sid
     * @param mode mode
     */
    public void addLog(Timestamp time, int uid, String operation, int sid, String mode){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
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
     * 添加一条操作记录
     * @param time time
     * @param uid uid
     * @param type type
     * @param inScheme 套餐内使用
     * @param outScheme 套餐外使用
     * @param realCost 实际消费
     */
    public void addCost(Timestamp time, int uid, String type, double inScheme, double outScheme, double realCost){
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            CostEntity cost = new CostEntity(time, uid, type, inScheme, outScheme, realCost);
            session.save(cost);
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
