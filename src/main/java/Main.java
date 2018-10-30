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

//        //订购套餐
//        main.orderScheme(1,1);
//        main.orderScheme(1,2);
//        main.orderScheme(1,3);
//        main.orderScheme(1,4);
//        main.queryUserScheme(1);
//        main.orderScheme(2,1);
//        main.orderScheme(2, 5);
//        main.queryUserScheme(2);
//        main.orderScheme(3, 3);
//        main.orderScheme(3, 6);
//        main.queryUserScheme(3);
//        //订购、退订、查看历史
//        main.orderScheme(4,1);
//        main.cancelScheme(4,1, "立即生效");
//        main.orderScheme(4,1);
//        main.cancelScheme(4,1, "次月生效");
//        main.orderScheme(4,1);
//        main.queryOrderHistory(4);
//
//        //通话资费生成
//        main.queryUserScheme(1);
//        main.makeCost(1,"拨打", 5, "南京");
//        main.makeCost(1, "接听", 4, "杭州");
//        main.queryCost(1, "通话");
//        main.queryUserScheme(1);
//        //通话（套餐+套餐）
//        main.queryUserScheme(2);
//        main.makeCost(2, "拨打", 150, "南京");
//        main.queryUserScheme(2);
//        //通话（套餐+超出）
//        main.makeCost(2, "拨打", 150, "南京");
//
//        //短信资费生成
//        main.queryUserScheme(1);
//        main.makeCost(1, "短信", 3, "北京");
//        main.queryCost(1, "短信");
//        main.queryUserScheme(1);
//
//        //流量资费生成
//        main.queryUserScheme(1);
//        main.makeCost(1, "流量", 200, "南京");  //用户1所属地南京
//        main.makeCost(1, "流量", 100, "北京");
//        main.queryUserScheme(1);
//        //流量（套餐+套餐）
//        main.queryUserScheme(3);
//        main.makeCost(3, "流量", 2200, "南京");
//        main.queryUserScheme(3);
//        //流量（本地超出+国内补齐）
//        main.queryUserScheme(3);
//        main.makeCost(3, "流量", 2100, "南京");
//        main.queryUserScheme(3);
//        //流量（套餐外）
//        main.queryUserScheme(4);
//        main.makeCost(4, "流量", 5, "上海");    //用户4所属地上海
//        main.makeCost(4, "流量", 6, "杭州");    //国内超出
//
//        //账单生成
//        main.queryMonthBill(1);
//        main.queryMonthBill(2);
//        main.queryMonthBill(3);
        main.queryMonthBill(4);

        //月初，模拟月初时，进行统一处理，如何判断已到月初不做实现(测试不太可行)，这里模拟已到月初
//        main.monthBegin();      //该方法的前提为时间到了月初
//        main.queryUserScheme(1);

