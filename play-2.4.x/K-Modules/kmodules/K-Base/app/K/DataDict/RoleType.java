package K.DataDict;

import java.util.HashMap;

/**
 * Created by kk on 15/2/2.
 */
public class RoleType {
    public final int code;
    public final String desc;

    private static HashMap<Integer, RoleType> code_map;

    static {
        code_map = new HashMap<Integer, RoleType>();
    }

    public static RoleType GetRoleType(int code) {
        Integer key = new Integer(code);
        if (!code_map.containsKey(key)) {
            throw new RuntimeException("错误的 SysUserRoleType: " + code);
        }
        return code_map.get(key);
    }

    public RoleType(int code, String desc) {
        this.code = code;
        this.desc = desc;
        Integer key = new Integer(code);
        code_map.put(key, this);
    }

    /*
    INSERT INTO `pnr_role` (`id`, `description`, `user_defined`)
VALUES(1, '运营平台工作人员', 0),
	(2, '系统管理员', 0),
	(3, '项目/企业信息录入员', 0),
	(4, '项目/企业信息审核员', 0),
	(5, '客服人员', 0),
	(6, '平台财务人员', 0),
	(7, '平台财务人员审核员', 0),
	(8, '企业联系人账户', 0),
	(9, '借款个人账户', 0),
	(10, '担保公司联系人账户', 0),
	(11, '个人投资人账户', 0);
     */

    public static final RoleType BossStaff = new RoleType(1, "运营平台工作人员");
    public static final RoleType Admin = new RoleType(2, "系统管理员");
    public static final RoleType Submitter = new RoleType(3, "资料信息录入员");
    public static final RoleType Approver = new RoleType(4, "资料信息审核员");
    public static final RoleType CustomerService = new RoleType(5, "客服");
    public static final RoleType FinancialStaff = new RoleType(6, "平台财务人员");
    //    public static final RoleType FinancialApprover = new RoleType(7, "平台资金调拨审核员");
    public static final RoleType CorpContract = new RoleType(8, "企业联系人账户");
    public static final RoleType BorrowerPerson = new RoleType(9, "借款个人账户");
    public static final RoleType GuaranteeContract = new RoleType(10, "担保公司联系人账户");
    public static final RoleType PersonInvestor = new RoleType(11, "个人投资人账户");
}
