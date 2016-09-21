package models.tushare

import com.avaje.ebean.Model
import jodd.datetime.JDateTime
import k.aop.annotations.DBIndexed
import models.BaseModel
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity

/**
 * Created by kk on 16/9/21.
 */

@Entity
class HistoryDay : BaseModel() {

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(6) COMMENT '股票代码, 6位数字代码，或者指数代码 [sh=上证指数 sz=深圳成指 hs300=沪深300指数 sz50=上证50 zxb=中小板 cyb=创业板]'", nullable = false)
    var code: String = ""

    @DBIndexed
    @Column(columnDefinition = "DATETIME COMMENT '交易日期'", nullable = false)
    var t_date: JDateTime? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '开盘价'")
    var open: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '最高价'")
    var high: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '收盘价'")
    var close: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '最低价'")
    var low: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '成交量'")
    var volume: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '价格变动'")
    var price_change: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '5日均价'")
    var ma5: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '10日均价'")
    var ma10: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '20日均价'")
    var ma20: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '5日均量'")
    var v_ma5: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '10日均量'")
    var v_ma10: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '20日均量'")
    var v_ma20: BigDecimal? = null

    @Column(columnDefinition = "DECIMAL(20,4) COMMENT '换手率 [注：指数无此项]'")
    var turnover: BigDecimal? = null

    companion object : Model.Find<Long, HistoryDay>() {

    }

}