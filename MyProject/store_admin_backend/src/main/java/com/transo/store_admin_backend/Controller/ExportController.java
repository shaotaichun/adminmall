package com.transo.store_admin_backend.Controller;

import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.Order;
import com.transo.store_repository.Repository.*;
import com.transo.store_tools.Tools.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.tagext.PageData;
import javax.transaction.Transactional;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "导出Excel表操作", tags = "导出Excel表操作")
@RequestMapping("/exportExcel")
public class ExportController {
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

    /**
     * 导出报表
     *
     * @return
     */
    @GetMapping(value = "/export")
    @ApiOperation(value = "导出为Excel")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "startTime", value = "确认支付开始时间", dataType = "Date"),
            @ApiImplicitParam(paramType = "param", name = "endTime", value = "确认支付结束时间", dataType = "Date"),
    })
    public void export(@RequestParam(value = "startTime",required = false) Date startTime, @RequestParam(value = "endTime",required = false) Date endTime,HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取数据
        List<Order> list = orderRepository.findAll(new Specification<Order>() {
            @Override
            public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (startTime != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startTime));
                }
                if (endTime != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endTime));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        for (Order all:list) {
            all.setStore(storeRepository.findAllByShopId(all.getShopId()));
            if (all.getState() == 1){
                all.setStateCh("待支付");
            }else if (all.getState() == 2){
                all.setStateCh("待接单");
            }else if (all.getState() == 3){
                all.setStateCh("已接单");
            }else if (all.getState() == 4){
                all.setStateCh("配货中");
            }else if (all.getState() == 5){
                all.setStateCh("配送中");
            }else if (all.getState() == 6){
                all.setStateCh("待取货");
            }else if (all.getState() == 7){
                all.setStateCh("已完成");
            }else if (all.getState() == 8){
                all.setStateCh("已取消");
            }else if (all.getState() == 9){
                all.setStateCh("已退款");
            }

        }
        //excel标题
        String[] title = {"订单号", "支付价格", "商家ID","商家联系方式","商家银行卡账号","商铺ID","订单状态"};

        //excel文件名
        String fileName = "orders.xls";

        //sheet名
        String sheetName = "订单成交表";
        String[][] content = new String[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            content[i] = new String[title.length];
            Order obj = list.get(i);
            content[i][0] = obj.getOrderNum();
            content[i][1] = obj.getRealPrice() + "";
            content[i][2] = obj.getSalesUserId().toString();
            content[i][3] = obj.getStore().getContactPhone();
            content[i][4] = obj.getStore().getBankCard();
            content[i][5] = obj.getStore().getShopId()+"";
            content[i][6] = obj.getStateCh()+"";
        }

//创建HSSFWorkbook
        HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(sheetName, title, content, null);

        //响应到客户端
        try {
            this.setResponseHeader(response, fileName);
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送响应流方法
    public void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            try {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            response.setContentType("application/octet-stream;charset=ISO8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
