package models.K.EbeanConfig;

import com.avaje.ebean.config.ScalarTypeConverter;
import jodd.datetime.JDateTime;

import java.util.Date;

/**
 * Created by kk on 16/2/21.
 */
public class JDateTimeConverter implements ScalarTypeConverter<JDateTime, Date> {
    @Override
    public JDateTime getNullValue() {
        return null;
    }

    @Override
    public JDateTime wrapValue(Date scalarType) {
        return new JDateTime(scalarType);
    }

    @Override
    public Date unwrapValue(JDateTime beanType) {
        return beanType.convertToDate();
    }
}
