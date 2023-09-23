
package ltd.newbee.mall.controller.vo;

import lombok.Getter;
import ltd.newbee.mall.entity.GoodsCategory;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索页面分类数据VO
 */
@Getter
public class SearchPageCategoryVO implements Serializable {

    private String firstLevelCategoryName;

    private List<GoodsCategory> secondLevelCategoryList;

    private String secondLevelCategoryName;

    private List<GoodsCategory> thirdLevelCategoryList;

    private String currentCategoryName;

    public void setFirstLevelCategoryName(String firstLevelCategoryName) {
        this.firstLevelCategoryName = firstLevelCategoryName;
    }

    public void setSecondLevelCategoryList(List<GoodsCategory> secondLevelCategoryList) {
        this.secondLevelCategoryList = secondLevelCategoryList;
    }

    public void setSecondLevelCategoryName(String secondLevelCategoryName) {
        this.secondLevelCategoryName = secondLevelCategoryName;
    }

    public void setThirdLevelCategoryList(List<GoodsCategory> thirdLevelCategoryList) {
        this.thirdLevelCategoryList = thirdLevelCategoryList;
    }

    public void setCurrentCategoryName(String currentCategoryName) {
        this.currentCategoryName = currentCategoryName;
    }
}
