package com.transo.store_admin_backend.Tools;

import com.transo.store_bean.Entity.Order;
import com.transo.store_repository.Repository.OrderRepository;
import com.transo.store_tools.Tools.HttpClientUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
public class TimeTask {
    @Autowired
    OrderRepository orderRepository;

    /**
     * 待支付订单15分钟没操作取消该订单
     * @throws Exception
     */
    @Scheduled(fixedDelay = 60*1000)
    public void cancelOrderScheduled() throws Exception {
        System.out.println("定时取消订单任务执行了!");
        List<Order> all = orderRepository.findAll();
        for (Order alt:all) {
            long existTime = new Date().getTime() - alt.getCreateTime().getTime();
            if (alt.getState() == 1 && existTime>15*60*1000){
                alt.setState(8);
                orderRepository.save(alt);
            }
        }
    }
}
