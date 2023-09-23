
package ltd.newbee.mall.entity;

import java.util.Date;
import java.util.StringJoiner;

public class NewBeeMallOrderItem {
    private Long orderItemId;

    private Long orderId;

    private Long seckillId;

    private Long goodsId;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;

    private Integer goodsCount;

    private Date createTime;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName == null ? null : goodsName.trim();
    }

    public String getGoodsCoverImg() {
        return goodsCoverImg;
    }

    public void setGoodsCoverImg(String goodsCoverImg) {
        this.goodsCoverImg = goodsCoverImg == null ? null : goodsCoverImg.trim();
    }

    public Integer getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Integer sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getGoodsCount() {
        return goodsCount;
    }

    public void setGoodsCount(Integer goodsCount) {
        this.goodsCount = goodsCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(Long seckillId) {
        this.seckillId = seckillId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewBeeMallOrderItem.class.getSimpleName() + "[", "]")
                .add("orderItemId=" + orderItemId)
                .add("orderId=" + orderId)
                .add("seckillId=" + seckillId)
                .add("goodsId=" + goodsId)
                .add("goodsName='" + goodsName + "'")
                .add("goodsCoverImg='" + goodsCoverImg + "'")
                .add("sellingPrice=" + sellingPrice)
                .add("goodsCount=" + goodsCount)
                .add("createTime=" + createTime)
                .toString();
    }
}
