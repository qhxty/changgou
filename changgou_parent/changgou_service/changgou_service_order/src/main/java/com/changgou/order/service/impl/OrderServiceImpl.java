package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.config.RabbitMQConfig;
import com.changgou.order.dao.*;
import com.changgou.order.pojo.*;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.pay.feign.PayFeign;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param order
     */
    @GlobalTransactional(name = "order_add")
    @Override
    public String add(Order order){
        Map cartMap = cartService.list(order.getUsername());
        List<OrderItem> orderItemList = (List<OrderItem>) cartMap.get("orderItemList");
        order.setTotalNum((Integer) cartMap.get("totalNum"));
        order.setTotalMoney((Integer) cartMap.get("totalMoney"));
//        order.setPayMoney((Integer) cartMap.get("totalPayMoney"));
        order.setPayMoney(1);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setBuyerRate("0");//0未评价，1已评价
        order.setSourceType("1");//来源 web
        order.setOrderStatus("0");//0未完成，1已完成，2已退货
        order.setPayStatus("0");//0未支付，1已支付
        order.setConsignStatus("0");//0未发货，1已发货
//        order.setId(idWorker.nextId()+"");
        String orderId = idWorker.nextId()+"";
        order.setId(orderId);
        orderMapper.insert(order);

        for(OrderItem orderItem : orderItemList) {
            orderItem.setId(idWorker.nextId()+"");
            orderItem.setIsReturn("0");
            orderItem.setOrderId(order.getId());
            orderItemMapper.insertSelective(orderItem);
        }
        skuFeign.decrCount(order.getUsername());
//        int x = 1/0;
        //添加任务数据
        Task task = new Task();
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setMqExchange(RabbitMQConfig.EX_BUYING_ADDPOINTUSER);
        task.setMqRoutingkey(RabbitMQConfig.CG_BUYING_ADDPOINT_KEY);

        Map map = new HashMap();
        map.put("userName",order.getUsername());
        map.put("orderId",order.getId());
        map.put("point",order.getPayMoney());
        task.setRequestBody(JSON.toJSONString(map));
        taskMapper.insertSelective(task);

        redisTemplate.delete("Cart_"+order.getUsername());
        payFeign.closeOrder(orderId);
        rabbitTemplate.convertAndSend("","queue.ordercreate",orderId);
        return orderId;
    }


    /**
     * 修改
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Order>)orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Order>)orderMapper.selectByExample(example);
    }

    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order!=null && "0".equals(order.getPayStatus())) {
            order.setPayStatus("1");
            order.setOrderStatus("1");
            order.setUpdateTime(new Date());
            order.setPayTime(new Date());
            order.setTransactionId(transactionId);
            orderMapper.updateByPrimaryKeySelective(order);

            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("支付流水号："+transactionId);
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
        }
    }


    @Autowired
    private PayFeign payFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void closeOrder(String orderId) {
        System.out.println("关闭订单"+orderId);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order==null) {
            throw new RuntimeException("订单不存在");
        }
        //判断关闭状态
        if(!"0".equals(order.getOrderStatus())) {
            System.out.println("此订单不用关闭");
            return;
        }
        System.out.println("关闭订单通过校验");
        //检查支付状态
        Map wxQueryMap = (Map) payFeign.queryOrder(orderId).getData();
        System.out.println("查询到订单"+wxQueryMap);

        if("SUCCESS".equals(wxQueryMap.get("result_code"))) {//支付状态成功，进行补偿操作
            updatePayStatus(orderId,(String) wxQueryMap.get("transaction_id"));
            System.out.println("补偿");
        }
        if("NOTPAY".equals(wxQueryMap.get("trade_state"))) {//未支付状态，关闭订单
            System.out.println("执行关闭");
            order.setCloseTime(new Date());
            order.setOrderStatus("4");
            orderMapper.updateByPrimaryKeySelective(order);
        }
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId()+"");
        orderLog.setOperater("system");
        orderLog.setOperateTime(new Date());
        orderLog.setOrderStatus("4");
        orderLog.setOrderId(order.getId());
        orderLogMapper.insert(orderLog);

        //回复库存和销量
        OrderItem _oderItem = new OrderItem();
        _oderItem.setOrderId(orderId);
        List<OrderItem> orderItemList = orderItemMapper.select(_oderItem);

        for(OrderItem orderItem : orderItemList) {
            skuFeign.resumeStockNum(orderItem.getSkuId(),orderItem.getNum());
        }


    }

    @Override
    public void batchSend(List<Order> orders) {
        //订单号非空判断
        for(Order order : orders) {
            if(order.getId()==null) {
                throw new RuntimeException("订单号为空");
            }
            if(order.getShippingName()==null || order.getShippingCode()==null) {
                throw new RuntimeException("没有选择物流公司");
            }
        }
        for(Order order : orders) {
            Order order1 = orderMapper.selectByPrimaryKey(order.getId());
            if(!"0".equals(order1.getConsignStatus()) || !"1".equals(order1.getOrderStatus())) {
                throw new RuntimeException("订单状态有误");
            }
        }
        for(Order order : orders) {
            order.setOrderStatus("2");//已发货
            order.setConsignStatus("1");//已发货
            order.setConsignTime(new Date());//发货时间
            order.setUpdateTime(new Date());//更新时间
            orderMapper.updateByPrimaryKeySelective(order);
            //记录到日志表中
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("4");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
        }
    }

    @Override
    public void tack(String orderId, String operator) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order==null) {
            throw new RuntimeException("订单不存在");
        }
        if(!"1".equals(order.getConsignStatus())) {
            throw new RuntimeException("订单没发货");
        }
        order.setConsignStatus("2");//已送达
        order.setOrderStatus("3");//已完成
        order.setUpdateTime(new Date());
        order.setEndTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);

        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId()+"");
        orderLog.setOperater("system");
        orderLog.setOperateTime(new Date());
        orderLog.setOrderStatus("4");
        orderLog.setOrderId(order.getId());
        orderLogMapper.insert(orderLog);
    }

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Override
    public void autoTack() {
        //获取订单信息
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        //获取时间节点
        LocalDate now = LocalDate.now();
        LocalDate date = now.plusDays( -orderConfig.getTakeTimeout());
        System.out.println(date);

        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("consignTime",date);
        criteria.andEqualTo("orderSatus","2");
        List<Order> orders = orderMapper.selectByExample(example);
        for(Order order:orders) {
            tack(order.getId(),"system");
        }
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andEqualTo("payType",searchMap.get("payType"));
           	}
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
           	}
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
           	}
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
           	}
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
           	}
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
           	}
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
           	}
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
           	}
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
           	}
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andEqualTo("sourceType",searchMap.get("sourceType"));
           	}
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
           	}
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andEqualTo("orderStatus",searchMap.get("orderStatus"));
           	}
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andEqualTo("payStatus",searchMap.get("payStatus"));
           	}
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andEqualTo("consignStatus",searchMap.get("consignStatus"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
