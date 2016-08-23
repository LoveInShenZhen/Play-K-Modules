package K.Ebean;

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 上午10:41
 * To change this template use File | Settings | File Templates.
 */

import K.Aop.annotations.DBIndexed;
import K.Common.BizLogicException;
import com.avaje.ebean.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import jodd.bean.BeanUtil;
import jodd.datetime.JDateTime;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import play.Application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


class IndexInfo {
    public String index_name;
    public Set<String> columns;

    public IndexInfo(String index_name) {
        this.index_name = index_name;
        this.columns = new TreeSet<>();
    }

    public void AddClumn(String column) {
        this.columns.add(column);
    }

    public boolean IsCombinedIndex() {
        return this.columns.size() > 1;
    }

    public static Map<String, IndexInfo> LoadIndexInfoForTable(String tableName) {
        Map<String, IndexInfo> index_map = new HashMap<>();
        String sql = String.format("show index from `%s`", tableName);
        List<SqlRow> rows = DB.Default().createSqlQuery(sql)
                .findList();
        for (SqlRow row: rows) {
            String column_name = row.getString("Column_name");
            String index_name = row.getString("Key_name");

            if (!index_map.containsKey(index_name)) {
                IndexInfo indexInfo = new IndexInfo(index_name);
                index_map.put(index_name, indexInfo);
            }

            IndexInfo indexInfo = index_map.get(index_name);
            indexInfo.AddClumn(column_name);
        }

        return index_map;
    }

    // 判断指定的字段是否有索引, 排除联合索引
    public static boolean IndexExists(Map<String, IndexInfo> index_map, String column_name) {
        for (String index_name : index_map.keySet()) {
            IndexInfo indexInfo = index_map.get(index_name);
            if (indexInfo.IsCombinedIndex()) {
                continue;
            }

            if (indexInfo.columns.contains(column_name)) {
                return true;
            }
        }

        return false;
    }

}

public class DB {

    public static EbeanServer Default() {
        return Ebean.getServer(null);
    }

