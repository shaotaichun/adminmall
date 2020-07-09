package com.transo.store_admin_backend.Controller;
import com.alibaba.fastjson.JSONObject;
import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.*;
import com.transo.store_repository.Repository.*;
import com.transo.store_tools.Tools.IpUtils;
import com.transo.store_tools.Tools.JsonResult;
import com.transo.store_tools.Tools.JwtUtil;
import com.transo.store_tools.Tools.RowData;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "店铺操作", tags = "店铺操作")
@RequestMapping("/backEndAdminStore")
public class StoreController {
    @Value("${token.key}")
    private String token_key;
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
    private StoreAndWayRepository storeAndWayRepository;
    @Autowired
    private GoodsWayRepository goodsWayRepository;
    @Autowired
    private PayAndStoreRepository payAndStoreRepository;
    @PostMapping(value = "addStore")
    @ApiOperation(value = "添加/修改商铺")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addStore(@RequestBody @Valid @ApiParam(name = "商铺类型模型", value = "传入json格式", required = true) Store store, HttpServletRequest request, HttpServletResponse response){
        Integer sort = store.getSort();
        if (storeRepository.findAllByOrderBySortDesc().size()>0&&  (sort == null||sort ==0)){//新增广告并且广告数大于0
            sort = storeRepository.findAllByOrderBySortDesc().get(0).getSort().intValue()+1;
        }else if (storeRepository.findAllByOrderBySortDesc().size() ==0){//新增第一条广告
            sort = 1;
        }
        store.setSort(sort);
        Store save = storeRepository.save(store);
        StoreAndSecondClassification storeAndSecondClassification = new StoreAndSecondClassification();
        storeAndSecondClassificationRepository.deleteAllByStoreId(save.getShopId());

        int[] secondClassificationArray = store.getShopClassify();
        for (int all:secondClassificationArray) {
            storeAndSecondClassification.setStoreId(save.getShopId());
            storeAndSecondClassification.setSecondClassificationId(all);
            storeAndSecondClassificationRepository.save(storeAndSecondClassification);
            storeAndSecondClassification = new  StoreAndSecondClassification();
        }
        //关联提货方式,先删除所有关联,再重新关联
        storeAndWayRepository.deleteAllByStoreId(save.getShopId());
        int[] goodsWayArray = store.getGoodsWayArray();

        /*传统for循环*/
        List<GoodsWay> list = new LinkedList<>();
        boolean addAddress = false;
        for (int altt:goodsWayArray) {
            list.add(goodsWayRepository.findAllByGwId(altt));
            if (altt ==1){
                addAddress = true;
            }
        }
        baseAddressRepository.deleteAllByShopId(save.getShopId());
        if (addAddress){
            BaseAddress baseAddress = new BaseAddress();
            baseAddress.setAddress(save.getAddress());
            baseAddress.setShopId(save.getShopId());
            baseAddressRepository.save(baseAddress);
        }
        if (list.size()>0){
            for (GoodsWay alt:list) {
                StoreAndWay storeAndWay = new StoreAndWay();
                storeAndWay.setStoreId(save.getShopId());
                storeAndWay.setWayId(alt.getGwId());
                storeAndWayRepository.save(storeAndWay);
            }
        }
        payAndStoreRepository.deleteAllByStoreId(save.getShopId());
        PayAndStore payAndStore = new PayAndStore();
        payAndStore.setStoreId(save.getShopId());
        payAndStore.setPayId(1);
        payAndStoreRepository.save(payAndStore);

        return new JsonResult(200);
    }
    @GetMapping(value = "showAllStores")
    @ApiOperation(value = "展示商铺列表")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "shopName", value = "商铺名称", dataType = "String"),
            @ApiImplicitParam(paramType = "param", name = "storeType", value = "商铺类型", dataType = "String"),
            @ApiImplicitParam(paramType = "param", name = "shopClassify", value = "商铺分类", dataType = "String"),
    })
   /* @LoginRequired(required = true)*/
    public JsonResult showAllStores(@RequestParam(value = "pageSize") int pageSize,@RequestParam(value = "page") int page,@RequestParam(value = "shopName",required = false) String shopName,@RequestParam(value = "storeType",required = false) String storeType,@RequestParam(value = "shopClassify",required = false) String shopClassify ,HttpServletRequest request, HttpServletResponse response){
        if (shopName == null||"".equals(shopName)){
            shopName =  "%";
        }
        if (storeType == null||"".equals(storeType)){
            storeType =  "%";
        }
        if (shopClassify == null||"".equals(shopClassify)){
            shopClassify =  "%";
        }
        List<Store> stores = storeRepository.findAllBySort(shopName,storeType,shopClassify,(page-1)*pageSize,pageSize);
        int count = storeRepository.getCountBySort(shopName,storeType,shopClassify);
        for (Store all2:stores) {
            List<StoreAndWay> allByStoreId1 = storeAndWayRepository.findAllByStoreId(all2.getShopId());
            List<StoreAndSecondClassification> allByStoreId = storeAndSecondClassificationRepository.findAllByStoreId(all2.getShopId());
            List<SecondClassification> list  = new LinkedList<>();
            for (StoreAndSecondClassification all:allByStoreId) {
                SecondClassification allById = secondClassificationRepository.findAllByIdAndIsDelete(all.getSecondClassificationId(),"1");
                list.add(allById);
            }
            List<GoodsWay> goodsWays = new LinkedList<>();
            for (StoreAndWay alt:allByStoreId1) {
                goodsWays.add(goodsWayRepository.findAllByGwId(alt.getWayId()));
            }

            all2.setSecondClassifications(list);
            all2.setGoodsWays(goodsWays);
        }
        return new JsonResult(new RowData(count,stores),200);
    }
    @GetMapping(value = "showStoreDetail")
    @ApiOperation(value = "展示商铺详情")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "id", value = "商铺ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showStoreDetail(@RequestParam(value = "id") int id){
        Store allByShopId = storeRepository.findAllByShopId(id);
        String typeName = storeTypeRepository.findAllByStId(allByShopId.getStoreType()).getTypeName();
        allByShopId.setStoreTypeCh(typeName);
        Integer secondClassificationId = storeAndSecondClassificationRepository.findAllByStoreId(allByShopId.getShopId()).get(0).getSecondClassificationId();
        allByShopId.setFirstClassificationId(secondClassificationRepository.findAllByIdAndIsDelete(secondClassificationId,"1").getFirstClassifyId());
        List<StoreAndSecondClassification> allByStoreId = storeAndSecondClassificationRepository.findAllByStoreId(id);
        List<SecondClassification> list  = new LinkedList<>();
        for (StoreAndSecondClassification all:allByStoreId) {
            SecondClassification allById = secondClassificationRepository.findAllByIdAndIsDelete(all.getSecondClassificationId(),"1");
            list.add(allById);
        }
        List<GoodsWay> goodsWays = new LinkedList<>();
        List<StoreAndWay> allByStoreId1 = storeAndWayRepository.findAllByStoreId(allByShopId.getShopId());
        for (StoreAndWay alt:allByStoreId1) {
            goodsWays.add(goodsWayRepository.findAllByGwId(alt.getWayId()));
        }
        User allById = userRepository.findAllById(allByShopId.getUserId());
        allByShopId.setUser(allById);
        allByShopId.setSecondClassifications(list);
        allByShopId.setGoodsWays(goodsWays);
        return  new  JsonResult(allByShopId,200);
    }
    @PostMapping(value = "changeStoreState")
    @ApiOperation(value = "修改商铺状态")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "state", value = "状态,1=>开业，2=>停业", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "shopId", value = "商铺ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult changeStoreState(@RequestBody JSONObject obj, HttpServletRequest request, HttpServletResponse response){
        Integer state = obj.getInteger("state");
        Integer shopId = obj.getInteger("shopId");
        Store allByShopId = storeRepository.findAllByShopId(shopId);
        allByShopId.setShopState(state);
        storeRepository.save(allByShopId);
        return new JsonResult(200);
    }
}
