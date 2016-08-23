package models;

import com.avaje.ebean.Model;
import jodd.datetime.JDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by kk on 15/7/11.
 */

@Entity
public class SampleModel extends Model {

    @Id
    public long id;

    @Column(columnDefinition = "DATETIME COMMENT '任务计划执行时间'")
    public JDateTime plan_run_time;

    public static Finder<Long, SampleModel> find = new Finder<>(SampleModel.class);
}
