package com.transo.store_admin_backend.Controller;

import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.*;
import com.transo.store_repository.Repository.*;
import com.transo.store_tools.Tools.ExceptionResult;
import com.transo.store_tools.Tools.HttpClientUtils;
import com.transo.store_tools.Tools.JsonResult;
import com.transo.store_tools.Tools.RowData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "订单操作", tags = "订单操作")
@RequestMapping("/backEndAdminOrder")
public class OrderController {
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SecondStandardsRepository secondStandardsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderAndGoodsRepository orderAndGoodsRepository;
    @Autowired
    private BaseAddressRepository baseAddressRepository;
    @Autowired
    private StoreTypeRepository storeTypeRepository;
    @Autowired
    private AdvertisementRepository advertisementRepository;
    @Autowired
    private FirstClassificationRepository firstClassificationRepository;
    @Autowired
    private SecondClassificationRepository secondClassificationRepository;
    @Autowired
    private StoreAndSecondClassificationRepository storeAndSecondClassificationRepository;
    @Autowired
    private FloatAccountRepository floatAccountRepository;

    @GetMapping(value = "getAllOrder")
    @ApiOperation(value = "获取所有订单信息")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "startTime", value = "确认支付开始时间", dataType = "Date"),
            @ApiImplicitParam(paramType = "param", name = "endTime", value = "确认支付结束时间", dataType = "Date"),
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "orderState", value = "订单状态", dataType = "String"),
            @ApiImplicitParam(paramType = "param", name = "salesPhone", value = "卖家手机号", dataType = "String"),
            @ApiImplicitParam(paramType = "param", name = "phone", value = "买家手机号", dataType = "String"),
            @ApiImplicitParam(paramType = "param", name = "orderNum", value = "订单编号", dataType = "String"),
    })
    @LoginRequired(required = true)
    public JsonResult getAllOrder(@RequestParam(value = "pageSize") int pageSize, @RequestParam(value = "page") int page, @RequestParam(value = "startTime", required = false) Date startTime, @RequestParam(value = "endTime", required = false) Date endTime, @RequestParam(value = "orderState", required = false) String orderState, @RequestParam(value = "salesPhone", required = false) String salesPhone, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "orderNum", required = false) String orderNum) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (startTime == null || "".equals(startTime)) {
            startTime = dateFormat.parse("0000-01-01");
        }
        if (endTime == null || "".equals(endTime)) {
            endTime = dateFormat.parse("9999-12-31");
        }
        if ("10".equals(orderState)) {
            orderState = "%";
        }
        if (salesPhone == null || "".equals(salesPhone)) {
            salesPhone = "%";
        }
        if (phone == null || "".equals(phone)) {
            phone = "%";
        }
        if (orderNum == null || "".equals(orderNum)) {
            orderNum = "%";
        }
        List<Order> all = orderRepository.findAllByLotProperties(phone, salesPhone, orderState, orderNum, startTime, endTime, (page - 1) * pageSize, pageSize);
        int count = orderRepository.getCountByLotProperties(phone, salesPhone, orderState, orderNum, startTime, endTime);
        for (Order alt : all) {
            alt.setStore(storeRepository.findAllByShopId(alt.getShopId()));
            alt.setStore(storeRepository.findAllByShopId(alt.getShopId()));
            List<OrderAndGoods> allByOrderId = orderAndGoodsRepository.findAllByOrderId(alt.getId());
            List<Goods> goods = new LinkedList<>();
            for (OrderAndGoods alt1 : allByOrderId) {
                Goods allBySpuId = goodsRepository.findAllBySpuId(alt1.getGoodsId());
                goods.add(allBySpuId);
            }
            alt.setGoods(goods);
        }
        return new JsonResult(new RowData(count, all), 200);
    }

    @GetMapping(value = "showFloatAccount")
    @ApiOperation(value = "展示流水账")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "startTime", value = "开始时间", required = true, dataType = "Date"),
            @ApiImplicitParam(paramType = "param", name = "endTime", value = "结束时间", required = true, dataType = "Date"),
    })
    @LoginRequired(required = true)
    public JsonResult showFloatAccount(@RequestParam(value = "startTime", required = false) Date startTime, @RequestParam(value = "endTime", required = false) Date endTime, @RequestParam(value = "page") int page, @RequestParam(value = "pageSize") int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("createTime")));
        Page<FloatAccount> all = floatAccountRepository.findAll(new Specification<FloatAccount>() {
            @Override
            public Predicate toPredicate(Root<FloatAccount> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (startTime != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startTime));
                }
                if (endTime != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endTime));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        List<FloatAccount> alt = all.getContent();
        alt = alt.stream().peek(s -> {
            s.setPutStyleCh("1".equals(s.getPutStyle()) ? "借入" : "贷出");
            s.setStore(storeRepository.findAllByShopId(orderRepository.findAllByOrderNum(s.getOrderNum()).getShopId()));
        }).collect(Collectors.toList());
        return new JsonResult(new RowData((int) all.getTotalElements(), alt), 200);
    }

    @PostMapping(value = "changeOrderState")
    @ApiOperation(value = "改变订单的状态,1=>待支付,2=>待接单,3=>已接单,4=>配货中,5=>配送中,6=>待取货,7=>已完成,8=>已取消,9=>已退款,10=>全部")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "订单ID", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "state", value = "状态码", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult changeOrderState(@RequestBody @Valid JSONObject obj, HttpServletRequest request, HttpServletResponse response) {
        Order order = orderRepository.findAllById(obj.getInt("id"));
        int changeState = obj.getInt("state");
        if (obj.getInt("state") == 8 && order.getState() != 1 && order.getState() != 9 && order.getState() != 8&& order.getState() != 7) {//取消订单
            if (order.getRealPrice() > 0) {
                String url = "http://new.farmwode.cn/wxpay/refund";
                String content = "{\"orderNum\":" + order.getOrderNum() + "}";
                String result = HttpClientUtils.httpPost(url, content);
                return new JsonResult(result, 200);
            } else if (order.getRealPrice() <= 0) {
                Order allByOrderNum = orderRepository.findAllByOrderNum(order.getOrderNum());
                User allById = userRepository.findAllById(allByOrderNum.getUserId());//买方账号
                User mainUser = userRepository.findAllById(1);//公司账号
                allById.setFruit(allById.getFruit() + allByOrderNum.getFruitPrice() + allByOrderNum.getChargeFruitPrice());
                allById.setMoney(allById.getMoney() + allByOrderNum.getUseMoney());
                userRepository.save(allById);
                mainUser.setFruit(mainUser.getFruit() - allByOrderNum.getFruitPrice() - allByOrderNum.getChargeFruitPrice());
                mainUser.setMoney(mainUser.getMoney() - allByOrderNum.getUseMoney());
                userRepository.save(mainUser);
                allByOrderNum.setState(8);
                orderRepository.save(allByOrderNum);
                FloatAccount floatAccount = new FloatAccount();
                floatAccount.setRealMoney(0);
                floatAccount.setFruit(allByOrderNum.getFruitPrice());
                floatAccount.setGold(allByOrderNum.getUseMoney());
                floatAccount.setChargeFruit(allByOrderNum.getChargeFruitPrice());
                floatAccount.setOrderNum(allByOrderNum.getOrderNum());
                floatAccount.setPutStyle("2");
                floatAccountRepository.save(floatAccount);
                List<OrderAndGoods> list = orderAndGoodsRepository.findAllByOrderId(allByOrderNum.getId());
                for (OrderAndGoods all : list) {
                    Goods allBySpuId = goodsRepository.findAllBySpuId(all.getGoodsId());
                    allBySpuId.setSalesCount(allBySpuId.getSalesCount() - all.getGoodsCount());
                    goodsRepository.save(allBySpuId);
                }
                return new JsonResult(200);
            }
            order.setState(obj.getInt("state"));
            orderRepository.save(order);
            return new JsonResult("修改成功!", 200);
        } else if (order.getState() == 9 || order.getState() == 8|| order.getState() == 7) {
            throw new ExceptionResult("该状态订单不能更改!");
        } else if ((order.getState() == 1 && changeState != 2) ||
                (order.getState() == 2 && changeState != 3) ||
                (order.getState() == 3 && changeState != 4) ||
                (order.getState() == 4 && changeState != 5) ||
                (order.getState() == 5 && changeState != 6) ||
                (order.getState() == 6 && changeState != 7)) {
            throw new ExceptionResult("无效的订单状态更改!");
        }
        order.setState(changeState);
        orderRepository.save(order);
        return new JsonResult("修改成功!", 200);
    }
}
