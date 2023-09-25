
package star.xingxing.mall.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.dao.MallUserMapper;
import star.xingxing.mall.dao.StarMallCouponMapper;
import star.xingxing.mall.dao.StarMallUserCouponRecordMapper;
import star.xingxing.mall.entity.MallUser;
import star.xingxing.mall.entity.StarMallCoupon;
import star.xingxing.mall.entity.StarMallUserCouponRecord;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.service.StarMallUserService;
import star.xingxing.mall.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StarMallUserServiceImpl implements StarMallUserService {

    @Resource
    private MallUserMapper mallUserMapper;

    @Resource
    private StarMallCouponMapper starMallCouponMapper;

    @Resource
    private StarMallUserCouponRecordMapper newBeeMallUserCouponRecordMapper;

    @Override
    public PageResult getNewBeeMallUsersPage(PageQueryUtil pageUtil) {
        List<MallUser> mallUsers = mallUserMapper.findMallUserList(pageUtil);
        int total = mallUserMapper.getTotalMallUsers(pageUtil);
        return new PageResult(mallUsers, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String register(String loginName, String password) {
        if (mallUserMapper.selectByLoginName(loginName) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        String passwordMD5 = MD5Util.MD5Encode(password, Constants.UTF_ENCODING);
        registerUser.setPasswordMd5(passwordMD5);
        if (mallUserMapper.insertSelective(registerUser) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        // 添加注册赠券
        List<StarMallCoupon> starMallCoupons = starMallCouponMapper.selectAvailableGiveCoupon();
        for (StarMallCoupon starMallCoupon : starMallCoupons) {
            StarMallUserCouponRecord couponUser = new StarMallUserCouponRecord();
            couponUser.setUserId(registerUser.getUserId());
            couponUser.setCouponId(starMallCoupon.getCouponId());
            newBeeMallUserCouponRecordMapper.insertSelective(couponUser);
        }
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public String login(String loginName, String passwordMD5, HttpSession httpSession) {
        MallUser user = mallUserMapper.selectByLoginNameAndPasswd(loginName, passwordMD5);
        if (user != null && httpSession != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED.getResult();
            }
            // 昵称太长 影响页面展示
            if (user.getNickName() != null && user.getNickName().length() > 7) {
                String tempNickName = user.getNickName().substring(0, 7) + "..";
                user.setNickName(tempNickName);
            }
            StarMallUserVO starMallUserVO = new StarMallUserVO();
            BeanUtil.copyProperties(user, starMallUserVO);
            // 设置购物车中的数量
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, starMallUserVO);
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }

    @Override
    public StarMallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession) {
        StarMallUserVO userTemp = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        MallUser userFromDB = mallUserMapper.selectByPrimaryKey(userTemp.getUserId());
        if (userFromDB == null) {
            return null;
        }
        if (StringUtils.equals(mallUser.getNickName(), userFromDB.getNickName())
                && StringUtils.equals(mallUser.getAddress(), userFromDB.getAddress())
                && StringUtils.equals(mallUser.getIntroduceSign(), userFromDB.getIntroduceSign())) {
            throw new StarMallException("个人信息无变更！");
        }

        if (StringUtils.equals(mallUser.getAddress(), userFromDB.getAddress())
                && mallUser.getNickName() == null
                && mallUser.getIntroduceSign() == null) {
            throw new StarMallException("个人信息无变更！");
        }

        if (!StringUtils.isEmpty(mallUser.getNickName())) {
            userFromDB.setNickName(NewBeeMallUtils.cleanString(mallUser.getNickName()));
        }
        if (!StringUtils.isEmpty(mallUser.getAddress())) {
            userFromDB.setAddress(NewBeeMallUtils.cleanString(mallUser.getAddress()));
        }
        if (!StringUtils.isEmpty(mallUser.getIntroduceSign())) {
            userFromDB.setIntroduceSign(NewBeeMallUtils.cleanString(mallUser.getIntroduceSign()));
        }
        if (mallUserMapper.updateByPrimaryKeySelective(userFromDB) > 0) {
            StarMallUserVO starMallUserVO = new StarMallUserVO();
            BeanUtil.copyProperties(userFromDB, starMallUserVO);
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, starMallUserVO);
            return starMallUserVO;
        }
        return null;
    }

    @Override
    public Boolean lockUsers(Integer[] ids, int lockStatus) {
        if (ids.length < 1) {
            return false;
        }
        return mallUserMapper.lockUserBatch(ids, lockStatus) > 0;
    }
}
