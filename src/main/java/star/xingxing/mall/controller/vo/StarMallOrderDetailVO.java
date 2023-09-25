
package star.xingxing.mall.controller.vo;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单详情页页面VO
 */
@Getter
@Data
public class StarMallOrderDetailVO implements Serializable {

    private String orderNo;

    private Integer totalPrice;

    private Byte payStatus;

    private String payStatusString;

    private Byte payType;

    private String payTypeString;

    private Date payTime;

    private Byte orderStatus;

    private String orderStatusString;

    private String userAddress;

    private Date createTime;

    private Integer discount;

    private List<StarMallOrderItemVO> starMallOrderItemVOS;

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setPayStatus(Byte payStatus) {
        this.payStatus = payStatus;
    }

    public void setPayType(Byte payType) {
        this.payType = payType;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public void setOrderStatus(Byte orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setPayStatusString(String payStatusString) {
        this.payStatusString = payStatusString;
    }

    public void setPayTypeString(String payTypeString) {
        this.payTypeString = payTypeString;
    }

    public void setOrderStatusString(String orderStatusString) {
        this.orderStatusString = orderStatusString;
    }

    public void setStarMallOrderItemVOS(List<StarMallOrderItemVO> starMallOrderItemVOS) {
        this.starMallOrderItemVOS = starMallOrderItemVOS;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }
}
