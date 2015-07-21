package K.Controllers.ApiDoc.SampleForm;

import K.Aop.annotations.Comment;
import play.data.Form;

/**
 * Created by kk on 15/7/17.
 */
public class PostFormSample {

    @Comment("表单字段1")
    public String form_field_1;

    @Comment("表单字段2")
    public String form_field_2;

    @Comment("表单字段3")
    public String form_field_3;

    public static Form<PostFormSample> theForm = Form.form(PostFormSample.class);

}