    public static <T> T RunInTransaction(TxCallable<T> txCallable) {
        TxScope txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED);
        return Ebean.execute(txScope, txCallable);
    }

    public static void RunInTransaction(TxRunnable txRunnable) {
        TxScope txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED);
        Ebean.execute(txScope, txRunnable);
    }

    public static String GetCreateIndexSql(Application application) throws IOException {
        StringBuilder sb = new StringBuilder();
        ClassPath cp = ClassPath.from(application.classloader());
        ImmutableSet<ClassPath.ClassInfo> classes = cp.getTopLevelClassesRecursive("models");
        for (ClassPath.ClassInfo classInfo : classes) {
            Class<?> modelClass = classInfo.load();
            if (isEntityClass(classInfo.load())) {
                String table_name = getTableName(modelClass);
                String drop_sql = GetDropIndexSqlBy(modelClass);
                String create_sql = GetCreateIndexSqlByModel(modelClass);
                if (StringUtils.isNotBlank(drop_sql + create_sql)) {
                    sb.append("-- ").append("================================\n");
                    sb.append("-- ").append("Table: ").append(table_name).append("\n");
                    sb.append("-- ").append("================================\n");
                    sb.append(drop_sql);
                    sb.append("\n").append(create_sql).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private static boolean isEntityClass(Class<?> modelClass) {
        Entity annoEntity = modelClass.getAnnotation(Entity.class);
        return annoEntity != null;
    }

    private static boolean isColumnField(Field field) {
        Column annoColumn = field.getAnnotation(Column.class);
        return annoColumn != null;
    }

    private static String getTableName(Class<?> modelClass) {
        Entity annoEntity = modelClass.getAnnotation(Entity.class);
        if (annoEntity == null) {
            // 不是实体类
            throw new BizLogicException("不是实体类");
        }

        String table_name = StringUtil.fromCamelCase(modelClass.getSimpleName(), '_');

        Table annoTable = modelClass.getAnnotation(Table.class);
        if (annoTable != null && StringUtils.isNoneBlank(annoTable.name())) {
            table_name = annoTable.name();
        }

        return table_name;
    }

    private static String getColumnName(Field field) {
        Column annoColumn = field.getAnnotation(Column.class);
        if (annoColumn == null) {
            throw new BizLogicException("不是数据字段");
        }
        String column_name = StringUtil.fromCamelCase(field.getName(), '_');// Helper.ToUnderscoreNaming(field.getName());
        if (StringUtils.isNotBlank(annoColumn.name())) {
            column_name = annoColumn.name();
        }
        return column_name;
    }

    private static Set<String> getIndexedFieldNames(Class<?> modelClass) {
        Set<String> nameList = new TreeSet<>();
        Field[] fields = modelClass.getFields();
        for (Field field : fields) {
            if (isColumnField(field)) {
                DBIndexed annoDBIndexed = field.getAnnotation(DBIndexed.class);
                if (annoDBIndexed != null) {
                    String field_name = getColumnName(field);
                    nameList.add(field_name);
                }
            }
        }
        return nameList;
    }

    private static Set<String> getUnIndexedFieldNames(Class<?> modelClass) {
        Set<String> nameList = new TreeSet<>();
        Field[] fields = modelClass.getFields();
        for (Field field : fields) {
            if (isColumnField(field)) {
                DBIndexed annoDBIndexed = field.getAnnotation(DBIndexed.class);
                if (annoDBIndexed == null) {
                    String field_name = getColumnName(field);
                    nameList.add(field_name);
                }
            }
        }
        return nameList;
    }

    private static Map<String, String> getIndexedColumns(String tableName) {
        String sql = String.format("show index from `%s`", tableName);
        List<SqlRow> rows = Ebean.createSqlQuery(sql)
                .findList();
        HashMap<String, String> indexMap = new HashMap<>();
        for (SqlRow row : rows) {
            indexMap.put(row.getString("Column_name"), row.getString("Key_name"));
        }
        return indexMap;
    }


    private static String GetCreateIndexSqlByModel(Class<?> modelClass) {
        String tableName = getTableName(modelClass);
        Set<String> fieldNames = getIndexedFieldNames(modelClass);

        StringBuilder sb = new StringBuilder();

        Map<String, IndexInfo> indexMap = IndexInfo.LoadIndexInfoForTable(tableName);
        for (String field_name : fieldNames) {
            if (!IndexInfo.IndexExists(indexMap, field_name)) {
                // 对应的字段索引不存在
                String idx_name = String.format("idx_%s_%s", tableName, field_name);
                String create_sql = String.format("CREATE INDEX `%s` ON `%s` (`%s`);",
                        idx_name,
                        tableName,
                        field_name);
                sb.append(create_sql).append("\n");
            }
        }

        if (sb.length() > 0) {
            sb.append("\n");
        }

        return sb.toString();
    }

    private static String GetDropIndexSqlBy(Class<?> modelClass) {
        String table_name = getTableName(modelClass);
        Set<String> UnIndexedFields = getUnIndexedFieldNames(modelClass);

        StringBuilder sb = new StringBuilder();

        Map<String, IndexInfo> index_map = IndexInfo.LoadIndexInfoForTable(table_name);

        for (String field_name : UnIndexedFields) {
            if (IndexInfo.IndexExists(index_map, field_name)) {
                for (IndexInfo index_info: index_map.values()) {
                    if (index_info.IsCombinedIndex()) {
                        continue;
                    }

                    if (index_info.index_name.equalsIgnoreCase("PRIMARY")) {
                        continue;
                    }

                    if (index_info.index_name.startsWith("uq_")) {
                        continue;
                    }

                    if (index_info.columns.contains(field_name)) {
                        sb.append(String.format("DROP INDEX `%s` ON `%s`;\n",
                                index_info.index_name,
                                table_name));
                    }
                }

            }
        }
        if (sb.length() > 0) {
            sb.append("\n");
        }
        return sb.toString();
    }

    // 约定 SqlRow 的列名称 和 BeanType 里的 public 的 field 的 name 一致
    // Populate 后的 bean, 如果想更新回数据库, 必须先调用refresh()方法, 再更新其字段,最后再 Save()
    public static <BeanType> void Populate(BeanType bean, SqlRow row) {
        Field[] fields = bean.getClass().getFields();
        for (Field field : fields) {
            String field_name = field.getName();
            if (row.containsKey(field_name)) {
                Object val = ReadValue(field, row);
                BeanUtil.forced.setProperty(bean, field_name, val);
//                FieldUtils.writeField(field, bean, val);
            }
        }
    }

    // 根据 field 的 name, 并判断期数据类型, 在 row 里读取对应的值
    public static Object ReadValue(Field field, SqlRow row) {
        String type_name = field.getGenericType().getTypeName();
        String field_name = field.getName();
        if (type_name.equals(int.class.getTypeName()) || type_name.equals(Integer.class.getTypeName())) {
            return row.getInteger(field_name);
        }

        if (type_name.equals(long.class.getTypeName()) || type_name.equals(Long.class.getTypeName())) {
            return row.getLong(field_name);
        }

        if (type_name.equals(double.class.getTypeName()) || type_name.equals(Double.class.getTypeName())) {
            return row.getDouble(field_name);
        }

        if (type_name.equals(float.class.getTypeName()) || type_name.equals(Float.class.getTypeName())) {
            return row.getFloat(field_name);
        }

        if (type_name.equals(boolean.class.getTypeName()) || type_name.equals(Boolean.class.getTypeName())) {
            return row.getBoolean(field_name);
        }

        if (type_name.equals(BigDecimal.class.getTypeName())) {
            return row.getBigDecimal(field_name);
        }

        if (type_name.equals(Date.class.getTypeName())) {
            return row.getUtilDate(field_name);
        }

        if (type_name.equals(JDateTime.class.getTypeName())) {
            return new JDateTime(row.getUtilDate(field_name));
        }

        if (type_name.equals(java.sql.Date.class.getTypeName())) {
            return row.getDate(field_name);
        }

        if (type_name.equals(Timestamp.class.getTypeName())) {
            return row.getTimestamp(field_name);
        }

        if (type_name.equals(UUID.class.getTypeName())) {
            return row.getUUID(field_name);
        }

        if (type_name.equals(String.class.getTypeName())) {
            return row.getString(field_name);
        }

        return row.get(field_name);
    }

    public static double getRowDouble(SqlRow row, String column) {
        if (row == null) {
            return 0.0;
        }
        Double amt = row.getDouble(column);
        return amt == null ? 0.0 : amt.doubleValue();
    }

    public static long getRowLong(SqlRow row, String column) {
        Long amt = row.getLong(column);
        return amt == null ? 0L : amt.longValue();
    }

    public static int getRowInt(SqlRow row, String column) {
        Integer amt = row.getInteger(column);
        return amt == null ? 0 : amt.intValue();
    }

    public static String getRowDecryptStr(SqlRow row, String column) {
        Object obj = row.get(column);
        if (obj == null) {
            return null;
        }
        byte[] bytes = (byte[]) obj;
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
