package com.transo.store_admin_backend.Controller;

import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.*;
import com.transo.store_repository.Repository.FeedbackRepository;
import com.transo.store_repository.Repository.StoreRepository;
import com.transo.store_repository.Repository.UserRepository;
import com.transo.store_tools.Tools.*;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "用户操作", tags = "用户操作")
@RequestMapping("/backEndAdminUser")
public class UserController {
    @Value("${token.key}")
    private String token_key;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping(value = "findUser")
    @ApiOperation(value = "获取用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "userId", value = "商铺用户ID", required = true, dataType = "Integer"),
    })
    @Transactional
    @LoginRequired(required = true)
    public JsonResult findUser(@RequestParam(value = "userId")int userId, HttpServletRequest request, HttpServletResponse response){
        Store allByUserId = storeRepository.findAllByUserId(userId);
        User allById = userRepository.findAllById(userId);
        if (allById == null){
            throw new ExceptionResult("该用户不存在!");
        }
        allById.setStore(allByUserId);
        return new JsonResult(allById,200);
    }
    @GetMapping(value = "findUser1")
    @ApiOperation(value = "获取用户信息")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult findUser(HttpServletRequest request, HttpServletResponse response){
        int userId = Integer.parseInt(JwtUtil.decode(request.getHeader("token"), token_key, IpUtils.getIpAddr(request)).get("userId").toString());
        Store allByUserId = storeRepository.findAllByUserId(userId);
        User allById = userRepository.findAllById(userId);
        allById.setStore(allByUserId);
        return new JsonResult(allById,200);
    }

    @GetMapping(value = "showFeedbackAll")
    @ApiOperation(value = "展示所有的反馈")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "startTime", value = "开始时间", required = true, dataType = "Date"),
            @ApiImplicitParam(paramType = "param", name = "endTime", value = "结束时间", required = true, dataType = "Date"),
    })

    public JsonResult showFeedbackAll(@RequestParam(value = "startTime",required = false)Date startTime,@RequestParam(value = "endTime",required = false) Date endTime, @RequestParam(value = "page")int page, @RequestParam(value = "pageSize")int pageSize, HttpServletRequest request, HttpServletResponse response){
        Pageable pageable = PageRequest.of(page-1,pageSize);
        Page<Feedback> all = feedbackRepository.findAll(new Specification<Feedback>() {
            @Override
            public Predicate toPredicate(Root<Feedback> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (startTime !=null){
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class),startTime));
                }
                if (endTime !=null){
                    predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endTime));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        List<Feedback> feedbackList = all.getContent();
        feedbackList = all.stream().peek(s -> {s.setUser(userRepository.findAllById(s.getUserId()));}).collect(Collectors.toList());
        return new JsonResult(new RowData((int)all.getTotalElements(),feedbackList),200);
    }
    @GetMapping(value = "showFeedbackOne")
    @ApiOperation(value = "展示单条反馈详情")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "id", value = "反馈主键ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showFeedbackOne(@RequestParam(value = "id") int id,HttpServletRequest request, HttpServletResponse response){
        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        feedback.setUser(userRepository.findAllById(feedback.getUserId()));
        return new JsonResult(feedback,200);
    }
    /*@PostMapping(value = "PostFeedback")
    @ApiOperation(value = "反馈回馈")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult PostFeedback(@Valid @RequestBody @ApiParam(name = "反馈模型", value = "传入json格式", required = true) Feedback feedback, HttpServletRequest request, HttpServletResponse response){
        feedback.setIsDeal("1");
        @Valid Feedback save = feedbackRepository.save(feedback);
        String phone = userRepository.findAllById(feedback.getUserId()).getPhone();
        CuccSMS.sendSMS("505002", phone, "wzcy002", save.getBackContent(), 0);
        return new JsonResult(200);
    }
*/
}
