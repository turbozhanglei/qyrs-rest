package com.gy.resource.request.manager;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gy.resource.request.TokenRequest;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: gaolanyu
 * @date: 2020-02-14
 * @remark: 请求参数: 1.资源信息编码 2.资源信息标题名称 3.资源信息类型 4.发布状态 5.置顶状态 6.创建开始时间 7.创建结束时间 8.发布人用户ID 9.发布人手机号
 * 10.信息图片 11.敏感词 12.浏览量开始数 13.浏览量结束数 14.分享开始数 15.分享结束数 16.资源信息标签 17.资源区域 18.内贸外贸 19.操作人id 20.操作人名字
 *
 * 返回参数: 1.资源信息编码 2.资源信息标题名称 3.资源信息类型 4.资源信息标签 5.资源区域 6.发布状态 7.内贸外贸 8.置顶状态 9.创建时间 10.发布人用户ID
 * 11.发布人手机号 12.浏览量 13.分享数
 *
 * 逻辑: 1.筛选条件: 标题名称为模糊查询，其他为精确查询，分享数和浏览量查询为闭区间查询，输入值必须为非负整数 2.查询: 点击查询时，查询条件为交集查询 3.重置: 点击重置，清除输入的条件
 * 4.导出: 点击导出，支持导出当前的数据，目前是同步导出，考虑可以做异步 5.列表字段: 创建时间取用户提交信息资源的时间，分享数取分享成功的数量，浏览量取用户进入信息详情页的次数，分享数和浏览量每个用户ID只记一次
 * 6.查看: 点击查看，跳转至对应资源信息详情页 7.审核: 用户发表资源信息，系统会进行敏感词审核，若无敏感词，发布状态为系统审核通过，如资源信息含有敏感词，则系统不审核通过，发布状态为待人工审核，“审核通过”状态时，按钮显示为“审核不通过”，前台展示资源信息内容
 * “审核不通过”状态时，按钮显示为“审核通过”，前台不展示资源信息内容 8.置顶: 控制资源信息在对应资源信息列表页的展示优先级，多个资源信息置顶时，按资源信息创建时间倒序排序
 * “已置顶”状态时，按钮显示为“取消置顶”，前台生效置顶展示,“未置顶”状态时，按钮显示为“置顶”，前台取消置顶展示,“人工审核不通过”、“待人工审核”状态时，“置顶”按钮不可用 9.翻页规则:
 * 默认显示10条记录，超过10条则分页显示；排序默认按照创建时间倒序显示
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryResourceManagerRequest extends TokenRequest {
    private static final long serialVersionUID = 6411666076751426670L;

    @ApiModelProperty(notes = "资源Id", required = true)
    private String resourceId;

    @ApiModelProperty(notes = "资源标题")
    private String resourceTitle;

    @ApiModelProperty(notes = "资源发布类型 -1全部 1求购 0出售")
    private String resourceType;

    @ApiModelProperty(notes = "发布状态 -1全部 0待审核 1系统审核通过 2待人工审核 3人工审核通过 4人工审核不通过")
    private String issureStatus;

    @ApiModelProperty(notes = "置顶状态 -1全部 0普通 1置顶")
    private String topStatus;

    @ApiModelProperty(notes = "创建开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createStartTime;

    @ApiModelProperty(notes = "创建结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createEndTime;

    @ApiModelProperty(notes = "发布人id")
    private String issureId;

    @ApiModelProperty(notes = "发布人电话")
    private String issurePhone;

    @ApiModelProperty(notes = "-1全部 0有图 1无图")
    private String issureImage;

    @ApiModelProperty(notes = "-1全部 0含有 1不含有")
    private String sensitiveCode;

    @ApiModelProperty(notes = "浏览量开始数")
    private String browseStartNum;

    @ApiModelProperty(notes = "浏览量结束数")
    private String browseEndNum;

    @ApiModelProperty(notes = "分享开始数")
    private String shareStartNum;

    @ApiModelProperty(notes = "分享结束数")
    private String shareEndNum;

    @ApiModelProperty(notes = "资源标签 -1全部 1乙二醇 2PTA 3成品油 4PVC 5甲醇 6塑料 0其他")
    private String resourceLabel;

    @ApiModelProperty(notes = "资源区域 -1全部 1东北 2华东 3华北 4华中 5华南 6西南 7西北 8境外")
    private String resourceArea;

    @ApiModelProperty(notes = "内贸外贸 -1全部 1内贸 2进口 3出口")
    private String tradeType;

//    @ApiModelProperty(notes = "操作人ID")
//    private String operateId;
//
//    @ApiModelProperty(notes = "操作人名字")
//    private String operateName;

    private Integer start = 1;
    private Integer limit = 10;
}
