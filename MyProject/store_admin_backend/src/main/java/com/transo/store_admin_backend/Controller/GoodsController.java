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
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "商品操作", tags = "商品操作")
@RequestMapping("/backEndAdminGoods")
public class GoodsController {
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
    private GoodsWayRepository goodsWayRepository;
    @Autowired
    private GoodsClassificationRepository goodsClassificationRepository;
    @Autowired
    private FirstStandardsRepository firstStandardsRepository;
    @GetMapping(value = "showUserClassify")
    @ApiOperation(value = "获取当前商户的商品分类列表")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "shopId", value = "商铺ID", required = true, dataType = "String"),
    })
    @LoginRequired(required = true)
    public JsonResult showUserClassify(@RequestParam(value = "pageSize") int pageSize, @RequestParam(value = "page") int page,@RequestParam(value = "shopId") String shopId, HttpServletRequest request, HttpServletResponse response){
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<GoodsClassification> allByShopId = goodsClassificationRepository.findAll(new Specification<GoodsClassification>() {
            @Override
            public Predicate toPredicate(Root<GoodsClassification> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("shopId").as(String.class),shopId));
                predicates.add(cb.equal(root.get("isDelete").as(String.class),"1"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        List<GoodsClassification> all = allByShopId.getContent();
        for (GoodsClassification alt:all) {
            alt.setSpuList(goodsRepository.findAllByClassifyId(alt.getCategoryId()));
        }
        return new JsonResult(new RowData((int)allByShopId.getTotalElements(),all),200);//存在修正返回数据类型问题,暂不修正
    }
    @PostMapping(value = "addUserClassify")
    @ApiOperation(value = "给商户添加/修改商品分类")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addUserClassify(@RequestBody @Valid @ApiParam(name = "商品分类模型", value = "传入json格式", required = true) GoodsClassification goodsClassification,HttpServletRequest request, HttpServletResponse response){
        goodsClassificationRepository.save(goodsClassification);
        return new JsonResult(200);
    }
    @PostMapping(value = "addUpdateGoods")
    @ApiOperation(value = "添加/修改商品")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addGoods(@RequestBody @Valid @ApiParam(name = "商品模型", value = "传入json格式", required = true) Goods goods, HttpServletRequest request, HttpServletResponse response) {
            Goods save = goodsRepository.save(goods);
            List<FirstStandards> spuAttrList = goods.getSpuAttrList();
            int volumeCount = 0;
            if (spuAttrList.size()>0) {
                for (FirstStandards all : firstStandardsRepository.findAllByGoodsId(save.getSpuId())) {
                    secondStandardsRepository.deleteAllByFirstStandardsId(all.getId());
                }
                firstStandardsRepository.deleteByGoodsId(save.getSpuId());
                for (FirstStandards all : spuAttrList) {
                    all.setGoodsId(save.getSpuId());
                    FirstStandards save1 = firstStandardsRepository.save(all);
                    List<SecondStandards> spuAttrValueList = all.getSpuAttrValueList();
                    for (SecondStandards alt : spuAttrValueList) {
                        alt.setFirstStandardsId(save1.getId());
                        SecondStandards save2 = secondStandardsRepository.save(alt);
                        volumeCount += save2.getVolumeCount();
                        if (save2.getFruitPrice()>save2.getOriginPrice()){
                            throw new RuntimeException("可抵扣果实数不能大于规格参数的价格");
                        }
                    }
                }
            }else{
                volumeCount = goods.getVolumeCount();
            }
            save.setVolumeCount(volumeCount);
            goodsRepository.save(save);
            if (goods.getFruitPrice()>goods.getOriginPrice()){
                throw new RuntimeException("可抵扣果实数不能大于商品的价格");
            }
            return new JsonResult(200);
    }
    @PostMapping(value = "updateGoodsState")
    @ApiOperation(value = "修改商品状态")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "state", value = "状态,1=>在销,2=>下架", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "id", value = "商品ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult updateGoodsState(@ApiIgnore @RequestBody JSONObject obj, HttpServletRequest request, HttpServletResponse response) {
            Goods goods = goodsRepository.findAllBySpuId(obj.getInteger("id"));
            goods.setSellStatus(obj.getInteger("state"));
            goodsRepository.save(goods);
            return new JsonResult(200);
    }
    @GetMapping(value = "showAllGoods")
    @ApiOperation(value = "展示当前商户商品列表")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "goodsContent", value = "商品关键字", dataType = "String"),
            @ApiImplicitParam(paramType = "param", name = "shopId", value = "商铺ID",required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showAllGoods(@RequestParam(value = "goodsContent",required = false) String goodsContent,@RequestParam(value = "pageSize") int pageSize,@RequestParam(value = "page") int page,@RequestParam(value = "shopId") int shopId, HttpServletRequest request, HttpServletResponse response){
        List<GoodsClassification> allByShopId = goodsClassificationRepository.findAllByShopIdAndIsDelete(shopId,"1");
        List<Integer> classify = new LinkedList<>();
        for (GoodsClassification all:allByShopId) {
            classify.add(all.getCategoryId());
        }
        if (goodsContent == null){
            goodsContent = "%";
        }
        List<Goods> bySpuNameLike = goodsRepository.findBySpuNameLike(goodsContent, classify, (page - 1) * pageSize, pageSize);
        for (Goods all:bySpuNameLike) {
            List<FirstStandards> allByGoodsId = firstStandardsRepository.findAllByGoodsId(all.getSpuId());
            for (FirstStandards alt:allByGoodsId) {
                List<SecondStandards> allByFirstStandardsId = secondStandardsRepository.findAllByFirstStandardsId(alt.getId());
                alt.setSpuAttrValueList(allByFirstStandardsId);
            }
            all.setGoodsClassifyCh(goodsClassificationRepository.findAllByCategoryId(all.getClassifyId()).getCategoryName());
            all.setSpuAttrList(allByGoodsId);
        }
        int countByClassifyId = goodsRepository.getCountByClassifyId(goodsContent,classify);
        return new JsonResult(new RowData((int) countByClassifyId, bySpuNameLike), 200);
    }
    @GetMapping(value = "demo")
    public JsonResult demo(){
        Goods goods = goodsRepository.findById(222).orElse(null);
        return new JsonResult(goods,200);
    }

}