//        main.queryOrderHistory(1);
//        main.makeCost(1, "通话", 2, "南京");
//        main.makeCost(1, "通话", 105, "南京");
//        main.makeCost(1, "接听", 3, "南京");
//        main.makeCost(1, "拨打", 2, "南京");
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
//        main.queryMonthBill(1);
//        main.monthBegin();
    }

    /**
     * 月初函数，月初时调用处理
     */
    public void monthBegin(){
        long time1 = System.currentTimeMillis();

        Session session = sessionFactory.openSession();
        String hql;
        Query query;
        Transaction tx = null;

        //先将退订且次月生效的进行生效
        hql = "FROM LogEntity WHERE mode='次月生效'";
        query = session.createQuery(hql);
        List<LogEntity> results_nextMonth = query.list();
        for(int i = 0; i < results_nextMonth.size(); i++){
            LogEntity tempLog = results_nextMonth.get(i);
            //为用户删除该套餐
            deleteUserScheme(tempLog.getUid(), tempLog.getSid());
        }
        hql = "UPDATE FROM LogEntity SET mode='次月已生效' WHERE mode='次月生效'";       //将次月生效改为次月已生效，进行区分
        try{
            tx = session.beginTransaction();
            query = session.createQuery(hql);
            query.executeUpdate();
            tx.commit();
        }catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }

        //为用户扣除月费
        hql = "SELECT us.uid, SUM(s.cost) FROM UserschemeEntity AS us, SchemeEntity AS s WHERE us.sid=s.sid GROUP BY us.uid";    //表连接获取套餐的月费
        query = session.createQuery(hql);
        List<Object[]> results_schemeCost = query.list();
        if(results_schemeCost.get(0)[0] != null){
            int uid = (Integer) results_schemeCost.get(0)[0];
            int costSum = ((Long)results_schemeCost.get(0)[1]).intValue();
            updateUserBalance(uid, -costSum);        //扣除月费
        }

        System.out.println("月初处理结束");

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 用户订购套餐
     * @param uid 用户ID
     * @param sid 套餐ID
     */
    public void orderScheme(int uid, int sid){
        long time1 = System.currentTimeMillis();

        //判断是否重复订购同一套餐
        String hql = "FROM UserschemeEntity WHERE uid="+uid+" AND sid="+sid;
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List results = query.list();
        session.close();

        if(results.size() > 0){
            System.out.println("用户"+uid+"已订购过套餐"+sid);
            return;
        }

        SchemeEntity scheme = getScheme(sid);       //获取套餐详情
        addUserScheme(uid, scheme.getSid(), scheme.getPhonecall(), scheme.getMessage(), scheme.getLocal(), scheme.getDomestic());       //新增用户订购的套餐

        Timestamp time = new Timestamp(new Date().getTime());
        addLog(time, uid, "订购", sid, "");
        updateUserBalance(uid, -scheme.getCost());               //更新用户余额

        System.out.println(df.format(time)+"\t用户"+uid+"\t订购\t套餐"+sid+"\t"+scheme.getSname());

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 用户退订套餐
     * @param uid 用户ID
     * @param sid 套餐ID
     * @param mode 退订模式{立即生效，次月生效}
     */
    public void cancelScheme(int uid, int sid, String mode){
        long time1 = System.currentTimeMillis();

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

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 用户查询套餐余额
     * @param uid 用户ID
     */
    public void queryUserScheme(int uid){
        long time1 = System.currentTimeMillis();

        String hql = "FROM UserschemeEntity WHERE uid="+uid;        //查询该用户订购的套餐
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<UserschemeEntity> results = query.list();
        session.close();

        System.out.println("查询套餐余额：用户"+uid);
        for(int i = 0; i < results.size(); i++){
            UserschemeEntity tempUs = results.get(i);
            System.out.println("套餐"+tempUs.getSid()+"\t剩余：\t通话："+tempUs.getPhonecall()+"分钟\t短信："+tempUs.getMessage()
                +"条\t本地流量："+tempUs.getLocal()+"M\t国内流量："+tempUs.getDomestic()+"M");
        }

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 用户查询套餐历史订购情况
     * @param uid 用户ID
     */
    public void queryOrderHistory(int uid){
        long time1 = System.currentTimeMillis();

        String hql = "FROM LogEntity WHERE uid="+uid;
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<LogEntity> results = query.list();
        session.close();

        System.out.println("查询套餐历史：用户"+uid);
        for(int i = 0; i < results.size(); i++){
            LogEntity tempLog = results.get(i);
            SchemeEntity scheme = getScheme(tempLog.getSid());
            System.out.println(df.format(tempLog.getTime())+"\t"+tempLog.getOperation()+"\t套餐"+tempLog.getSid()+"\t"+scheme.getSname()+"\t"+tempLog.getMode());
        }

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 用户造成消费
     * @param uid 用户ID
     * @param type 消费类型{拨打，接听，短信，流量}
     * @param used 使用多少
     * @param city 消费时所在城市
     */
    public void makeCost(int uid, String type, double used, String city){
        long time1 = System.currentTimeMillis();

        if(!type.equals("拨打") && !type.equals("接听") && !type.equals("短信") && !type.equals("流量")){
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
            if(type.equals("拨打")){
//            if(type.equals("通话")){
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
            if(type.equals("拨打")){
                realCost = outScheme * 0.5;
            }else if(type.equals("短信")){
                realCost = outScheme * 0.1;
            }else if(type.equals("本地流量")){
                realCost = outScheme * 2;
            }else if(type.equals("国内流量")){
                realCost = outScheme * 5;
            }
            //更新用户余额
            updateUserBalance(uid, -realCost);
        }

        if(!type.equals("接听")){     //接听免费
            //更新用户套餐余额
            for(int i = 0; i < userScheme.size(); i++){
                updateUserScheme(userScheme.get(i));
            }
        }else {
            inScheme = 0;       //接听免费
            outScheme = 0;
        }

        String unit = "";
        if(type.equals("拨打") || type.equals("接听")){
            unit = "分钟";
        }else if(type.equals("短信")){
            unit = "条";
        }else if(type.equals("本地流量") || type.equals("国内流量")){
            unit = "M";
        }
        Timestamp time = new Timestamp(new Date().getTime());
        addCost(time, uid, type, inScheme, outScheme, realCost);
        System.out.println("用户"+uid+"\t消费\t"+df.format(time)+"\t类型："+type+"\t套餐内使用量"+inScheme+unit+"\t套餐外使用量"+outScheme+unit+"\t实际消费"+realCost+"元");

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 查询用户的消费情况
     * @param uid uid
     */
    public void queryCost(int uid, String type){
        long time1 = System.currentTimeMillis();

        String hql = "FROM CostEntity WHERE uid="+uid;
        if(!type.equals("全部")){
            if(type.equals("通话")){
                hql += " AND (type='拨打' OR type='接听')";
            }
            else{
                hql += " AND type LIKE '%"+type+"'";
            }
        }
        Session session = sessionFactory.openSession();
        Query query = session.createQuery(hql);
        List<CostEntity> results = query.list();
        session.close();

        String unit = "";
        if(type.indexOf("流量") >= 0){
            unit = "M";
        }else if(type.equals("短信")){
            unit = "条";
        }else{
            unit = "分钟";
        }
        System.out.println("查询用户消费情况：用户"+uid+"\t"+type);
        for(int i = 0; i < results.size(); i++){
            CostEntity tempCost = results.get(i);
            System.out.println(df.format(tempCost.getTime())+"\t类型："+tempCost.getType()+"\t套餐内使用量"+tempCost.getInScheme()
                    +unit+"\t套餐外使用量"+tempCost.getOutScheme()+unit+"\t实际消费"+tempCost.getRealcost()+"元");
        }

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }

    /**
     * 查询用户当前月的账单
     * @param uid uid
     */
    public void queryMonthBill(int uid){
        long time1 = System.currentTimeMillis();

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
        //退订立即生效的套餐也算入套餐月费
        hql = "SELECT l.uid, SUM(s.cost) FROM LogEntity AS l, SchemeEntity AS s WHERE l.sid = s.sid AND l.uid="+uid
                +" AND l.operation='退订' AND l.mode='立即生效' GROUP BY l.uid";
        query = session.createQuery(hql);
        List<Object[]> results_schemeCanceledCost = query.list();
        if(results_schemeCanceledCost.get(0)[0] != null){
            schemeCost += ((Long) results_schemeCost.get(0)[1]).intValue();
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
        hql = "SELECT uid, SUM(inScheme), SUM(outScheme) FROM CostEntity WHERE type = '拨打' AND uid="+uid;
//        hql = "SELECT uid, SUM(inScheme), SUM(outScheme) FROM CostEntity WHERE type = '通话' AND uid="+uid;
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

        System.out.println("查询本月账单：用户"+uid);
        System.out.println("余额："+balance+"元\t总消费："+(schemeCost+outSchemeCost)+"元\t套餐月费："+schemeCost +"元\t套餐外消费："+outSchemeCost
                +"元\t通话"+phonecallSum+"分钟\t短信"+messageSum+"条\t本地流量"+localSum+"M\t国内流量"+domesticSum+"M");

        long time2 = System.currentTimeMillis();
        System.out.println("用时："+(time2-time1)+"ms");
    }


    /**
     * 更新用户余额
     * @param uid 用户ID
     * @param changed 改变金额
     */
    public void updateUserBalance(int uid, double
            changed){
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
