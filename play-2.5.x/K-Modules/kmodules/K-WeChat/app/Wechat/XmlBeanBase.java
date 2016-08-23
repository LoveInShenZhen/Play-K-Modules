package Wechat;

import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by kk on 15/6/10.
 */
public class XmlBeanBase {

    public String ToXmlStr() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Persister persister = new Persister();
            persister.write(this, bos, "UTF-8");
            return bos.toString("UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean Validate(String xml_str) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(xml_str.getBytes("UTF-8"));
            Persister persister = new Persister();
            return persister.validate(this.getClass(), bis, false);
        } catch (Exception ex) {
            return false;
        }

    }

    public void ParseFromStr(String xml_str) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(xml_str.getBytes("UTF-8"));
            Persister persister = new Persister();
            persister.read(this, bis, false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
